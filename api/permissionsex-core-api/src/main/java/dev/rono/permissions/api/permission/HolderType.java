package dev.rono.permissions.api.permission;

/**
 * Kind of {@link PermissionHolder}.
 */
public enum HolderType {
    /** Player or offline user record. */
    USER,
    /** Named permission group. */
    GROUP,
    /** Registered permission namespace. */
    WORLD,
    /** Rank ladder identifier. */
    LADDER
}
