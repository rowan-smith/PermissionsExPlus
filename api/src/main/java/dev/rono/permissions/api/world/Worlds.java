package dev.rono.permissions.api.world;

/**
 * World context helpers for the modern API.
 *
 * <p>Classic PEX uses {@code null} for the global (all-worlds) namespace. Empty strings are treated
 * as global when passed into API methods.</p>
 */
public final class Worlds {
    private Worlds() {}

    /** {@code null} — permissions/options/inheritance that apply across all worlds unless overridden. */
    public static final String GLOBAL = null;

    public static boolean isGlobal(String world) {
        return world == null || world.isEmpty();
    }

    /** Returns {@code null} for global; otherwise the trimmed world name. */
    public static String normalize(String world) {
        if (isGlobal(world)) {
            return GLOBAL;
        }
        return world.trim();
    }

    /** Map key for {@link java.util.Map} views ({@code null} keys become {@code ""}). */
    public static String mapKey(String world) {
        return isGlobal(world) ? "" : world;
    }

    /** Restores a map key to an API world ({@code ""} becomes global {@code null}). */
    public static String fromMapKey(String key) {
        return key == null || key.isEmpty() ? GLOBAL : key;
    }
}
