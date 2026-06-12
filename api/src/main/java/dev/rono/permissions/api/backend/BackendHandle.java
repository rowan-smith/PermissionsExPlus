package dev.rono.permissions.api.backend;

import dev.rono.permissions.api.PermissionsExException;

/**
 * Non-active backend instance for inspection and data transfer.
 *
 * <p>Created via {@link dev.rono.permissions.api.service.PermissionService#createBackendHandle(String)}.</p>
 */
public interface BackendHandle {

    BackendInfo info();

    /** Copy all users, groups, and world inheritance from the active backend into this backend. */
    void copyFromActive() throws PermissionsExException;

    /** Replace active backend data with contents of this backend (merge semantics of {@code loadFrom}). */
    void applyToActive() throws PermissionsExException;
}
