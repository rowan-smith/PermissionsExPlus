package dev.rono.permissions.api.query;

import dev.rono.permissions.api.service.PermissionServiceBridge;
import dev.rono.permissions.api.subject.PexGroup;
import dev.rono.permissions.api.subject.PexGroupWorldContext;
import dev.rono.permissions.api.world.PexWorlds;
import java.util.NoSuchElementException;
import java.util.Optional;

/** Optional group lookup — obtain via {@link dev.rono.permissions.api.service.PermissionService#findGroup(String)}. */
public final class PexFoundGroup {

    private final GroupRef ref;

    PexFoundGroup(GroupRef ref) {
        this.ref = ref;
    }

    public static PexFoundGroup of(PermissionServiceBridge service, String name) {
        return new PexFoundGroup(SubjectRefs.group(service, name));
    }

    /** Persisted group; throws if absent from the backend. */
    public PexGroup get() {
        return optional().orElseThrow(() -> new NoSuchElementException("PexGroup is not persisted in the backend"));
    }

    public Optional<PexGroup> optional() {
        return ref.find();
    }

    public Optional<PexGroupWorldContext> inWorld(String world) {
        return ref.findInWorld(world);
    }

    public Optional<PexGroupWorldContext> global() {
        return ref.findInWorld(PexWorlds.GLOBAL);
    }
}
