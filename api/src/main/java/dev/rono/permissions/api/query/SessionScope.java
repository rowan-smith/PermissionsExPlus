package dev.rono.permissions.api.query;

import dev.rono.permissions.api.service.PermissionServiceBridge;
import dev.rono.permissions.api.session.PermissionEditSession;

/** Batch edit sessions — obtain via {@link dev.rono.permissions.api.service.PermissionService#session()}. */
public final class SessionScope {

    private final PermissionServiceBridge service;

    public SessionScope(PermissionServiceBridge service) {
        this.service = service;
    }

    public PermissionEditSession open() {
        return service.openEditSession();
    }

    /** Alias for {@link #open()}. */
    public PermissionEditSession start() {
        return open();
    }
}
