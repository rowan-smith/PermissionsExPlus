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
 * <p>Registered on Spigot/Paper {@code ServicesManager} under this type.</p>
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

    /**
     * Returns the user registry ({@link UsersScope#count()}, {@link UsersScope#identifiers()}).
     *
     * @return user registry scope
     */
    default UsersScope users() {
        return new UsersScope(requireBridge(this));
    }

    /**
     * Returns the group registry ({@link GroupsScope#count()}, {@link GroupsScope#names()}).
     *
     * @return group registry scope
     */
    default GroupsScope groups() {
        return new GroupsScope(requireBridge(this));
    }

    /**
     * Returns registered server realms.
     *
     * @return worlds registry scope
     */
    default WorldsScope worlds() {
        return new WorldsScope(requireBridge(this));
    }

    /**
     * Resolves a user by UUID, materializing a record when none exists yet.
     *
     * @param uuid player UUID
     * @return a live {@link User} handle
     */
    default User user(UUID uuid) {
        return requireBridge(this).user(uuid);
    }

    /**
     * Resolves a user by name or UUID string, materializing a record when none exists yet.
     *
     * @param identifier user name or UUID string
     * @return a live {@link User} handle
     */
    default User user(String identifier) {
        return requireBridge(this).user(identifier);
    }

    /**
     * Looks up a persisted user by UUID without materializing a new record.
     *
     * @param uuid player UUID
     * @return optional lookup chain; use {@link FoundUser#get()} or {@link FoundUser#optional()}
     */
    default FoundUser findUser(UUID uuid) {
        return FoundUser.of(requireBridge(this), uuid, null);
    }

    /**
     * Looks up a persisted user by identifier without materializing a new record.
     *
     * @param identifier user name or UUID string
     * @return optional lookup chain; use {@link FoundUser#get()} or {@link FoundUser#optional()}
     */
    default FoundUser findUser(String identifier) {
        return FoundUser.of(requireBridge(this), null, identifier);
    }

    /**
     * Resolves a group by name, materializing a record when none exists yet.
     *
     * @param name group name
     * @return a live {@link Group} handle
     */
    default Group group(String name) {
        return requireBridge(this).group(name);
    }

    /**
     * Looks up a persisted group by name without materializing a new record.
     *
     * @param name group name
     * @return optional lookup chain; use {@link FoundGroup#get()} or {@link FoundGroup#optional()}
     */
    default FoundGroup findGroup(String name) {
        return FoundGroup.of(requireBridge(this), name);
    }

    /**
     * Returns a world-scoped chain for checks and edits.
     *
     * @param world world name, or {@code null} for global
     * @return world scope preset to the given realm
     */
    default WorldScope world(String world) {
        return new WorldScope(requireBridge(this), world);
    }

    /**
     * Returns a world scope for the global namespace ({@link Worlds#GLOBAL}).
     *
     * @return global world scope
     */
    default WorldScope global() {
        return world(Worlds.GLOBAL);
    }

    /**
     * Returns a world scope when the realm is registered on the platform.
     *
     * @param world world name to look up
     * @return scope when registered, otherwise empty
     */
    default Optional<WorldScope> findWorld(String world) {
        return requireBridge(this).registeredWorlds().contains(world) ? Optional.of(world(world)) : Optional.empty();
    }

    /**
     * Returns backend introspection and administration helpers.
     *
     * @return backend scope
     */
    default BackendScope backend() {
        return new BackendScope(requireBridge(this));
    }

    /**
     * Returns batch edit session helpers.
     *
     * @return session scope; call {@link SessionScope#start()} to open a session
     */
    default SessionScope session() {
        return new SessionScope(requireBridge(this));
    }

    /**
     * Returns the permission-domain event bus.
     *
     * @return shared {@link PermissionEventBus}
     */
    default PermissionEventBus events() {
        return requireBridge(this).events();
    }

    /**
     * Reloads permission data from the active backend synchronously.
     *
     * @throws PermissionsExException if reload fails
     */
    default void reload() throws PermissionsExException {
        requireBridge(this).reload();
    }

    /**
     * Reloads permission data from the active backend asynchronously.
     *
     * @return future completing when reload finishes
     */
    default CompletableFuture<Void> reloadAsync() {
        return requireBridge(this).reloadAsync();
    }

    /**
     * Reports whether PermissionsEx debug logging is enabled.
     *
     * @return {@code true} when debug mode is active
     */
    default boolean isDebug() {
        return requireBridge(this).isDebug();
    }

    /**
     * Casts {@code service} to {@link PermissionServiceBridge} when the runtime exposes the bridge API.
     *
     * @param service registered {@link PermissionService} instance
     * @return the same instance as a {@link PermissionServiceBridge}
     * @throws IllegalStateException if {@code service} does not implement {@link PermissionServiceBridge}
     */
    static PermissionServiceBridge requireBridge(PermissionService service) {
        if (service instanceof PermissionServiceBridge bridge) {
            return bridge;
        }
        throw new IllegalStateException(
                "PermissionService must implement PermissionServiceBridge: " + service.getClass().getName());
    }
}
