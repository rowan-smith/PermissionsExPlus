package dev.rono.permissions.api.query;

import dev.rono.permissions.api.service.PermissionService;
import dev.rono.permissions.api.subject.Group;
import dev.rono.permissions.api.subject.GroupWorldContext;
import dev.rono.permissions.api.world.Worlds;
import java.util.Optional;

final class GroupRef {

    private final PermissionService service;
    private final String name;

    GroupRef(PermissionService service, String name) {
        this.service = service;
        this.name = name;
    }

    Group resolve() {
        return service.group(name);
    }

    Optional<Group> find() {
        return service.findGroup(name);
    }

    GroupWorldContext inWorld(String world) {
        return resolve().inWorld(world);
    }

    Optional<GroupWorldContext> findInWorld(String world) {
        return find().map(group -> group.inWorld(world));
    }

    GroupWorldContext inPresetWorld(String presetWorld) {
        return inWorld(presetWorld != null ? presetWorld : Worlds.GLOBAL);
    }

    Optional<GroupWorldContext> findInPresetWorld(String presetWorld) {
        return findInWorld(presetWorld != null ? presetWorld : Worlds.GLOBAL);
    }
}
