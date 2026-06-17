package dev.rono.permissions.runtime.startup;

import dev.rono.permissions.api.runtime.PlatformRuntime;
import dev.rono.permissions.bungee.BungeePermissionsExConfig;
import dev.rono.permissions.bungee.ProxyPermissionServices;
import dev.rono.permissions.bungee.backends.memory.BungeeMemoryBackend;
import dev.rono.permissions.core.DefaultPermissionManager;
import dev.rono.permissions.runtime.legacy.ProxyLegacyBridgeController;
import ru.tehkode.permissions.backends.PermissionBackend;
import ru.tehkode.permissions.exceptions.PermissionBackendException;

import java.io.File;
import java.util.logging.Logger;

/**
 * Shared proxy bootstrap for BungeeCord and Velocity (config, backends, API registration).
 */
public final class ProxyPlatformInitializer {
    private ProxyPlatformInitializer() {}

    public record ProxyStartupResult(
            BungeePermissionsExConfig config,
            DefaultPermissionManager manager,
            ProxyLegacyBridgeController legacyBridge) {}

    public static ProxyStartupResult start(
            File dataDirectory, Logger logger, PlatformRuntime platformRuntime) throws PermissionBackendException {
        PermissionBackend.registerBackendAlias("memory", BungeeMemoryBackend.class);
        var config = new BungeePermissionsExConfig(dataDirectory, logger);
        var manager = new DefaultPermissionManager(config, logger, platformRuntime);
        manager.initTimer();
        ProxyPermissionServices.registerModern(manager.permissionsExApi());
        var legacyBridge = new ProxyLegacyBridgeController(manager);
        return new ProxyStartupResult(config, manager, legacyBridge);
    }

    public static void shutdown(DefaultPermissionManager manager) {
        ProxyPermissionServices.unregister();
        if (manager != null) {
            manager.end();
        }
    }
}
