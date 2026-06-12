package dev.rono.permissions.api.session;

import dev.rono.permissions.api.subject.PexGroup;
import dev.rono.permissions.api.subject.PexUser;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * Batch edit helper: track subjects touched in a session and {@link #save()} once.
 *
 * <p>Obtain via {@link dev.rono.permissions.api.service.PermissionService#session()}. Subjects
 * retrieved through this session are tracked for a single deferred {@link #save()} call.</p>
 */
public interface PermissionEditSession extends AutoCloseable {

    /**
     * Resolves a user by string identifier and registers the subject for batch persistence.
     *
     * <p>Materializes a backend record when none exists yet (resolve semantics).</p>
     *
     * @param identifier user name or UUID string
     * @return a live {@link PexUser} handle tracked by this session
     */
    PexUser user(String identifier);

    /**
     * Resolves a user by UUID and registers the subject for batch persistence.
     *
     * <p>Materializes a backend record when none exists yet (resolve semantics).</p>
     *
     * @param uuid player UUID
     * @return a live {@link PexUser} handle tracked by this session
     */
    PexUser user(UUID uuid);

    /**
     * Resolves a group by name and registers the subject for batch persistence.
     *
     * <p>Materializes a backend record when none exists yet (resolve semantics).</p>
     *
     * @param name group name
     * @return a live {@link PexGroup} handle tracked by this session
     */
    PexGroup group(String name);

    /**
     * Resolves a user, applies edits, and registers the subject for batch persistence.
     *
     * @param identifier user name or UUID string
     * @param edits callback invoked with the resolved user before save
     * @return the same {@link PexUser} instance passed to {@code edits}
     */
    default PexUser editUser(String identifier, Consumer<PexUser> edits) {
        PexUser user = user(identifier);
        edits.accept(user);
        return user;
    }

    /**
     * Resolves a group, applies edits, and registers the subject for batch persistence.
     *
     * @param name group name
     * @param edits callback invoked with the resolved group before save
     * @return the same {@link PexGroup} instance passed to {@code edits}
     */
    default PexGroup editGroup(String name, Consumer<PexGroup> edits) {
        PexGroup group = group(name);
        edits.accept(group);
        return group;
    }

    /**
     * Persists every subject retrieved through this session since it was opened.
     */
    void save();

    /**
     * Discards the session without invoking {@link #save()}.
     *
     * <p>In-memory mutations on already-resolved subjects may remain until those subjects are
     * saved explicitly or another session persists them.</p>
     */
    @Override
    void close();
}
