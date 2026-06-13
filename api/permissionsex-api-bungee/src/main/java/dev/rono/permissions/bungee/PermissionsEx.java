package dev.rono.permissions.bungee;

import dev.rono.permissions.api.PermissionsExApi;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import ru.tehkode.permissions.PermissionManager;

/**
 * Static entry points for Bungee/Waterfall hook plugins.
 */
public final class PermissionsEx {

    private static final String PLUGIN_NAME = "PermissionsEx";

    private PermissionsEx() {}

    public static Plugin getPlugin() {
        return ProxyServer.getInstance().getPluginManager().getPlugin(PLUGIN_NAME);
    }

    public static boolean isAvailable() {
        return getPlugin() != null && ProxyPermissionServices.isRegistered();
    }

    public static PermissionsExApi getApi() {
        if (!isAvailable()) {
            throw new IllegalStateException(
                    "PermissionsExApi is not registered on this proxy — is PermissionsEx loaded?");
        }
        return ProxyPermissionServices.permissionsExApi();
    }

    /**
     * @deprecated Use {@link #getApi()} and {@link PermissionsExApi#getPermissionManager()}.
     */
    @Deprecated(forRemoval = false)
    public static PermissionManager getPermissionManager() {
        return getApi().getPermissionManager();
    }
}
