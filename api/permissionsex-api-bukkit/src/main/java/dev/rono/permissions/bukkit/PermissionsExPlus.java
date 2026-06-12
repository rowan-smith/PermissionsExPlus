package dev.rono.permissions.bukkit;

import dev.rono.permissions.api.service.PexPermissionService;
import dev.rono.permissions.api.subject.PexUser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.UUID;

/**
 * Modern static entry points for hook plugins.
 *
 * <p>Resolves {@link PexPermissionService} from Bukkit {@code ServicesManager}. Legacy integrations
 * continue to use {@link ru.tehkode.permissions.bukkit.PermissionsEx#getPermissionManager()}.</p>
 *
 * <pre>{@code
 * if (PermissionsExPlus.isAvailable()) {
 *     PexPermissionService pex = PermissionsExPlus.getPermissionService();
 *     PexUser user = PermissionsExPlus.getUser(player);
 * }
 * }</pre>
 */
public final class PermissionsExPlus {

    private static final String PLUGIN_NAME = "PermissionsEx";

    private PermissionsExPlus() {}

    /**
     * Returns the PermissionsEx Bukkit plugin instance, if loaded.
     *
     * @return the plugin, or {@code null} if PermissionsEx is not installed
     */
    public static Plugin getPlugin() {
        return Bukkit.getPluginManager().getPlugin(PLUGIN_NAME);
    }

    /**
     * Returns whether PermissionsEx is loaded, enabled, and exposing a {@link PexPermissionService}.
     *
     * @return {@code true} if the plugin and its modern service provider are available
     */
    public static boolean isAvailable() {
        Plugin plugin = getPlugin();
        if (plugin == null || !plugin.isEnabled()) {
            return false;
        }
        RegisteredServiceProvider<PexPermissionService> reg =
                Bukkit.getServer().getServicesManager().getRegistration(PexPermissionService.class);
        return reg != null && reg.getProvider() != null;
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
                    "PexPermissionService is not registered — is PermissionsEx loaded and enabled?");
        }
        RegisteredServiceProvider<PexPermissionService> reg =
                Bukkit.getServer().getServicesManager().getRegistration(PexPermissionService.class);
        return reg.getProvider();
    }

    /**
     * Returns the permission user associated with an online player.
     *
     * @param player online player; must not be {@code null}
     * @return modern user handle; never {@code null} when PermissionsEx is available
     * @throws IllegalStateException if PermissionsEx is not loaded or the service is not registered
     */
    public static PexUser getUser(Player player) {
        return getPermissionService().user(player.getUniqueId());
    }

    /**
     * Returns the permission user identified by name.
     *
     * @param name player or user name
     * @return modern user handle; never {@code null} when PermissionsEx is available
     * @throws IllegalStateException if PermissionsEx is not loaded or the service is not registered
     */
    public static PexUser getUser(String name) {
        return getPermissionService().user(name);
    }

    /**
     * Returns the permission user identified by UUID.
     *
     * @param uuid player UUID
     * @return modern user handle; never {@code null} when PermissionsEx is available
     * @throws IllegalStateException if PermissionsEx is not loaded or the service is not registered
     */
    public static PexUser getUser(UUID uuid) {
        return getPermissionService().user(uuid);
    }
}
