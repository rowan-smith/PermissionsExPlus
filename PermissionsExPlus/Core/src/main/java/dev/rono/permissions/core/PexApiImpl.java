package dev.rono.permissions.core;

import dev.rono.permissions.api.PexApi;
import dev.rono.permissions.api.event.group.GroupModifiedEvent;
import dev.rono.permissions.api.event.user.UserModifiedEvent;
import dev.rono.permissions.api.permission.PermissionResult;
import dev.rono.permissions.core.backend.BackendManagerImpl;
import dev.rono.permissions.core.command.CommandManagerImpl;
import dev.rono.permissions.core.config.BackendConfiguration;
import dev.rono.permissions.core.config.CacheFailureFallback;
import dev.rono.permissions.core.config.CacheLogMode;
import dev.rono.permissions.core.config.ConfigurationManagerImpl;
import dev.rono.permissions.core.config.ExpiryLogMode;
import dev.rono.permissions.core.context.ContextManagerImpl;
import dev.rono.permissions.core.context.CoreStateTracker;
import dev.rono.permissions.core.context.RuntimeContextCalculators;
import dev.rono.permissions.core.context.RuntimeContextRegistry;
import dev.rono.permissions.core.context.RuntimeStateTracker;
import dev.rono.permissions.core.engine.PermissionEngine;
import dev.rono.permissions.core.engine.PermissionEngines;
import dev.rono.permissions.core.event.EventBusImpl;
import dev.rono.permissions.core.identity.IdentityResolver;
import dev.rono.permissions.core.logger.AuditLogger;
import dev.rono.permissions.core.logger.DebugLogger;
import dev.rono.permissions.core.logger.LoggerManagerImpl;
import dev.rono.permissions.core.manager.GroupManagerImpl;
import dev.rono.permissions.core.manager.LadderManagerImpl;
import dev.rono.permissions.core.manager.UserManagerImpl;
import dev.rono.permissions.core.placeholder.PlaceholderApiService;
import dev.rono.permissions.core.platform.Platform;
import dev.rono.permissions.core.resolver.ResolutionSupport;
import dev.rono.permissions.core.resolver.ResolverImpl;
import dev.rono.permissions.core.store.DataStore;
import dev.rono.permissions.core.store.FlatDataStore;
import dev.rono.permissions.core.store.HibernateDataStore;
import dev.rono.permissions.core.store.MemoryDataStore;
import dev.rono.permissions.core.sync.RedisSyncService;
import dev.rono.permissions.core.threading.CoreTaskExecutor;
import dev.rono.permissions.core.threading.ManagedPlatformScheduler;
import dev.rono.permissions.core.util.ShorthandCompiler;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import org.jetbrains.annotations.ApiStatus.Internal;

