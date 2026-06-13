package dev.rono.permissions.bungee.backends.file;

import dev.rono.permissions.core.DefaultPermissionManager;
import dev.rono.permissions.api.runtime.PlatformRuntime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.backends.PermissionBackend;

import java.nio.file.Path;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BungeeFileBackendTest {

    @TempDir
    Path tempDir;

    private DefaultPermissionManager manager;
    private BungeeTestConfig config;

    @BeforeEach
    void setUp() throws Exception {
        PermissionBackend.registerBackendAlias("file", BungeeFileBackend.class);
        config = new BungeeTestConfig(tempDir);
        manager = new DefaultPermissionManager(config, java.util.logging.Logger.getLogger("bungee-test"), PlatformRuntime.adapterOnly(new BungeeTestPlatform()));
    }

    @Test
    void persistsGroupPermissionAcrossReload() throws Exception {
        PermissionGroup group = manager.getGroup("staff");
        group.addPermission("proxy.command.alert", null);
        manager.getBackend().close();

        DefaultPermissionManager reloaded = new DefaultPermissionManager(config, manager.getLogger(), PlatformRuntime.adapterOnly(new BungeeTestPlatform()));
        assertTrue(reloaded.getGroup("staff").getPermissions(null).contains("proxy.command.alert"));
    }

    @Test
    void persistsWorldInheritance() throws Exception {
        manager.getBackend().setWorldInheritance("lobby", Collections.singletonList("global"));
        manager.getBackend().close();

        DefaultPermissionManager reloaded = new DefaultPermissionManager(config, manager.getLogger(), PlatformRuntime.adapterOnly(new BungeeTestPlatform()));
        assertEquals(Collections.singletonList("global"), reloaded.getBackend().getWorldInheritance("lobby"));
    }

    private static final class BungeeTestConfig implements dev.rono.permissions.core.PermissionsExConfig {
        private final Path base;
        private final AtomicReference<dev.rono.permissions.core.config.PexConfigData> snapshot;

        BungeeTestConfig(Path base) {
            this.base = base;
            this.snapshot = new AtomicReference<>(dev.rono.permissions.core.config.PexConfigData.testDefaults("file", base.toString()));
        }

        @Override
        public dev.rono.permissions.core.config.PexRef<dev.rono.permissions.core.config.PexConfigData> options() {
            return () -> snapshot.get();
        }

        @Override
        public void setDefaultBackend(String backendName) {
            snapshot.set(snapshot.get().withBackend(backendName));
        }

        @Override
        public boolean isDebug() {
            return false;
        }

        @Override
        public boolean allowOps() {
            return false;
        }

        @Override
        public boolean userAddGroupsLast() {
            return false;
        }

        @Override
        public String getDefaultBackend() {
            return snapshot.get().backend();
        }

        @Override
        public boolean shouldLogPlayers() {
            return false;
        }

        @Override
        public boolean createUserRecords() {
            return true;
        }

        @Override
        public boolean saveDefaultGroup() {
            return true;
        }

        @Override
        public boolean informPlayers() {
            return false;
        }

        @Override
        public String getBasedir() {
            return base.toString();
        }

        @Override
        public ru.tehkode.permissions.PEXBackendConfiguration pexBackendConfiguration(String backend) {
            return new ru.tehkode.permissions.PEXBackendConfiguration() {
                @Override
                public String getName() {
                    return backend;
                }

                @Override
                public String getString(String path) {
                    return "permissions.yml".equals(path) ? "permissions.yml" : null;
                }

                @Override
                public String getString(String path, String def) {
                    String s = getString(path);
                    return s != null ? s : def;
                }

                @Override
                public void set(String path, Object value) {}

                @Override
                public java.util.List<String> getStringList(String path) {
                    return java.util.List.of();
                }

                @Override
                public ru.tehkode.permissions.PEXBackendConfiguration getConfigurationSection(String path) {
                    return null;
                }

                @Override
                public ru.tehkode.permissions.PEXBackendConfiguration createSection(String path) {
                    return null;
                }

                @Override
                public boolean isConfigurationSection(String path) {
                    return false;
                }

                @Override
                public java.util.Map<String, Object> getValues(boolean deep) {
                    return java.util.Map.of("file", "permissions.yml");
                }
            };
        }

        @Override
        public void save() {}
    }

    private static final class BungeeTestPlatform implements dev.rono.permissions.api.runtime.PlatformAdapter {
        @Override
        public java.util.UUID serverId() {
            return java.util.UUID.randomUUID();
        }

        @Override
        public java.util.Collection<String> realmNames() {
            return java.util.List.of("lobby");
        }

        @Override
        public String onlineRealm(java.util.UUID player) {
            return null;
        }

        @Override
        public String onlineDisplayName(java.util.UUID player) {
            return null;
        }

        @Override
        public java.util.UUID nameToUuid(String name) {
            return java.util.UUID.nameUUIDFromBytes(name.getBytes());
        }

        @Override
        public String uuidToName(java.util.UUID uuid) {
            return null;
        }

        @Override
        public boolean isOnline(java.util.UUID uuid) {
            return false;
        }

        @Override
        public boolean isOperator(java.util.UUID uuid) {
            return false;
        }
    }
}
