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

/**
 * Classic static entry points retained for third-party integrations. Implemented without referencing the concrete
 * {@code JavaPlugin} type so consumers can compile against {@code permissionsex-legacy-api} alone.
 */
public interface PermissionsEx {

    String PLUGIN_NAME = "PermissionsEx";

    static Plugin getPlugin() {
        return Bukkit.getPluginManager().getPlugin(PLUGIN_NAME);
    }

    /** Bukkit-facing enable check (does not imply the backing {@link PermissionManager} is wired yet). */
    static boolean isEnabled() {
        Plugin plugin = getPlugin();
        return plugin != null && plugin.isEnabled();
    }

    /** True when the PermissionsEx plugin is enabled and publishes a registered {@link PermissionManager}. */
    static boolean isAvailable() {
        if (!isEnabled()) {
            return false;
        }
        RegisteredServiceProvider<PermissionManager> reg =
                Bukkit.getServer().getServicesManager().getRegistration(PermissionManager.class);
        return reg != null && reg.getProvider() != null;
    }

    static PermissionManager getPermissionManager() {
        if (!isEnabled()) {
            throw new PermissionsNotAvailable();
        }
        RegisteredServiceProvider<PermissionManager> reg =
                Bukkit.getServer().getServicesManager().getRegistration(PermissionManager.class);
        if (reg == null || reg.getProvider() == null) {
            throw new PermissionsNotAvailable();
        }
        return reg.getProvider();
    }

    static PermissionBackend getBackend() {
        return getPermissionManager().getBackend();
    }

    static PermissionUser getUser(Player player) {
        return getPermissionManager().getUser(player.getUniqueId());
    }

    static PermissionUser getUser(String name) {
        return getPermissionManager().getUser(name);
    }

    static PermissionUser getUser(UUID uuid) {
        return getPermissionManager().getUser(uuid);
    }
}
