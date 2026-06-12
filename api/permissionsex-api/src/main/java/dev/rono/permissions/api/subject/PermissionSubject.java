package dev.rono.permissions.api.subject;

import dev.rono.permissions.api.world.Worlds;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Shared permission-subject operations for users and groups.
 *
 * <p>{@code world} is {@link Worlds#GLOBAL} or empty for the global context (classic PEX {@code null} world).</p>
 *
 * <p>Parameterless overloads and {@link #global()} operate in the global namespace, equivalent to passing
 * {@link Worlds#GLOBAL} explicitly.</p>
 */
public interface PermissionSubject {

    /**
     * Returns whether this subject is a user or a group.
     *
     * @return {@link SubjectType#USER} or {@link SubjectType#GROUP}
     */
    SubjectType type();

    /**
     * Returns the stable backend identifier for this subject.
     *
     * <p>UUID string for users; group name for groups.</p>
     *
     * @return subject identifier; never {@code null} for a live instance
     */
    String identifier();

    /**
     * Returns the display name for this subject.
     *
     * <p>May differ from {@link #identifier()} when a {@code name} option is set; otherwise falls back
     * to the identifier.</p>
     *
     * @return display name
     */
    String name();

    /**
     * Returns whether this subject exists only in memory and is not persisted to the backend.
     *
     * @return {@code true} if virtual (transient), {@code false} if backed by stored data
     */
    boolean virtual();

    // --- Global convenience (same as passing {@link Worlds#GLOBAL}) ---

    /**
     * Checks whether this subject effectively holds the given permission in the global namespace.
     *
     * <p>Delegates to {@link #has(String, String)} with {@link Worlds#GLOBAL}. Performs full effective
     * resolution (inheritance, negation, timed grants).</p>
     *
     * @param permission permission node to check
     * @return {@code true} if the permission is granted, {@code false} otherwise
     */
    default boolean hasPermission(String permission) {
        return has(permission, Worlds.GLOBAL);
    }

    /**
     * Alias for {@link #hasPermission(String)}.
     *
     * @param permission permission node to check
     * @return {@code true} if the permission is granted, {@code false} otherwise
     */
    default boolean has(String permission) {
        return hasPermission(permission);
    }

    /**
     * Returns direct permission assignments in the global namespace.
     *
     * <p>Delegates to {@link #permissions(String)} with {@link Worlds#GLOBAL}.</p>
     *
     * @return unmodifiable list of own permission expressions (not inherited)
     */
    default List<String> permissions() {
        return permissions(Worlds.GLOBAL);
    }

    /**
     * Returns effective permissions in the global namespace after inheritance.
     *
     * <p>Delegates to {@link #effectivePermissions(String)} with {@link Worlds#GLOBAL}.</p>
     *
     * @return unmodifiable list of effective permission expressions
     */
    default List<String> effectivePermissions() {
        return effectivePermissions(Worlds.GLOBAL);
    }

    /**
     * Adds a permission assignment in the global namespace.
     *
     * <p>Delegates to {@link #addPermission(String, String)} with {@link Worlds#GLOBAL}.</p>
     *
     * @param permission permission node to add
     */
    default void addPermission(String permission) {
        addPermission(permission, Worlds.GLOBAL);
    }

    /**
     * Removes a permission assignment in the global namespace.
     *
     * <p>Delegates to {@link #removePermission(String, String)} with {@link Worlds#GLOBAL}.</p>
     *
     * @param permission permission node to remove
     */
    default void removePermission(String permission) {
        removePermission(permission, Worlds.GLOBAL);
    }

    /**
     * Replaces direct permission assignments in the global namespace.
     *
     * <p>Delegates to {@link #setPermissions(List, String)} with {@link Worlds#GLOBAL}.</p>
     *
     * @param permissions new permission expressions
     */
    default void setPermissions(List<String> permissions) {
        setPermissions(permissions, Worlds.GLOBAL);
    }

    /**
     * Adds a timed permission grant in the global namespace.
     *
     * <p>Delegates to {@link #addTimedPermission(String, String, int)} with {@link Worlds#GLOBAL}.</p>
     *
     * @param permission       permission node to grant temporarily
     * @param lifetimeSeconds  seconds until expiry; {@code 0} for transient (in-memory only)
     */
    default void addTimedPermission(String permission, int lifetimeSeconds) {
        addTimedPermission(permission, Worlds.GLOBAL, lifetimeSeconds);
    }

    /**
     * Removes a timed permission grant in the global namespace.
     *
     * <p>Delegates to {@link #removeTimedPermission(String, String)} with {@link Worlds#GLOBAL}.</p>
     *
     * @param permission permission node to remove from timed grants
     */
    default void removeTimedPermission(String permission) {
        removeTimedPermission(permission, Worlds.GLOBAL);
    }

    /**
     * Returns permission nodes with active timed grants in the global namespace.
     *
     * <p>Delegates to {@link #timedPermissions(String)} with {@link Worlds#GLOBAL}.</p>
     *
     * @return list of timed permission nodes
     */
    default List<String> timedPermissions() {
        return timedPermissions(Worlds.GLOBAL);
    }

    /**
     * Returns timed permission entries with remaining lifetime metadata in the global namespace.
     *
     * <p>Delegates to {@link #timedPermissionEntries(String)} with {@link Worlds#GLOBAL}.</p>
     *
     * @return list of timed permission entries
     */
    default List<TimedPermissionEntry> timedPermissionEntries() {
        return timedPermissionEntries(Worlds.GLOBAL);
    }

    /**
     * Returns seconds remaining on a timed permission grant in the global namespace.
     *
     * <p>Delegates to {@link #timedPermissionRemainingSeconds(String, String)} with {@link Worlds#GLOBAL}.</p>
     *
     * @param permission timed permission node
     * @return seconds until expiry; {@code 0} if the timed permission is absent
     */
    default int timedPermissionRemainingSeconds(String permission) {
        return timedPermissionRemainingSeconds(permission, Worlds.GLOBAL);
    }

    /**
     * Returns whether the permission is directly assigned as a timed grant in the global namespace.
     *
     * <p>Delegates to {@link #hasTimedPermission(String, String)} with {@link Worlds#GLOBAL}. Unlike
     * {@link #has(String)}, this does not perform full effective permission resolution.</p>
     *
     * @param permission permission node to look up
     * @return {@code true} if a timed grant exists for the node, {@code false} otherwise
     */
    default boolean hasTimedPermission(String permission) {
        return hasTimedPermission(permission, Worlds.GLOBAL);
    }

    /**
     * Returns the effective chat prefix in the global namespace.
     *
     * <p>Delegates to {@link #prefix(String)} with {@link Worlds#GLOBAL}.</p>
     *
     * @return resolved prefix, or empty string when none is defined
     */
    default String prefix() {
        return prefix(Worlds.GLOBAL);
    }

    /**
     * Returns the effective chat suffix in the global namespace.
     *
     * <p>Delegates to {@link #suffix(String)} with {@link Worlds#GLOBAL}.</p>
     *
     * @return resolved suffix, or empty string when none is defined
     */
    default String suffix() {
        return suffix(Worlds.GLOBAL);
    }

    /**
     * Sets the chat prefix stored directly on this subject in the global namespace.
     *
     * <p>Delegates to {@link #setPrefix(String, String)} with {@link Worlds#GLOBAL}.</p>
     *
     * @param prefix new prefix value
     */
    default void setPrefix(String prefix) {
        setPrefix(prefix, Worlds.GLOBAL);
    }

    /**
     * Sets the chat suffix stored directly on this subject in the global namespace.
     *
     * <p>Delegates to {@link #setSuffix(String, String)} with {@link Worlds#GLOBAL}.</p>
     *
     * @param suffix new suffix value
     */
    default void setSuffix(String suffix) {
        setSuffix(suffix, Worlds.GLOBAL);
    }

    /**
     * Returns an effective option value in the global namespace.
     *
     * <p>Delegates to {@link #option(String, String)} with {@link Worlds#GLOBAL}.</p>
     *
     * @param key option key
     * @return option value, or {@code null} if unset
     */
    default String option(String key) {
        return option(key, Worlds.GLOBAL);
    }

    /**
     * Sets an option stored directly on this subject in the global namespace.
     *
     * <p>Delegates to {@link #setOption(String, String, String)} with {@link Worlds#GLOBAL}.</p>
     *
     * @param key   option key
     * @param value option value; {@code null} or empty removes the option
     */
    default void setOption(String key, String value) {
        setOption(key, value, Worlds.GLOBAL);
    }

    /**
     * Returns effective options in the global namespace.
     *
     * <p>Delegates to {@link #options(String)} with {@link Worlds#GLOBAL}.</p>
     *
     * @return map of option keys to resolved values
     */
    default Map<String, String> options() {
        return options(Worlds.GLOBAL);
    }

    /**
     * Returns a world-scoped view of this subject for permission and metadata operations.
     *
     * <p>Methods on the returned context apply to {@code world} without repeating the world argument.
     * {@link Worlds#GLOBAL} selects the global namespace.</p>
     *
     * @param world world name, or {@link Worlds#GLOBAL} for the global namespace
     * @return world-bound context for this subject
     */
    default SubjectWorldContext inWorld(String world) {
        return SubjectWorldContexts.subject(this, world);
    }

    /**
     * Returns a view of this subject bound to the global namespace.
     *
     * <p>Equivalent to {@link #inWorld(String)} with {@link Worlds#GLOBAL}.</p>
     *
     * @return global world context for this subject
     */
    default SubjectWorldContext global() {
        return inWorld(Worlds.GLOBAL);
    }

    // --- Per-world operations ---

    /**
     * Checks whether this subject effectively holds the given permission in the specified world.
     *
     * <p>Resolves inheritance, timed permissions, negated nodes ({@code -node}), and non-inheritable
     * expressions. This is the subject-level permission grant check; platform APIs often expose the same
     * concept as {@code hasPermission}. Unlike {@link #hasTimedPermission(String, String)}, this performs
     * full effective resolution rather than testing only direct timed assignments.</p>
     *
     * @param permission permission node to check
     * @param world      world name, or {@link Worlds#GLOBAL} for the global namespace
     * @return {@code true} if the permission is granted, {@code false} otherwise
     */
    boolean has(String permission, String world);

    /**
     * Alias for {@link #has(String, String)}.
     *
     * @param permission permission node to check
     * @param world      world name, or {@link Worlds#GLOBAL} for the global namespace
     * @return {@code true} if the permission is granted, {@code false} otherwise
     */
    default boolean hasPermission(String permission, String world) {
        return has(permission, world);
    }

    /**
     * Returns direct permission assignments in the given world (not inherited).
     *
     * @param world world name, or {@link Worlds#GLOBAL} for the global namespace
     * @return unmodifiable list of own permission expressions
     */
    List<String> permissions(String world);

    /**
     * Returns effective permissions after inheritance in the given world.
     *
     * @param world world name, or {@link Worlds#GLOBAL} for the global namespace
     * @return unmodifiable list of effective permission expressions
     */
    List<String> effectivePermissions(String world);

    /**
     * Adds a direct permission assignment in the given world.
     *
     * @param permission permission node to add
     * @param world      world name, or {@link Worlds#GLOBAL} for the global namespace
     */
    void addPermission(String permission, String world);

    /**
     * Removes a direct permission assignment in the given world.
     *
     * @param permission permission node to remove
     * @param world      world name, or {@link Worlds#GLOBAL} for the global namespace
     */
    void removePermission(String permission, String world);

    /**
     * Replaces direct permission assignments in the given world.
     *
     * @param permissions new permission expressions
     * @param world       world name, or {@link Worlds#GLOBAL} for the global namespace
     */
    void setPermissions(List<String> permissions, String world);

    /**
     * Adds a timed permission grant in the given world.
     *
     * @param permission       permission node to grant temporarily
     * @param world            world name, or {@link Worlds#GLOBAL} for the global namespace
     * @param lifetimeSeconds  seconds until expiry; {@code 0} for transient (in-memory only)
     */
    void addTimedPermission(String permission, String world, int lifetimeSeconds);

    /**
     * Removes a timed permission grant in the given world.
     *
     * @param permission permission node to remove from timed grants
     * @param world      world name, or {@link Worlds#GLOBAL} for the global namespace
     */
    void removeTimedPermission(String permission, String world);

    /**
     * Returns permission nodes with active timed grants in the given world.
     *
     * @param world world name, or {@link Worlds#GLOBAL} for the global namespace
     * @return list of timed permission nodes
     */
    List<String> timedPermissions(String world);

    /**
     * Returns timed permission nodes with remaining lifetime metadata in the given world.
     *
     * @param world world name, or {@link Worlds#GLOBAL} for the global namespace
     * @return list of timed permission entries
     */
    List<TimedPermissionEntry> timedPermissionEntries(String world);

    /**
     * Returns timed permission entries across every configured world, including global.
     *
     * @return aggregated timed permission entries from all worlds with data
     */
    default List<TimedPermissionEntry> allTimedPermissionEntries() {
        java.util.LinkedHashSet<String> worlds = new java.util.LinkedHashSet<>();
        worlds.add(Worlds.GLOBAL);
        worlds.addAll(configuredWorlds());
        return worlds.stream().flatMap(world -> timedPermissionEntries(world).stream()).toList();
    }

    /**
     * Returns seconds remaining on a timed permission grant in the given world.
     *
     * @param permission timed permission node
     * @param world      world name, or {@link Worlds#GLOBAL} for the global namespace
     * @return seconds until expiry; {@code 0} if the timed permission is absent
     */
    int timedPermissionRemainingSeconds(String permission, String world);

    /**
     * Returns whether the permission is directly assigned as a timed grant in the given world.
     *
     * <p>Unlike {@link #has(String, String)}, this checks only the timed-permission list and does not
     * perform full effective permission resolution.</p>
     *
     * @param permission permission node to look up
     * @param world      world name, or {@link Worlds#GLOBAL} for the global namespace
     * @return {@code true} if a timed grant exists for the node, {@code false} otherwise
     */
    default boolean hasTimedPermission(String permission, String world) {
        return timedPermissions(world).contains(permission);
    }

    /**
     * Returns worlds where this subject has permissions, options, or inheritance data.
     *
     * @return set of configured world names (global is represented by {@link Worlds#GLOBAL})
     */
    Set<String> configuredWorlds();

    /**
     * Returns direct permissions keyed by world.
     *
     * <p>The {@link Worlds#GLOBAL} key ({@code ""} in map views) holds the global namespace.</p>
     *
     * @return map of world to own permission lists
     */
    Map<String, List<String>> permissionsByWorld();

    /**
     * Returns effective permissions keyed by world.
     *
     * @return map of world to effective permission lists
     */
    Map<String, List<String>> effectivePermissionsByWorld();

    /**
     * Returns the effective chat prefix for the given world, including inheritance.
     *
     * @param world world name, or {@link Worlds#GLOBAL} for the global namespace
     * @return resolved prefix, or empty string when none is defined
     */
    String prefix(String world);

    /**
     * Returns the effective chat suffix for the given world, including inheritance.
     *
     * @param world world name, or {@link Worlds#GLOBAL} for the global namespace
     * @return resolved suffix, or empty string when none is defined
     */
    String suffix(String world);

    /**
     * Sets the chat prefix stored directly on this subject for the given world.
     *
     * @param prefix new prefix value
     * @param world  world name, or {@link Worlds#GLOBAL} for the global namespace
     */
    void setPrefix(String prefix, String world);

    /**
     * Sets the chat suffix stored directly on this subject for the given world.
     *
     * @param suffix new suffix value
     * @param world  world name, or {@link Worlds#GLOBAL} for the global namespace
     */
    void setSuffix(String suffix, String world);

    /**
     * Returns an effective option value for the given world, including inheritance.
     *
     * @param key   option key
     * @param world world name, or {@link Worlds#GLOBAL} for the global namespace
     * @return option value, or {@code null} if unset
     */
    String option(String key, String world);

    /**
     * Sets an option stored directly on this subject for the given world.
     *
     * @param key   option key
     * @param value option value; {@code null} or empty removes the option
     * @param world world name, or {@link Worlds#GLOBAL} for the global namespace
     */
    void setOption(String key, String value, String world);

    /**
     * Returns effective options for the given world, including inheritance.
     *
     * @param world world name, or {@link Worlds#GLOBAL} for the global namespace
     * @return map of option keys to resolved values
     */
    Map<String, String> options(String world);

    /**
     * Persists pending changes for this subject to the active backend.
     */
    void save();

    /**
     * Removes this subject from the backend and evicts it from in-memory caches.
     */
    void delete();
}
