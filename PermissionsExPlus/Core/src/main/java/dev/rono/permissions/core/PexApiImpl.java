package dev.rono.permissions.core;

import lombok.var;

import lombok.Value;
import lombok.experimental.Accessors;
import dev.rono.permissions.api.PexApi;
import dev.rono.permissions.api.event.Event;
import dev.rono.permissions.api.event.Subscription;
import dev.rono.permissions.api.event.group.GroupCreatedEvent;
import dev.rono.permissions.api.event.group.GroupDeletedEvent;
import dev.rono.permissions.api.event.group.GroupModifiedEvent;
import dev.rono.permissions.api.event.ladder.LadderCreatedEvent;
import dev.rono.permissions.api.event.ladder.LadderDeletedEvent;
import dev.rono.permissions.api.event.ladder.LadderModifiedEvent;
import dev.rono.permissions.api.event.user.UserCreatedEvent;
import dev.rono.permissions.api.event.user.UserDeletedEvent;
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
import dev.rono.permissions.core.context.WorldInheritanceImpl;
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
import dev.rono.permissions.core.store.jdbc.JdbcDataStore;
import dev.rono.permissions.core.store.jdbc.JdbcDialect;
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
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import org.jetbrains.annotations.ApiStatus.Internal;
import dev.rono.permissions.core.util.Immutables;

/** Platform-independent, API native PermissionsExPlus runtime. */
@Internal
public final class PexApiImpl<C> implements PexApi {
    private final Platform<C> platform;

    private volatile Runtime<C> runtime;

    private volatile RedisSyncService redis;

    /*
     * Set while a change received from another server is applied, so that the
     * local events it publishes are not sent back over Redis.
     */
    private final ThreadLocal<Boolean> applyingRemoteChange = ThreadLocal.withInitial(() -> false);

    private volatile int maintenanceTask = -1, checkpointTask = -1;

    private volatile boolean started;
    private volatile boolean starting;

    /*
     * Plugin-lifetime services. These survive reload() so that platform
     * subscriptions, first-party adapters, and registered commands remain
     * attached to the same objects for as long as the plugin is installed.
     */
    private final EventBusImpl events;
    private final CommandManagerImpl<C> commands;
    private final CopyOnWriteArrayList<Runnable> reloadListeners = new CopyOnWriteArrayList<>();

    private volatile CommandMode commandMode = CommandMode.MODERN;
    private boolean modernCommandsRegistered;

    /**
     * Which command surface owns the command tree. Internal; not part of
     * {@link PexApi}.
     */
    @Internal
    public enum CommandMode {
        /** PermissionsExPlus Cloud commands are registered. */
        MODERN,
        /**
         * A first-party adapter (PermissionsExCommandAdapter) owns the command tree.
         */
        LEGACY,
        /** No base commands are registered. */
        DISABLED
    }

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

        events = new EventBusImpl(error -> platform.logger().error("PermissionsExPlus event subscriber failed", error));
        runtime = createRuntime();
        commands = createCommands(runtime.config);

        if (commands == null) {
            commandMode = CommandMode.DISABLED;
        }
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
        return events;
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
    public WorldInheritanceImpl worlds() {
        return runtime.worlds;
    }

    /** This plugin's data folder. */
    public Path dataDirectory() {
        return platform.configuration().dataDirectory();
    }

    /** Runs long work, such as an import, off the server thread. */
    public void runAsync(Runnable task) {
        runtime.tasks.execute(task);
    }

    @Override
    public ConfigurationManagerImpl config() {
        return runtime.config;
    }

    public CommandManagerImpl<C> commands() {
        return commands;
    }

    public CommandMode commandMode() {
        return commandMode;
    }

    /**
     * Hands the command tree to a legacy command surface. The modern Cloud
     * commands are removed and are not re-registered by {@link #reload()}.
     */
    @Internal
    public synchronized void useLegacyCommands() {
        if (commands == null) {
            return;
        }

        commandMode = CommandMode.LEGACY;
        commands.clear();
        modernCommandsRegistered = false;
    }

    /**
     * Registers a callback run after every successful {@link #reload()}, once the
     * new runtime is serving requests. Platforms use this to recalculate state
     * derived from the previous runtime (for example injected permissibles).
     */
    @Internal
    public Subscription onReload(Runnable listener) {
        Objects.requireNonNull(listener, "listener");

        reloadListeners.add(listener);

        return () -> reloadListeners.remove(listener);
    }

