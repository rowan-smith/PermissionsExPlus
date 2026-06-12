package dev.rono.permissions.api.query;

import dev.rono.permissions.api.service.PermissionServiceBridge;
import dev.rono.permissions.api.subject.PexUser;
import dev.rono.permissions.api.subject.PexUserWorldContext;
import dev.rono.permissions.api.world.PexWorlds;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

/** Optional user lookup — obtain via {@link dev.rono.permissions.api.service.PermissionService#findUser(String)}. */
public final class PexFoundUser {

    private final UserRef ref;

    PexFoundUser(UserRef ref) {
        this.ref = ref;
    }

    public static PexFoundUser of(PermissionServiceBridge service, UUID uuid, String identifier) {
        return new PexFoundUser(SubjectRefs.user(service, uuid, identifier));
    }

    /** Persisted user; throws if absent from the backend. */
    public PexUser get() {
        return optional().orElseThrow(() -> new NoSuchElementException("PexUser is not persisted in the backend"));
    }

    public Optional<PexUser> optional() {
        return ref.find();
    }

    public Optional<PexUserWorldContext> inWorld(String world) {
        return ref.findInWorld(world);
    }

    public Optional<PexUserWorldContext> global() {
        return ref.findInWorld(PexWorlds.GLOBAL);
    }
}
