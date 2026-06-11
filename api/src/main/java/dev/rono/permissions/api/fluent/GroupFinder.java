package dev.rono.permissions.api.fluent;

import dev.rono.permissions.api.service.PermissionService;
import dev.rono.permissions.api.subject.GroupWorldContext;
import java.util.Optional;

/** Fluent optional group lookup — obtain via {@link PermissionService#findGroup()}. */
public final class GroupFinder {

    private final PermissionService service;
    private final String presetWorld;

    GroupFinder(PermissionService service, String presetWorld) {
        this.service = service;
        this.presetWorld = presetWorld;
    }

    public static GroupFinder of(PermissionService service) {
        return new GroupFinder(service, null);
    }

    public static GroupFinder inWorld(PermissionService service, String world) {
        return new GroupFinder(service, world);
    }

    public OptionalGroup named(String name) {
        return new OptionalGroup(service, name);
    }

    public OptionalGroup by(String name) {
        return named(name);
    }

    public Optional<GroupWorldContext> byWorld(String name) {
        return presetWorld != null
                ? new OptionalGroup(service, name).inPresetWorld(presetWorld)
                : new OptionalGroup(service, name).global();
    }
}
