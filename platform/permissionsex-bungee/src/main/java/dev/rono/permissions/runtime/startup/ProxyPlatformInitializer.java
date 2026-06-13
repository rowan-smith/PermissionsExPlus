package dev.rono.permissions.runtime.startup;

import dev.rono.permissions.api.runtime.PlatformRuntime;
import dev.rono.permissions.bungee.BungeePermissionsExConfig;
import dev.rono.permissions.bungee.ProxyPermissionServices;
import dev.rono.permissions.bungee.backends.file.BungeeFileBackend;
import dev.rono.permissions.bungee.backends.memory.BungeeMemoryBackend;
import dev.rono.permissions.core.DefaultPermissionManager;
import ru.tehkode.permissions.backends.PermissionBackend;
import ru.tehkode.permissions.exceptions.PermissionBackendException;

import java.io.File;
import java.util.logging.Logger;

/**
 * Shared proxy bootstrap for BungeeCord and Velocity (config, backends, API registration).
 */
public final class ProxyPlatformInitializer {
    private ProxyPlatformInitializer() {}

    public record ProxyStartupResult(BungeePermissionsExConfig config, DefaultPermissionManager manager) {}

    public static ProxyStartupResult start(
            File dataDirectory, Logger logger, PlatformRuntime platformRuntime) throws PermissionBackendException {
        PermissionBackend.registerBackendAlias("memory", BungeeMemoryBackend.class);
        PermissionBackend.registerBackendAlias("file", BungeeFileBackend.class);
        var config = new BungeePermissionsExConfig(dataDirectory, logger);
        var manager = new DefaultPermissionManager(config, logger, platformRuntime);
        manager.initTimer();
        ProxyPermissionServices.register(manager.permissionsExApi(), manager);
        return new ProxyStartupResult(config, manager);
    }

    public static void shutdown(DefaultPermissionManager manager) {
        ProxyPermissionServices.unregister();
        if (manager != null) {
            manager.end();
        }
    }
}
