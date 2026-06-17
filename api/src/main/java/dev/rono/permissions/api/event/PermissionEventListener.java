package dev.rono.permissions.api.event;

import dev.rono.permissions.api.bus.EntityDispatch;
import dev.rono.permissions.api.bus.SystemDispatch;

/**
 * Listener for modern permission bus dispatches (parallel to legacy Bukkit events on Spigot).
 *
 * <p>Register via {@link PermissionEventBus#subscribe(PermissionEventListener)}. Override one or
 * both callback methods; unhandled dispatch types use empty default implementations.</p>
 */
public interface PermissionEventListener {

    /**
     * Called when a subject- or world-scoped entity dispatch is published.
     *
     * <p>Default implementation is a no-op.</p>
     *
     * @param dispatch entity-scoped event payload (users, groups, permissions, etc.)
     */
    default void onEntity(EntityDispatch dispatch) {}

    /**
     * Called when an installation-wide system dispatch is published.
     *
     * <p>Default implementation is a no-op.</p>
     *
     * @param dispatch system-scoped event payload (reload, backend changes, etc.)
     */
    default void onSystem(SystemDispatch dispatch) {}
}
