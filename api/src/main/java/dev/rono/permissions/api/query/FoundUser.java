package dev.rono.permissions.api.query;

import dev.rono.permissions.api.service.PermissionServiceBridge;
import dev.rono.permissions.api.subject.User;
import dev.rono.permissions.api.subject.UserWorldContext;
import dev.rono.permissions.api.world.Worlds;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

/** Optional user lookup — obtain via {@link dev.rono.permissions.api.service.PermissionService#findUser(String)}. */
public final class FoundUser {

    private final UserRef ref;

    FoundUser(UserRef ref) {
        this.ref = ref;
    }

    public static FoundUser of(PermissionServiceBridge service, UUID uuid, String identifier) {
        return new FoundUser(SubjectRefs.user(service, uuid, identifier));
    }

    /** Persisted user; throws if absent from the backend. */
    public User get() {
        return optional().orElseThrow(() -> new NoSuchElementException("User is not persisted in the backend"));
    }

    public Optional<User> optional() {
        return ref.find();
    }

    public Optional<UserWorldContext> inWorld(String world) {
        return ref.findInWorld(world);
    }

    public Optional<UserWorldContext> global() {
        return ref.findInWorld(Worlds.GLOBAL);
    }
}
