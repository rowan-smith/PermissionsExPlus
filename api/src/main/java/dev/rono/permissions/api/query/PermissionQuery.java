package dev.rono.permissions.api.query;

import dev.rono.permissions.api.PermissionsExException;
import dev.rono.permissions.api.event.PermissionEventBus;
import dev.rono.permissions.api.service.PermissionService;
import dev.rono.permissions.api.session.PermissionEditSession;
import dev.rono.permissions.api.world.Worlds;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Single fluent entry for the modern PermissionsEx API.
 *
 * <pre>{@code
 * pex.query().world(player.getWorld().getName()).user(player.getUniqueId()).inGroup("vip", true);
 * pex.query().users().count();
 * pex.query().backend().activate("file");
 * }</pre>
 */
public final class PermissionQuery {

    private final PermissionService service;

    private PermissionQuery(PermissionService service) {
        this.service = service;
    }

    public static PermissionQuery of(PermissionService service) {
        return new PermissionQuery(service);
    }

    /** World-scoped chain (recommended for checks and edits). */
    public WorldScope world(String world) {
        return new WorldScope(service, world);
    }

    /** Global namespace ({@link Worlds#GLOBAL}). */
    public WorldScope global() {
        return world(Worlds.GLOBAL);
    }

    /** Optional world scope when the realm is registered on the platform. */
    public Optional<WorldScope> findWorld(String world) {
        return service.worlds().contains(world) ? Optional.of(world(world)) : Optional.empty();
    }

    /** User registry ({@link UsersScope#count()}, resolve/find subjects). */
    public UsersScope users() {
        return new UsersScope(service);
    }

    /** Group registry ({@link GroupsScope#count()}, resolve/find subjects). */
    public GroupsScope groups() {
        return new GroupsScope(service);
    }

    /** Backend introspection and administration. */
    public BackendScope backend() {
        return new BackendScope(service);
    }

    public PermissionEventBus events() {
        return service.events();
    }

    public Collection<String> worlds() {
        return service.worlds();
    }

    public boolean isDebug() {
        return service.isDebug();
    }

    public void reload() throws PermissionsExException {
        service.reload();
    }

    public CompletableFuture<Void> reloadAsync() {
        return service.reloadAsync();
    }

    public PermissionEditSession editSession() {
        return service.openEditSession();
    }
}
