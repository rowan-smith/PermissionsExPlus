package dev.rono.permissions.core.sync;

import lombok.var;

import dev.rono.permissions.core.config.AdvancedConfiguration;
import dev.rono.permissions.core.platform.PlatformLogger;
import dev.rono.permissions.core.platform.PlatformScheduler;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.UUID;
import java.util.function.Consumer;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;
import dev.rono.permissions.core.util.Strings;

/**
 * Redis pub/sub cache-invalidation transport. Payloads are versioned and
 * deliberately minimal.
 */
public final class RedisSyncService {
    private static final String PREFIX = "permissionsexplus:invalidate-user:";
    private static final String GROUP_PREFIX = "permissionsexplus:invalidate-group:";
    private static final String LADDER_PREFIX = "permissionsexplus:invalidate-ladder:";
    private static final String AUDIT_PREFIX = "permissionsexplus:audit:";

    private final AdvancedConfiguration config;
    private final PlatformScheduler scheduler;
    private final PlatformLogger logger;
    private final Consumer<UUID> invalidator;
    private final Consumer<String> groupInvalidator;
    private final Consumer<String> ladderInvalidator;
    private final Consumer<String> auditReceiver;
    private final UUID origin;

    private volatile Jedis subscriber;
    private volatile boolean running;

    public RedisSyncService(
            AdvancedConfiguration config,
            PlatformScheduler scheduler,
            PlatformLogger logger,
            Consumer<UUID> invalidator) {

        this(config, scheduler, logger, invalidator, ignored -> {}, ignored -> {}, ignored -> {}, UUID.randomUUID());
    }

    public RedisSyncService(
            AdvancedConfiguration config,
            PlatformScheduler scheduler,
            PlatformLogger logger,
            Consumer<UUID> invalidator,
            Consumer<String> auditReceiver) {

        this(config, scheduler, logger, invalidator, ignored -> {}, ignored -> {}, auditReceiver, UUID.randomUUID());
    }

    public RedisSyncService(
            AdvancedConfiguration config,
            PlatformScheduler scheduler,
            PlatformLogger logger,
            Consumer<UUID> invalidator,
            Consumer<String> groupInvalidator,
            Consumer<String> ladderInvalidator,
            Consumer<String> auditReceiver) {

        this(config, scheduler, logger, invalidator, groupInvalidator, ladderInvalidator, auditReceiver, UUID.randomUUID());
    }

    RedisSyncService(AdvancedConfiguration config,
            PlatformScheduler scheduler,
            PlatformLogger logger,
            Consumer<UUID> invalidator,
            UUID origin) {

        this(config, scheduler, logger, invalidator, ignored -> {}, ignored -> {}, ignored -> {}, origin);
    }

    RedisSyncService(AdvancedConfiguration config,
            PlatformScheduler scheduler,
            PlatformLogger logger,
            Consumer<UUID> invalidator,
            Consumer<String> auditReceiver,
            UUID origin) {

        this(config, scheduler, logger, invalidator, ignored -> {}, ignored -> {}, auditReceiver, origin);
    }

    RedisSyncService(AdvancedConfiguration config,
            PlatformScheduler scheduler,
            PlatformLogger logger,
            Consumer<UUID> invalidator,
            Consumer<String> groupInvalidator,
            Consumer<String> ladderInvalidator,
            Consumer<String> auditReceiver,
            UUID origin) {

        this.config = config;
        this.scheduler = scheduler;
        this.logger = logger;
        this.invalidator = invalidator;
        this.groupInvalidator = groupInvalidator;
        this.ladderInvalidator = ladderInvalidator;
        this.auditReceiver = auditReceiver;
        this.origin = origin;
    }

    /**
     * Publishes an encoded administrative audit record to the configured network
     * channel.
     */
    public void publishAudit(String entry) {
        scheduler.executeAsync(() -> {
            try (var jedis = connection()) {
                var encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(entry.getBytes(StandardCharsets.UTF_8));

                jedis.publish(config.redisChannel(), AUDIT_PREFIX + origin + ':' + encoded);
            } catch (Exception error) {
                logger.warn("[Sync] Redis audit publish failed: " + error.getMessage());
            }
        });
    }

    public void start() {
        running = true;
        scheduler.executeAsync(this::subscribeOnce);
    }

    public void publishInvalidation(UUID user) {
        publish(PREFIX, user.toString(), "UUID " + user);
    }

