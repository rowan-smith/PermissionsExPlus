package dev.rono.permissions.api.world;

import dev.rono.permissions.api.permission.PermissionHolder;

/**
 * Registered permission namespace (realm/world).
 *
 * <p>Represents a logical scoping key for permissions and inheritance — not necessarily a loaded
 * server dimension.</p>
 */
public interface World {

    /**
     * Returns the world name.
     *
     * @return registered world identifier
     */
    String getName();

    /**
     * Returns a {@link PermissionHolder} identity for holder-based permission operations.
     *
     * @return holder view of this world
     */
    PermissionHolder asHolder();
}
