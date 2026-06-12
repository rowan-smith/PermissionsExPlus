package dev.rono.permissions.api.query;

import dev.rono.permissions.api.service.PermissionServiceBridge;
import java.util.Set;

/** Group registry — obtain via {@link dev.rono.permissions.api.service.PermissionService#groups()}. */
public final class GroupsScope {

    private final PermissionServiceBridge service;

    public GroupsScope(PermissionServiceBridge service) {
        this.service = service;
    }

    public int count() {
        return service.groupCount();
    }

    public Set<String> names() {
        return service.groupNames();
    }

    public void delete(String name) {
        service.deleteGroup(name);
    }
}
