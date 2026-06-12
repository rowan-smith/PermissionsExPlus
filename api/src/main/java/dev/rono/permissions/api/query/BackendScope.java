package dev.rono.permissions.api.query;

import dev.rono.permissions.api.PermissionsExException;
import dev.rono.permissions.api.backend.BackendHandle;
import dev.rono.permissions.api.backend.BackendInfo;
import dev.rono.permissions.api.data.ImportMode;
import dev.rono.permissions.api.service.PermissionServiceBridge;

/**
 * Backend administration — obtain via {@link dev.rono.permissions.api.service.PermissionService#backend()}.
 */
public final class BackendScope {

    private final PermissionServiceBridge service;

    public BackendScope(PermissionServiceBridge service) {
        this.service = service;
    }

    public BackendInfo getActive() {
        return service.activeBackend();
    }

    public boolean isActive() {
        return service.activeBackend() != null;
    }

    public boolean isActive(String alias) {
        return alias != null && alias.equals(getActive().type());
    }

    public String type() {
        return getActive().type();
    }

    public String simpleName() {
        return getActive().simpleName();
    }

    public String diagnosticLabel() {
        return getActive().diagnosticLabel();
    }

    public void activate(String alias) throws PermissionsExException {
        service.setActiveBackend(alias);
    }

    public BackendHandle createHandle(String alias) throws PermissionsExException {
        return service.createBackendHandle(alias);
    }

    public void importFrom(String backendAlias) throws PermissionsExException {
        service.importFromBackend(backendAlias);
    }

    public String exportData() throws PermissionsExException {
        return service.exportData();
    }

    public void importData(String document, ImportMode mode) throws PermissionsExException {
        service.importData(document, mode);
    }
}
