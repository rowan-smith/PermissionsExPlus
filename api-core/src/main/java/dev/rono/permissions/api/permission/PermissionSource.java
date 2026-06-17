package dev.rono.permissions.api.permission;

/**
 * Provenance metadata for permission mutations.
 */
public enum PermissionSource {
    /** Grant initiated by a user subject. */
    USER,
    /** Grant initiated by a group subject. */
    GROUP,
    /** Grant scoped to a world namespace. */
    WORLD,
    /** Grant associated with a rank ladder. */
    LADDER,
    /** Grant initiated by the engine or a plugin via the API. */
    SYSTEM
}