    /**
     * Tells other servers to re-read one group. Sent for created, changed,
     * renamed and deleted groups.
     */
    public void publishGroupInvalidation(String group) {
        publish(GROUP_PREFIX, group, "group '" + group + "'");
    }

    /** Tells other servers to re-read one ladder. */
    public void publishLadderInvalidation(String ladder) {
        publish(LADDER_PREFIX, ladder, "ladder '" + ladder + "'");
    }

    private void publish(String prefix, String subject, String description) {
        scheduler.executeAsync(() -> {
            try (var jedis = connection()) {
                jedis.publish(config.redisChannel(), prefix + origin + ':' + subject);

                logger.info("[Sync] Published cache refresh for " + description + " to Redis channel '" + config.redisChannel() + "'");
            } catch (Exception error) {
                logger.warn("[Sync] Redis publish failed: " + error.getMessage());
            }
        });
    }

    private void subscribeOnce() {
        if (!running) {
            return;
        }

        try {
            subscriber = connection();

            logger.info("[Sync] Connected to Redis channel '" + config.redisChannel() + "'");

            subscriber.subscribe(new JedisPubSub() {
                @Override
                public void onMessage(String channel, String message) {
                    handleMessage(channel, message);
                }
            }, config.redisChannel());
        } catch (Exception error) {
            if (!running) {
                return;
            }

            logger.warn("[Sync] Redis subscription dropped: " + error.getMessage() + "; retrying in 5 seconds");

            scheduler.executeLaterAsync(this::subscribeOnce, Duration.ofSeconds(5));
        }
    }

    private Jedis connection() {
        var jedis = new Jedis(config.redisHost(), config.redisPort(), config.redisTimeout());

        if (!Strings.isBlank(config.redisPassword())) {
            jedis.auth(config.redisPassword());
        }

        return jedis;
    }

    /**
     * Decodes a received packet and hands cache mutation back to the platform
     * thread.
     * Package-visible to permit deterministic transport-free tests.
     */
    void handleMessage(String channel, String message) {
        if (!config.redisChannel().equals(channel)) {
            return;
        }

        if (message.startsWith(AUDIT_PREFIX)) {
            handleAudit(message);
            return;
        }

        if (message.startsWith(GROUP_PREFIX)) {
            handleNamed(message, GROUP_PREFIX, "group", groupInvalidator);
            return;
        }

        if (message.startsWith(LADDER_PREFIX)) {
            handleNamed(message, LADDER_PREFIX, "ladder", ladderInvalidator);
            return;
        }

        if (!message.startsWith(PREFIX)) {
            return;
        }

        try {
            var parts = message.substring(PREFIX.length()).split(":", 2);
            if (parts.length != 2) {
                throw new IllegalArgumentException("Missing origin or user id");
            }

            var source = UUID.fromString(parts[0]);
            var id = UUID.fromString(parts[1]);

            if (origin.equals(source)) {
                return;
            }

            logger.info("[Sync] Received remote refresh for UUID " + id);

            invalidator.accept(id);
        } catch (IllegalArgumentException ignored) {
            logger.warn("[Sync] Dropped malformed Redis payload");
        }
    }

    private void handleNamed(String message, String prefix, String kind, Consumer<String> invalidator) {
        try {
            var parts = message.substring(prefix.length()).split(":", 2);
            if (parts.length != 2 || parts[1].isEmpty()) {
                throw new IllegalArgumentException("Missing origin or " + kind + " name");
            }

            var source = UUID.fromString(parts[0]);
            if (origin.equals(source)) {
                return;
            }

            logger.info("[Sync] Received remote refresh for " + kind + " '" + parts[1] + "'");

            invalidator.accept(parts[1]);
        } catch (IllegalArgumentException ignored) {
            logger.warn("[Sync] Dropped malformed Redis payload");
        }
    }

    private void handleAudit(String message) {
        try {
            var parts = message.substring(AUDIT_PREFIX.length()).split(":", 2);
            if (parts.length != 2) {
                throw new IllegalArgumentException("Missing audit origin or payload");
            }

            var source = UUID.fromString(parts[0]);
            if (origin.equals(source)) {
                return;
            }

            auditReceiver.accept(new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8));
        } catch (IllegalArgumentException ignored) {
            logger.warn("[Sync] Dropped malformed Redis audit payload");
        }
    }

    public void stop() {
        running = false;

        var value = subscriber;
        if (value != null) {
            value.close();
        }
    }
}
