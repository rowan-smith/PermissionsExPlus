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

    /**
     * Reports whether a world argument denotes the global namespace.
     *
     * @param world world name, or {@code null} for global
     * @return {@code true} when {@code world} is {@code null} or empty
     */
    public static boolean isGlobal(String world) {
        return world == null || world.isEmpty();
    }

    /**
     * Normalizes a world argument for API use.
     *
     * <p>Global inputs ({@code null} or blank) become {@link #GLOBAL}; other values are trimmed.</p>
     *
     * @param world world name as supplied by callers
     * @return {@code null} for global, otherwise the trimmed world name
     */
    public static String normalize(String world) {
        if (world != null) {
            world = world.trim();
        }
        if (isGlobal(world)) {
            return GLOBAL;
        }
        return world;
    }

    /**
     * Converts an API world value to a safe {@link java.util.Map} key.
     *
     * <p>{@code null} global worlds become {@code ""} because map keys cannot be {@code null}.</p>
     *
     * @param world world name, or {@code null} for global
     * @return {@code ""} for global, otherwise the world name unchanged
     */
    public static String mapKey(String world) {
        return isGlobal(world) ? "" : world;
    }

    /**
     * Restores a map key to an API world value.
     *
     * <p>{@code null} or empty keys become {@link #GLOBAL}.</p>
     *
     * @param key map key produced by {@link #mapKey(String)} or equivalent
     * @return {@code null} for global, otherwise the original world name
     */
    public static String fromMapKey(String key) {
        return key == null || key.isEmpty() ? GLOBAL : key;
    }
}
