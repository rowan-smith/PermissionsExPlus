package dev.rono.permissions.api.fluent;

import dev.rono.permissions.api.service.PermissionService;
import dev.rono.permissions.api.subject.User;
import dev.rono.permissions.api.subject.UserWorldContext;
import dev.rono.permissions.api.world.Worlds;
import java.util.UUID;

/** Fluent handle for a user resolved via {@link PermissionService#user()}. */
public final class ResolvedUser {

    private final PermissionService service;
    private final UUID uuid;
    private final String identifier;

    ResolvedUser(PermissionService service, UUID uuid, String identifier) {
        this.service = service;
        this.uuid = uuid;
        this.identifier = identifier;
    }

    /** Materialized user (classic {@code getUser} semantics). */
    public User get() {
        return uuid != null ? service.user(uuid) : service.user(identifier);
    }

    /** World-scoped view for fluent checks and edits. */
    public UserWorldContext inWorld(String world) {
        return get().inWorld(world);
    }

    /** Global namespace view. */
    public UserWorldContext global() {
        return get().global();
    }

    /** When this reference was created from a {@link WorldQuery}, returns that world's context. */
    UserWorldContext inPresetWorld(String presetWorld) {
        return inWorld(presetWorld != null ? presetWorld : Worlds.GLOBAL);
    }
}
