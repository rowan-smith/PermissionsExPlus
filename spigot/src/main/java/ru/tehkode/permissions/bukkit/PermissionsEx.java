package ru.tehkode.permissions.bukkit;

import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.backends.PermissionBackend;
import ru.tehkode.permissions.exceptions.PermissionsNotAvailable;
import ru.tehkode.permissions.spigot.bukkit.SpigotPermissionsExPlugin;

/**
 * Legacy Bukkit entrypoint kept for binary compatibility with integrations
 * that cast the plugin instance to the historical class name.
 */
public final class PermissionsEx extends SpigotPermissionsExPlugin {

    public static Plugin getPlugin() {
        return Bukkit.getPluginManager().getPlugin("PermissionsEx");
    }

    public static boolean isAvailable() {
        Plugin plugin = getPlugin();
        if (plugin == null || !plugin.isEnabled()) {
            return false;
        }
        RegisteredServiceProvider<PermissionManager> reg =
                Bukkit.getServer().getServicesManager().getRegistration(PermissionManager.class);
        return reg != null && reg.getProvider() != null;
    }

    public static PermissionManager getPermissionManager() {
        Plugin plugin = getPlugin();
        if (plugin == null || !plugin.isEnabled()) {
            throw new PermissionsNotAvailable();
        }
        RegisteredServiceProvider<PermissionManager> reg =
                Bukkit.getServer().getServicesManager().getRegistration(PermissionManager.class);
        if (reg == null || reg.getProvider() == null) {
            throw new PermissionsNotAvailable();
        }
        return reg.getProvider();
    }

    public static PermissionBackend getBackend() {
        return getPermissionManager().getBackend();
    }

    public PermissionUser getUser(Player player) {
        return getPermissionManager().getUser(player.getUniqueId());
    }

    public static PermissionUser getUser(String name) {
        return getPermissionManager().getUser(name);
    }

    public static PermissionUser getUser(UUID uuid) {
        return getPermissionManager().getUser(uuid);
    }
}
