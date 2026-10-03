package dev.rono.permissions.core.engine;

import dev.rono.permissions.api.permission.PermissionHolder;
import dev.rono.permissions.api.permission.PermissionResult;
import dev.rono.permissions.api.resolver.PermissionResolution;
import dev.rono.permissions.api.resolver.QueryOptions;
import java.time.Instant;
import java.util.Optional;

/**
 * Internal authorization engine boundary.
 *
 * <p>
 * jCasbin is the authoritative permission evaluator. Implementations must not
 * expose jCasbin types through the PermissionsExPlus API; callers continue to
 * use {@code PermissionResolver}.
 * </p>
 */
public interface PermissionEngine {

    String id();

    PermissionResult check(PermissionHolder holder, String permission, QueryOptions options);

    /**
     * Authoritative explanation of a permission decision. {@code result()} must
     * match {@link #check} for the same inputs.
     */
    PermissionResolution explain(PermissionHolder holder, String permission, QueryOptions options);

    /**
     * Earliest expiry among policy nodes that can affect checks for this holder
     * under the given options. Used by the decision cache so timed permissions
     * cannot outlive their expiry.
     */
    Optional<Instant> earliestPolicyExpiry(PermissionHolder holder, QueryOptions options);

    /**
     * Rebuild compiled authorization state from the current domain snapshot.
     * No-op for engines that evaluate the live domain directly.
     */
    void rebuild();

    /**
     * Invalidate derived authorization state. Implementations should bump their
     * policy revision so cached decisions are discarded.
     */
    void invalidate();

    long revision();
}
