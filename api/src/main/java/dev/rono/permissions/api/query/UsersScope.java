package dev.rono.permissions.api.query;

import dev.rono.permissions.api.service.PermissionService;
import dev.rono.permissions.api.subject.User;
import dev.rono.permissions.api.subject.UserWorldContext;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * User registry — obtain via {@link PermissionQuery#users()}.
 *
 * <pre>{@code
 * int n = pex.query().users().count();
 * pex.query().users().resolve(uuid).inWorld(world).addPermission("node");
 * }</pre>
 */
public final class UsersScope {

    private final PermissionService service;

    UsersScope(PermissionService service) {
        this.service = service;
    }

    public int count() {
        return service.userCount();
    }

    public Set<String> identifiers() {
        return service.userIdentifiers();
    }

    public void delete(String identifier) {
        service.deleteUser(identifier);
    }

    /** Resolve or materialize; chain with {@link UserRef#inWorld(String)} via returned handle. */
    public ResolvedUser resolve(UUID uuid) {
        return new ResolvedUser(SubjectRefs.user(service, uuid, null));
    }

    public ResolvedUser resolve(String identifier) {
        return new ResolvedUser(SubjectRefs.user(service, null, identifier));
    }

    /** Optional lookup — persisted users only. */
    public FoundUser find(UUID uuid) {
        return new FoundUser(SubjectRefs.user(service, uuid, null));
    }

    public FoundUser find(String identifier) {
        return new FoundUser(SubjectRefs.user(service, null, identifier));
    }

    /** Resolve or materialize a user already scoped to a world. */
    public UserWorldContext inWorld(UUID uuid, String world) {
        return resolve(uuid).inWorld(world);
    }

    public UserWorldContext inWorld(String identifier, String world) {
        return resolve(identifier).inWorld(world);
    }

    public Optional<UserWorldContext> findInWorld(UUID uuid, String world) {
        return find(uuid).inWorld(world);
    }

    public Optional<UserWorldContext> findInWorld(String identifier, String world) {
        return find(identifier).inWorld(world);
    }

    /** Fluent terminal for a resolved user. */
    public static final class ResolvedUser {
        private final UserRef ref;

        ResolvedUser(UserRef ref) {
            this.ref = ref;
        }

        public User get() {
            return ref.resolve();
        }

        public UserWorldContext inWorld(String world) {
            return ref.inWorld(world);
        }

        public UserWorldContext global() {
            return ref.inWorld(dev.rono.permissions.api.world.Worlds.GLOBAL);
        }
    }

    /** Fluent terminal for an optional user. */
    public static final class FoundUser {
        private final UserRef ref;

        FoundUser(UserRef ref) {
            this.ref = ref;
        }

        public Optional<User> get() {
            return ref.find();
        }

        public Optional<UserWorldContext> inWorld(String world) {
            return ref.findInWorld(world);
        }

        public Optional<UserWorldContext> global() {
            return ref.findInWorld(dev.rono.permissions.api.world.Worlds.GLOBAL);
        }
    }
}
