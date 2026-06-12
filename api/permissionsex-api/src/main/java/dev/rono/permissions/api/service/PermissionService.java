package dev.rono.permissions.api.service;

import dev.rono.permissions.api.PermissionsExException;
import dev.rono.permissions.api.event.PermissionEventBus;
import dev.rono.permissions.api.query.*;
import dev.rono.permissions.api.subject.PexGroup;
import dev.rono.permissions.api.subject.PexUser;
import dev.rono.permissions.api.world.PexWorlds;

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
     * Returns the user registry ({@link PexUsersScope#count()}, {@link PexUsersScope#identifiers()}).
     *
     * @return user registry scope
     */
    default PexUsersScope users() {
        return new PexUsersScope(requireBridge(this));
    }

    /**
     * Returns the group registry ({@link PexGroupsScope#count()}, {@link PexGroupsScope#names()}).
     *
     * @return group registry scope
     */
    default PexGroupsScope groups() {
        return new PexGroupsScope(requireBridge(this));
    }

    /**
     * Returns registered server realms.
     *
     * @return worlds registry scope
     */
    default PexWorldsScope worlds() {
        return new PexWorldsScope(requireBridge(this));
    }

    /**
     * Resolves a user by UUID, materializing a record when none exists yet.
     *
     * @param uuid player UUID
     * @return a live {@link PexUser} handle
     */
    default PexUser user(UUID uuid) {
        return requireBridge(this).user(uuid);
    }

    /**
     * Resolves a user by name or UUID string, materializing a record when none exists yet.
     *
     * @param identifier user name or UUID string
     * @return a live {@link PexUser} handle
     */
    default PexUser user(String identifier) {
        return requireBridge(this).user(identifier);
    }

    /**
     * Looks up a persisted user by UUID without materializing a new record.
     *
     * @param uuid player UUID
     * @return optional lookup chain; use {@link PexFoundUser#get()} or {@link PexFoundUser#optional()}
     */
    default PexFoundUser findUser(UUID uuid) {
        return PexFoundUser.of(requireBridge(this), uuid, null);
    }

    /**
     * Looks up a persisted user by identifier without materializing a new record.
     *
     * @param identifier user name or UUID string
     * @return optional lookup chain; use {@link PexFoundUser#get()} or {@link PexFoundUser#optional()}
     */
    default PexFoundUser findUser(String identifier) {
        return PexFoundUser.of(requireBridge(this), null, identifier);
    }

    /**
     * Resolves a group by name, materializing a record when none exists yet.
     *
     * @param name group name
     * @return a live {@link PexGroup} handle
     */
    default PexGroup group(String name) {
        return requireBridge(this).group(name);
    }

    /**
     * Looks up a persisted group by name without materializing a new record.
     *
     * @param name group name
     * @return optional lookup chain; use {@link PexFoundGroup#get()} or {@link PexFoundGroup#optional()}
     */
    default PexFoundGroup findGroup(String name) {
        return PexFoundGroup.of(requireBridge(this), name);
    }

    /**
     * Returns a world-scoped chain for checks and edits.
     *
     * @param world world name, or {@code null} for global
     * @return world scope preset to the given realm
     */
    default PexWorldScope world(String world) {
        return new PexWorldScope(requireBridge(this), world);
    }

    /**
     * Returns a world scope for the global namespace ({@link PexWorlds#GLOBAL}).
     *
     * @return global world scope
     */
    default PexWorldScope global() {
        return world(PexWorlds.GLOBAL);
    }

    /**
     * Returns a world scope when the realm is registered on the platform.
     *
     * @param world world name to look up
     * @return scope when registered, otherwise empty
     */
    default Optional<PexWorldScope> findWorld(String world) {
        return requireBridge(this).registeredWorlds().contains(world) ? Optional.of(world(world)) : Optional.empty();
    }

    /**
     * Returns backend introspection and administration helpers.
     *
     * @return backend scope
     */
    default PexBackendScope backend() {
        return new PexBackendScope(requireBridge(this));
    }

    /**
     * Returns batch edit session helpers.
     *
     * @return session scope; call {@link PexSessionScope#start()} to open a session
     */
    default PexSessionScope session() {
        return new PexSessionScope(requireBridge(this));
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
