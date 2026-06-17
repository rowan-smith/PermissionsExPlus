package dev.rono.permissions.paper;

import org.bukkit.Server;

/**
 * Paper-only runtime probes kept out of the generic Spigot module.
 */
public final class PaperPlatformProbe {
    private PaperPlatformProbe() {}

    /**
     * Returns Paper build metadata when {@code io.papermc.paper.ServerBuildInfo} is available.
     */
    public static String tryVendorDetails(Server server) {
        try {
            Class<?> buildInfo = Class.forName("io.papermc.paper.ServerBuildInfo");
            var buildInfoMethod = buildInfo.getMethod("buildInfo");
            Object info = buildInfoMethod.invoke(null);
            if (info == null) {
                return null;
            }
            var mcVer = info.getClass().getMethod("minecraftVersionId");
            Object mc = mcVer.invoke(info);
            var build = info.getClass().getMethod("buildNumber");
            Object num = build.invoke(info);
            if (mc instanceof String mcs && num != null) {
                return "Paper build " + num + " (" + mcs + ")";
            }
        } catch (ReflectiveOperationException | ClassCastException ignored) {
        }
        return null;
    }
}
