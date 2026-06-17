/**
 * User registry and {@link dev.rono.permissions.api.user.User} subject operations.
 *
 * <p>Obtain users through {@link dev.rono.permissions.api.user.UserManager} — never construct
 * implementations directly. Lifecycle: {@code find} (no materialize) → {@code get} (requires
 * persisted) → {@code create} (explicit new record).</p>
 */
package dev.rono.permissions.api.user;
