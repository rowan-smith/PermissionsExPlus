package ru.tehkode.permissions.spigot.bukkit;

import dev.rono.permissions.core.DefaultPermissionManager;
import dev.rono.permissions.core.PermissionsExConfig;
import org.bukkit.entity.Player;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.exceptions.PermissionBackendException;

import java.util.logging.Logger;

/**
 * Bukkit game-server permission manager with {@link Player}-typed legacy API support.
 */
public final class SpigotPermissionManager extends DefaultPermissionManager {

    public SpigotPermissionManager(
            PermissionsExConfig config,
            Logger logger,
            dev.rono.permissions.api.runtime.PlatformRuntime platformRuntime) throws PermissionBackendException {
        super(config, logger, platformRuntime);
    }

    @Override
    public boolean has(Player player, String permission) {
        return has(player.getUniqueId(), permission, player.getWorld().getName());
    }

    @Override
    public boolean has(Player player, String permission, String world) {
        return has(player.getUniqueId(), permission, world);
    }

    @Override
    public PermissionUser getUser(Player player) {
        return getUser(player.getUniqueId());
    }

    @Override
    public void resetUser(Player player) {
        resetUser(player.getUniqueId().toString());
    }

    @Override
    public void clearUserCache(Player player) {
        clearUserCache(player.getUniqueId());
    }
}
