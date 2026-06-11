package dev.rono.permissions.api.query;

import dev.rono.permissions.api.service.PermissionService;
import dev.rono.permissions.api.subject.Group;
import dev.rono.permissions.api.subject.GroupWorldContext;
import dev.rono.permissions.api.subject.UserWorldContext;
import dev.rono.permissions.api.world.Worlds;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * World-scoped fluent chain — obtain via {@link PermissionQuery#world(String)}.
 *
 * <pre>{@code
 * pex.query().world(world).user(uuid).inGroup("vip", true);
 * pex.query().world(world).findUser(uuid).map(u -> u.has("node")).orElse(false);
 * }</pre>
 */
public final class WorldScope {

    private final PermissionService service;
    private final String world;

    WorldScope(PermissionService service, String world) {
        this.service = service;
        this.world = Worlds.normalize(world);
    }

    /** {@link Worlds#GLOBAL} or a specific world name. */
    public String name() {
        return world;
    }

    public boolean isGlobal() {
        return Worlds.isGlobal(world);
    }

    // --- World configuration ---

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

    // --- Subjects (world bound) ---

    /** Resolve or materialize a user in this world. */
    public UserWorldContext user(UUID uuid) {
        return SubjectRefs.user(service, uuid, null).inPresetWorld(world);
    }

    /** Resolve or materialize a user in this world. */
    public UserWorldContext user(String identifier) {
        return SubjectRefs.user(service, null, identifier).inPresetWorld(world);
    }

    /** Persisted user in this world, if present. */
    public Optional<UserWorldContext> findUser(UUID uuid) {
        return SubjectRefs.user(service, uuid, null).findInPresetWorld(world);
    }

    /** Persisted user in this world, if present. */
    public Optional<UserWorldContext> findUser(String identifier) {
        return SubjectRefs.user(service, null, identifier).findInPresetWorld(world);
    }

    /** Resolve a group in this world. */
    public GroupWorldContext group(String name) {
        return SubjectRefs.group(service, name).inPresetWorld(world);
    }

    /** Persisted group in this world, if present. */
    public Optional<GroupWorldContext> findGroup(String name) {
        return SubjectRefs.group(service, name).findInPresetWorld(world);
    }
}
