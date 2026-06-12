package dev.rono.permissions.core.api;

import dev.rono.permissions.api.PermissionsExException;
import dev.rono.permissions.api.backend.BackendHandle;
import dev.rono.permissions.api.backend.BackendInfo;
import dev.rono.permissions.core.DefaultPermissionManager;
import ru.tehkode.permissions.backends.PermissionBackend;
import ru.tehkode.permissions.exceptions.PermissionBackendException;

public final class ModernBackendHandle implements BackendHandle {
    private final PermissionBackend backend;
    private final String alias;
    private final DefaultPermissionManager manager;

    public ModernBackendHandle(PermissionBackend backend, String alias, DefaultPermissionManager manager) {
        this.backend = backend;
        this.alias = alias;
        this.manager = manager;
    }

    @Override
    public BackendInfo info() {
        return new BackendInfo(alias, backend.getClass().getSimpleName(), alias + " (" + backend.getClass().getSimpleName() + ")");
    }

    @Override
    public void copyFromActive() throws PermissionsExException {
        try {
            backend.loadFrom(manager.getBackend());
        } catch (RuntimeException e) {
            throw new PermissionsExException("Failed to copy active backend into " + alias, e);
        }
    }

    @Override
    public void applyToActive() throws PermissionsExException {
        try {
            manager.applyBackendData(backend);
        } catch (RuntimeException e) {
            throw new PermissionsExException("Failed to apply backend " + alias + " to active store", e);
        }
    }

    PermissionBackend delegate() {
        return backend;
    }
}
