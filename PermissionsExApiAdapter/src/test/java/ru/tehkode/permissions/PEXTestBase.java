package ru.tehkode.permissions;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import ru.tehkode.permissions.backends.PermissionBackend;
import ru.tehkode.permissions.backends.data.PlusTestFixture;
import ru.tehkode.permissions.backends.file.FileBackend;
import ru.tehkode.permissions.backends.memory.MemoryBackend;
import ru.tehkode.permissions.bukkit.PermissionsExConfig;
import ru.tehkode.permissions.events.PermissionEvent;

/**
 * Boots a real PermissionsExPlus runtime and a PermissionManager shim on top.
 */
public abstract class PEXTestBase {
    @TempDir
    Path tempDir;

    protected PermissionManager manager;
    protected PermissionsExConfig config;
    protected NativeInterface nativeI;
    protected Server server;
    protected World world;
    protected PlusTestFixture fixture;

    static {
        PermissionBackend.registerBackendAlias("memory", MemoryBackend.class);
        PermissionBackend.registerBackendAlias("file", FileBackend.class);
        PermissionBackend.registerBackendAlias("data", ru.tehkode.permissions.backends.data.PermissionBackend.class);
    }

    @BeforeEach
    public void setUp() throws Exception {
        fixture = new PlusTestFixture(tempDir);

        world = (World) Proxy.newProxyInstance(World.class.getClassLoader(), new Class[]{World.class}, (proxy, method, args) -> {
            return switch (method.getName()) {
                case "getName" -> "world";
                case "equals" -> proxy == args[0];
                case "hashCode" -> System.identityHashCode(proxy);
                case "toString" -> "MockWorld";
                default -> null;
            };
        });

        server = (Server) Proxy.newProxyInstance(Server.class.getClassLoader(), new Class[]{Server.class}, new InvocationHandler() {
            private final Map<Class<? extends Event>, List<Listener>> listeners = new HashMap<>();
            private Object pluginManager;

            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                return switch (method.getName()) {
                    case "equals" -> proxy == args[0];
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "toString" -> "MockServer";
                    case "getLogger" -> Logger.getLogger("Minecraft");
                    case "getName" -> "TestServer";
                    case "getVersion", "getBukkitVersion" -> "1.0";
                    case "getWorlds" -> Collections.singletonList(world);
                    case "getWorld" -> world;
                    case "getOnlinePlayers" -> Collections.emptyList();
                    case "getPluginManager" -> {
                        if (pluginManager == null) {
                            pluginManager = Proxy.newProxyInstance(PluginManager.class.getClassLoader(),
                                    new Class[]{PluginManager.class}, (pm, pmMethod, pmArgs) -> {
                                        if (pmMethod.getName().equals("equals")) {
                                            return pm == pmArgs[0];
                                        }
                                        if (pmMethod.getName().equals("hashCode")) {
                                            return System.identityHashCode(pm);
                                        }
                                        if (pmMethod.getName().equals("toString")) {
                                            return "MockPluginManager";
                                        }
                                        if (pmMethod.getName().equals("getPermissions")) {
                                            return Collections.emptySet();
                                        }
                                        if (pmMethod.getName().equals("registerEvents")) {
                                            Listener listener = (Listener) pmArgs[0];
                                            for (Method m : listener.getClass().getMethods()) {
                                                if (m.isAnnotationPresent(EventHandler.class) && m.getParameterCount() == 1) {
                                                    @SuppressWarnings("unchecked")
                                                    Class<? extends Event> eventClass = (Class<? extends Event>) m.getParameterTypes()[0];
                                                    listeners.computeIfAbsent(eventClass, k -> new ArrayList<>()).add(listener);
                                                }
                                            }
                                            return null;
                                        }
                                        if (pmMethod.getName().equals("callEvent")) {
                                            Event event = (Event) pmArgs[0];
                                            List<Listener> eventListeners = listeners.get(event.getClass());
                                            if (eventListeners != null) {
                                                for (Listener l : eventListeners) {
                                                    for (Method m : l.getClass().getMethods()) {
                                                        if (m.isAnnotationPresent(EventHandler.class) && m.getParameterCount() == 1
                                                                && m.getParameterTypes()[0].isAssignableFrom(event.getClass())) {
                                                            m.invoke(l, event);
                                                        }
                                                    }
                                                }
                                            }
                                            return null;
                                        }
                                        return null;
                                    });
                        }
                        yield pluginManager;
                    }
                    default -> null;
                };
            }
        });

        try {
            Field serverField = Bukkit.class.getDeclaredField("server");
            serverField.setAccessible(true);
            serverField.set(null, server);
        } catch (Exception e) {
            try {
                Bukkit.setServer(server);
            } catch (UnsupportedOperationException ignored) {
            }
        }

        config = new PermissionsExConfig(null);
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

    @AfterEach
    public void tearDown() {
        if (manager != null) {
            try {
                manager.end();
            } catch (Exception ignored) {
            }
        }
        if (fixture != null) {
            fixture.close();
        }
    }

    public void waitForExecutor() throws InterruptedException {
        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        manager.getExecutor().execute(latch::countDown);
        latch.await(5, java.util.concurrent.TimeUnit.SECONDS);
    }
}
