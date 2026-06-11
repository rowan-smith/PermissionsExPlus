package dev.rono.permissions.api.fluent;

import dev.rono.permissions.api.service.PermissionService;
import java.util.Optional;

/**
 * Optional world scope — obtain via {@link PermissionService#findWorld(String)}.
 *
 * <p>Returns a {@link WorldQuery} only when the world is known to the platform adapter.</p>
 */
public final class WorldFinder {

    private final PermissionService service;
    private final String world;

    WorldFinder(PermissionService service, String world) {
        this.service = service;
        this.world = world;
    }

    public static WorldFinder of(PermissionService service, String world) {
        return new WorldFinder(service, world);
    }

    /** World scope when the realm is registered; empty otherwise. */
    public Optional<WorldQuery> orEmpty() {
        return service.worlds().contains(world) ? Optional.of(WorldQuery.of(service, world)) : Optional.empty();
    }

    /** World scope regardless of platform registration (any namespace may hold PEX data). */
    public WorldQuery orAny() {
        return WorldQuery.of(service, world);
    }
}
