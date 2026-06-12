package dev.rono.permissions.bungee;

import dev.rono.permissions.api.service.PexPermissionService;
import dev.rono.permissions.api.subject.PexUser;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import ru.tehkode.permissions.PermissionManager;

import java.util.UUID;

/**
 * Modern static entry points for Bungee/Waterfall hook plugins.
 *
 * <p>Legacy integrations continue to use {@code PermissionManager} via
 * {@link #getPermissionManager()} or {@link ProxyPermissionServices#permissionManager()}.</p>
 *
 * <pre>{@code
 * if (PermissionsExPlus.isAvailable()) {
 *     PexPermissionService pex = PermissionsExPlus.getPermissionService();
 * }
 * }</pre>
 */
public final class PermissionsExPlus {

    private static final String PLUGIN_NAME = "PermissionsEx";

    private PermissionsExPlus() {}

    /**
     * Returns the PermissionsEx Bungee plugin instance, if loaded.
     *
     * @return the plugin, or {@code null} if PermissionsEx is not installed
     */
    public static Plugin getPlugin() {
        return ProxyServer.getInstance().getPluginManager().getPlugin(PLUGIN_NAME);
    }

    /**
     * Returns whether PermissionsEx is loaded and exposing services on this proxy.
     *
     * @return {@code true} if the plugin is enabled and services are registered
     */
    public static boolean isAvailable() {
        return getPlugin() != null && ProxyPermissionServices.isRegistered();
    }

    /**
     * Returns the registered {@link PexPermissionService}.
     *
     * @return active modern permission service; never {@code null}
     * @throws IllegalStateException if PermissionsEx is not loaded or the service is not registered
     */
    public static PexPermissionService getPermissionService() {
        if (!isAvailable()) {
            throw new IllegalStateException(
                    "PexPermissionService is not registered on this proxy — is PermissionsEx loaded and enabled?");
        }
        return ProxyPermissionServices.permissionService();
    }

    /**
     * Returns the registered legacy {@link PermissionManager} facade.
     *
     * @return active permission manager; never {@code null}
     * @throws IllegalStateException if PermissionsEx is not loaded or the manager is not registered
     */
    public static PermissionManager getPermissionManager() {
        if (!isAvailable()) {
            throw new IllegalStateException(
                    "PermissionManager is not registered on this proxy — is PermissionsEx loaded and enabled?");
        }
        return ProxyPermissionServices.permissionManager();
    }

    /**
     * Returns the permission user identified by name.
     *
     * @param name player or user name
     * @return modern user handle; never {@code null} when PermissionsEx is available
     */
    public static PexUser getUser(String name) {
        return getPermissionService().user(name);
    }

    /**
     * Returns the permission user identified by UUID.
     *
     * @param uuid player UUID
     * @return modern user handle; never {@code null} when PermissionsEx is available
     */
    public static PexUser getUser(UUID uuid) {
        return getPermissionService().user(uuid);
    }
}
