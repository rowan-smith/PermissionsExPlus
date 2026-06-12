package dev.rono.permissions.api.data;

/** How {@link dev.rono.permissions.api.service.PermissionService#importData(String, PexImportMode)} merges data. */
public enum PexImportMode {
    /** Merge imported users/groups/world inheritance into the active backend. */
    MERGE,
    /** Clear in-memory caches before merge (backend {@code loadFrom} semantics). */
    REPLACE
}
