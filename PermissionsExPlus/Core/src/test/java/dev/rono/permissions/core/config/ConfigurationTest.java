package dev.rono.permissions.core.config;

import dev.rono.permissions.core.TestFiles;
import lombok.var;

import dev.rono.permissions.core.platform.PlatformConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static org.junit.jupiter.api.Assertions.*;
import dev.rono.permissions.core.util.Immutables;

class ConfigurationTest {
    @TempDir
    Path directory;

    @Test
    void loadsGeneralConfigurationIncludingNestedWildcardSetting() throws IOException {
        TestFiles.writeString(directory.resolve("config.yml"), ("default-group: member\n"
                + "case-sensitive: true\n"
                + "wildcards:\n"
                + "  enabled: false\n"
                + "  allow-negations: false # explanatory comment\n"
                + "  enable-shorthand-expansions: false\n"
                + "debug:\n"
                + "  verbose: true\n"
                + "hooks:\n"
                + "  vault:\n"
                + "    enabled: false\n"
                + "  placeholder-api: false\n"
                + "  bstats: false\n"));

        var configuration = GeneralConfiguration.load(platform());

        assertEquals("member", configuration.defaultGroup());
        assertTrue(configuration.caseSensitive());
        assertFalse(configuration.wildcardsEnabled());
        assertFalse(configuration.allowNegations());
        assertFalse(configuration.shorthandExpansionsEnabled());
        assertTrue(configuration.verboseDebug());
        assertFalse(configuration.vaultEnabled());
        assertFalse(configuration.placeholderApiEnabled());
        assertFalse(configuration.bstatsEnabled());
    }

    @Test
    void loadsAdvancedConfigurationAndEnvironmentValues() throws IOException {
        TestFiles.writeString(directory.resolve("advanced.yml"), ("messaging:\n"
                + "  type: redis\n"
                + "  redis:\n"
                + "    host: redis.internal\n"
                + "    port: 6380\n"
                + "    password: secret\n"
                + "    channel: permissions\n"
                + "    timeout: 2500\n"
                + "cache:\n"
                + "  offline-eviction-time: 17\n"
                + "  max-cached-offline-users: 25\n"
                + "  preload-on-join: false\n"
                + "  cache-failure-fallback: allow\n"
                + "  auto-save-interval: 9\n"
                + "  log-save: true\n"
                + "  log-cache-eviction: true\n"
                + "  log-cache-mode: individual\n"
                + "temporary-permissions:\n"
                + "  expiry-check-interval: 11\n"
                + "  log-expiry: true\n"
                + "  log-expiry-mode: individual\n"
                + "identity:\n"
                + "  uuid-source: online_only\n"
                + "server-context:\n"
                + "  global-name: lobby\n"
                + "  environment:\n"
                + "    region: au\n"
                + "inheritance:\n"
                + "  max-depth: 3\n"
                + "  conflict-resolution: true_wins\n"
                + "  meta-formatting: accumulated\n"
                + "threading:\n"
                + "  worker-pool-size: 7\n"
                + "commands:\n"
                + "  register-base-commands: false\n"
                + "audit-log:\n"
                + "  log-to-file: false\n"
                + "  broadcast-to-ops: false\n"
                + "  network-wide-logging: false\n"));

        var configuration = AdvancedConfiguration.load(platform());

        assertEquals("redis", configuration.messagingType());
        assertEquals(17, configuration.offlineEvictionTime());
        assertEquals(25, configuration.maxCachedOfflineUsers());
        assertFalse(configuration.preloadOnJoin());
        assertEquals(CacheFailureFallback.ALLOW, configuration.cacheFailureFallback());
        assertEquals(11, configuration.expiryCheckInterval());
        assertTrue(configuration.logExpiry());
        assertEquals(ExpiryLogMode.INDIVIDUAL, configuration.logExpiryMode());
        assertEquals("lobby", configuration.globalContextName());
        assertEquals("au", configuration.environment().get("region"));
        assertEquals(3, configuration.maxInheritanceDepth());
        assertEquals(PermissionConflictResolution.TRUE_WINS, configuration.conflictResolution());
        assertEquals(MetaFormatting.ACCUMULATED, configuration.metaFormatting());
        assertEquals("redis.internal", configuration.redisHost());
        assertEquals(6380, configuration.redisPort());
        assertEquals("permissions", configuration.redisChannel());
        assertFalse(configuration.auditLogToFile());
        assertEquals(9, configuration.autoSaveInterval());
        assertTrue(configuration.logSave());
        assertTrue(configuration.logCacheEviction());
        assertEquals(CacheLogMode.INDIVIDUAL, configuration.logCacheMode());
        assertEquals(7, configuration.workerPoolSize());
        assertFalse(configuration.registerBaseCommands());
        assertFalse(configuration.broadcastToOps());
        assertFalse(configuration.networkWideLogging());
    }

