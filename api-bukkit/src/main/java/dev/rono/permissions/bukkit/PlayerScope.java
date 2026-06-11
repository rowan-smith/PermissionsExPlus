package dev.rono.permissions.bukkit;

import dev.rono.permissions.api.service.PermissionService;
import dev.rono.permissions.api.subject.User;
import dev.rono.permissions.api.subject.UserWorldContext;
import org.bukkit.entity.Player;

/**
 * Player-scoped fluent permissions — obtain via {@link BukkitPermissions#on(Player)}.
 *
 * <pre>{@code
 * if (BukkitPermissions.on(player).has("my.node")) { ... }
 * BukkitPermissions.on(player).context().inGroup("vip");
 * }</pre>
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
    public boolean has(String permission) {
        return context().has(permission);
    }

    /** Effective check in the global namespace. */
    public boolean hasGlobal(String permission) {
        return service.query().global().user(player.getUniqueId()).has(permission);
    }

    /** World-scoped view (player's current world). */
    public UserWorldContext context() {
        return service.query().world(player.getWorld().getName()).user(player.getUniqueId());
    }

    /** Materialized user (any world). */
    public User user() {
        return service.user(player.getUniqueId());
    }
}
