package ru.tehkode.permissions.backends;

import ru.tehkode.permissions.PEXBackendConfiguration;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.PermissionsGroupData;
import ru.tehkode.permissions.PermissionsUserData;
import ru.tehkode.permissions.exceptions.PermissionBackendException;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Storage backend contract ({@code ru.tehkode.permissions.backends}).
 *
 * <p>Implementations persist and load permission data (users, groups, world inheritance, and related
 * metadata). Concrete backend bodies live on {@code AbstractPermissionBackend} in
 * {@code permissionsex-core}; aliases and factory helpers stay on this interface for classpath-stable
 * static calls.</p>
 *
 * <p>Each backend is constructed with a {@link PermissionManager} and a
 * {@link PEXBackendConfiguration} and is responsible for schema versioning, reload, and lifecycle
 * management.</p>
 */
public interface PermissionBackend {

    /**
     * Default backend alias used when no backend name is specified.
     */
    String DEFAULT_BACKEND = "local";

    /**
     * Internal holder for backend alias-to-class mappings.
     *
     * <p>Not intended for direct use; access is through the static registry methods on
     * {@link PermissionBackend}.</p>
     */
    final class AliasRegistry {
        /** Map of registered alias names to backend implementation classes. */
        static final ConcurrentHashMap<String, Class<? extends PermissionBackend>> ALIASES = new ConcurrentHashMap<>();

        private AliasRegistry() {}
    }

    /**
     * Returns all registered backend aliases, sorted case-insensitively.
     *
     * @return an unmodifiable list of alias names; never {@code null}
     */
    static List<String> getRegisteredBackendAliases() {
        ArrayList<String> out = new ArrayList<>(AliasRegistry.ALIASES.keySet());
        out.sort(String.CASE_INSENSITIVE_ORDER);
        return Collections.unmodifiableList(out);
    }

    /**
     * Resolves a backend alias to its fully qualified class name.
     *
     * <p>If {@code alias} is a registered alias, returns the canonical name of the mapped class.
     * Otherwise returns {@code alias} unchanged, treating it as a raw class name.</p>
     *
     * @param alias backend alias or fully qualified class name
     * @return the resolved class name; never {@code null}
     */
    static String getBackendClassName(String alias) {
        if (AliasRegistry.ALIASES.containsKey(alias)) {
            return AliasRegistry.ALIASES.get(alias).getName();
        }
        return alias;
    }

    /**
     * Resolves a backend alias or class name to a {@link PermissionBackend} implementation class.
     *
     * @param alias backend alias or fully qualified class name
     * @return the resolved backend class; never {@code null}
     * @throws ClassNotFoundException if {@code alias} is not registered and no class with that name exists
     * @throws IllegalArgumentException if the resolved class does not extend {@link PermissionBackend}
     */
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

    /**
     * Registers a short alias for a {@link PermissionBackend} implementation class.
     *
     * @param alias        human-readable alias (for example {@code "file"} or {@code "sql"})
     * @param backendClass backend implementation class
     * @throws IllegalArgumentException if {@code backendClass} is not a subclass of {@link PermissionBackend}
     */
    static void registerBackendAlias(String alias, Class<? extends PermissionBackend> backendClass) {
        if (!PermissionBackend.class.isAssignableFrom(backendClass)) {
            throw new IllegalArgumentException("Provided class should be subclass of PermissionBackend");
        }
        AliasRegistry.ALIASES.put(alias, backendClass);
    }

    /**
     * Returns the registered alias for a backend class, or the class name if no alias is registered.
     *
     * @param backendClass backend implementation class
     * @return the alias associated with {@code backendClass}, or its fully qualified name
     */
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

    /**
     * Instantiates a backend by name, falling back to {@link #DEFAULT_BACKEND} on failure.
     *
     * @param backendName name or alias of the backend to initialize
     * @param manager     permission manager that owns this backend
     * @param config      backend-specific configuration
     * @return a new backend instance
     * @throws PermissionBackendException if the backend fails to initialize in a controlled manner
     * @throws RuntimeException if the backend class cannot be loaded or constructed
     */
    static PermissionBackend getBackend(
            String backendName, PermissionManager manager, PEXBackendConfiguration config)
            throws PermissionBackendException {
        return getBackend(backendName, manager, config, DEFAULT_BACKEND);
    }

