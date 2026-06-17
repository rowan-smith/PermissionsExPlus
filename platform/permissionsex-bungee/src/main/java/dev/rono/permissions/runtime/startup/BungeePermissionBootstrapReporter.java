package dev.rono.permissions.runtime.startup;

import dev.rono.permissions.bootstrap.PlatformDescriptor;
import dev.rono.permissions.bootstrap.PlatformFamily;
import dev.rono.permissions.bungee.BungeePermissionsExPlugin;
import dev.rono.permissions.core.InternalPermissionManager;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import ru.tehkode.permissions.PermissionManager;

import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Proxy-side variant of the startup banner (no Bukkit {@code ServicesManager}).
 */
public final class BungeePermissionBootstrapReporter {
    private static final String PREFIX = "[Permissions] ";
    private static final Set<String> KNOWN_INTEGRATIONS = Set.of(
            "vault",
            "placeholderapi",
            "luckperms",
            "skinsrestorer",
            "plan",
            "slimeworldmanager");

    private BungeePermissionBootstrapReporter() {}

    public static void log(BungeePermissionsExPlugin plugin, PermissionManager manager, boolean legacyApiActive) {
        Logger log = plugin.getLogger();
        PlatformDescriptor desc = describe(plugin.getProxy());
        log.info(PREFIX + "Runtime: " + desc.runtimeBannerLine());
        log.info(PREFIX + "Platform adapter: "
                + InternalPermissionManager.require(manager).getPlatform().getClass().getSimpleName());
        log.info(PREFIX + "Core engine: started");
        log.info(PREFIX + "API: modern v2 (PermissionsExApi via PermissionsEx.getApi())");
        if (legacyApiActive) {
            log.info(PREFIX + "API: legacy v1 compatibility enabled");
        } else {
            log.info(PREFIX + "API: legacy v1 compatibility deferred (no hook plugins detected)");
        }
        log.info(PREFIX + "Context resolvers: server, static");
        log.info(PREFIX + "Storage: " + manager.getBackend().diagnosticLabel());
        logConsumerScan(plugin, log);
    }

    private static PlatformDescriptor describe(ProxyServer proxy) {
        String kind = proxySoftwareLabel(proxy);
        String ver = resolveProxyVersion(proxy);
        return new PlatformDescriptor(kind, ver, PlatformFamily.BUNGEECORD, null);
    }

    private static String proxySoftwareLabel(ProxyServer proxy) {
        String cn = proxy.getClass().getName().toLowerCase(Locale.ROOT);
        if (cn.contains("waterfall")) {
            return "Waterfall";
        }
        if (cn.contains("velocity")) {
            return "Velocity";
        }
        return "BungeeCord";
    }

    private static String resolveProxyVersion(ProxyServer proxy) {
        String v = tryInvokeString(proxy, "getGameVersion");
        if (!v.isBlank()) {
            return v;
        }
        v = tryInvokeString(proxy, "getVersion");
        if (!v.isBlank()) {
            return v;
        }
        return "unknown";
    }

    private static String tryInvokeString(Object target, String method) {
        try {
            Method m = target.getClass().getMethod(method);
            Object o = m.invoke(target);
            if (o instanceof String s) {
                return s;
            }
        } catch (ReflectiveOperationException ignored) {
        }
        return "";
    }

    private static void logConsumerScan(BungeePermissionsExPlugin self, Logger log) {
        var plugins = self.getProxy().getPluginManager().getPlugins();
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
        log.info(PREFIX + "Consumer scan: " + plugins.size() + " plugins loaded, " + knownHits
                + " known integrations found");
    }
}
