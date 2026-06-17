package dev.rono.permissions.runtime.startup;

import dev.rono.permissions.api.PermissionsExApi;
import dev.rono.permissions.bootstrap.PlatformDescriptor;
import dev.rono.permissions.bootstrap.PlatformFamily;
import dev.rono.permissions.core.InternalPermissionManager;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.spigot.bukkit.SpigotPermissionsExPlugin;

import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Logger;

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
        PlatformDescriptor desc = describePlatform(plugin.getServer());
        log.info(PREFIX + "Runtime: " + desc.runtimeBannerLine());
        log.info(PREFIX + "Platform adapter: "
                + InternalPermissionManager.require(manager).getPlatform().getClass().getSimpleName());
        log.info(PREFIX + "Core engine: started");
        logModernApi(plugin, log);
        log.info(PREFIX + "Context resolvers: world, server, static");
        log.info(PREFIX + "Storage: " + manager.getBackend().diagnosticLabel());
        logConsumerScan(plugin, log);
    }

    private static void logModernApi(SpigotPermissionsExPlugin plugin, Logger log) {
        RegisteredServiceProvider<PermissionsExApi> reg =
                plugin.getServer().getServicesManager().getRegistration(PermissionsExApi.class);
        if (reg != null) {
            log.info(PREFIX + "API: PermissionsExApi registered via ServicesManager");
        } else {
            log.warning(PREFIX + "API: PermissionsExApi registration missing or superseded");
        }
    }

    private static PlatformDescriptor describePlatform(Server server) {
        String impl = server.getName();
        String mc = resolveMinecraftVersion(server);
        String vendor = tryPaperVendorDetails(server);
        return new PlatformDescriptor(impl, mc, PlatformFamily.BUKKIT, vendor);
    }

    private static String tryPaperVendorDetails(Server server) {
        try {
            Class<?> probe = Class.forName("dev.rono.permissions.paper.PaperPlatformProbe");
            var method = probe.getMethod("tryVendorDetails", Server.class);
            Object details = method.invoke(null, server);
            return details instanceof String s && !s.isBlank() ? s : null;
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
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
