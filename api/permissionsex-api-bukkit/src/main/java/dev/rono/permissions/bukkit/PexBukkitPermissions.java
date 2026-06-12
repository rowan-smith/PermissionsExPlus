package dev.rono.permissions.bukkit;

import org.bukkit.entity.Player;

/**
 * Bukkit {@link Player} convenience helpers for PermissionsEx.
 *
 * <p>Resolves {@link dev.rono.permissions.api.service.PexPermissionService} via
 * {@link PermissionsExPlus#getPermissionService()} — hook plugins do not pass the service explicitly.</p>
 *
 * <pre>{@code
 * PexBukkitPermissions.on(player).hasPermission("my.node");
 * PexBukkitPermissions.on(player).context().inGroup("vip");
 * }</pre>
 */
public final class PexBukkitPermissions {

    private PexBukkitPermissions() {}

    /**
     * Begin a player-scoped permission chain.
     *
     * @param player online player to query or edit
     * @return fluent scope bound to the player
     * @throws IllegalStateException if {@code PexPermissionService} is not registered
     */
    public static PexPlayerScope on(Player player) {
        return new PexPlayerScope(PexServices.require(), player);
    }
}
