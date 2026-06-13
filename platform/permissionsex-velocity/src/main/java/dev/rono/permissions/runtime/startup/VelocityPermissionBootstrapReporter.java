package dev.rono.permissions.runtime.startup;

import com.velocitypowered.api.proxy.ProxyServer;
import dev.rono.permissions.bootstrap.PlatformDescriptor;
import dev.rono.permissions.bootstrap.PlatformFamily;
import dev.rono.permissions.core.InternalPermissionManager;
import dev.rono.permissions.velocity.VelocityPermissionsExPlugin;
import org.slf4j.Logger;
import ru.tehkode.permissions.PermissionManager;

/**
 * Velocity startup banner (mirrors {@link BungeePermissionBootstrapReporter}).
 */
public final class VelocityPermissionBootstrapReporter {
    private static final String PREFIX = "[Permissions] ";

    private VelocityPermissionBootstrapReporter() {}

    public static void log(VelocityPermissionsExPlugin plugin, PermissionManager manager, Logger logger) {
        PlatformDescriptor desc = describe(plugin.server());
        logger.info(PREFIX + "Runtime: " + desc.runtimeBannerLine());
        logger.info(PREFIX + "Platform adapter: "
                + InternalPermissionManager.require(manager).getPlatform().getClass().getSimpleName());
        logger.info(PREFIX + "Core engine: started");
        logger.info(PREFIX + "API: modern v2 (PermissionsExApi via PermissionsEx.getApi())");
        logger.info(PREFIX + "API: legacy v1 compatibility enabled");
        logger.info(PREFIX + "Context resolvers: server, static");
        logger.info(PREFIX + "Storage: " + manager.getBackend().diagnosticLabel());
        logger.info(PREFIX + "Proxy plugins loaded: " + plugin.server().getPluginManager().getPlugins().size());
    }

    private static PlatformDescriptor describe(ProxyServer server) {
        String version = server.getVersion().getVersion();
        if (version == null || version.isBlank()) {
            version = "unknown";
        }
        return new PlatformDescriptor("Velocity", version, PlatformFamily.VELOCITY, null);
    }
}
