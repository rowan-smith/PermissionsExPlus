package dev.rono.permissions.api.backend;

import dev.rono.permissions.api.PermissionsExException;

/**
 * Non-active backend instance for inspection and data transfer.
 *
 * <p>The handle targets a configured backend without making it active.</p>
 */
public interface BackendHandle {

    /**
     * Returns metadata describing this backend instance.
     *
     * @return backend type, implementation name, and diagnostic label
     */
    BackendInfo info();

    /**
     * Copies all users, groups, and world inheritance from the active backend into this backend.
     *
     * @throws PermissionsExException if the copy operation fails
     */
    void copyFromActive() throws PermissionsExException;

    /**
     * Replaces active-backend data with the contents of this backend.
     *
     * <p>Uses the merge semantics of the underlying {@code loadFrom} implementation.</p>
     *
     * @throws PermissionsExException if applying data to the active backend fails
     */
    void applyToActive() throws PermissionsExException;
}
