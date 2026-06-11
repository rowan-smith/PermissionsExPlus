package dev.rono.permissions.api.fluent;

import dev.rono.permissions.api.service.PermissionService;
import dev.rono.permissions.api.subject.User;
import dev.rono.permissions.api.subject.UserWorldContext;
import dev.rono.permissions.api.world.Worlds;
import java.util.Optional;
import java.util.UUID;

/** Fluent handle for an optional user from {@link PermissionService#findUser()}. */
public final class OptionalUser {

    private final PermissionService service;
    private final UUID uuid;
    private final String identifier;

    OptionalUser(PermissionService service, UUID uuid, String identifier) {
        this.service = service;
        this.uuid = uuid;
        this.identifier = identifier;
    }

    /** Persisted user only — empty when absent from the backend. */
    public Optional<User> get() {
        return uuid != null ? service.findUser(uuid) : service.findUser(identifier);
    }

    /** World-scoped view when the user exists. */
    public Optional<UserWorldContext> inWorld(String world) {
        return get().map(user -> user.inWorld(world));
    }

    /** Global namespace view when the user exists. */
    public Optional<UserWorldContext> global() {
        return get().map(User::global);
    }

    Optional<UserWorldContext> inPresetWorld(String presetWorld) {
        return inWorld(presetWorld != null ? presetWorld : Worlds.GLOBAL);
    }
}
