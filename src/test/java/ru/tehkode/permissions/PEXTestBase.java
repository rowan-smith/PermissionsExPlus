package ru.tehkode.permissions;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.BeforeEach;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredListener;
import ru.tehkode.permissions.backends.PermissionBackend;
import ru.tehkode.permissions.backends.file.FileBackend;
import ru.tehkode.permissions.backends.memory.MemoryBackend;
import ru.tehkode.permissions.backends.memory.MemoryData;
import ru.tehkode.permissions.bukkit.PermissionsEx;
import ru.tehkode.permissions.bukkit.PermissionsExConfig;
import ru.tehkode.permissions.events.PermissionEvent;
import ru.tehkode.permissions.exceptions.PermissionBackendException;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public abstract class PEXTestBase {
    protected PermissionManager manager;
    protected PermissionsExConfig config;
    protected PermissionsEx plugin;
    protected NativeInterface nativeI;
    protected Server server;
    protected World world;
    protected YamlConfiguration yamlConfig;

    public static class MockBackend extends PermissionBackend {
        private final Map<String, MemoryData> users = new ConcurrentHashMap<>();
        private final Map<String, MemoryData> groups = new ConcurrentHashMap<>();

        public MockBackend(PermissionManager manager, ConfigurationSection config) throws PermissionBackendException {
            super(manager, config);
        }

        @Override
        public PermissionsUserData getUserData(String userName) {
            MemoryData data = users.get(userName);
            if (data == null) {
                data = new MemoryData(userName);
                users.put(userName, data);
            }
            return data;
        }

        @Override
        public PermissionsGroupData getGroupData(String groupName) {
            MemoryData data = groups.get(groupName);
            if (data == null) {
                data = new MemoryData(groupName);
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
        public int getSchemaVersion() { return 0; }
        @Override
        protected void setSchemaVersion(int version) {}
        @Override
        public void reload() throws PermissionBackendException {}
        @Override
        public List<String> getWorldInheritance(String world) { return Collections.emptyList(); }
        @Override
        public Map<String, List<String>> getAllWorldInheritance() { return Collections.emptyMap(); }
        @Override
        public void setWorldInheritance(String world, List<String> inheritance) {}
        @Override
        public void writeContents(Writer writer) throws IOException {}
    }

    static {
        PermissionBackend.registerBackendAlias("mock", MockBackend.class);
        PermissionBackend.registerBackendAlias("memory", MemoryBackend.class);
        PermissionBackend.registerBackendAlias("file", FileBackend.class);
    }

    @BeforeEach
    public void setUp() throws Exception {
        world = (World) Proxy.newProxyInstance(World.class.getClassLoader(), new Class[]{World.class}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if (method.getName().equals("getName")) {
                    return "world";
                }
                if (method.getName().equals("equals")) {
                    return proxy == args[0];
                }
                if (method.getName().equals("hashCode")) {
                    return System.identityHashCode(proxy);
                }
                if (method.getName().equals("toString")) {
                    return "MockWorld";
                }
                return null;
            }
        });

        server = (Server) Proxy.newProxyInstance(Server.class.getClassLoader(), new Class[]{Server.class}, new InvocationHandler() {
            private final Map<Class<? extends Event>, List<Listener>> listeners = new HashMap<>();
            private Object pluginManager;

            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if (method.getName().equals("equals")) {
                    return proxy == args[0];
                }
                if (method.getName().equals("hashCode")) {
                    return System.identityHashCode(proxy);
                }
                if (method.getName().equals("toString")) {
                    return "MockServer";
                }
                if (method.getName().equals("getPluginManager")) {
                    if (pluginManager == null) {
                        pluginManager = Proxy.newProxyInstance(PluginManager.class.getClassLoader(), new Class[]{PluginManager.class}, new InvocationHandler() {
                            @Override
                            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                                if (method.getName().equals("equals")) {
                                    return proxy == args[0];
                                }
                                if (method.getName().equals("hashCode")) {
                                    return System.identityHashCode(proxy);
                                }
                                if (method.getName().equals("toString")) {
                                    return "MockPluginManager";
                                }
                                if (method.getName().equals("getPermissions")) {
                                    return Collections.emptySet();
                                }
                                if (method.getName().equals("registerEvents")) {
                                    Listener listener = (Listener) args[0];
                                    for (Method m : listener.getClass().getMethods()) {
                                        if (m.isAnnotationPresent(EventHandler.class) && m.getParameterCount() == 1) {
                                            Class<? extends Event> eventClass = (Class<? extends Event>) m.getParameterTypes()[0];
                                            listeners.computeIfAbsent(eventClass, k -> new ArrayList<>()).add(listener);
                                        }
                                    }
                                    return null;
                                }
                                if (method.getName().equals("callEvent")) {
                                    Event event = (Event) args[0];
                                    List<Listener> eventListeners = listeners.get(event.getClass());
                                    if (eventListeners != null) {
                                        for (Listener l : eventListeners) {
                                            for (Method m : l.getClass().getMethods()) {
                                                if (m.isAnnotationPresent(EventHandler.class) && m.getParameterCount() == 1 && m.getParameterTypes()[0].isAssignableFrom(event.getClass())) {
                                                    m.invoke(l, event);
                                                }
                                            }
                                        }
                                    }
                                    return null;
                                }
                                return null;
                            }
                        });
                    }
                    return pluginManager;
                }
                if (method.getName().equals("getLogger")) {
                    return Logger.getLogger("Minecraft");
                }
                if (method.getName().equals("getName")) {
                    return "TestServer";
                }
                if (method.getName().equals("getVersion")) {
                    return "1.0";
                }
                if (method.getName().equals("getBukkitVersion")) {
                    return "1.0";
                }
                if (method.getName().equals("getWorlds")) {
                    return Collections.singletonList(world);
                }
                if (method.getName().equals("getWorld")) {
                    return world;
                }
                if (method.getName().equals("getOnlinePlayers")) {
                    return Collections.emptyList();
                }
                return null;
            }
        });
        
        try {
            Field serverField = Bukkit.class.getDeclaredField("server");
            serverField.setAccessible(true);
            serverField.set(null, server);
        } catch (Exception e) {
            try {
                Bukkit.setServer(server);
            } catch (UnsupportedOperationException ex) {
                // Ignore
            }
        }

        plugin = null; 
        yamlConfig = new YamlConfiguration();
        yamlConfig.set("permissions.backend", "mock");
        config = new PermissionsExConfig(yamlConfig, plugin) {
            @Override
            public void save() {
                // do nothing
            }
        };
        
        nativeI = new NativeInterface() {
            @Override
            public String UUIDToName(UUID uid) {
                return "Player_" + uid.toString().substring(0, 8);
            }

            @Override
            public UUID nameToUUID(String name) {
                return UUID.nameUUIDFromBytes(name.getBytes());
            }

            @Override
            public boolean isOnline(UUID uuid) {
                return false;
            }

            @Override
            public UUID getServerUUID() {
                return UUID.fromString("00000000-0000-0000-0000-000000000000");
            }

            @Override
            public void callEvent(PermissionEvent event) {
                Bukkit.getServer().getPluginManager().callEvent(event);
            }
        };
        
        manager = new PermissionManager(config, Logger.getLogger("PEX"), nativeI);
    }

    public void waitForExecutor() throws InterruptedException {
        // Since we use a single thread executor, we can just submit a task and wait for it to finish
        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        manager.getExecutor().execute(latch::countDown);
        latch.await(5, java.util.concurrent.TimeUnit.SECONDS);
    }
}