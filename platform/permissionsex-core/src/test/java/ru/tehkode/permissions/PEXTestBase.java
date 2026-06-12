package ru.tehkode.permissions;

import dev.rono.permissions.api.bus.EntityDispatch;
import dev.rono.permissions.api.bus.PermissionDispatch;
import dev.rono.permissions.api.bus.SystemDispatch;
import dev.rono.permissions.api.runtime.PlatformAdapter;
import dev.rono.permissions.core.PermissionsExConfig;
import dev.rono.permissions.core.backends.AbstractPermissionBackend;
import ru.tehkode.permissions.backends.PermissionBackend;
import ru.tehkode.permissions.exceptions.PermissionBackendException;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import dev.rono.permissions.core.config.PexConfigData;
import dev.rono.permissions.core.config.PexRef;

import org.junit.jupiter.api.BeforeEach;

import dev.rono.permissions.core.DefaultPermissionManager;

public abstract class PEXTestBase {
    protected PermissionManager manager;
    protected PermissionsExConfig config;
    protected PlatformAdapter platformAdapter;
    protected final List<PermissionDispatch> firedDispatches = new ArrayList<>();
    private final Map<String, MapPEXBackendConfiguration> backendConfigs = new ConcurrentHashMap<>();

    static final class MapPEXBackendConfiguration implements PEXBackendConfiguration {
        private final String name;
        private final Map<String, Object> values;

        MapPEXBackendConfiguration(String name, Map<String, Object> values) {
            this.name = name;
            this.values = values;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getString(String path) {
            Object v = values.get(path);
            return v instanceof String ? (String) v : null;
        }

        @Override
        public String getString(String path, String def) {
            String s = getString(path);
            return s != null ? s : def;
        }

        @Override
        public void set(String path, Object value) {
            values.put(path, value);
        }

        @Override
        @SuppressWarnings("unchecked")
        public List<String> getStringList(String path) {
            Object v = values.get(path);
            if (v instanceof List) {
                return (List<String>) v;
            }
            return Collections.emptyList();
        }

        @Override
        public PEXBackendConfiguration getConfigurationSection(String path) {
            Object v = values.get(path);
            if (v instanceof MapPEXBackendConfiguration) {
                return (MapPEXBackendConfiguration) v;
            }
            return null;
        }

        @Override
        public PEXBackendConfiguration createSection(String path) {
            Map<String, Object> m = new LinkedHashMap<>();
            MapPEXBackendConfiguration sec = new MapPEXBackendConfiguration(path, m);
            values.put(path, sec);
            return sec;
        }

        @Override
        public boolean isConfigurationSection(String path) {
            return values.get(path) instanceof MapPEXBackendConfiguration;
        }

        @Override
        public Map<String, Object> getValues(boolean deep) {
            Map<String, Object> out = new LinkedHashMap<>();
            for (Map.Entry<String, Object> e : values.entrySet()) {
                if (!(e.getValue() instanceof MapPEXBackendConfiguration)) {
                    out.put(e.getKey(), e.getValue());
                }
            }
            return out;
        }
    }

    public static class MockBackend extends AbstractPermissionBackend {
        private final Map<String, MockData> users = new ConcurrentHashMap<>();
        private final Map<String, MockData> groups = new ConcurrentHashMap<>();
        private final Map<String, List<String>> worldInheritance = new ConcurrentHashMap<>();

        public MockBackend(PermissionManager manager, PEXBackendConfiguration config) throws PermissionBackendException {
            super(manager, config);
        }

        @Override
        public PermissionsUserData getUserData(String userName) {
            MockData data = users.get(userName);
            if (data == null) {
                data = new MockData(userName);
                users.put(userName, data);
            }
            return data;
        }

        @Override
        public PermissionsGroupData getGroupData(String groupName) {
            MockData data = groups.get(groupName);
            if (data == null) {
                data = new MockData(groupName);
                groups.put(groupName, data);
            }
            return data;
        }

        @Override
        public boolean hasUser(String userName) {
            return users.containsKey(userName);
        }

        @Override
        public boolean hasGroup(String group) {
            return groups.containsKey(group);
        }

        @Override
        public Collection<String> getUserIdentifiers() {
            return users.keySet();
        }

        @Override
        public Collection<String> getUserNames() {
            return users.keySet();
        }

        @Override
        public Collection<String> getGroupNames() {
            return groups.keySet();
        }

        @Override
        public int getSchemaVersion() {
            return 0;
        }

        @Override
        protected void setSchemaVersion(int version) {}

        @Override
        public void reload() throws PermissionBackendException {}

        @Override
        public List<String> getWorldInheritance(String world) {
            List<String> inheritance = worldInheritance.get(world);
            return inheritance == null ? Collections.emptyList() : new ArrayList<>(inheritance);
        }

        @Override
        public Map<String, List<String>> getAllWorldInheritance() {
            Map<String, List<String>> copy = new HashMap<>();
            for (Map.Entry<String, List<String>> entry : worldInheritance.entrySet()) {
                copy.put(entry.getKey(), new ArrayList<>(entry.getValue()));
            }
            return copy;
        }

        @Override
        public void setWorldInheritance(String world, List<String> inheritance) {
            worldInheritance.put(world, new ArrayList<>(inheritance));
        }

        @Override
        public void writeContents(Writer writer) throws IOException {}
    }

    public static class MockData implements PermissionsUserData, PermissionsGroupData {
        private String identifier;
        private final Map<String, List<String>> permissions = new HashMap<>();
        private final Map<String, Map<String, String>> options = new HashMap<>();
        private final Map<String, List<String>> parents = new HashMap<>();

