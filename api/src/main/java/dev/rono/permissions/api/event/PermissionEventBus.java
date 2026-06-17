package dev.rono.permissions.api.event;

/**
 * Subscribe to permission-domain notifications from PermissionsEx.
 *
 * <p>Obtain via {@link dev.rono.permissions.api.PermissionsExApi#getEventBus()}.</p>
 *
 * <h2>Threading and lifecycle</h2>
 * <ul>
 *   <li><strong>Dispatch:</strong> listeners are invoked synchronously on the thread that publishes the
 *       dispatch (typically the server main thread on Spigot/Paper).</li>
 *   <li><strong>Ordering:</strong> listeners run in registration order; no priority tiers.</li>
 *   <li><strong>Cancellation:</strong> dispatches are informational — listeners cannot cancel engine
 *       mutations. Use platform permission APIs for cancellable checks.</li>
 *   <li><strong>Subscriptions:</strong> hold the {@link Subscription} token and call
 *       {@link #unsubscribe(Subscription)} on plugin disable to avoid leaks.</li>
 *   <li><strong>Thread-safety:</strong> {@link #subscribe(PermissionEventListener)} and
 *       {@link #unsubscribe(Subscription)} are safe from any thread; listener callbacks should not block.</li>
 * </ul>
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
