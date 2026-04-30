package ru.tehkode.permissions.backends;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import ru.tehkode.permissions.PEXBackendConfiguration;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.PermissionsGroupData;
import ru.tehkode.permissions.PermissionsUserData;
import ru.tehkode.permissions.exceptions.PermissionBackendException;

/**
 * Storage backend contract ({@code ru.tehkode.permissions.backends}).
 *
 * <p>Bodies live on {@code AbstractPermissionBackend} in {@code permissionsex-core}; aliases and factory helpers stay on this interface for classpath-stable static calls.</p>
 */
public interface PermissionBackend {

    String DEFAULT_BACKEND = "file";

    final class AliasRegistry {
        static final ConcurrentHashMap<String, Class<? extends PermissionBackend>> ALIASES = new ConcurrentHashMap<>();

        private AliasRegistry() {}
    }

    static List<String> getRegisteredBackendAliases() {
        ArrayList<String> out = new ArrayList<>(AliasRegistry.ALIASES.keySet());
        out.sort(String.CASE_INSENSITIVE_ORDER);
        return Collections.unmodifiableList(out);
    }

    static String getBackendClassName(String alias) {
        if (AliasRegistry.ALIASES.containsKey(alias)) {
            return AliasRegistry.ALIASES.get(alias).getName();
        }
        return alias;
    }

    static Class<? extends PermissionBackend> getBackendClass(String alias) throws ClassNotFoundException {
        if (!AliasRegistry.ALIASES.containsKey(alias)) {
            Class<?> clazz = Class.forName(alias);
            if (!PermissionBackend.class.isAssignableFrom(clazz)) {
                throw new IllegalArgumentException(
                        "Provided class " + alias + " is not a subclass of PermissionBackend!");
            }
            return clazz.asSubclass(PermissionBackend.class);
        }
        return AliasRegistry.ALIASES.get(alias);
    }

    static void registerBackendAlias(String alias, Class<? extends PermissionBackend> backendClass) {
        if (!PermissionBackend.class.isAssignableFrom(backendClass)) {
            throw new IllegalArgumentException("Provided class should be subclass of PermissionBackend");
        }
        AliasRegistry.ALIASES.put(alias, backendClass);
    }

    static String getBackendAlias(Class<? extends PermissionBackend> backendClass) {
        if (AliasRegistry.ALIASES.containsValue(backendClass)) {
            for (String alias : AliasRegistry.ALIASES.keySet()) {
                if (AliasRegistry.ALIASES.get(alias).equals(backendClass)) {
                    return alias;
                }
            }
        }
        return backendClass.getName();
    }

    static PermissionBackend getBackend(
            String backendName, PermissionManager manager, PEXBackendConfiguration config)
            throws PermissionBackendException {
        return getBackend(backendName, manager, config, DEFAULT_BACKEND);
    }

    static PermissionBackend getBackend(
            String backendName,
            PermissionManager manager,
            PEXBackendConfiguration config,
            String fallBackBackend)
            throws PermissionBackendException {
        if (backendName == null || backendName.isEmpty()) {
            backendName = DEFAULT_BACKEND;
        }

        String className = getBackendClassName(backendName);

        try {
            Class<? extends PermissionBackend> backendClass = getBackendClass(backendName);

            manager.getLogger().info("Initializing " + backendName + " backend");

            Constructor<? extends PermissionBackend> constructor =
                    backendClass.getConstructor(PermissionManager.class, PEXBackendConfiguration.class);
            return constructor.newInstance(manager, config);
        } catch (ClassNotFoundException e) {
            manager.getLogger().warning("Specified backend \"" + backendName + "\" is unknown.");
            if (fallBackBackend == null) {
                throw new RuntimeException(e);
            }
            if (!className.equals(getBackendClassName(fallBackBackend))) {
                return getBackend(fallBackBackend, manager, config, null);
            } else {
                throw new RuntimeException(e);
            }
        } catch (Throwable e) {
            if (e instanceof InvocationTargetException) {
                e = e.getCause();
                if (e instanceof PermissionBackendException) {
                    throw ((PermissionBackendException) e);
                }
            }
            throw new RuntimeException(e);
        }
    }

    int getSchemaVersion();

    int getLatestSchemaVersion();

    void reload() throws PermissionBackendException;

    PermissionsUserData getUserData(String userName);

    PermissionsGroupData getGroupData(String groupName);

    boolean hasUser(String userName);

    boolean hasGroup(String group);

    Collection<String> getUserIdentifiers();

    Collection<String> getUserNames();

    Collection<String> getGroupNames();

    List<String> getWorldInheritance(String world);

    Map<String, List<String>> getAllWorldInheritance();

    void setWorldInheritance(String world, List<String> inheritance);

    void close() throws PermissionBackendException;

    Logger getLogger();

    void loadFrom(PermissionBackend backend);

    void revertUUID();

    void setPersistent(boolean persistent);

    void writeContents(Writer writer) throws IOException;

    String diagnosticLabel();
}
