package ru.tehkode.permissions.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.exceptions.PermissionsNotAvailable;

/**
 * Compile-only static entry points for hook plugins. The live {@code JavaPlugin} implementation ships in
 * {@code permissionsex-spigot} and is registered via {@code plugin.yml}.
 */
public final class PermissionsEx {
    private PermissionsEx() {}

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
        if (!isAvailable()) {
            throw new PermissionsNotAvailable();
        }
        RegisteredServiceProvider<PermissionManager> reg =
                Bukkit.getServer().getServicesManager().getRegistration(PermissionManager.class);
        return reg.getProvider();
    }

    public static PermissionUser getUser(Player player) {
        return getPermissionManager().getUser(player);
    }

    public static PermissionUser getUser(String name) {
        return getPermissionManager().getUser(name);
    }
}
