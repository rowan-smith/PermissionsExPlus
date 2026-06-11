package dev.rono.permissions.api.subject;

import dev.rono.permissions.api.world.Worlds;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Shared permission-subject operations for users and groups.
 *
 * <p>{@code world} is {@link Worlds#GLOBAL} or empty for the global context (classic PEX {@code null} world).</p>
 */
public interface PermissionSubject {

    SubjectType type();

    String identifier();

    String name();

    boolean virtual();

    // --- Global convenience (same as passing {@link Worlds#GLOBAL}) ---

    default boolean has(String permission) {
        return has(permission, Worlds.GLOBAL);
    }

    default List<String> permissions() {
        return permissions(Worlds.GLOBAL);
    }

    default List<String> effectivePermissions() {
        return effectivePermissions(Worlds.GLOBAL);
    }

    default void addPermission(String permission) {
        addPermission(permission, Worlds.GLOBAL);
    }

    default void removePermission(String permission) {
        removePermission(permission, Worlds.GLOBAL);
    }

    default void setPermissions(List<String> permissions) {
        setPermissions(permissions, Worlds.GLOBAL);
    }

    default void addTimedPermission(String permission, int lifetimeSeconds) {
        addTimedPermission(permission, Worlds.GLOBAL, lifetimeSeconds);
    }

    default void removeTimedPermission(String permission) {
        removeTimedPermission(permission, Worlds.GLOBAL);
    }

    default List<String> timedPermissions() {
        return timedPermissions(Worlds.GLOBAL);
    }

    default List<TimedPermissionEntry> timedPermissionEntries() {
        return timedPermissionEntries(Worlds.GLOBAL);
    }

    default int timedPermissionRemainingSeconds(String permission) {
        return timedPermissionRemainingSeconds(permission, Worlds.GLOBAL);
    }

    default boolean hasTimedPermission(String permission) {
        return hasTimedPermission(permission, Worlds.GLOBAL);
    }

    default String prefix() {
        return prefix(Worlds.GLOBAL);
    }

    default String suffix() {
        return suffix(Worlds.GLOBAL);
    }

    default void setPrefix(String prefix) {
        setPrefix(prefix, Worlds.GLOBAL);
    }

    default void setSuffix(String suffix) {
        setSuffix(suffix, Worlds.GLOBAL);
    }

    default String option(String key) {
        return option(key, Worlds.GLOBAL);
    }

    default void setOption(String key, String value) {
        setOption(key, value, Worlds.GLOBAL);
    }

    default Map<String, String> options() {
        return options(Worlds.GLOBAL);
    }

    default SubjectWorldContext inWorld(String world) {
        return SubjectWorldContexts.subject(this, world);
    }

    default SubjectWorldContext global() {
        return inWorld(Worlds.GLOBAL);
    }

    // --- Per-world operations ---

    boolean has(String permission, String world);

    /** Direct assignments in the given world (not inherited). */
    List<String> permissions(String world);

    /** Effective permissions after inheritance in the given world. */
    List<String> effectivePermissions(String world);

    void addPermission(String permission, String world);

    void removePermission(String permission, String world);

    void setPermissions(List<String> permissions, String world);

    void addTimedPermission(String permission, String world, int lifetimeSeconds);

    void removeTimedPermission(String permission, String world);

    List<String> timedPermissions(String world);

    /** Timed permission nodes with remaining lifetime metadata. */
    List<TimedPermissionEntry> timedPermissionEntries(String world);

    /** All timed permission entries across every configured world (including global). */
    default List<TimedPermissionEntry> allTimedPermissionEntries() {
        java.util.LinkedHashSet<String> worlds = new java.util.LinkedHashSet<>();
        worlds.add(Worlds.GLOBAL);
        worlds.addAll(configuredWorlds());
        return worlds.stream().flatMap(world -> timedPermissionEntries(world).stream()).toList();
    }

    /** Seconds remaining; {@code 0} if the timed permission is absent. */
    int timedPermissionRemainingSeconds(String permission, String world);

    default boolean hasTimedPermission(String permission, String world) {
        return timedPermissions(world).contains(permission);
    }

    /** Worlds where this subject has permissions, options, or inheritance data. */
    Set<String> configuredWorlds();

    /** Direct permissions keyed by world ({@link Worlds#GLOBAL} key = global namespace). */
    Map<String, List<String>> permissionsByWorld();

    /** Effective permissions keyed by world. */
    Map<String, List<String>> effectivePermissionsByWorld();

    String prefix(String world);

    String suffix(String world);

    void setPrefix(String prefix, String world);

    void setSuffix(String suffix, String world);

    String option(String key, String world);

    void setOption(String key, String value, String world);

    Map<String, String> options(String world);

    void save();

    void delete();
}
