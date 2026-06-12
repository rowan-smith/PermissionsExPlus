package dev.rono.permissions.api.query;

import dev.rono.permissions.api.service.PermissionServiceBridge;
import java.util.Collection;

/** Registered server realms — obtain via {@link dev.rono.permissions.api.service.PermissionService#worlds()}. */
public final class PexWorldsScope {

    private final PermissionServiceBridge service;

    public PexWorldsScope(PermissionServiceBridge service) {
        this.service = service;
    }

    public int count() {
        return service.registeredWorlds().size();
    }

    public Collection<String> names() {
        return service.registeredWorlds();
    }
}
