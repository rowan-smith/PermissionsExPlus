package dev.rono.permissions.api.permission;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Immutable platform-neutral permission scope.
 *
 * <p>Replaces raw {@code Map<String, String>} and Bukkit-centric {@code String world} parameters in the
 * modern API. The engine resolves checks through {@link dev.rono.permissions.api.runtime.ContextResolver}
 * inheritance chains; it never assumes a world dimension exists.</p>
 */
public interface PermissionContext {

    String SERVER = "server";

    String WORLD = "world";

    String DIMENSION = "dimension";

    String REGION = "region";

    String GAMEMODE = "gamemode";

    /** Optional state scope (for example {@code "event"} during minigames). */
    String STATE = "state";

    /** @return immutable attribute map for this scope */
    Map<String, String> attributes();

    /**
     * Returns the attribute value for {@code key}, if present and non-empty.
     *
     * @param key context key (for example {@link #WORLD})
     * @return optional attribute value
     */
    default Optional<String> get(String key) {
        return Optional.ofNullable(attributes().get(key)).filter(v -> !v.isEmpty());
    }

    /** @return global (empty) permission scope */
    static PermissionContext global() {
        return PermissionContextImpl.EMPTY;
    }

    /**
     * Builds a context from attribute map entries.
     *
     * @param attributes scope attributes; {@code null} or empty yields {@link #global()}
     * @return immutable context
     */
    static PermissionContext of(Map<String, String> attributes) {
        return PermissionContextImpl.of(attributes);
    }

    /** @param server backend or logical server id */
    static PermissionContext server(String server) {
        if (server == null || server.isEmpty()) {
            return global();
        }
        return of(Map.of(SERVER, server));
    }

    /** @param world loaded world or realm name */
    static PermissionContext world(String world) {
        if (world == null || world.isEmpty()) {
            return global();
        }
        return of(Map.of(WORLD, world));
    }

    /** @param server logical server id */
    static PermissionContext world(String server, String world) {
        return of(Map.of(SERVER, server, WORLD, world));
    }

    /**
     * Builds a context with the common world/server/region/gamemode keys.
     *
     * @param world    realm or world name
     * @param server   server name (proxy) or logical server id
     * @param region   region name
     * @param gamemode gamemode name
     * @return immutable context
     */
    static PermissionContext of(String world, String server, String region, String gamemode) {
        Map<String, String> attrs = new HashMap<>(4);
        putIfPresent(attrs, WORLD, world);
        putIfPresent(attrs, SERVER, server);
        putIfPresent(attrs, REGION, region);
        putIfPresent(attrs, GAMEMODE, gamemode);
        return of(attrs);
    }

    /**
     * Builds a context with world and optional {@link #STATE}.
     *
     * @param world world or realm name
     * @param state optional state value
     * @return immutable context
     */
    static PermissionContext withState(String world, String state) {
        Map<String, String> attrs = new HashMap<>(2);
        putIfPresent(attrs, WORLD, world);
        putIfPresent(attrs, STATE, state);
        return of(attrs);
    }

    /**
     * Converts a legacy holder context map to {@link PermissionContext}.
     *
     * @param legacy context map from holder APIs; {@code null} yields global
     * @return immutable context
     */
    static PermissionContext fromMap(Map<String, String> legacy) {
        return of(legacy);
    }

    /**
     * Returns a legacy map view for holder interop.
     *
     * @return immutable attribute map
     */
    default Map<String, String> toMap() {
        return attributes();
    }

    /** @return {@code true} when this context represents global scope */
    default boolean isGlobal() {
        return attributes().isEmpty();
    }

    private static void putIfPresent(Map<String, String> attrs, String key, String value) {
        if (value != null && !value.isEmpty()) {
            attrs.put(key, value);
        }
    }
}
