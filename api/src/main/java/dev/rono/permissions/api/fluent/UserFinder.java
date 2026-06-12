package dev.rono.permissions.api.fluent;

import dev.rono.permissions.api.service.PermissionService;
import dev.rono.permissions.api.subject.UserWorldContext;
import java.util.Optional;
import java.util.UUID;

/**
 * Fluent optional user lookup — obtain via {@link PermissionService#findUser()} or
 * {@link WorldQuery#findUser()}.
 */
public final class UserFinder {

    private final PermissionService service;
    private final String presetWorld;

    UserFinder(PermissionService service, String presetWorld) {
        this.service = service;
        this.presetWorld = presetWorld;
    }

    public static UserFinder of(PermissionService service) {
        return new UserFinder(service, null);
    }

    public static UserFinder inWorld(PermissionService service, String world) {
        return new UserFinder(service, world);
    }

    public OptionalUser by(UUID uuid) {
        return new OptionalUser(service, uuid, null);
    }

    public OptionalUser by(String identifier) {
        return new OptionalUser(service, null, identifier);
    }

    public OptionalUser uuid(UUID uuid) {
        return by(uuid);
    }

    public OptionalUser named(String identifier) {
        return by(identifier);
    }

    /** When scoped to a world, returns the world context when the user exists. */
    public Optional<UserWorldContext> byWorld(UUID uuid) {
        return presetWorld != null
                ? new OptionalUser(service, uuid, null).inPresetWorld(presetWorld)
                : new OptionalUser(service, uuid, null).global();
    }

    public Optional<UserWorldContext> byWorld(String identifier) {
        return presetWorld != null
                ? new OptionalUser(service, null, identifier).inPresetWorld(presetWorld)
                : new OptionalUser(service, null, identifier).global();
    }
}
