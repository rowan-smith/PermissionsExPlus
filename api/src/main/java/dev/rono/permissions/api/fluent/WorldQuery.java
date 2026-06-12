package dev.rono.permissions.api.fluent;

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
 * World-scoped fluent entry — obtain via {@link PermissionService#world(String)}.
 *
 * <pre>{@code
 * pex.world(world).user().byWorld(uuid).inGroup("vip", true);
 * pex.world(world).defaultGroups();
 * }</pre>
 */
public final class WorldQuery {

    private final PermissionService service;
    private final String world;

    WorldQuery(PermissionService service, String world) {
        this.service = service;
        this.world = Worlds.normalize(world);
    }

    public static WorldQuery of(PermissionService service, String world) {
        return new WorldQuery(service, world);
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

    // --- Subject lookups (world already bound) ---

    /** User lookup scoped to this world — {@link UserQuery#byWorld(UUID)} skips {@code inWorld}. */
    public UserQuery user() {
        return UserQuery.inWorld(service, world);
    }

    /** Optional user lookup scoped to this world. */
    public UserFinder findUser() {
        return UserFinder.inWorld(service, world);
    }

    /** Group lookup scoped to this world. */
    public GroupQuery group() {
        return GroupQuery.inWorld(service, world);
    }

    /** Optional group lookup scoped to this world. */
    public GroupFinder findGroup() {
        return GroupFinder.inWorld(service, world);
    }

    /** Shorthand for {@code user().byWorld(uuid)}. */
    public UserWorldContext user(UUID uuid) {
        return user().byWorld(uuid);
    }

    /** Shorthand for {@code user().byWorld(identifier)}. */
    public UserWorldContext user(String identifier) {
        return user().byWorld(identifier);
    }

    /** Shorthand for {@code findUser().byWorld(uuid)}. */
    public Optional<UserWorldContext> findUser(UUID uuid) {
        return findUser().byWorld(uuid);
    }

    /** Shorthand for {@code findUser().byWorld(identifier)}. */
    public Optional<UserWorldContext> findUser(String identifier) {
        return findUser().byWorld(identifier);
    }

    /** Shorthand for {@code group().byWorld(name)}. */
    public GroupWorldContext group(String name) {
        return group().byWorld(name);
    }

    /** Shorthand for {@code findGroup().byWorld(name)}. */
    public Optional<GroupWorldContext> findGroup(String name) {
        return findGroup().byWorld(name);
    }
}
