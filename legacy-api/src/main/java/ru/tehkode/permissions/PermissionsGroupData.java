package ru.tehkode.permissions;

/**
 * Mutable data handle for a single permission group.
 *
 * <p>Groups share the full {@link PermissionsData} contract for world-scoped permissions, options,
 * and parent-group inheritance. Group-specific operations, if any, are added by implementations rather
 * than on this marker interface.</p>
 *
 * @see PermissionsData
 * @see PermissionsUserData
 */
public interface PermissionsGroupData extends PermissionsData {
}
