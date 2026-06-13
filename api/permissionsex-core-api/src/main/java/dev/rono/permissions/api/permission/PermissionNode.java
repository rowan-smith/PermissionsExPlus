package dev.rono.permissions.api.permission;

import java.time.Instant;
import java.util.Map;

/**
 * Result of adding a permission node to a holder.
 *
 * <p>Describes the grant that was applied, including optional expiry and context metadata. Listing
 * permissions via {@code getPermissions(holder)} may not round-trip timed expiry for all storage shapes.</p>
 */
public interface PermissionNode {

    /**
     * Returns the holder that received the grant.
     *
     * @return permission target
     */
    PermissionHolder holder();

    /**
     * Returns the permission node expression that was granted.
     *
     * @return permission node
     */
    String permission();

    /**
     * Returns when the grant expires, if timed.
     *
     * @return expiry instant, or {@code null} for permanent direct grants
     */
    Instant expiresAt();

    /**
     * Returns the context map supplied for world-scoped grants.
     *
     * @return immutable context map (empty for global grants)
     */
    Map<String, String> context();

    /**
     * Returns provenance metadata for the grant.
     *
     * @return permission source
     */
    PermissionSource source();
}
