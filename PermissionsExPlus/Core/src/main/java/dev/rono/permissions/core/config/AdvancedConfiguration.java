package dev.rono.permissions.core.config;

import lombok.var;

import lombok.Value;
import lombok.experimental.Accessors;
import dev.rono.permissions.core.platform.PlatformConfiguration;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;
import dev.rono.permissions.core.util.Immutables;

/**
 * Runtime, context and network settings read through a structural YAML parser.
 */
@Value
@Accessors(fluent = true)
public class AdvancedConfiguration {
    String messagingType;
    int offlineEvictionTime;
    int maxCachedOfflineUsers;
    boolean preloadOnJoin;
    CacheFailureFallback cacheFailureFallback;
    int expiryCheckInterval;
    boolean logExpiry;
    ExpiryLogMode logExpiryMode;
    String uuidSource;
    String globalContextName;
    boolean trackWorlds;
    boolean trackServers;
    boolean trackGamemodes;
    boolean trackProxies;
    Map<String, String> environment;
    int maxInheritanceDepth;
    PermissionConflictResolution conflictResolution;
    MetaFormatting metaFormatting;
    String redisHost;
    int redisPort;
    String redisPassword;
    String redisChannel;
    int redisTimeout;
    boolean auditLogToFile;
    int autoSaveInterval;
    boolean logSave;
    boolean logCacheEviction;
    CacheLogMode logCacheMode;
    int workerPoolSize;
    boolean registerBaseCommands;
    boolean broadcastToOps;
    boolean networkWideLogging;

    public AdvancedConfiguration(String messagingType, int offlineEvictionTime, int maxCachedOfflineUsers, boolean preloadOnJoin, CacheFailureFallback cacheFailureFallback, int expiryCheckInterval, boolean logExpiry, ExpiryLogMode logExpiryMode, String uuidSource, String globalContextName, boolean trackWorlds, boolean trackServers, boolean trackGamemodes, boolean trackProxies, Map<String, String> environment, int maxInheritanceDepth, PermissionConflictResolution conflictResolution, MetaFormatting metaFormatting, String redisHost, int redisPort, String redisPassword, String redisChannel, int redisTimeout, boolean auditLogToFile, int autoSaveInterval, boolean logSave, boolean logCacheEviction, CacheLogMode logCacheMode, int workerPoolSize, boolean registerBaseCommands, boolean broadcastToOps, boolean networkWideLogging) {
        this.messagingType = messagingType;
        this.offlineEvictionTime = offlineEvictionTime;
        this.maxCachedOfflineUsers = maxCachedOfflineUsers;
        this.preloadOnJoin = preloadOnJoin;
        this.cacheFailureFallback = cacheFailureFallback;
        this.expiryCheckInterval = expiryCheckInterval;
        this.logExpiry = logExpiry;
        this.logExpiryMode = logExpiryMode;
        this.uuidSource = uuidSource;
        this.globalContextName = globalContextName;
        this.trackWorlds = trackWorlds;
        this.trackServers = trackServers;
        this.trackGamemodes = trackGamemodes;
        this.trackProxies = trackProxies;
        this.environment = environment;
        this.maxInheritanceDepth = maxInheritanceDepth;
        this.conflictResolution = conflictResolution;
        this.metaFormatting = metaFormatting;
        this.redisHost = redisHost;
        this.redisPort = redisPort;
        this.redisPassword = redisPassword;
        this.redisChannel = redisChannel;
        this.redisTimeout = redisTimeout;
        this.auditLogToFile = auditLogToFile;
        this.autoSaveInterval = autoSaveInterval;
        this.logSave = logSave;
        this.logCacheEviction = logCacheEviction;
        this.logCacheMode = logCacheMode;
        this.workerPoolSize = workerPoolSize;
        this.registerBaseCommands = registerBaseCommands;
        this.broadcastToOps = broadcastToOps;
        this.networkWideLogging = networkWideLogging;
    }