        public MockData(String identifier) {
            this.identifier = identifier;
        }

        @Override
        public void load() {}

        @Override
        public String getIdentifier() {
            return identifier;
        }

        @Override
        public List<String> getPermissions(String worldName) {
            List<String> perms = permissions.get(worldName);
            return perms == null ? new ArrayList<>() : perms;
        }

        @Override
        public void setPermissions(List<String> permissions, String worldName) {
            this.permissions.put(worldName, new ArrayList<>(permissions));
        }

        @Override
        public Map<String, List<String>> getPermissionsMap() {
            return permissions;
        }

        @Override
        public Set<String> getWorlds() {
            Set<String> worlds = new HashSet<>(permissions.keySet());
            worlds.addAll(options.keySet());
            worlds.addAll(parents.keySet());
            return worlds;
        }

        @Override
        public String getOption(String option, String worldName) {
            Map<String, String> worldOptions = options.get(worldName);
            return worldOptions == null ? null : worldOptions.get(option);
        }

        @Override
        public void setOption(String option, String value, String world) {
            Map<String, String> worldOptions = options.computeIfAbsent(world, k -> new HashMap<>());
            if (value == null) {
                worldOptions.remove(option);
            } else {
                worldOptions.put(option, value);
            }
        }

        @Override
        public Map<String, String> getOptions(String worldName) {
            Map<String, String> worldOptions = options.get(worldName);
            return worldOptions == null ? new HashMap<>() : worldOptions;
        }

        @Override
        public Map<String, Map<String, String>> getOptionsMap() {
            return options;
        }

        @Override
        public List<String> getParents(String worldName) {
            List<String> worldParents = parents.get(worldName);
            return worldParents == null ? new ArrayList<>() : worldParents;
        }

        @Override
        public void setParents(List<String> parents, String worldName) {
            this.parents.put(worldName, new ArrayList<>(parents));
        }

        @Override
        public boolean isVirtual() {
            return false;
        }

        @Override
        public void save() {}

        @Override
        public void remove() {}

        @Override
        public Map<String, List<String>> getParentsMap() {
            return parents;
        }

        @Override
        public boolean setIdentifier(String identifier) {
            this.identifier = identifier;
            return true;
        }
    }

    static {
        PermissionBackend.registerBackendAlias("mock", MockBackend.class);
    }

    @BeforeEach
    public void setUp() throws Exception {
        firedDispatches.clear();
        backendConfigs.clear();

        AtomicReference<PexConfigData> snapshot =
                new AtomicReference<>(PexConfigData.testDefaults("mock", "."));

        config =
                new PermissionsExConfig() {
                    private final PexRef<PexConfigData> ref =
                            new PexRef<>() {
                                @Override
                                public PexConfigData current() {
                                    return snapshot.get();
                                }
                            };

                    @Override
                    public PexRef<PexConfigData> options() {
                        return ref;
                    }

                    @Override
                    public void setDefaultBackend(String backendName) {
                        snapshot.set(snapshot.get().withBackend(backendName));
                    }

                    @Override
                    public boolean isDebug() {
                        return snapshot.get().debug();
                    }

                    @Override
                    public boolean allowOps() {
                        return snapshot.get().allowOps();
                    }

                    @Override
                    public boolean userAddGroupsLast() {
                        return snapshot.get().userAddGroupsLast();
                    }

                    @Override
                    public String getDefaultBackend() {
                        return snapshot.get().backend();
                    }

                    @Override
                    public boolean shouldLogPlayers() {
                        return snapshot.get().logPlayers();
                    }

                    @Override
                    public boolean createUserRecords() {
                        return snapshot.get().createUserRecords();
                    }

                    @Override
                    public boolean saveDefaultGroup() {
                        return snapshot.get().saveDefaultGroup();
                    }

                    @Override
                    public boolean informPlayers() {
                        return snapshot.get().informPlayerChanges();
                    }

                    @Override
                    public String getBasedir() {
                        return snapshot.get().basedir();
                    }

                    @Override
                    public PEXBackendConfiguration pexBackendConfiguration(String backend) {
                        return backendConfigs.computeIfAbsent(
                                backend, b -> new MapPEXBackendConfiguration(b, new LinkedHashMap<>()));
                    }

                    @Override
                    public void save() {}
                };

        platformAdapter =
                new PlatformAdapter() {
                    final UUID pid = UUID.fromString("00000000-0000-0000-0000-000000000000");

                    @Override
                    public String uuidToName(UUID uid) {
                        return "Player_" + uid.toString().substring(0, 8);
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
                        return pid;
                    }

                    @Override
                    public Collection<String> realmNames() {
                        return Collections.singletonList("world");
                    }

                    @Override
                    public void publish(PermissionDispatch dispatch) {
                        firedDispatches.add(dispatch);
                        if (dispatch instanceof EntityDispatch ed) {
                            assert ed.mutation() != null;
                            return;
                        }
                        if (dispatch instanceof SystemDispatch sd) {
                            assert sd.mutation() != null;
                            return;
                        }
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
                };

        manager = new DefaultPermissionManager(config, Logger.getLogger("PEX"), platformAdapter);
    }

    public void waitForExecutor() throws InterruptedException {
        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        manager.getExecutor().execute(latch::countDown);
        latch.await(5, java.util.concurrent.TimeUnit.SECONDS);
    }
}
