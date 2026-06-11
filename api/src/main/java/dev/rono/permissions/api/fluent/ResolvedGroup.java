package dev.rono.permissions.api.fluent;

import dev.rono.permissions.api.service.PermissionService;
import dev.rono.permissions.api.subject.Group;
import dev.rono.permissions.api.subject.GroupWorldContext;
import dev.rono.permissions.api.world.Worlds;

/** Fluent handle for a group resolved via {@link PermissionService#group()}. */
public final class ResolvedGroup {

    private final PermissionService service;
    private final String name;

    ResolvedGroup(PermissionService service, String name) {
        this.service = service;
        this.name = name;
    }

    /** Materialized group. */
    public Group get() {
        return service.group(name);
    }

    /** World-scoped view for fluent checks and edits. */
    public GroupWorldContext inWorld(String world) {
        return get().inWorld(world);
    }

    /** Global namespace view. */
    public GroupWorldContext global() {
        return get().global();
    }

    GroupWorldContext inPresetWorld(String presetWorld) {
        return inWorld(presetWorld != null ? presetWorld : Worlds.GLOBAL);
    }
}
