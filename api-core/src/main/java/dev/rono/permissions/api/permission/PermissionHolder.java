package dev.rono.permissions.api.permission;

import java.util.UUID;

/**
 * Permission target identity for holder-based add/remove/has operations.
 *
 * <p>Obtain from {@code User#asHolder()}, {@code Group#asHolder()}, or related entity adapters.</p>
 */
public interface PermissionHolder {

    /**
     * Returns the stable holder identifier.
     *
     * <p>For users this is typically the UUID; for groups/worlds/ladders the configured name key.</p>
     *
     * @return holder identifier
     */
    UUID getId();

    /**
     * Returns the kind of entity this holder represents.
     *
     * @return holder type
     */
    HolderType getType();
}
