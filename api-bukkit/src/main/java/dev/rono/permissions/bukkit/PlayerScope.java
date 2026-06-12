package dev.rono.permissions.bukkit;

import dev.rono.permissions.api.service.PermissionService;
import dev.rono.permissions.api.subject.User;
import dev.rono.permissions.api.subject.UserWorldContext;
import org.bukkit.entity.Player;

/**
 * Player-scoped fluent permissions — obtain via {@link BukkitPermissions#on(Player)}.
 *
 * <p>Checks default to the player's <strong>current world</strong>. Use
 * {@link #hasPermissionGlobal(String)} for the global namespace
 * ({@link dev.rono.permissions.api.world.Worlds#GLOBAL}).</p>
 *
 * <pre>{@code
 * if (BukkitPermissions.on(player).hasPermission("my.node")) { ... }
 * BukkitPermissions.on(player).context().inGroup("vip");
 * }</pre>
 */
public final class PlayerScope {

    private final PermissionService service;
    private final Player player;

    /**
     * @param service registered permission service
     * @param player  bound player
     */
    PlayerScope(PermissionService service, Player player) {
        this.service = service;
        this.player = player;
    }

    /**
     * @return the bound Bukkit player
     */
    public Player player() {
        return player;
    }

    /**
     * @return the underlying {@link PermissionService}
     */
    public PermissionService service() {
        return service;
    }

    /**
     * Effective permission check in the player's current world.
     *
     * @param permission permission node to test
     * @return {@code true} if the player has the node in their current world context
     */
    public boolean hasPermission(String permission) {
        return context().hasPermission(permission);
    }

    /**
     * Effective permission check in the global namespace (all worlds unless overridden per world).
     *
     * @param permission permission node to test
     * @return {@code true} if the player has the node globally
     */
    public boolean hasPermissionGlobal(String permission) {
        return service.global().user(player.getUniqueId()).hasPermission(permission);
    }

    /**
     * World-scoped view using the player's current world.
     *
     * @return {@link UserWorldContext} for edits and checks in the active world
     */
    public UserWorldContext context() {
        return service.world(player.getWorld().getName()).user(player.getUniqueId());
    }

    /**
     * Materialized user subject (creates a virtual record if absent).
     *
     * @return {@link User} for global or multi-world operations
     */
    public User user() {
        return service.user(player.getUniqueId());
    }
}
