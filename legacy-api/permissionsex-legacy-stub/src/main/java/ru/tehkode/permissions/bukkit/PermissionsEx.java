package ru.tehkode.permissions.bukkit;

import dev.rono.permissions.api.PermissionsExApi;
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
 * <pre>{@code
 * PermissionsExApi api = PermissionsEx.getApi();
 * PermissionManager manager = api.getPermissionManager();
 * }</pre>
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
        RegisteredServiceProvider<PermissionsExApi> modern =
                Bukkit.getServer().getServicesManager().getRegistration(PermissionsExApi.class);
        if (modern != null && modern.getProvider() != null) {
            return true;
        }
        RegisteredServiceProvider<PermissionManager> legacy =
                Bukkit.getServer().getServicesManager().getRegistration(PermissionManager.class);
        return legacy != null && legacy.getProvider() != null;
    }

    public static PermissionsExApi getApi() {
        if (!isAvailable()) {
            throw new PermissionsNotAvailable();
        }
        RegisteredServiceProvider<PermissionsExApi> reg =
                Bukkit.getServer().getServicesManager().getRegistration(PermissionsExApi.class);
        if (reg != null && reg.getProvider() != null) {
            return reg.getProvider();
        }
        RegisteredServiceProvider<PermissionManager> legacyReg =
                Bukkit.getServer().getServicesManager().getRegistration(PermissionManager.class);
        if (legacyReg != null && legacyReg.getProvider() instanceof PermissionsExApi api) {
            return api;
        }
        throw new PermissionsNotAvailable();
    }

    /**
     * @deprecated Use {@link #getApi()} and {@link PermissionsExApi#getPermissionManager()}.
     */
    @Deprecated(forRemoval = false)
    public static PermissionManager getPermissionManager() {
        return getApi().getPermissionManager();
    }

    /**
     * @deprecated Use {@link PermissionsExApi#getPermissionManager()}.
     */
    @Deprecated(forRemoval = false)
    public static PermissionUser getUser(Player player) {
        return getPermissionManager().getUser(player);
    }

    /**
     * @deprecated Use {@link PermissionsExApi#getPermissionManager()}.
     */
    @Deprecated(forRemoval = false)
    public static PermissionUser getUser(String name) {
        return getPermissionManager().getUser(name);
    }
}
