package dev.rono.permissions.bukkit;

import dev.rono.permissions.api.service.PermissionService;
import org.bukkit.entity.Player;

/**
 * Bukkit {@link Player} convenience helpers for PermissionsEx.
 *
 * <p>Prefer {@link #on(Player)} — no {@link PermissionService} parameter required.</p>
 */
public final class BukkitPermissions {
    private BukkitPermissions() {}

    /** Player-scoped fluent entry (resolves {@link PermissionService} from {@code ServicesManager}). */
    public static PlayerScope on(Player player) {
        return new PlayerScope(PexServices.require(), player);
    }

    /** @deprecated use {@link #on(Player)} {@code .has(permission)} */
    @Deprecated
    public static boolean has(PermissionService service, Player player, String permission) {
        return service.query().world(player.getWorld().getName()).user(player.getUniqueId()).has(permission);
    }

    /** @deprecated use {@link #on(Player)} {@code .has(permission)} or {@code .context().has(permission)} */
    @Deprecated
    public static boolean has(PermissionService service, Player player, String permission, String world) {
        return service.query().world(world).user(player.getUniqueId()).has(permission);
    }

    /** @deprecated use {@link #on(Player)} {@code .has(permission)} */
    @Deprecated
    public static boolean hasInCurrentWorld(PermissionService service, Player player, String permission) {
        return has(service, player, permission);
    }

    /** @deprecated use {@link #on(Player)} {@code .hasGlobal(permission)} */
    @Deprecated
    public static boolean hasGlobal(PermissionService service, Player player, String permission) {
        return service.query().global().user(player.getUniqueId()).has(permission);
    }
}
