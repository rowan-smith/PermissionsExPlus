package dev.rono.permissions.runtime.startup;

import dev.rono.permissions.bootstrap.PlatformDescriptor;
import dev.rono.permissions.bootstrap.PlatformFamily;
import dev.rono.permissions.core.InternalPermissionManager;
import dev.rono.permissions.sponge.SpongePermissionsExPlugin;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Server;
import ru.tehkode.permissions.PermissionManager;

/**
 * Sponge startup banner aligned with other platform reporters.
 */
public final class SpongePermissionBootstrapReporter {
    private static final String PREFIX = "[Permissions] ";

    private SpongePermissionBootstrapReporter() {}

    public static void log(SpongePermissionsExPlugin plugin, PermissionManager manager, Server server, Logger logger) {
        PlatformDescriptor desc = describe(server.game().platform());
        logger.info(PREFIX + "Runtime: " + desc.runtimeBannerLine());
        logger.info(PREFIX + "Platform adapter: "
                + InternalPermissionManager.require(manager).getPlatform().getClass().getSimpleName());
        logger.info(PREFIX + "Core engine: started");
        logger.info(PREFIX + "API: modern v2 (PermissionsExApi via PermissionsEx.getApi())");
        logger.info(PREFIX + "API: legacy v1 compatibility enabled");
        logger.info(PREFIX + "Context resolvers: world, static");
        logger.info(PREFIX + "Storage: " + manager.getBackend().diagnosticLabel());
        logger.info(PREFIX + "Plugins loaded: " + server.game().pluginManager().plugins().size());
    }

    private static PlatformDescriptor describe(Platform platform) {
        String name = platform.type().name();
        String version = platform.minecraftVersion().name();
        return new PlatformDescriptor(name, version, PlatformFamily.SPONGE, null);
    }
}
