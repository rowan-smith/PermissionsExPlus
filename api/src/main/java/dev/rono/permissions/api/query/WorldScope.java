package dev.rono.permissions.api.query;

import dev.rono.permissions.api.service.PermissionServiceBridge;
import dev.rono.permissions.api.subject.Group;
import dev.rono.permissions.api.subject.GroupWorldContext;
import dev.rono.permissions.api.subject.UserWorldContext;
import dev.rono.permissions.api.world.Worlds;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** World-scoped chain — obtain via {@link dev.rono.permissions.api.service.PermissionService#world(String)}. */
public final class WorldScope {

    private final PermissionServiceBridge service;
    private final String world;

    public WorldScope(PermissionServiceBridge service, String world) {
        this.service = service;
        this.world = Worlds.normalize(world);
    }

    public String name() {
        return world;
    }

    public boolean isGlobal() {
        return Worlds.isGlobal(world);
    }

    public List<String> inheritance() {
        return service.worldInheritance(world);
    }

    public void setInheritance(List<String> parentWorlds) {
        service.setWorldInheritance(world, parentWorlds);
    }

    public List<Group> defaultGroups() {
        return service.defaultGroups(world);
    }

    public Map<Integer, Group> rankLadder(String ladderName) {
        return service.rankLadder(ladderName);
    }

    public UserWorldContext user(UUID uuid) {
        return SubjectRefs.user(service, uuid, null).inPresetWorld(world);
    }

    public UserWorldContext user(String identifier) {
        return SubjectRefs.user(service, null, identifier).inPresetWorld(world);
    }

    public Optional<UserWorldContext> findUser(UUID uuid) {
        return SubjectRefs.user(service, uuid, null).findInPresetWorld(world);
    }

    public Optional<UserWorldContext> findUser(String identifier) {
        return SubjectRefs.user(service, null, identifier).findInPresetWorld(world);
    }

    public GroupWorldContext group(String name) {
        return SubjectRefs.group(service, name).inPresetWorld(world);
    }

    public Optional<GroupWorldContext> findGroup(String name) {
        return SubjectRefs.group(service, name).findInPresetWorld(world);
    }
}
