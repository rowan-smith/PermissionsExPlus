package dev.rono.permissions.api.query;

import dev.rono.permissions.api.service.PermissionService;
import dev.rono.permissions.api.subject.User;
import dev.rono.permissions.api.subject.UserWorldContext;
import dev.rono.permissions.api.world.Worlds;
import java.util.Optional;
import java.util.UUID;

final class UserRef {

    private final PermissionService service;
    private final UUID uuid;
    private final String identifier;

    UserRef(PermissionService service, UUID uuid, String identifier) {
        this.service = service;
        this.uuid = uuid;
        this.identifier = identifier;
    }

    User resolve() {
        return uuid != null ? service.user(uuid) : service.user(identifier);
    }

    Optional<User> find() {
        return uuid != null ? service.findUser(uuid) : service.findUser(identifier);
    }

    UserWorldContext inWorld(String world) {
        return resolve().inWorld(world);
    }

    Optional<UserWorldContext> findInWorld(String world) {
        return find().map(user -> user.inWorld(world));
    }

    UserWorldContext inPresetWorld(String presetWorld) {
        return inWorld(presetWorld != null ? presetWorld : Worlds.GLOBAL);
    }

    Optional<UserWorldContext> findInPresetWorld(String presetWorld) {
        return findInWorld(presetWorld != null ? presetWorld : Worlds.GLOBAL);
    }
}
