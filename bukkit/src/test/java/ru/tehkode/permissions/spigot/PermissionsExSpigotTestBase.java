package ru.tehkode.permissions.spigot;

import dev.rono.permissions.api.runtime.PlatformRuntime;
import dev.rono.permissions.api.runtime.BukkitContextResolver;
import dev.rono.permissions.api.runtime.ContextResolver;
import dev.rono.permissions.core.DefaultPermissionManager;
import dev.rono.permissions.core.PermissionsExConfig;
import dev.rono.permissions.core.config.PexConfigData;
import dev.rono.permissions.core.config.PexRef;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.BeforeEach;
import ru.tehkode.permissions.PEXBackendConfiguration;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.backends.PermissionBackend;
import ru.tehkode.permissions.spigot.backends.FileBackend;
import ru.tehkode.permissions.spigot.backends.MemoryBackend;
import ru.tehkode.permissions.spigot.bukkit.BukkitPEXBackendConfiguration;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

public abstract class PermissionsExSpigotTestBase {
    protected PermissionManager manager;
    protected PermissionsExConfig config;
    protected PlatformRuntime platformRuntime;
    protected YamlConfiguration yamlConfig;

    static {
        PermissionBackend.registerBackendAlias("file", FileBackend.class);
        PermissionBackend.registerBackendAlias("memory", MemoryBackend.class);
    }

    @BeforeEach
    public void setUp() throws Exception {
        yamlConfig = new YamlConfiguration();
        yamlConfig.set("permissions.backend", "memory");

        AtomicReference<PexConfigData> cfgRef = new AtomicReference<>(
                PexConfigData.testDefaults(
                        yamlConfig.getString("permissions.backend", "memory"),
                        yamlConfig.getString("permissions.basedir", ".")));

        config = new PermissionsExConfig() {
            private final PexRef<PexConfigData> ref =
                    new PexRef<>() {
                        @Override
                        public PexConfigData current() {
                            return cfgRef.get();
                        }
                    };

            private void sync() {
                cfgRef.set(
                        PexConfigData.testDefaults(
                                yamlConfig.getString("permissions.backend", "memory"),
                                yamlConfig.getString("permissions.basedir", ".")));
            }

            @Override
            public PexRef<PexConfigData> options() {
                return ref;
            }

            @Override
            public void setDefaultBackend(String backendName) {
                yamlConfig.set("permissions.backend", backendName);
                sync();
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
                return yamlConfig.getString("permissions.backend", "memory");
            }

            @Override
            public boolean shouldLogPlayers() {
                return false;
            }

            @Override
            public boolean createUserRecords() {
                return false;
            }

            @Override
            public boolean saveDefaultGroup() {
                return false;
            }

            @Override
            public boolean informPlayers() {
                return false;
            }

            @Override
            public String getBasedir() {
                return yamlConfig.getString("permissions.basedir", ".");
            }

            @Override
            public PEXBackendConfiguration pexBackendConfiguration(String backend) {
                ConfigurationSection section =
                        yamlConfig.getConfigurationSection("permissions.backends." + backend);
                if (section == null) {
                    section = yamlConfig.createSection("permissions.backends." + backend);
                }
                return new BukkitPEXBackendConfiguration(section);
            }

            @Override
            public void save() {}
        };

        platformRuntime = PlatformRuntime.adapterOnly(
                new dev.rono.permissions.api.runtime.PlatformAdapter() {
                    @Override
                    public String uuidToName(UUID uid) {
                        return uid.toString();
                    }

                    @Override
                    public UUID nameToUuid(String name) {
                        return UUID.nameUUIDFromBytes(name.getBytes());
                    }

                    @Override
                    public boolean isOnline(UUID uuid) {
                        return false;
                    }

                    @Override
                    public UUID serverId() {
                        return UUID.fromString("00000000-0000-0000-0000-000000000000");
                    }

                    @Override
                    public Collection<String> realmNames() {
                        return Collections.singletonList("world");
                    }

                    @Override
                    public String onlineRealm(UUID uuid) {
                        return null;
                    }

                    @Override
                    public String onlineDisplayName(UUID uuid) {
                        return null;
                    }

                    @Override
                    public boolean isOperator(UUID uuid) {
                        return false;
                    }

                    @Override
                    public ContextResolver getContextResolver() {
                        return new BukkitContextResolver();
                    }
                });

        manager = new DefaultPermissionManager(config, Logger.getLogger("PEX-Test"), platformRuntime);
    }

    protected void waitForExecutor() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        manager.getExecutor().execute(latch::countDown);
        latch.await(5, TimeUnit.SECONDS);
    }
}
