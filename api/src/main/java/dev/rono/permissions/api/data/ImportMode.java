package dev.rono.permissions.api.data;

/** How backend import operations merge serialized permission data. */
public enum ImportMode {
    /** Merge imported users/groups/world inheritance into the active backend. */
    MERGE,
    /** Clear in-memory caches before merge (backend {@code loadFrom} semantics). */
    REPLACE
}
