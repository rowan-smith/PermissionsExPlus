package dev.rono.permissions.core.identity;

import lombok.var;

import dev.rono.permissions.api.user.User;
import dev.rono.permissions.core.logger.LoggerManagerImpl;
import dev.rono.permissions.core.manager.UserManagerImpl;
import dev.rono.permissions.core.platform.PlatformScheduler;
import java.net.URI;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 * Resolves stored, offline, and Mojang identities without blocking the platform
 * thread.
 */
public final class IdentityResolver {
    private static final Pattern MOJANG_ID = Pattern.compile("\\\"id\\\"\\s*:\\s*\\\"([0-9a-fA-F]{32})\\\"");

    private final UserManagerImpl users;
    private final PlatformScheduler scheduler;
    private final String source;
    private final LoggerManagerImpl logger;

    private final URI endpoint;

    public IdentityResolver(UserManagerImpl users, PlatformScheduler scheduler, String source, LoggerManagerImpl logger) {
        this(users, scheduler, source, logger, URI.create("https://api.mojang.com/users/profiles/minecraft/"));
    }

    IdentityResolver(UserManagerImpl users, PlatformScheduler scheduler, String source, LoggerManagerImpl logger, URI endpoint) {
        this.users = Objects.requireNonNull(users, "users");
        this.scheduler = Objects.requireNonNull(scheduler, "scheduler");
        this.source = Objects.requireNonNull(source, "source");
        this.logger = Objects.requireNonNull(logger, "logger");
        this.endpoint = Objects.requireNonNull(endpoint, "endpoint");
    }

    public void resolveAsync(String username, Consumer<Optional<UUID>> callback) {
        Objects.requireNonNull(username, "username");
        Objects.requireNonNull(callback, "callback");

        scheduler.executeAsync(() -> {
            var stored = users.find(username).toCompletableFuture().join();

            var mode = source.trim().toLowerCase(Locale.ROOT);

            var result = resolve(mode, username, stored);

            scheduler.execute(() -> callback.accept(result));
        });
    }

    private Optional<UUID> resolve(String mode, String username, Optional<User> stored) {
        switch (mode) {
            case "offline" :
            case "offline_mode" :
                return offline(username);
            case "online" :
            case "online_only" :
                return mojang(username);
            case "mix" :
            case "mixed" :
            case "mix_mode" :
                return stored.isPresent() ? stored.map(User::uniqueId) : mojang(username);
            default :
                return stored.map(User::uniqueId);
        }
    }

    private Optional<UUID> offline(String username) {
        logger.debug().warn("Identity", "Offline UUID fallback used for username='" + username + "'");

        return Optional.of(UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes(StandardCharsets.UTF_8)));
    }

    private Optional<UUID> mojang(String username) {
        try {
            var connection = (HttpURLConnection) endpoint.resolve(username).toURL().openConnection();

            try {
                connection.setRequestMethod("GET");
                connection.setConnectTimeout((int) Duration.ofSeconds(2).toMillis());
                connection.setReadTimeout((int) Duration.ofSeconds(5).toMillis());

                var status = connection.getResponseCode();
                if (status < 200 || status >= 300) {
                    return Optional.empty();
                }

                String body;

                try (var input = connection.getInputStream()) {
                    body = read(input);
                }

                return parse(body);
            } finally {
                connection.disconnect();
            }
        } catch (Exception error) {
            logger.debug().log("Identity", "Mojang profile request for username='" + username + "' failed: " + error.getClass().getSimpleName());

            return Optional.empty();
        }
    }

    private static String read(InputStream input) throws java.io.IOException {
        var buffer = new ByteArrayOutputStream();
        var chunk = new byte[4096];

        for (int read; (read = input.read(chunk)) != -1;) {
            buffer.write(chunk, 0, read);
        }

        return new String(buffer.toByteArray(), StandardCharsets.UTF_8);
    }

    private static Optional<UUID> parse(String body) {
        var match = MOJANG_ID.matcher(body);
        if (!match.find()) {
            return Optional.empty();
        }

        var id = match.group(1);

        return Optional.of(UUID.fromString(id.substring(0, 8) + '-' + id.substring(8, 12) + '-' + id.substring(12, 16) + '-' + id.substring(16, 20) + '-' + id.substring(20)));
    }
}
