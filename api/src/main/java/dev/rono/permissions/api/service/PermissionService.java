package dev.rono.permissions.api.service;

import dev.rono.permissions.api.PermissionsExException;
import dev.rono.permissions.api.event.PermissionEventBus;
import dev.rono.permissions.api.query.BackendScope;
import dev.rono.permissions.api.query.FoundGroup;
import dev.rono.permissions.api.query.FoundUser;
import dev.rono.permissions.api.query.GroupsScope;
import dev.rono.permissions.api.query.SessionScope;
import dev.rono.permissions.api.query.UsersScope;
import dev.rono.permissions.api.query.WorldScope;
import dev.rono.permissions.api.query.WorldsScope;
import dev.rono.permissions.api.subject.Group;
import dev.rono.permissions.api.subject.User;
import dev.rono.permissions.api.world.Worlds;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Modern PermissionsEx integration API ({@code dev.rono.permissions.api}).
 *
 * <pre>{@code
 * pex.user(uuid).hasPermission("node");                    // global
 * pex.world(w).user(uuid).hasPermission("node");             // per-world
 * pex.findUser("Steve").get().hasPermission("node");         // persisted only
 * pex.users().count();
 * pex.backend().getActive();
 * pex.session().start();
 * }</pre>
 */
public interface PermissionService {

    default UsersScope users() {
        return new UsersScope(requireBridge(this));
    }

    default GroupsScope groups() {
        return new GroupsScope(requireBridge(this));
    }

    default WorldsScope worlds() {
        return new WorldsScope(requireBridge(this));
    }

    default User user(UUID uuid) {
        return requireBridge(this).user(uuid);
    }

    default User user(String identifier) {
        return requireBridge(this).user(identifier);
    }

    default FoundUser findUser(UUID uuid) {
        return FoundUser.of(requireBridge(this), uuid, null);
    }

    default FoundUser findUser(String identifier) {
        return FoundUser.of(requireBridge(this), null, identifier);
    }

    default Group group(String name) {
        return requireBridge(this).group(name);
    }

    default FoundGroup findGroup(String name) {
        return FoundGroup.of(requireBridge(this), name);
    }

    default WorldScope world(String world) {
        return new WorldScope(requireBridge(this), world);
    }

    default WorldScope global() {
        return world(Worlds.GLOBAL);
    }

    default Optional<WorldScope> findWorld(String world) {
        return requireBridge(this).registeredWorlds().contains(world) ? Optional.of(world(world)) : Optional.empty();
    }

    default BackendScope backend() {
        return new BackendScope(requireBridge(this));
    }

    default SessionScope session() {
        return new SessionScope(requireBridge(this));
    }

    default PermissionEventBus events() {
        return requireBridge(this).events();
    }

    default void reload() throws PermissionsExException {
        requireBridge(this).reload();
    }

    default CompletableFuture<Void> reloadAsync() {
        return requireBridge(this).reloadAsync();
    }

    default boolean isDebug() {
        return requireBridge(this).isDebug();
    }

    static PermissionServiceBridge requireBridge(PermissionService service) {
        if (service instanceof PermissionServiceBridge bridge) {
            return bridge;
        }
        throw new IllegalStateException(
                "PermissionService must implement PermissionServiceBridge: " + service.getClass().getName());
    }
}
