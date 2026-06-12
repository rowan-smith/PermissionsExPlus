package dev.rono.permissions.bukkit;

import org.bukkit.entity.Player;

/**
 * Bukkit {@link Player} convenience helpers for PermissionsEx.
 *
 * <p>Resolves {@link dev.rono.permissions.api.service.PermissionService} from Bukkit
 * {@code ServicesManager} automatically — hook plugins do not pass the service explicitly.</p>
 *
 * <pre>{@code
 * BukkitPermissions.on(player).hasPermission("my.node");
 * BukkitPermissions.on(player).context().inGroup("vip");
 * }</pre>
 */
public final class BukkitPermissions {

    private BukkitPermissions() {}

    /**
     * Begin a player-scoped permission chain.
     *
     * @param player online player to query or edit
     * @return fluent scope bound to the player
     * @throws IllegalStateException if {@code PermissionService} is not registered
     */
    public static PlayerScope on(Player player) {
        return new PlayerScope(PexServices.require(), player);
    }
}
