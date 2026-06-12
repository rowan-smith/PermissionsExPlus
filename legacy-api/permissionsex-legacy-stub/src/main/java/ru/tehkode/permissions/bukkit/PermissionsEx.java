package ru.tehkode.permissions.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.exceptions.PermissionsNotAvailable;

/**
 * Compile-only static entry points for hook plugins.
 *
 * <p>The live {@code JavaPlugin} implementation ships in {@code permissionsex-spigot} and is
 * registered via {@code plugin.yml}. Hook plugins depend on this class for stable static accessors
 * without linking against the full plugin module.</p>
 */
public final class PermissionsEx {
    private PermissionsEx() {}

    /**
     * Returns the PermissionsEx Bukkit plugin instance, if loaded.
     *
     * @return the plugin, or {@code null} if PermissionsEx is not installed
     */
    public static Plugin getPlugin() {
        return Bukkit.getPluginManager().getPlugin("PermissionsEx");
    }

    /**
     * Returns whether PermissionsEx is loaded, enabled, and exposing a {@link PermissionManager}.
     *
     * @return {@code true} if the plugin and its service provider are available
     */
    public static boolean isAvailable() {
        Plugin plugin = getPlugin();
        if (plugin == null || !plugin.isEnabled()) {
            return false;
        }
        RegisteredServiceProvider<PermissionManager> reg =
                Bukkit.getServer().getServicesManager().getRegistration(PermissionManager.class);
        return reg != null && reg.getProvider() != null;
    }

    /**
     * Returns the registered {@link PermissionManager} service.
     *
     * @return active permission manager; never {@code null}
     * @throws PermissionsNotAvailable if PermissionsEx is not loaded or the manager is not registered
     */
    public static PermissionManager getPermissionManager() {
        if (!isAvailable()) {
            throw new PermissionsNotAvailable();
        }
        RegisteredServiceProvider<PermissionManager> reg =
                Bukkit.getServer().getServicesManager().getRegistration(PermissionManager.class);
        return reg.getProvider();
    }

    /**
     * Returns the permission user associated with an online player.
     *
     * @param player online player; must not be {@code null}
     * @return permission user handle; never {@code null} when PermissionsEx is available
     * @throws PermissionsNotAvailable if PermissionsEx is not loaded or the manager is not registered
     */
    public static PermissionUser getUser(Player player) {
        return getPermissionManager().getUser(player);
    }

    /**
     * Returns the permission user identified by name.
     *
     * @param name player or user name
     * @return permission user handle; never {@code null} when PermissionsEx is available
     * @throws PermissionsNotAvailable if PermissionsEx is not loaded or the manager is not registered
     */
    public static PermissionUser getUser(String name) {
        return getPermissionManager().getUser(name);
    }
}
