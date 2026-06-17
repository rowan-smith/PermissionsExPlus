package dev.rono.permissions.api.runtime;

import dev.rono.permissions.api.bus.PermissionDispatch;

/**
 * Host event surface for permission-domain dispatches.
 *
 * <p>Platform modules translate {@link PermissionDispatch} instances into native listener APIs.</p>
 */
public interface PlatformEventBus {

    /**
     * Publishes a permission dispatch to platform-specific listeners.
     *
     * @param dispatch entity or system dispatch payload
     */
    void publish(PermissionDispatch dispatch);
}
