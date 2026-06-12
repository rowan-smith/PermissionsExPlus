package dev.rono.permissions.api.query;

import dev.rono.permissions.api.service.PermissionServiceBridge;
import dev.rono.permissions.api.subject.PexUser;
import dev.rono.permissions.api.subject.PexUserWorldContext;
import dev.rono.permissions.api.world.PexWorlds;
import java.util.Optional;
import java.util.UUID;

final class UserRef {

    private final PermissionServiceBridge service;
    private final UUID uuid;
    private final String identifier;

    UserRef(PermissionServiceBridge service, UUID uuid, String identifier) {
        this.service = service;
        this.uuid = uuid;
        this.identifier = identifier;
    }

    PexUser resolve() {
        return uuid != null ? service.user(uuid) : service.user(identifier);
    }

    Optional<PexUser> find() {
        return uuid != null ? service.lookupUser(uuid) : service.lookupUser(identifier);
    }

    PexUserWorldContext inWorld(String world) {
        return resolve().inWorld(world);
    }

    Optional<PexUserWorldContext> findInWorld(String world) {
        return find().map(user -> user.inWorld(world));
    }

    PexUserWorldContext inPresetWorld(String presetWorld) {
        return inWorld(presetWorld != null ? presetWorld : PexWorlds.GLOBAL);
    }

    Optional<PexUserWorldContext> findInPresetWorld(String presetWorld) {
        return findInWorld(presetWorld != null ? presetWorld : PexWorlds.GLOBAL);
    }
}