/** Platform-independent, API native PermissionsExPlus runtime. */
    @Internal
    public final class PexApiImpl<C> implements PexApi {
    private final Platform<C> platform;

    private volatile Runtime<C> runtime;

    private volatile RedisSyncService redis;

    private volatile int maintenanceTask = -1, checkpointTask = -1;

    private volatile boolean started;
    private volatile boolean starting;

    private final RuntimeContextRegistry contextRegistry = new RuntimeContextRegistry();
    private final RuntimeContextCalculators contextCalculators = new RuntimeContextCalculators();
    private final RuntimeStateTracker stateTracker = new RuntimeStateTracker();

    private final CoreStateTracker platformStateTracker = new CoreStateTracker() {
        @Override
        public void updateState(UUID subject, String key, String value) {
            stateTracker.updateState(subject, key, value);

            invalidatePlaceholders(subject);
        }

        @Override
        public void replaceState(UUID subject, Map<String, String> values) {
            stateTracker.replaceState(subject, values);

            invalidatePlaceholders(subject);
        }

        @Override
        public void clearState(UUID subject) {
            stateTracker.clearState(subject);

            invalidatePlaceholders(subject);
        }
    };

    public PexApiImpl(Platform<C> platform) {
        this.platform = Objects.requireNonNull(platform, "platform");

        platform.logger().info("Enabling API (platform: " + platform.getClass().getSimpleName().replace("Platform", "") + ")");

        runtime = createRuntime();
    }

    @Internal
    @Override
    public UserManagerImpl users() {
        return runtime.users;
    }

    @Internal
    @Override
    public GroupManagerImpl groups() {
        return runtime.groups;
    }

    @Override
    public LadderManagerImpl ladders() {
        return runtime.ladders;
    }

    @Override
    public ResolverImpl resolvers() {
        return runtime.resolvers;
    }

    @Override
    public EventBusImpl events() {
        return runtime.events;
    }

    @Override
    public BackendManagerImpl backend() {
        return runtime.backend;
    }

    @Override
    public ContextManagerImpl contexts() {
        return runtime.contexts;
    }

    @Override
    public ConfigurationManagerImpl config() {
        return runtime.config;
    }

    public CommandManagerImpl<C> commands() {
        return runtime.commands;
    }

    public CoreStateTracker stateTracker() {
        return platformStateTracker;
    }

    public PlaceholderApiService placeholders() {
        return runtime.placeholders;
    }

    public LoggerManagerImpl logger() {
        return runtime.logger;
    }

    public synchronized void start() {
        if (started || starting) {
            throw new IllegalStateException("Already started");
        }

        starting = true;

        var state = runtime;

        try {
            state.tasks.submit(() -> {
                prepareStart(state);
                return null;
            }).toCompletableFuture().join();

            activateStart(state);

            starting = false;
            started = true;
        } catch (RuntimeException error) {
            starting = false;

            state.store.close();
            state.tasks.close();

            throw error;
        }
    }

    public synchronized void stop() {
        if (starting) {
            starting = false;

            runtime.tasks.close();
            runtime.store.close();

            return;
        }

        if (!started) {
            return;
        }

        if (redis != null) {
            redis.stop();
            redis = null;
        }

        if (maintenanceTask >= 0) {
            runtime.scheduler.cancelTask(maintenanceTask);
        }

        if (checkpointTask >= 0) {
            runtime.scheduler.cancelTask(checkpointTask);
        }

        maintenanceTask = checkpointTask = -1;

        runtime.store.close();
        runtime.tasks.close();

        started = false;
    }

    public synchronized void reload() {
        if (!started) {
            throw new IllegalStateException("Cannot reload before PermissionsExPlus has started");
        }

        stop();

        runtime = createRuntime();

        start();
    }

    public List<String> expandPermissionNode(String permission) {
        return runtime.config.general().shorthandExpansionsEnabled() ? ShorthandCompiler.expand(permission) : List.of(permission);
    }

    public PermissionResult cacheFailureFallback(UUID id, String permission) {
        if (runtime.config.advanced().cacheFailureFallback() == CacheFailureFallback.DENY) {
            return PermissionResult.DENY;
        }

        var options = runtime.contexts.queryOptions(id);

        return runtime.resolvers.defaultGroups().resolve()
                .map(group -> runtime.resolvers.permissions().check(group, permission, options))
                .filter(result -> result == PermissionResult.ALLOW).orElse(PermissionResult.DENY);
    }

    public void resolveUuidAsync(String username, Consumer<Optional<UUID>> callback) {
        runtime.identity.resolveAsync(username, callback);
    }

    private Runtime<C> createRuntime() {
        var config = ConfigurationManagerImpl.load(platform.configuration());

        var store = switch (config.backend().type()) {
            case MEMORY -> new MemoryDataStore();
            case YAML -> new FlatDataStore(createLocalStorageDirectory(config.backend()), true);
            case JSON -> new FlatDataStore(createLocalStorageDirectory(config.backend()), false);
            case H2 -> new HibernateDataStore("H2", "jdbc:h2:file:" + resolveLocalFile(config.backend()), "org.h2.Driver", null, null, config.backend().pool(), config.backend().ddlGeneration(), true);
            case SQLITE -> new HibernateDataStore("SQLite", "jdbc:sqlite:" + resolveLocalFile(config.backend()) + ".db", "org.sqlite.JDBC", null, null, config.backend().pool(), config.backend().ddlGeneration(), true);
            case MYSQL -> new HibernateDataStore("MySQL", "jdbc:mysql://" + config.backend().credentials().host() + ':' + config.backend().credentials().port() + '/' + config.backend().credentials().database(), "com.mysql.cj.jdbc.Driver", config.backend().credentials().username(), config.backend().credentials().password(), config.backend().pool(), config.backend().ddlGeneration(), true);
            case POSTGRES -> new HibernateDataStore("Postgres", "jdbc:postgresql://" + config.backend().credentials().host() + ':' + config.backend().credentials().port() + '/' + config.backend().credentials().database(), "org.postgresql.Driver", config.backend().credentials().username(), config.backend().credentials().password(), config.backend().pool(), config.backend().ddlGeneration(), true);
        };

        var tasks = new CoreTaskExecutor(config.advanced().workerPoolSize(), error -> platform.logger().error("Uncaught core worker failure", error));

        var scheduler = new ManagedPlatformScheduler(platform.scheduler(), tasks);

        var events = new EventBusImpl(error -> platform.logger().error("PermissionsExPlus event subscriber failed", error));
        var groups = new GroupManagerImpl(store, events, config.advanced().maxInheritanceDepth(), tasks::execute, config.general().shorthandExpansionsEnabled());
        var users = new UserManagerImpl(store, events, Duration.ofMinutes(config.advanced().offlineEvictionTime()), config.advanced().maxCachedOfflineUsers(), tasks::execute, config.general().shorthandExpansionsEnabled(), scheduler, platform.logger());
        var ladders = new LadderManagerImpl(store, events, tasks::execute);

        users.attachGroups(groups);
        groups.attach(users, ladders);
        ladders.attach(users, groups);

        var support = new ResolutionSupport(
                groups,
                config.advanced().maxInheritanceDepth(),
                config.general().caseSensitive(),
                config.general().wildcardsEnabled(),
                config.general().allowNegations(),
                config.general().defaultGroup(),
                config.advanced().conflictResolution(),
                platform.logger()::warn);

        var permissionEngine = PermissionEngines.createCached(support);

        var resolvers = new ResolverImpl(
                groups,
                config.advanced().maxInheritanceDepth(),
                config.general().caseSensitive(),
                config.general().wildcardsEnabled(),
                config.general().allowNegations(),
                config.general().defaultGroup(),
                config.advanced().conflictResolution(),
                config.advanced().metaFormatting(),
                platform.logger()::warn,
                permissionEngine);

        var contexts = new ContextManagerImpl(config.advanced(), contextRegistry, stateTracker, contextCalculators);

        var debug = new DebugLogger(platform.logger(), config.general()::verboseDebug);
        var audit = new AuditLogger(platform.configuration(), scheduler, platform.logger(), config.advanced()::auditLogToFile, config.advanced()::broadcastToOps, config.advanced()::networkWideLogging, platform::broadcastToOperators);
        var logger = new LoggerManagerImpl(platform.logger(), debug, audit);

        var identity = new IdentityResolver(users, scheduler, config.advanced().uuidSource(), logger);
        var placeholders = new PlaceholderApiService(users, groups, ladders, resolvers, contexts, store, config.advanced().messagingType());
        var backendManager = new BackendManagerImpl(store, () -> started);

        CommandManagerImpl<C> commands = null;
        if (config.advanced().registerBaseCommands()) {
            try {
                commands = new CommandManagerImpl<>(platform.createCommandManager(), platform.senderType(), contextRegistry);
            } catch (Exception error) {
                throw new IllegalStateException("Unable to create command manager", error);
            }
        }

        events.subscribe(UserModifiedEvent.class, event -> {
            placeholders.invalidate(event.current().uniqueId());
            permissionEngine.invalidate();
        });
        events.subscribe(GroupModifiedEvent.class, event -> {
            placeholders.invalidateAll();
            permissionEngine.invalidate();
        });

        platform.logger().info("Loaded config.yml, advanced.yml, and database.yml for API");
        platform.logger().info("Permission engine: " + permissionEngine.id());

        return new Runtime(config, store, events, groups, users, ladders, resolvers, permissionEngine, contexts, placeholders, backendManager, identity, tasks, scheduler, commands, logger);
    }

    private Path createLocalStorageDirectory(BackendConfiguration backend) {
        var directory = backend.resolveLocalStorageDirectory(platform.configuration().dataDirectory());

        try {
            Files.createDirectories(directory);
        } catch (IOException error) {
            throw new IllegalStateException("Unable to create local storage directory " + directory, error);
        }

        return directory;
    }

    private Path resolveLocalFile(BackendConfiguration backend) {
        createLocalStorageDirectory(backend);

        return backend.resolveLocalFile(platform.configuration().dataDirectory());
    }

    private void invalidatePlaceholders(UUID subject) {
        var current = runtime;

        if (current != null) {
            current.placeholders.invalidate(subject);
        }
    }

    private void prepareStart(Runtime<C> state) {
        state.store.open();
        state.groups.loadAll();
        state.ladders.loadAll();

        platform.onlineUserIds().forEach(state.users.cache()::markOnline);

        var defaultName = state.config.general().defaultGroup();

        if (state.groups.cache().get(defaultName).isEmpty()) {
            state.groups.create(defaultName).toCompletableFuture().join();
        }
    }

    private void activateStart(Runtime<C> state) {
        startMessaging(state);

        maintenanceTask = state.scheduler.scheduleRepeatingAsync(() -> {
            var expired = new ArrayList<>(state.users.purgeExpired());

            expired.addAll(state.groups.purgeExpired());

            if (!expired.isEmpty()) {
                state.permissionEngine.invalidate();

                if (state.config.advanced().logExpiry()) {
                    if (state.config.advanced().logExpiryMode() == ExpiryLogMode.INDIVIDUAL) {
                        expired.forEach(removal -> platform.logger().info("Removed expired " + removal.nodeType() + " '" + removal.node() + "' from " + removal.subjectType() + " " + removal.subject()));
                    } else {
                        platform.logger().info("Cleaned up " + expired.size() + " expired node" + (expired.size() == 1 ? "" : "s") + " across the network");
                    }
                }
            }

            var evicted = state.users.cache().evictInactive();

            if (!evicted.isEmpty() && state.config.advanced().logCacheEviction()) {
                if (state.config.advanced().logCacheMode() == CacheLogMode.INDIVIDUAL) {
                    evicted.forEach(user -> platform.logger().info("Evicted inactive user " + user.name() + " (" + user.uniqueId() + ") from the cache"));
                } else {
                    platform.logger().info("Evicted " + evicted.size() + " inactive user" + (evicted.size() == 1 ? "" : "s") + " from the cache");
                }
            }
        }, Duration.ofSeconds(state.config.advanced().expiryCheckInterval()));

        if (state.store.supportsCheckpoints()) {
            checkpointTask = state.scheduler.scheduleRepeatingAsync(() -> {
                state.store.checkpoint();

                if (state.config.advanced().logSave()) {
                    platform.logger().info("Saved " + state.store.name() + " storage checkpoint");
                }
            }, Duration.ofMinutes(state.config.advanced().autoSaveInterval()));
        }

        if (state.config.advanced().registerBaseCommands()) {
            state.commands.registerDefaultCommands(this, platform::sendMessage);
        }

        state.permissionEngine.rebuild();

        platform.logger().info("API ready: " + state.groups.cache().all().size() + " groups, " + state.ladders.cache().all().size() + " ladders, " + state.users.cache().all().size() + " cached users (engine=" + state.permissionEngine.id() + ")");
    }

    private void startMessaging(Runtime<C> state) {
        if ("none".equalsIgnoreCase(state.config.advanced().messagingType())) {
            return;
        }

        if (!"redis".equalsIgnoreCase(state.config.advanced().messagingType())) {
            platform.logger().warn("Messaging type '" + state.config.advanced().messagingType() + "' is not supported by API");

            return;
        }

        redis = new RedisSyncService(state.config.advanced(), state.scheduler, platform.logger(), id -> {
            if (!state.users.cache().isCached(id)) {
                return;
            }

            state.users.load(id).exceptionally(error -> {
                state.users.cache().unload(id);

                return null;
            });
        }, state.logger().audit()::receiveRemote);

        state.logger().audit().attachNetworkPublisher(redis::publishAudit);

        redis.start();

        state.events.subscribe(UserModifiedEvent.class, event -> redis.publishInvalidation(event.current().uniqueId()));
        state.events.subscribe(GroupModifiedEvent.class, event -> state.users.cache().identifiers().forEach(redis::publishInvalidation));
    }

    private record Runtime<C>(
            ConfigurationManagerImpl config,
            DataStore store,
            EventBusImpl events,
            GroupManagerImpl groups,
            UserManagerImpl users,
            LadderManagerImpl ladders,
            ResolverImpl resolvers,
            PermissionEngine permissionEngine,
            ContextManagerImpl contexts,
            PlaceholderApiService placeholders,
            BackendManagerImpl backend,
            IdentityResolver identity,
            CoreTaskExecutor tasks,
            ManagedPlatformScheduler scheduler,
            CommandManagerImpl<C> commands,
            LoggerManagerImpl logger) {}
}
