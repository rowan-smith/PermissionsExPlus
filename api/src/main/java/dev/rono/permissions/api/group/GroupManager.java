package dev.rono.permissions.api.group;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * Group registry with explicit find/get/create/exists lifecycle.
 *
 * <p>Returned {@link Group} instances are live adapters — call {@link Group#save()} after mutations.</p>
 */
public interface GroupManager {

    /**
     * Looks up a persisted group without creating an in-memory record.
     *
     * @param name group identifier
     * @return the group when stored in the backend; empty if absent
     */
    Optional<Group> findGroup(String name);

    /**
     * Returns a persisted group by name.
     *
     * @param name group identifier
     * @return live group adapter
     * @throws GroupNotFoundException if no backend record exists for {@code name}
     */
    Group getGroup(String name) throws GroupNotFoundException;

    /**
     * Creates a new group record.
     *
     * @param name group identifier
     * @return live group adapter for the new record
     * @throws GroupAlreadyExistsException if a group with {@code name} already exists
     */
    Group createGroup(String name) throws GroupAlreadyExistsException;

    /**
     * Reports whether a group record exists.
     *
     * @param name group identifier
     * @return {@code true} if persisted or already materialized in memory
     */
    boolean exists(String name);

    /**
     * Returns the number of group records stored in the active backend.
     *
     * @return total persisted group count
     */
    int count();

    /**
     * Returns how many persisted groups match {@code filter}.
     *
     * @param filter predicate applied to each stored group; must not be {@code null}
     * @return count of groups for which the predicate is {@code true}
     */
    int count(Predicate<Group> filter);
}
