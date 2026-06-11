package dev.rono.permissions.api.fluent;

import dev.rono.permissions.api.service.PermissionService;
import dev.rono.permissions.api.subject.GroupWorldContext;

/**
 * Fluent group lookup — obtain via {@link PermissionService#group()} or {@link WorldQuery#group()}.
 *
 * <pre>{@code
 * pex.group().named("vip").inWorld(world).members(true);
 * pex.world(world).group().named("vip").members();
 * }</pre>
 */
public final class GroupQuery {

    private final PermissionService service;
    private final String presetWorld;

    GroupQuery(PermissionService service, String presetWorld) {
        this.service = service;
        this.presetWorld = presetWorld;
    }

    public static GroupQuery resolve(PermissionService service) {
        return new GroupQuery(service, null);
    }

    public static GroupQuery inWorld(PermissionService service, String world) {
        return new GroupQuery(service, world);
    }

    public ResolvedGroup named(String name) {
        return new ResolvedGroup(service, name);
    }

    /** Alias for {@link #named(String)}. */
    public ResolvedGroup by(String name) {
        return named(name);
    }

    /** When scoped to a world, returns the world context directly. */
    public GroupWorldContext byWorld(String name) {
        return presetWorld != null
                ? new ResolvedGroup(service, name).inPresetWorld(presetWorld)
                : new ResolvedGroup(service, name).global();
    }
}
