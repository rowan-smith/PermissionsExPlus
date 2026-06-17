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
    private static volatile ProxyLegacyBridgeHost bridgeHost;

    private PermissionsEx() {}

    public static void registerBridgeHost(ProxyLegacyBridgeHost host) {
        bridgeHost = host;
    }

    public static void clearBridgeHost() {
        bridgeHost = null;
    }

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
    @Deprecated(since = "3.0.0", forRemoval = false)
    public static PermissionManager getPermissionManager() {
        ensureLegacyBridge("PermissionsEx.getPermissionManager()");
        return getApi().getPermissionManager();
    }

    private static void ensureLegacyBridge(String reason) {
        var host = bridgeHost;
        if (host != null) {
            host.ensureLegacyBridgeForHook(reason);
        }
    }

    /** Implemented by proxy plugin entry classes to activate deferred legacy registration. */
    public interface ProxyLegacyBridgeHost {
        void ensureLegacyBridgeForHook(String reason);
    }
}
