package dev.rono.permissions.runtime.legacy;

import dev.rono.permissions.bungee.ProxyPermissionServices;
import dev.rono.permissions.core.DefaultPermissionManager;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Activates legacy hook compatibility on proxy runtimes.
 */
public final class ProxyLegacyBridgeController {

    private final DefaultPermissionManager manager;
    private boolean active;

    public ProxyLegacyBridgeController(DefaultPermissionManager manager) {
        this.manager = manager;
    }

    public boolean isActive() {
        return active;
    }

    public void activate(String reason, Logger logger) {
        if (active) {
            return;
        }
        ProxyPermissionServices.activateLegacy(manager);
        active = true;
        logger.log(Level.INFO, "Legacy hook API enabled ({0})", reason);
    }
}