    public AdvancedConfiguration(String messagingType,
            int offlineEvictionTime,
            int expiryCheckInterval,
            String uuidSource,
            String globalContextName,
            boolean trackWorlds,
            boolean trackServers,
            boolean trackGamemodes,
            boolean trackProxies,
            Map<String, String> environment,
            int maxInheritanceDepth,
            String redisHost,
            int redisPort,
            String redisPassword,
            String redisChannel,
            int redisTimeout,
            boolean auditLogToFile) {

        this(messagingType,
                offlineEvictionTime,
                5000,
                true,
                CacheFailureFallback.DENY,
                expiryCheckInterval,
                false,
                ExpiryLogMode.TOTAL,
                uuidSource,
                globalContextName,
                trackWorlds,
                trackServers,
                trackGamemodes,
                trackProxies,
                environment,
                maxInheritanceDepth,
                PermissionConflictResolution.DENY_WINS,
                MetaFormatting.HIGHEST_WEIGHT,
                redisHost,
                redisPort,
                redisPassword,
                redisChannel,
                redisTimeout,
                auditLogToFile,
                5,
                false,
                false,
                CacheLogMode.TOTAL,
                4,
                true,
                true,
                true);
    }

    public static AdvancedConfiguration load(PlatformConfiguration platform) {
        var file = platform.resolve("advanced.yml");

        try {
            Files.createDirectories(file.getParent());

            if (Files.notExists(file)) {
                platform.saveResource("advanced.yml", false);
            }

            if (Files.notExists(file)) {
                throw new IllegalStateException("Bundled advanced.yml was not saved to " + file);
            }

            var root = YamlConfigurationLoader.builder().path(file).build().load();
            if (root.node("advanced-version").getInt(1) != 1) {
                throw new IllegalStateException("Unsupported advanced-version; expected 1");
            }

            var environment = new LinkedHashMap<String, String>();

            root.node("server-context", "environment").childrenMap().forEach((key, value) -> environment.put(String.valueOf(key), value.getString("")));

            var result = new AdvancedConfiguration(
                    root.node("messaging", "type").getString("none"),
                    root.node("cache", "offline-eviction-time").getInt(10),
                    root.node("cache", "max-cached-offline-users").getInt(5000),
                    root.node("cache", "preload-on-join").getBoolean(true),
                    CacheFailureFallback.parse(root.node("cache", "cache-failure-fallback").getString("deny")),
                    root.node("temporary-permissions", "expiry-check-interval").getInt(30),
                    root.node("temporary-permissions", "log-expiry").getBoolean(false),
                    ExpiryLogMode.parse(root.node("temporary-permissions", "log-expiry-mode").getString("total")),
                    root.node("identity", "uuid-source").getString("mix_mode"),
                    root.node("server-context", "global-name").getString("global"),
                    root.node("server-context", "track-worlds").getBoolean(true),
                    root.node("server-context", "track-servers").getBoolean(true),
                    root.node("server-context", "track-gamemode").getBoolean(true),
                    root.node("server-context", "track-proxies").getBoolean(true),
                    Immutables.copyOfMap(environment), root.node("inheritance", "max-depth").getInt(10),
                    PermissionConflictResolution.parse(root.node("inheritance", "conflict-resolution").getString("deny_wins")),
                    MetaFormatting.parse(root.node("inheritance", "meta-formatting").getString("highest_weight")),
                    root.node("messaging", "redis", "host").getString("localhost"),
                    root.node("messaging", "redis", "port").getInt(6379),
                    root.node("messaging", "redis", "password").getString(""),
                    root.node("messaging", "redis", "channel").getString("networkpermissions:sync"),
                    root.node("messaging", "redis", "timeout").getInt(2000),
                    root.node("audit-log", "log-to-file").getBoolean(true),
                    root.node("cache", "auto-save-interval").getInt(5),
                    root.node("cache", "log-save").getBoolean(false),
                    root.node("cache", "log-cache-eviction").getBoolean(false),
                    CacheLogMode.parse(root.node("cache", "log-cache-mode").getString("total")),
                    root.node("threading", "worker-pool-size").getInt(4),
                    root.node("commands", "register-base-commands").getBoolean(true),
                    root.node("audit-log", "broadcast-to-ops").getBoolean(true),
                    root.node("audit-log", "network-wide-logging").getBoolean(true));

            if (result.autoSaveInterval() < 1) {
                throw new IllegalStateException("cache.auto-save-interval must be at least 1 minute");
            }

            if (result.offlineEvictionTime() < 1) {
                throw new IllegalStateException("cache.offline-eviction-time must be at least 1 minute");
            }

            if (result.maxCachedOfflineUsers() < 0) {
                throw new IllegalStateException("cache.max-cached-offline-users cannot be negative");
            }

            if (result.workerPoolSize() < 1) {
                throw new IllegalStateException("threading.worker-pool-size must be at least 1");
            }

            if (result.expiryCheckInterval() < 1) {
                throw new IllegalStateException("temporary-permissions.expiry-check-interval must be at least 1 second");
            }

            return result;
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to load " + file, exception);
        }
    }
}