    /**
     * Instantiates a backend by name with an explicit fallback backend.
     *
     * <p>If {@code backendName} is {@code null} or empty, {@link #DEFAULT_BACKEND} is used. When the
     * primary backend class cannot be found, {@code fallBackBackend} is attempted once unless it
     * resolves to the same class name.</p>
     *
     * @param backendName     name or alias of the backend to initialize; may be {@code null} or empty
     * @param manager         permission manager that owns this backend
     * @param config          backend-specific configuration
     * @param fallBackBackend alias or class name to use when the primary backend is unknown; may be {@code null}
     * @return a new backend instance
     * @throws PermissionBackendException if the backend fails to initialize in a controlled manner
     * @throws RuntimeException if neither the primary nor fallback backend can be loaded or constructed
     */
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
            if ("file".equalsIgnoreCase(backendName)) {
                manager.getLogger().warning(
                        "The 'file' YAML backend is deprecated for active storage. "
                                + "Falling back to 'local' H2 storage. Use 'yaml-import' for one-time imports only.");
            }
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

    /**
     * Returns the schema version currently stored by this backend.
     *
     * @return the persisted schema version number
     */
    int getSchemaVersion();

    /**
     * Returns the latest schema version supported by this backend implementation.
     *
     * @return the highest schema version this backend can read and write
     */
    int getLatestSchemaVersion();

    /**
     * Reloads all permission data from the underlying storage.
     *
     * @throws PermissionBackendException if the reload fails due to a storage or configuration error
     */
    void reload() throws PermissionBackendException;

    /**
     * Returns mutable data for the user identified by {@code userName}.
     *
     * @param userName user identifier (name or UUID string, depending on backend mode)
     * @return user data handle; never {@code null} for known identifiers
     */
    PermissionsUserData getUserData(String userName);

    /**
     * Returns mutable data for the group identified by {@code groupName}.
     *
     * @param groupName group name
     * @return group data handle; never {@code null} for known group names
     */
    PermissionsGroupData getGroupData(String groupName);

    /**
     * Returns whether a user record exists in this backend.
     *
     * @param userName user identifier to check
     * @return {@code true} if the user exists in storage
     */
    boolean hasUser(String userName);

    /**
     * Returns whether a group record exists in this backend.
     *
     * @param group group name to check
     * @return {@code true} if the group exists in storage
     */
    boolean hasGroup(String group);

    /**
     * Returns all user identifiers known to this backend.
     *
     * @return collection of user identifiers; never {@code null}
     */
    Collection<String> getUserIdentifiers();

    /**
     * Returns all user display names known to this backend.
     *
     * @return collection of user names; never {@code null}
     */
    Collection<String> getUserNames();

    /**
     * Returns all group names known to this backend.
     *
     * @return collection of group names; never {@code null}
     */
    Collection<String> getGroupNames();

    /**
     * Returns the world inheritance chain for a specific world.
     *
     * <p>World inheritance defines which groups apply in a given world before user-specific
     * permissions are evaluated.</p>
     *
     * @param world world name
     * @return ordered list of inherited group names; never {@code null}
     */
    List<String> getWorldInheritance(String world);

    /**
     * Returns world inheritance mappings for all worlds.
     *
     * @return map from world name to ordered inheritance group lists; never {@code null}
     */
    Map<String, List<String>> getAllWorldInheritance();

    /**
     * Sets the world inheritance chain for a specific world.
     *
     * @param world       world name
     * @param inheritance ordered list of group names to inherit in that world
     */
    void setWorldInheritance(String world, List<String> inheritance);

    /**
     * Closes the backend and releases any held resources.
     *
     * @throws PermissionBackendException if shutdown or final persistence fails
     */
    void close() throws PermissionBackendException;

    /**
     * Returns the logger used by this backend for diagnostic output.
     *
     * @return backend logger; never {@code null}
     */
    Logger getLogger();

    /**
     * Replaces this backend's in-memory contents with data copied from another backend.
     *
     * @param backend source backend whose data should be loaded into this instance
     */
    void loadFrom(PermissionBackend backend);

    /**
     * Reverts user identifiers from UUID form back to legacy name-based identifiers.
     *
     * <p>Used during migration or when switching away from UUID-backed storage.</p>
     */
    void revertUUID();

    /**
     * Enables or disables automatic persistence of in-memory changes.
     *
     * @param persistent {@code true} to write changes to storage immediately or on save;
     *                   {@code false} to hold changes in memory only
     */
    void setPersistent(boolean persistent);

    /**
     * Writes a human-readable dump of backend contents to the given writer.
     *
     * @param writer destination writer
     * @throws IOException if writing fails
     */
    void writeContents(Writer writer) throws IOException;

    /**
     * Returns a short label describing this backend instance for diagnostics and logging.
     *
     * @return diagnostic label (for example backend type and connection target)
     */
    String diagnosticLabel();
}
