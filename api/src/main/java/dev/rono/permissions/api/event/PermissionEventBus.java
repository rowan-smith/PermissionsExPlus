package dev.rono.permissions.api.event;

/**
 * Subscribe to permission-domain notifications from PermissionsEx.
 *
 * <p>On Spigot/Paper, dispatches are also translated into legacy {@code ru.tehkode.permissions.events.*}
 * when the platform adapter publishes them.</p>
 */
public interface PermissionEventBus {

    /** @return handle used to {@link #unsubscribe(Subscription) unsubscribe} */
    Subscription subscribe(PermissionEventListener listener);

    void unsubscribe(Subscription subscription);

    /** Opaque subscription token. */
    interface Subscription {}
}
