package dev.rono.permissions.api.event;

/**
 * Subscribe to permission-domain notifications from PermissionsEx.
 *
 * <p>Obtain via {@link dev.rono.permissions.api.service.PermissionService#events()}.</p>
 *
 * <p>On Spigot/Paper, dispatches are also translated into legacy {@code ru.tehkode.permissions.events.*}
 * when the platform adapter publishes them.</p>
 */
public interface PermissionEventBus {

    /**
     * Registers a listener for entity and system dispatches.
     *
     * @param listener callback implementation (typically overriding {@link PermissionEventListener}
     *                 default methods)
     * @return an opaque subscription handle used to {@link #unsubscribe(Subscription) unsubscribe}
     */
    Subscription subscribe(PermissionEventListener listener);

    /**
     * Removes a previously registered listener.
     *
     * @param subscription handle returned by {@link #subscribe(PermissionEventListener)}
     */
    void unsubscribe(Subscription subscription);

    /**
     * Opaque token representing an active event subscription.
     *
     * <p>Pass to {@link PermissionEventBus#unsubscribe(Subscription)} to stop receiving dispatches.</p>
     */
    interface Subscription {}
}
