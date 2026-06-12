package dev.rono.permissions.api.subject;

import dev.rono.permissions.api.world.PexWorlds;
import java.util.List;
import java.util.Map;

/**
 * World-scoped view of a {@link PexPermissionSubject}.
 *
 * <p>Obtained via {@link PexPermissionSubject#inWorld(String)}. Every method on this context applies to
 * the bound world returned by {@link #world()} — callers do not pass a world argument again.
 * {@link PexWorlds#GLOBAL} selects the global namespace.</p>
 */
public interface PexSubjectWorldContext {

    /**
     * Returns the world this context is bound to.
     *
     * @return {@link PexWorlds#GLOBAL} or a specific world name
     */
    String world();

    /**
     * Returns the underlying permission subject.
     *
     * @return the subject this context wraps
     */
    PexPermissionSubject subject();

    /**
     * Checks whether the subject effectively holds the given permission in this context's world.
     *
     * <p>Equivalent to {@link PexPermissionSubject#has(String, String)} with {@link #world()}. Performs
     * full effective resolution. Unlike {@link #hasTimedPermission(String)}, this does not test only
     * direct timed assignments.</p>
     *
     * @param permission permission node to check
     * @return {@code true} if the permission is granted, {@code false} otherwise
     */
    boolean hasPermission(String permission);

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
     * Returns direct permission assignments in this context's world (not inherited).
     *
     * <p>Equivalent to {@link PexPermissionSubject#permissions(String)} with {@link #world()}.</p>
     *
     * @return unmodifiable list of own permission expressions
     */
    List<String> permissions();

    /**
     * Returns effective permissions after inheritance in this context's world.
     *
     * <p>Equivalent to {@link PexPermissionSubject#effectivePermissions(String)} with {@link #world()}.</p>
     *
     * @return unmodifiable list of effective permission expressions
     */
    List<String> effectivePermissions();

    /**
     * Adds a direct permission assignment in this context's world.
     *
     * <p>Equivalent to {@link PexPermissionSubject#addPermission(String, String)} with {@link #world()}.</p>
     *
     * @param permission permission node to add
     */
    void addPermission(String permission);

    /**
     * Removes a direct permission assignment in this context's world.
     *
     * <p>Equivalent to {@link PexPermissionSubject#removePermission(String, String)} with {@link #world()}.</p>
     *
     * @param permission permission node to remove
     */
    void removePermission(String permission);

    /**
     * Replaces direct permission assignments in this context's world.
     *
     * <p>Equivalent to {@link PexPermissionSubject#setPermissions(List, String)} with {@link #world()}.</p>
     *
     * @param permissions new permission expressions
     */
    void setPermissions(List<String> permissions);

    /**
     * Adds a timed permission grant in this context's world.
     *
     * <p>Equivalent to {@link PexPermissionSubject#addTimedPermission(String, String, int)} with
     * {@link #world()}.</p>
     *
     * @param permission       permission node to grant temporarily
     * @param lifetimeSeconds  seconds until expiry; {@code 0} for transient (in-memory only)
     */
    void addTimedPermission(String permission, int lifetimeSeconds);

    /**
     * Removes a timed permission grant in this context's world.
     *
     * <p>Equivalent to {@link PexPermissionSubject#removeTimedPermission(String, String)} with
     * {@link #world()}.</p>
     *
     * @param permission permission node to remove from timed grants
     */
    void removeTimedPermission(String permission);

    /**
     * Returns permission nodes with active timed grants in this context's world.
     *
     * <p>Equivalent to {@link PexPermissionSubject#timedPermissions(String)} with {@link #world()}.</p>
     *
     * @return list of timed permission nodes
     */
    List<String> timedPermissions();

    /**
     * Returns timed permission entries with remaining lifetime metadata in this context's world.
     *
     * <p>Equivalent to {@link PexPermissionSubject#timedPermissionEntries(String)} with {@link #world()}.</p>
     *
     * @return list of timed permission entries
     */
    List<PexTimedPermissionEntry> timedPermissionEntries();

    /**
     * Returns seconds remaining on a timed permission grant in this context's world.
     *
     * <p>Equivalent to {@link PexPermissionSubject#timedPermissionRemainingSeconds(String, String)} with
     * {@link #world()}.</p>
     *
     * @param permission timed permission node
     * @return seconds until expiry; {@code 0} if the timed permission is absent
     */
    int timedPermissionRemainingSeconds(String permission);

    /**
     * Returns whether the permission is directly assigned as a timed grant in this context's world.
     *
     * <p>Equivalent to {@link PexPermissionSubject#hasTimedPermission(String, String)} with {@link #world()}.
     * Unlike {@link #has(String)}, this checks only the timed-permission list.</p>
     *
     * @param permission permission node to look up
     * @return {@code true} if a timed grant exists for the node, {@code false} otherwise
     */
    boolean hasTimedPermission(String permission);

    /**
     * Returns the effective chat prefix in this context's world, including inheritance.
     *
     * <p>Equivalent to {@link PexPermissionSubject#prefix(String)} with {@link #world()}.</p>
     *
     * @return resolved prefix, or empty string when none is defined
     */
    String prefix();

    /**
     * Returns the effective chat suffix in this context's world, including inheritance.
     *
     * <p>Equivalent to {@link PexPermissionSubject#suffix(String)} with {@link #world()}.</p>
     *
     * @return resolved suffix, or empty string when none is defined
     */
    String suffix();

    /**
     * Sets the chat prefix stored directly on the subject in this context's world.
     *
     * <p>Equivalent to {@link PexPermissionSubject#setPrefix(String, String)} with {@link #world()}.</p>
     *
     * @param prefix new prefix value
     */
    void setPrefix(String prefix);

    /**
     * Sets the chat suffix stored directly on the subject in this context's world.
     *
     * <p>Equivalent to {@link PexPermissionSubject#setSuffix(String, String)} with {@link #world()}.</p>
     *
     * @param suffix new suffix value
     */
    void setSuffix(String suffix);

    /**
     * Returns an effective option value in this context's world, including inheritance.
     *
     * <p>Equivalent to {@link PexPermissionSubject#option(String, String)} with {@link #world()}.</p>
     *
     * @param key option key
     * @return option value, or {@code null} if unset
     */
    String option(String key);

    /**
     * Sets an option stored directly on the subject in this context's world.
     *
     * <p>Equivalent to {@link PexPermissionSubject#setOption(String, String, String)} with {@link #world()}.</p>
     *
     * @param key   option key
     * @param value option value; {@code null} or empty removes the option
     */
    void setOption(String key, String value);

    /**
     * Returns effective options in this context's world, including inheritance.
     *
     * <p>Equivalent to {@link PexPermissionSubject#options(String)} with {@link #world()}.</p>
     *
     * @return map of option keys to resolved values
     */
    Map<String, String> options();
}
