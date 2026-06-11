package dev.rono.permissions.api.query;

import dev.rono.permissions.api.service.PermissionService;
import dev.rono.permissions.api.subject.Group;
import dev.rono.permissions.api.subject.GroupWorldContext;
import java.util.Optional;
import java.util.Set;

/**
 * Group registry — obtain via {@link PermissionQuery#groups()}.
 *
 * <pre>{@code
 * int n = pex.query().groups().count();
 * pex.query().groups().resolve("vip").inWorld(world).members(true);
 * }</pre>
 */
public final class GroupsScope {

    private final PermissionService service;

    GroupsScope(PermissionService service) {
        this.service = service;
    }

    public int count() {
        return service.groupCount();
    }

    public Set<String> names() {
        return service.groupNames();
    }

    public void delete(String name) {
        service.deleteGroup(name);
    }

    public ResolvedGroup resolve(String name) {
        return new ResolvedGroup(SubjectRefs.group(service, name));
    }

    public FoundGroup find(String name) {
        return new FoundGroup(SubjectRefs.group(service, name));
    }

    public GroupWorldContext inWorld(String name, String world) {
        return resolve(name).inWorld(world);
    }

    public Optional<GroupWorldContext> findInWorld(String name, String world) {
        return find(name).inWorld(world);
    }

    /** Fluent terminal for a resolved group. */
    public static final class ResolvedGroup {
        private final GroupRef ref;

        ResolvedGroup(GroupRef ref) {
            this.ref = ref;
        }

        public Group get() {
            return ref.resolve();
        }

        public GroupWorldContext inWorld(String world) {
            return ref.inWorld(world);
        }

        public GroupWorldContext global() {
            return ref.inWorld(dev.rono.permissions.api.world.Worlds.GLOBAL);
        }
    }

    /** Fluent terminal for an optional group. */
    public static final class FoundGroup {
        private final GroupRef ref;

        FoundGroup(GroupRef ref) {
            this.ref = ref;
        }

        public Optional<Group> get() {
            return ref.find();
        }

        public Optional<GroupWorldContext> inWorld(String world) {
            return ref.findInWorld(world);
        }

        public Optional<GroupWorldContext> global() {
            return ref.findInWorld(dev.rono.permissions.api.world.Worlds.GLOBAL);
        }
    }
}
