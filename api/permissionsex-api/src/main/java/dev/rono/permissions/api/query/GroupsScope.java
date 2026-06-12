package dev.rono.permissions.api.query;

import dev.rono.permissions.api.service.PermissionServiceBridge;
import java.util.Set;

/**
 * Group registry — obtain via {@link dev.rono.permissions.api.service.PermissionService#groups()}.
 *
 * <p>For subject lookup, use top-level {@link dev.rono.permissions.api.service.PermissionService#group(String)}
 * (resolve/materialize) or {@link dev.rono.permissions.api.service.PermissionService#findGroup(String)}
 * (persisted only).</p>
 */
public final class GroupsScope {

    private final PermissionServiceBridge service;

    public GroupsScope(PermissionServiceBridge service) {
        this.service = service;
    }

    /**
     * Returns the number of group records in the active backend.
     *
     * @return persisted group count
     */
    public int count() {
        return service.groupCount();
    }

    /**
     * Returns every group name stored in the active backend.
     *
     * @return set of persisted group names
     */
    public Set<String> names() {
        return service.groupNames();
    }

    /**
     * Removes a group record from the active backend.
     *
     * @param name group name to delete
     */
    public void delete(String name) {
        service.deleteGroup(name);
    }
}
