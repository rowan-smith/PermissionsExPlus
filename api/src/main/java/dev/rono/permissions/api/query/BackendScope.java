package dev.rono.permissions.api.query;

import dev.rono.permissions.api.PermissionsExException;
import dev.rono.permissions.api.backend.BackendHandle;
import dev.rono.permissions.api.backend.BackendInfo;
import dev.rono.permissions.api.data.ImportMode;
import dev.rono.permissions.api.service.PermissionService;

/**
 * Backend administration — obtain via {@link PermissionQuery#backend()}.
 *
 * <pre>{@code
 * pex.query().backend().info();
 * pex.query().backend().activate("sql");
 * pex.query().backend().exportData();
 * }</pre>
 */
public final class BackendScope {

    private final PermissionService service;

    BackendScope(PermissionService service) {
        this.service = service;
    }

    /** Active backend snapshot. */
    public BackendInfo info() {
        return service.backend();
    }

    public String type() {
        return info().type();
    }

    public String simpleName() {
        return info().simpleName();
    }

    public String diagnosticLabel() {
        return info().diagnosticLabel();
    }

    /** Switch the active backend alias. */
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