    /** The platform's health lines, for {@code /pex version}. */
    public List<String> platformHealth() {
        return platform.healthReport();
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

            shutdown(state);

            throw error;
        }
    }

    public synchronized void stop() {
        if (starting) {
            starting = false;

            releaseSubscriptions(runtime);
            runtime.tasks.close();
            runtime.store.close();

            return;
        }

        if (!started) {
            return;
        }

        shutdown(runtime);

        started = false;
    }

    /** Releases everything a runtime has started, including a partial start. */
    private void shutdown(Runtime<C> state) {
        if (redis != null) {
            redis.stop();
            redis = null;
        }

        if (maintenanceTask >= 0) {
            state.scheduler.cancelTask(maintenanceTask);
        }

        if (checkpointTask >= 0) {
            state.scheduler.cancelTask(checkpointTask);
        }

        maintenanceTask = checkpointTask = -1;

        releaseSubscriptions(state);
        state.store.close();
        state.tasks.close();
    }

    public synchronized void reload() {
        if (!started) {
            throw new IllegalStateException("Cannot reload before PermissionsExPlus has started");
        }

        var previous = runtime;

        // Build the replacement first: a configuration error leaves the current
        // runtime serving requests untouched.
        var next = createRuntime();

        stop();

        runtime = next;

        try {
            start();
        } catch (RuntimeException error) {
            // Storage is only opened by start(), so an unreachable database or
            // unreadable data fails here. Restart with the configuration that was
            // working rather than leaving no runtime at all.
            platform.logger().warn("Reload failed (" + error.getMessage() + "); restarting with the previous configuration");

            try {
                runtime = createRuntime(previous.config);
                start();
            } catch (RuntimeException rollbackError) {
                error.addSuppressed(rollbackError);
            }

            notifyReloadListeners();

            throw error;
        }

        notifyReloadListeners();
    }

    private void notifyReloadListeners() {
        if (!started) {
            return;
        }

        for (var listener : reloadListeners) {
            try {
                listener.run();
            } catch (RuntimeException error) {
                platform.logger().error("PermissionsExPlus reload listener failed", error);
            }
        }
    }

    public List<String> expandPermissionNode(String permission) {
        return runtime.config.general().shorthandExpansionsEnabled() ? ShorthandCompiler.expand(permission) : Immutables.listOf(permission);
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
        return createRuntime(ConfigurationManagerImpl.load(platform.configuration()));
    }

    private Runtime<C> createRuntime(ConfigurationManagerImpl config) {
        var store = createStore(config);

        var tasks = new CoreTaskExecutor(config.advanced().workerPoolSize(), error -> platform.logger().error("Uncaught core worker failure", error));

        var scheduler = new ManagedPlatformScheduler(platform.scheduler(), tasks);

        var subscriptions = new CopyOnWriteArrayList<Subscription>();
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
                support,
                config.advanced().metaFormatting(),
                permissionEngine);

        var contexts = new ContextManagerImpl(config.advanced(), contextRegistry, stateTracker, contextCalculators);

        var debug = new DebugLogger(platform.logger(), config.general()::verboseDebug);
        var audit = new AuditLogger(platform.configuration(), scheduler, platform.logger(), config.advanced()::auditLogToFile, config.advanced()::broadcastToOps, config.advanced()::networkWideLogging, platform::broadcastToOperators);
        var logger = new LoggerManagerImpl(platform.logger(), debug, audit);

        var identity = new IdentityResolver(users, scheduler, config.advanced().uuidSource(), logger);
        var placeholders = new PlaceholderApiService(users, groups, ladders, resolvers, contexts, store, config.advanced().messagingType());
        var backendManager = new BackendManagerImpl(store, () -> started);

        var worlds = new WorldInheritanceImpl(platform.configuration().resolve("worlds.yml"), () -> {
            placeholders.invalidateAll();
            permissionEngine.invalidate();
        });
        resolvers.expandContexts(worlds::expand);

        // The event bus outlives this runtime, so every runtime-owned subscription is
        // tracked and released when the runtime stops.
        subscriptions.add(events.subscribe(UserModifiedEvent.class, event -> {
            placeholders.invalidate(event.current().uniqueId());
            permissionEngine.invalidateSubject("user:" + event.current().uniqueId());
        }));
        subscriptions.add(events.subscribe(UserDeletedEvent.class, event -> {
            placeholders.invalidate(event.user().uniqueId());
            permissionEngine.invalidateSubject("user:" + event.user().uniqueId());
        }));
        // Group membership fans out to many users — full invalidation is required.
        subscriptions.add(events.subscribe(GroupModifiedEvent.class, event -> {
            placeholders.invalidateAll();
            permissionEngine.invalidate();
        }));
        subscriptions.add(events.subscribe(GroupCreatedEvent.class, event -> {
            placeholders.invalidateAll();
            permissionEngine.invalidate();
        }));
        subscriptions.add(events.subscribe(GroupDeletedEvent.class, event -> {
            placeholders.invalidateAll();
            permissionEngine.invalidate();
        }));

        platform.logger().info("Loaded config.yml, advanced.yml, and database.yml for API");
        platform.logger().info("Permission engine: " + permissionEngine.id());

        return new Runtime<>(config, store, groups, users, ladders, resolvers, permissionEngine, contexts, worlds, placeholders, backendManager, identity, tasks, scheduler, logger, subscriptions);
    }

    private DataStore createStore(ConfigurationManagerImpl config) {
        switch (config.backend().type()) {
            case MEMORY :
                return new MemoryDataStore();
            case YAML :
                return new FlatDataStore(createLocalStorageDirectory(config.backend()), true);
            case JSON :
                return new FlatDataStore(createLocalStorageDirectory(config.backend()), false);
            case H2 :
                return new JdbcDataStore("H2", "jdbc:h2:file:" + resolveLocalFile(config.backend()), "org.h2.Driver", null, null, config.backend().pool(), config.backend().ddlGeneration(), JdbcDialect.H2, true);
            case SQLITE :
                return new JdbcDataStore("SQLite", "jdbc:sqlite:" + resolveLocalFile(config.backend()) + ".db", "org.sqlite.JDBC", null, null, config.backend().pool(), config.backend().ddlGeneration(), JdbcDialect.SQLITE, true);
            case MYSQL :
                return new JdbcDataStore("MySQL", "jdbc:mysql://" + config.backend().credentials().host() + ':' + config.backend().credentials().port() + '/' + config.backend().credentials().database(), "com.mysql.cj.jdbc.Driver", config.backend().credentials().username(), config.backend().credentials().password(), config.backend().pool(), config.backend().ddlGeneration(), JdbcDialect.MYSQL, true);
            case POSTGRES :
                return new JdbcDataStore("Postgres", "jdbc:postgresql://" + config.backend().credentials().host() + ':' + config.backend().credentials().port() + '/' + config.backend().credentials().database(), "org.postgresql.Driver", config.backend().credentials().username(), config.backend().credentials().password(), config.backend().pool(), config.backend().ddlGeneration(), JdbcDialect.POSTGRES, true);
            default :
                throw new IllegalStateException("Unexpected value: " + config.backend().type());
        }
    }

    private CommandManagerImpl<C> createCommands(ConfigurationManagerImpl config) {
        if (!config.advanced().registerBaseCommands()) {
            return null;
        }

        try {
            return new CommandManagerImpl<>(platform.createCommandManager(), platform.senderType(), contextRegistry);
        } catch (Exception error) {
            throw new IllegalStateException("Unable to create command manager", error);
        }
    }

    private static void releaseSubscriptions(Runtime<?> state) {
        state.subscriptions.forEach(Subscription::unsubscribe);
        state.subscriptions.clear();
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

        if (!state.groups.cache().get(defaultName).isPresent()) {
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

        // Commands live for the plugin lifetime and reference this facade, so they
        // are registered once and never re-registered over a legacy command surface.
        if (commands != null && commandMode == CommandMode.MODERN && !modernCommandsRegistered) {
            commands.registerDefaultCommands(this, platform::sendMessage);
            modernCommandsRegistered = true;
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

        redis = new RedisSyncService(state.config.advanced(), state.scheduler, platform.logger(),
                id -> applyRemoteChange(state, () -> refreshRemoteUser(state, id)),
                group -> applyRemoteChange(state, () -> state.groups.refreshFromStorage(group)),
                ladder -> applyRemoteChange(state, () -> state.ladders.refreshFromStorage(ladder)),
                state.logger().audit()::receiveRemote);

        state.logger().audit().attachNetworkPublisher(redis::publishAudit);

        redis.start();

        var publisher = redis;

        publishLocal(state, UserCreatedEvent.class, event -> publisher.publishInvalidation(event.user().uniqueId()));
        publishLocal(state, UserModifiedEvent.class, event -> publisher.publishInvalidation(event.current().uniqueId()));
        publishLocal(state, UserDeletedEvent.class, event -> publisher.publishInvalidation(event.user().uniqueId()));
        publishLocal(state, GroupCreatedEvent.class, event -> publisher.publishGroupInvalidation(event.group().name()));
        publishLocal(state, GroupModifiedEvent.class, event -> {
            publisher.publishGroupInvalidation(event.current().name());

            // A rename: the old name no longer exists.
            if (!event.previous().name().equals(event.current().name())) {
                publisher.publishGroupInvalidation(event.previous().name());
            }
        });
        publishLocal(state, GroupDeletedEvent.class, event -> publisher.publishGroupInvalidation(event.group().name()));
        publishLocal(state, LadderCreatedEvent.class, event -> publisher.publishLadderInvalidation(event.ladder().name()));
        publishLocal(state, LadderModifiedEvent.class, event -> publisher.publishLadderInvalidation(event.current().name()));
        publishLocal(state, LadderDeletedEvent.class, event -> publisher.publishLadderInvalidation(event.ladder().name()));
    }

    /** Sends a local change to other servers, but not one that came from them. */
    private <E extends Event> void publishLocal(Runtime<C> state, Class<E> type, Consumer<? super E> publish) {
        state.subscriptions.add(events.subscribe(type, event -> {
            if (!applyingRemoteChange.get()) {
                publish.accept(event);
            }
        }));
    }

    private void applyRemoteChange(Runtime<C> state, Runnable change) {
        state.tasks.execute(() -> {
            applyingRemoteChange.set(true);

            try {
                change.run();
            } finally {
                applyingRemoteChange.set(false);
            }
        });
    }

    private void refreshRemoteUser(Runtime<C> state, UUID id) {
        // Decisions can outlive the cached user, so they are dropped even when the
        // user is not cached here.
        state.placeholders.invalidate(id);
        state.permissionEngine.invalidateSubject("user:" + id);
        state.users.refreshFromStorage(id);
    }

    @Value
    @Accessors(fluent = true)
    private static class Runtime<C> {
        ConfigurationManagerImpl config;
        DataStore store;
        GroupManagerImpl groups;
        UserManagerImpl users;
        LadderManagerImpl ladders;
        ResolverImpl resolvers;
        PermissionEngine permissionEngine;
        ContextManagerImpl contexts;
        WorldInheritanceImpl worlds;
        PlaceholderApiService placeholders;
        BackendManagerImpl backend;
        IdentityResolver identity;
        CoreTaskExecutor tasks;
        ManagedPlatformScheduler scheduler;
        LoggerManagerImpl logger;
        List<Subscription> subscriptions;

        private Runtime(ConfigurationManagerImpl config, DataStore store, GroupManagerImpl groups, UserManagerImpl users, LadderManagerImpl ladders, ResolverImpl resolvers, PermissionEngine permissionEngine, ContextManagerImpl contexts, WorldInheritanceImpl worlds, PlaceholderApiService placeholders, BackendManagerImpl backend, IdentityResolver identity, CoreTaskExecutor tasks, ManagedPlatformScheduler scheduler, LoggerManagerImpl logger, List<Subscription> subscriptions) {
            this.config = config;
            this.store = store;
            this.groups = groups;
            this.users = users;
            this.ladders = ladders;
            this.resolvers = resolvers;
            this.permissionEngine = permissionEngine;
            this.contexts = contexts;
            this.worlds = worlds;
            this.placeholders = placeholders;
            this.backend = backend;
            this.identity = identity;
            this.tasks = tasks;
            this.scheduler = scheduler;
            this.logger = logger;
            this.subscriptions = subscriptions;
        }
    }
}
