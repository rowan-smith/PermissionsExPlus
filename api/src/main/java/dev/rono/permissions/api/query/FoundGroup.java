package dev.rono.permissions.api.query;

import dev.rono.permissions.api.service.PermissionServiceBridge;
import dev.rono.permissions.api.subject.Group;
import dev.rono.permissions.api.subject.GroupWorldContext;
import dev.rono.permissions.api.world.Worlds;
import java.util.NoSuchElementException;
import java.util.Optional;

/** Optional group lookup — obtain via {@link dev.rono.permissions.api.service.PermissionService#findGroup(String)}. */
public final class FoundGroup {

    private final GroupRef ref;

    FoundGroup(GroupRef ref) {
        this.ref = ref;
    }

    public static FoundGroup of(PermissionServiceBridge service, String name) {
        return new FoundGroup(SubjectRefs.group(service, name));
    }

    /** Persisted group; throws if absent from the backend. */
    public Group get() {
        return optional().orElseThrow(() -> new NoSuchElementException("Group is not persisted in the backend"));
    }

    public Optional<Group> optional() {
        return ref.find();
    }

    public Optional<GroupWorldContext> inWorld(String world) {
        return ref.findInWorld(world);
    }

    public Optional<GroupWorldContext> global() {
        return ref.findInWorld(Worlds.GLOBAL);
    }
}