    @Test
    void cacheAnnouncementsDefaultToDisabled() throws IOException {
        TestFiles.writeString(directory.resolve("advanced.yml"), "advanced-version: 1\n");

        var configuration = AdvancedConfiguration.load(platform());

        assertFalse(configuration.logSave());
        assertFalse(configuration.logCacheEviction());
        assertEquals(CacheLogMode.TOTAL, configuration.logCacheMode());
        assertEquals(5000, configuration.maxCachedOfflineUsers());
        assertTrue(configuration.preloadOnJoin());
        assertEquals(CacheFailureFallback.DENY, configuration.cacheFailureFallback());
        assertFalse(configuration.logExpiry());
        assertEquals(ExpiryLogMode.TOTAL, configuration.logExpiryMode());
        assertEquals(PermissionConflictResolution.DENY_WINS, configuration.conflictResolution());
        assertEquals(MetaFormatting.HIGHEST_WEIGHT, configuration.metaFormatting());
        assertEquals(4, configuration.workerPoolSize());
        assertTrue(configuration.registerBaseCommands());
    }

    @Test
    void rejectsInvalidProductionControlValues() throws IOException {
        TestFiles.writeString(directory.resolve("advanced.yml"), ("threading:\n"
                + "  worker-pool-size: 0\n"));

        assertThrows(IllegalStateException.class, () -> AdvancedConfiguration.load(platform()));

        TestFiles.writeString(directory.resolve("advanced.yml"), ("inheritance:\n"
                + "  conflict-resolution: random\n"));

        assertThrows(IllegalStateException.class, () -> AdvancedConfiguration.load(platform()));

        TestFiles.writeString(directory.resolve("advanced.yml"), ("inheritance:\n"
                + "  meta-formatting: random\n"));

        assertThrows(IllegalStateException.class, () -> AdvancedConfiguration.load(platform()));

        TestFiles.writeString(directory.resolve("advanced.yml"), ("temporary-permissions:\n"
                + "  log-expiry-mode: random\n"));

        assertThrows(IllegalStateException.class, () -> AdvancedConfiguration.load(platform()));

        TestFiles.writeString(directory.resolve("advanced.yml"), ("temporary-permissions:\n"
                + "  expiry-check-interval: 0\n"));

        assertThrows(IllegalStateException.class, () -> AdvancedConfiguration.load(platform()));
    }

    @Test
    void requestsBundledResourceWhenConfigurationIsMissing() {
        var saved = new java.util.ArrayList<String>();

        var platform = new PlatformConfiguration() {
            @Override
            public Path dataDirectory() {
                return directory;
            }

            @Override
            public void saveResource(String resource, boolean replace) {
                saved.add(resource);

                try {
                    TestFiles.writeString(resolve(resource), "default-group: guest");
                } catch (IOException exception) {
                    throw new AssertionError(exception);
                }
            }
        };

        assertEquals("guest", GeneralConfiguration.load(platform).defaultGroup());
        assertEquals(Immutables.listOf("config.yml"), saved);
    }

