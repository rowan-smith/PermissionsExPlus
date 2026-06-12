package dev.rono.permissions.api.session;

import dev.rono.permissions.api.service.PermissionService;
import dev.rono.permissions.api.subject.Group;
import dev.rono.permissions.api.subject.User;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Batch edit helper: track subjects touched in a session and {@link #save()} once.
 *
 * <p>Obtain via {@link dev.rono.permissions.api.service.PermissionService#session()}.</p>
 */
public interface PermissionEditSession extends AutoCloseable {

    User user(String identifier);

    User user(UUID uuid);

    Group group(String name);

    /** Apply edits to a user in one call; subject is tracked for {@link #save()}. */
    default User editUser(String identifier, Consumer<User> edits) {
        User user = user(identifier);
        edits.accept(user);
        return user;
    }

    /** Apply edits to a group in one call; subject is tracked for {@link #save()}. */
    default Group editGroup(String name, Consumer<Group> edits) {
        Group group = group(name);
        edits.accept(group);
        return group;
    }

    /** Persist every subject retrieved through this session. */
    void save();

    /** Discards the session without saving (in-memory mutations may remain until explicit subject save). */
    @Override
    void close();
}
