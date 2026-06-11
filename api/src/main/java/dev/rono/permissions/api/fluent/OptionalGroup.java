package dev.rono.permissions.api.fluent;

import dev.rono.permissions.api.service.PermissionService;
import dev.rono.permissions.api.subject.Group;
import dev.rono.permissions.api.subject.GroupWorldContext;
import dev.rono.permissions.api.world.Worlds;
import java.util.Optional;

/** Fluent handle for an optional group from {@link PermissionService#findGroup()}. */
public final class OptionalGroup {

    private final PermissionService service;
    private final String name;

    OptionalGroup(PermissionService service, String name) {
        this.service = service;
        this.name = name;
    }

    /** Persisted group only — empty when absent from the backend. */
    public Optional<Group> get() {
        return service.findGroup(name);
    }

    /** World-scoped view when the group exists. */
    public Optional<GroupWorldContext> inWorld(String world) {
        return get().map(group -> group.inWorld(world));
    }

    /** Global namespace view when the group exists. */
    public Optional<GroupWorldContext> global() {
        return get().map(Group::global);
    }

    Optional<GroupWorldContext> inPresetWorld(String presetWorld) {
        return inWorld(presetWorld != null ? presetWorld : Worlds.GLOBAL);
    }
}
