package dev.rono.permissions.bukkit;

import dev.rono.permissions.api.service.PermissionService;
import dev.rono.permissions.api.subject.User;
import dev.rono.permissions.api.subject.UserWorldContext;
import org.bukkit.entity.Player;

/**
 * Player-scoped fluent permissions — obtain via {@link BukkitPermissions#on(Player)}.
 */
public final class PlayerScope {

    private final PermissionService service;
    private final Player player;

    PlayerScope(PermissionService service, Player player) {
        this.service = service;
        this.player = player;
    }

    public Player player() {
        return player;
    }

    public PermissionService service() {
        return service;
    }

    /** Effective check in the player's current world. */
    public boolean hasPermission(String permission) {
        return context().hasPermission(permission);
    }

    /** Effective check in the global namespace. */
    public boolean hasPermissionGlobal(String permission) {
        return service.global().user(player.getUniqueId()).hasPermission(permission);
    }

    /** World-scoped view (player's current world). */
    public UserWorldContext context() {
        return service.world(player.getWorld().getName()).user(player.getUniqueId());
    }

    /** Materialized user — global namespace for {@link User#hasPermission(String)}. */
    public User user() {
        return service.user(player.getUniqueId());
    }
}
