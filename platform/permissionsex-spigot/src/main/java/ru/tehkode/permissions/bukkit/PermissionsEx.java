package ru.tehkode.permissions.bukkit;

import dev.rono.permissions.api.PermissionsExApi;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.PermissionUser;
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
        var plugin = getPlugin();
        if (plugin == null || !plugin.isEnabled()) {
            return false;
        }
        var modern = Bukkit.getServer().getServicesManager().getRegistration(PermissionsExApi.class);
        if (modern != null && modern.getProvider() != null) {
            return true;
        }
        var legacy = Bukkit.getServer().getServicesManager().getRegistration(PermissionManager.class);
        return legacy != null && legacy.getProvider() != null;
    }

    public static PermissionsExApi getApi() {
        if (!isAvailable()) {
            throw new PermissionsNotAvailable();
        }
        var reg = Bukkit.getServer().getServicesManager().getRegistration(PermissionsExApi.class);
        if (reg != null && reg.getProvider() != null) {
            return reg.getProvider();
        }
        var legacyReg = Bukkit.getServer().getServicesManager().getRegistration(PermissionManager.class);
        if (legacyReg != null && legacyReg.getProvider() instanceof PermissionsExApi api) {
            return api;
        }
        throw new PermissionsNotAvailable();
    }

    @Deprecated(forRemoval = false)
    public static PermissionManager getPermissionManager() {
        return getApi().getPermissionManager();
    }

    @Deprecated(forRemoval = false)
    public static PermissionUser getUser(Player player) {
        return getPermissionManager().getUser(player);
    }

    @Deprecated(forRemoval = false)
    public static PermissionUser getUser(String name) {
        return getPermissionManager().getUser(name);
    }
}