    @Test
    void loadsDatabaseBackendConfigurationAndRejectsMissingRequiredValues() throws IOException {
        var valid = ("type: postgresql\n"
                + "local:\n"
                + "  storage-directory: storage/sql\n"
                + "  filename: permissions.db\n"
                + "credentials:\n"
                + "  host: db.internal\n"
                + "  port: 5432 # default PostgreSQL port\n"
                + "  database: permissions\n"
                + "  username: pex\n"
                + "  password: secret\n"
                + "pool:\n"
                + "  maximum-pool-size: 12\n"
                + "  minimum-idle: 3\n"
                + "  connection-timeout: 4321\n"
                + "  max-lifetime: 60000\n"
                + "hibernate:\n"
                + "  ddl-generation: validate\n");

        TestFiles.writeString(directory.resolve("database.yml"), valid);

        var backend = BackendConfiguration.load(platform());

        assertEquals(BackendType.POSTGRES, backend.type());
        assertEquals("storage/sql", backend.localStorageDirectory());
        assertEquals("permissions.db", backend.localFilename());
        assertEquals(directory.resolve("storage/sql").toAbsolutePath(), backend.resolveLocalStorageDirectory(directory));
        assertEquals(directory.resolve("storage/sql/permissions.db").toAbsolutePath(), backend.resolveLocalFile(directory));
        assertEquals("db.internal", backend.credentials().host());
        assertEquals(5432, backend.credentials().port());
        assertEquals(12, backend.pool().maximumPoolSize());
        assertEquals(4321L, backend.pool().connectionTimeout());
        assertEquals(DdlGeneration.VALIDATE, backend.ddlGeneration());

        TestFiles.writeString(directory.resolve("database.yml"), valid.replace("4321", "249"));

        assertThrows(IllegalStateException.class, () -> BackendConfiguration.load(platform()));

        TestFiles.writeString(directory.resolve("database.yml"), valid.replace("validate", "create-drop"));

        assertThrows(IllegalStateException.class, () -> BackendConfiguration.load(platform()));

        TestFiles.writeString(directory.resolve("database.yml"), valid.replace("storage/sql", "../outside"));

        assertThrows(IllegalStateException.class, () -> BackendConfiguration.load(platform()));

        TestFiles.writeString(directory.resolve("database.yml"), "type: memory\n");

        assertThrows(IllegalStateException.class, () -> BackendConfiguration.load(platform()));
    }

    @Test
    void parsesBackendAliasesAndExposesPersistenceFlags() {
        assertEquals(BackendType.POSTGRES, BackendType.parse("postgresql"));
        assertEquals(BackendType.YAML, BackendType.parse("yml"));
        assertEquals(DdlGeneration.UPDATE, DdlGeneration.parse("update"));
        assertEquals(DdlGeneration.NONE, DdlGeneration.parse("NONE"));
        assertFalse(BackendType.MEMORY.persistent());
        assertTrue(BackendType.SQLITE.persistent());
        assertThrows(IllegalArgumentException.class, () -> BackendType.parse("mongodb"));
    }

    @Test
    void bundledConfigurationFilesMatchTheTypedConfigurationModels() throws IOException {
        for (var resource : Immutables.listOf("config.yml", "advanced.yml", "database.yml")) {
            try (var input = ConfigurationTest.class.getClassLoader().getResourceAsStream(resource)) {
                assertNotNull(input, resource);

                Files.copy(input, directory.resolve(resource), StandardCopyOption.REPLACE_EXISTING);
            }
        }

        var general = GeneralConfiguration.load(platform());
        var advanced = AdvancedConfiguration.load(platform());
        var backend = BackendConfiguration.load(platform());

        assertEquals("default", general.defaultGroup());
        assertTrue(general.shorthandExpansionsEnabled());
        assertTrue(general.bstatsEnabled());
        assertFalse(general.nativePermissionsRequired());
        assertEquals("mix_mode", advanced.uuidSource());
        assertEquals("localhost", advanced.redisHost());
        assertEquals(BackendType.H2, backend.type());
        assertEquals("data", backend.localStorageDirectory());
        assertEquals(5_000L, backend.pool().connectionTimeout());
        assertEquals(DdlGeneration.UPDATE, backend.ddlGeneration());
    }

    @Test
    void rejectsConfigurationVersionsTheRuntimeCannotInterpret() throws IOException {
        TestFiles.writeString(directory.resolve("config.yml"), "config-version: 2\n");
        TestFiles.writeString(directory.resolve("advanced.yml"), "advanced-version: 2\n");
        TestFiles.writeString(directory.resolve("database.yml"), "data-version: 2\n");

        assertThrows(IllegalStateException.class, () -> GeneralConfiguration.load(platform()));
        assertThrows(IllegalStateException.class, () -> AdvancedConfiguration.load(platform()));
        assertThrows(IllegalStateException.class, () -> BackendConfiguration.load(platform()));
    }

    private PlatformConfiguration platform() {
        return new PlatformConfiguration() {
            @Override
            public Path dataDirectory() {
                return directory;
            }

            @Override
            public void saveResource(String resource, boolean replace) {
                throw new AssertionError("resource should exist");
            }
        };
    }
}
