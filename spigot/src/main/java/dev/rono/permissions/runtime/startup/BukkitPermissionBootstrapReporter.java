package dev.rono.permissions.runtime.startup;

import dev.rono.permissions.api.service.PermissionService;
import dev.rono.permissions.bootstrap.PlatformDescriptor;
import dev.rono.permissions.bootstrap.PlatformFamily;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Logger;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import dev.rono.permissions.core.InternalPermissionManager;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.spigot.bukkit.SpigotPermissionsExPlugin;

/**
 * Emits structured {@code [Permissions]} lines once the core engine, services and compatibility shims are up.
 */
public final class BukkitPermissionBootstrapReporter {
    private static final String PREFIX = "[Permissions] ";
    /** Recognized dependents often seen in the wild (lowercase ids). */
    private static final Set<String> KNOWN_INTEGRATIONS = Set.of(
            "vault",
            "placeholderapi",
            "worldedit",
            "worldguard",
            "multiverse-core",
            "multiverse-netherportals",
            "multiverse-inventories",
            "essentials",
            "essentialsx");

    private BukkitPermissionBootstrapReporter() {}

    public static void log(SpigotPermissionsExPlugin plugin, PermissionManager manager) {
        Logger log = plugin.getLogger();
        PlatformDescriptor desc = describe(plugin.getServer());
        log.info(PREFIX + "Runtime: " + desc.runtimeBannerLine());
        log.info(PREFIX + "Platform adapter: "
                + InternalPermissionManager.require(manager).getPlatform().getClass().getSimpleName());
        log.info(PREFIX + "Core engine: started");
        logModernApi(plugin, manager, log);
        log.info(PREFIX + "API: legacy v1 compatibility enabled");
        log.info(PREFIX + "Context resolvers: world, server, static");
        log.info(PREFIX + "Storage: " + manager.getBackend().diagnosticLabel());
        logConsumerScan(plugin, log);
    }

    private static void logModernApi(SpigotPermissionsExPlugin plugin, PermissionManager manager, Logger log) {
        RegisteredServiceProvider<PermissionService> reg =
                plugin.getServer().getServicesManager().getRegistration(PermissionService.class);
        if (reg != null && reg.getProvider() == manager) {
            log.info(PREFIX + "API: modern v2 registered via ServicesManager");
        } else {
            log.warning(PREFIX + "API: modern v2 PermissionService registration missing or superseded");
        }
    }

    private static PlatformDescriptor describe(Server server) {
        String impl = server.getName();
        String mc = resolveMinecraftVersion(server);
        String vendor = tryPaperVendorDetails(server);
        return new PlatformDescriptor(impl, mc, PlatformFamily.BUKKIT, vendor);
    }

    private static String resolveMinecraftVersion(Server server) {
        try {
            Method m = server.getClass().getMethod("getMinecraftVersion");
            Object v = m.invoke(server);
            if (v instanceof String s && !s.isBlank()) {
                return s;
            }
        } catch (ReflectiveOperationException ignored) {
        }
        return stripBukkitVersion(server.getBukkitVersion());
    }

    private static String stripBukkitVersion(String bukkitVersion) {
        if (bukkitVersion == null || bukkitVersion.isBlank()) {
            return "unknown";
        }
        int dash = bukkitVersion.indexOf('-');
        return dash > 0 ? bukkitVersion.substring(0, dash) : bukkitVersion;
    }

    private static String tryPaperVendorDetails(Server server) {
        try {
            Class<?> buildInfo = Class.forName("io.papermc.paper.ServerBuildInfo");
            Method buildInfoMethod = buildInfo.getMethod("buildInfo");
            Object info = buildInfoMethod.invoke(null);
            if (info == null) {
                return null;
            }
            Method mcVer = info.getClass().getMethod("minecraftVersionId");
            Object mc = mcVer.invoke(info);
            Method build = info.getClass().getMethod("buildNumber");
            Object num = build.invoke(info);
            if (mc instanceof String mcs && num != null) {
                return "Paper build " + num + " (" + mcs + ")";
            }
        } catch (ReflectiveOperationException | ClassCastException ignored) {
        }
        return null;
    }

    private static void logConsumerScan(SpigotPermissionsExPlugin self, Logger log) {
        Plugin[] plugins = self.getServer().getPluginManager().getPlugins();
        int knownHits = 0;
        for (Plugin p : plugins) {
            if (p == self) {
                continue;
            }
            String id = p.getDescription().getName().toLowerCase(Locale.ROOT);
            if (KNOWN_INTEGRATIONS.contains(id)) {
                knownHits++;
            }
        }
        log.info(PREFIX + "Consumer scan: " + plugins.length + " plugins loaded, " + knownHits
                + " known integrations found");
    }

}
