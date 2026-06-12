package dev.rono.permissions.api.query;

import dev.rono.permissions.api.service.PermissionServiceBridge;
import dev.rono.permissions.api.subject.PexGroup;
import dev.rono.permissions.api.subject.PexGroupWorldContext;
import dev.rono.permissions.api.world.PexWorlds;
import java.util.Optional;

final class GroupRef {

    private final PermissionServiceBridge service;
    private final String name;

    GroupRef(PermissionServiceBridge service, String name) {
        this.service = service;
        this.name = name;
    }

    PexGroup resolve() {
        return service.group(name);
    }

    Optional<PexGroup> find() {
        return service.lookupGroup(name);
    }

    PexGroupWorldContext inWorld(String world) {
        return resolve().inWorld(world);
    }

    Optional<PexGroupWorldContext> findInWorld(String world) {
        return find().map(group -> group.inWorld(world));
    }

    PexGroupWorldContext inPresetWorld(String presetWorld) {
        return inWorld(presetWorld != null ? presetWorld : PexWorlds.GLOBAL);
    }

    Optional<PexGroupWorldContext> findInPresetWorld(String presetWorld) {
        return findInWorld(presetWorld != null ? presetWorld : PexWorlds.GLOBAL);
    }
}
