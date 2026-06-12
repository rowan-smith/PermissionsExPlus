package dev.rono.permissions.api.query;

import dev.rono.permissions.api.service.PermissionServiceBridge;
import java.util.Set;

/** User registry — obtain via {@link dev.rono.permissions.api.service.PermissionService#users()}. */
public final class UsersScope {

    private final PermissionServiceBridge service;

    public UsersScope(PermissionServiceBridge service) {
        this.service = service;
    }

    public int count() {
        return service.userCount();
    }

    public Set<String> identifiers() {
        return service.userIdentifiers();
    }

    public void delete(String identifier) {
        service.deleteUser(identifier);
    }
}
