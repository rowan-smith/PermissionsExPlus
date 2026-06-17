package dev.rono.permissions.spigot.legacy;

import dev.rono.permissions.api.runtime.SwitchablePlatformEventBus;
import dev.rono.permissions.spigot.platform.SpigotEventPublisher;
import org.bukkit.plugin.ServicePriority;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.spigot.bukkit.SpigotPermissionsExPlugin;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Activates legacy hook compatibility (ServicesManager registration and Bukkit event translation).
 */
public final class LegacyBridgeController {

    private final SpigotPermissionsExPlugin plugin;
    private final SwitchablePlatformEventBus eventBus;
    private final Logger logger;
    private boolean active;

    public LegacyBridgeController(
            SpigotPermissionsExPlugin plugin,
            SwitchablePlatformEventBus eventBus,
            Logger logger) {
        this.plugin = plugin;
        this.eventBus = eventBus;
        this.logger = logger;
    }

    public boolean isActive() {
        return active;
    }

    public void activate(String reason) {
        if (active) {
            return;
        }
        PermissionManager manager = plugin.getPermissionsManager();
        if (manager == null) {
            logger.warning("Legacy API activation skipped — permission manager is not available");
            return;
        }

        eventBus.activate(new SpigotEventPublisher(plugin, () -> plugin.getPermissionsManager()));
        plugin.getServer().getServicesManager().register(
                PermissionManager.class,
                manager,
                plugin,
                ServicePriority.Normal);
        active = true;
        logger.log(Level.INFO, "Legacy hook API enabled ({0})", reason);
    }
}
