package dev.rono.permissions.api.realm;

/**
 * Realm context helpers for the modern API.
 *
 * <p>Classic PEX uses {@code null} for the global (all-realms) namespace. Empty strings are treated
 * as global when passed into API methods.</p>
 */
public final class Realms {
    private Realms() {}

    /** {@code null} — permissions/options/inheritance that apply across all realms unless overridden. */
    public static final String GLOBAL = null;

    /**
     * Reports whether a realm argument denotes the global namespace.
     *
     * @param realm realm name, or {@code null} for global
     * @return {@code true} when {@code realm} is {@code null} or empty
     */
    public static boolean isGlobal(String realm) {
        return realm == null || realm.isEmpty();
    }

    /**
     * Normalizes a realm argument for API use.
     *
     * <p>Global inputs ({@code null} or blank) become {@link #GLOBAL}; other values are trimmed.</p>
     *
     * @param realm realm name as supplied by callers
     * @return {@code null} for global, otherwise the trimmed realm name
     */
    public static String normalize(String realm) {
        if (realm != null) {
            realm = realm.trim();
        }
        if (isGlobal(realm)) {
            return GLOBAL;
        }
        return realm;
    }

    /**
     * Converts an API realm value to a safe {@link java.util.Map} key.
     *
     * <p>{@code null} global realms become {@code ""} because map keys cannot be {@code null}.</p>
     *
     * @param realm realm name, or {@code null} for global
     * @return {@code ""} for global, otherwise the realm name unchanged
     */
    public static String mapKey(String realm) {
        return isGlobal(realm) ? "" : realm;
    }

    /**
     * Restores a map key to an API realm value.
     *
     * <p>{@code null} or empty keys become {@link #GLOBAL}.</p>
     *
     * @param key map key produced by {@link #mapKey(String)} or equivalent
     * @return {@code null} for global, otherwise the original realm name
     */
    public static String fromMapKey(String key) {
        return key == null || key.isEmpty() ? GLOBAL : key;
    }
}
