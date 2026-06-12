package dev.rono.permissions.bukkit;

import dev.rono.permissions.api.service.PermissionService;
import org.bukkit.entity.Player;

/** Bukkit {@link Player} convenience helpers for {@link PermissionService}. */
public final class BukkitPermissions {
    private BukkitPermissions() {}

    public static boolean has(PermissionService service, Player player, String permission) {
        return has(service, player, permission, player.getWorld().getName());
    }

    public static boolean has(PermissionService service, Player player, String permission, String world) {
        return service.world(world).user(player.getUniqueId()).has(permission);
    }

    /** Check using the player's current world. */
    public static boolean hasInCurrentWorld(PermissionService service, Player player, String permission) {
        return has(service, player, permission);
    }

    /** Check in the global namespace only. */
    public static boolean hasGlobal(PermissionService service, Player player, String permission) {
        return service.global().user(player.getUniqueId()).has(permission);
    }
}
