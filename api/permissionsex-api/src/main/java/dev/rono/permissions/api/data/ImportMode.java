package dev.rono.permissions.api.data;

/** How {@link dev.rono.permissions.api.service.PermissionService#importData(String, ImportMode)} merges data. */
public enum ImportMode {
    /** Merge imported users/groups/world inheritance into the active backend. */
    MERGE,
    /** Clear in-memory caches before merge (backend {@code loadFrom} semantics). */
    REPLACE
}
