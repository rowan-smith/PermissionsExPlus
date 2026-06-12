package dev.rono.permissions.spigot.compat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Server;

/**
 * Parses Bukkit version strings and checks support for the declared Minecraft range ({@value #MIN_MC}–{@value #MAX_MC}).
 */
public final class ServerVersions {
    public static final String MIN_MC = "1.8.8";
    public static final String MAX_MC = "1.26.1";

    private static final Pattern MC_VERSION = Pattern.compile("(\\d+)\\.(\\d+)(?:\\.(\\d+))?");

    private ServerVersions() {}

    public static String minecraftVersion(Server server) {
        String bukkit = server.getBukkitVersion();
        if (bukkit == null || bukkit.isBlank()) {
            return "unknown";
        }
        int dash = bukkit.indexOf('-');
        return dash > 0 ? bukkit.substring(0, dash) : bukkit;
    }

    public static boolean isWithinSupportedRange(Server server) {
        return compare(minecraftVersion(server), MIN_MC) >= 0
                && compare(minecraftVersion(server), MAX_MC) <= 0;
    }

    public static int compare(String left, String right) {
        int[] l = parse(left);
        int[] r = parse(right);
        for (int i = 0; i < 3; i++) {
            int diff = l[i] - r[i];
            if (diff != 0) {
                return Integer.signum(diff);
            }
        }
        return 0;
    }

    private static int[] parse(String version) {
        Matcher matcher = MC_VERSION.matcher(version == null ? "" : version.trim());
        if (!matcher.find()) {
            return new int[] {0, 0, 0};
        }
        int patch = matcher.group(3) == null ? 0 : Integer.parseInt(matcher.group(3));
        return new int[] {
            Integer.parseInt(matcher.group(1)),
            Integer.parseInt(matcher.group(2)),
            patch
        };
    }
}
