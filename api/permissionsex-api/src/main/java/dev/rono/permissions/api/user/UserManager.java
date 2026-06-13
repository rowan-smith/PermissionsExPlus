package dev.rono.permissions.api.user;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * User registry with an explicit lifecycle: {@code find} never materializes, {@code get} requires
 * persistence, {@code create} explicitly creates a new record.
 *
 * <p>All methods operate on the active backend. Returned {@link User} instances are live adapters
 * over cached engine entities — call {@link User#save()} to persist mutations.</p>
 */
public interface UserManager {

    /**
     * Looks up a persisted user by UUID without creating an in-memory record.
     *
     * @param uuid user UUID
     * @return the user when stored in the backend; empty if absent
     */
    Optional<User> findUser(UUID uuid);

    /**
     * Looks up a persisted user by name without creating an in-memory record.
     *
     * @param name user name (case-sensitive per backend)
     * @return the user when stored in the backend; empty if absent
     */
    Optional<User> findUser(String name);

    /**
     * Returns a persisted user by UUID.
     *
     * @param uuid user UUID
     * @return live user adapter
     * @throws UserNotFoundException if no backend record exists for {@code uuid}
     */
    User getUser(UUID uuid) throws UserNotFoundException;

    /**
     * Returns a persisted user by name.
     *
     * @param name user name
     * @return live user adapter
     * @throws UserNotFoundException if no backend record exists for {@code name}
     */
    User getUser(String name) throws UserNotFoundException;

    /**
     * Creates a new user record keyed by UUID.
     *
     * @param uuid UUID for the new user
     * @return live user adapter for the new record
     * @throws UserAlreadyExistsException if a user with {@code uuid} already exists
     */
    User createUser(UUID uuid) throws UserAlreadyExistsException;

    /**
     * Creates a new user record keyed by name.
     *
     * @param name name for the new user
     * @return live user adapter for the new record
     * @throws UserAlreadyExistsException if a user with {@code name} already exists
     */
    User createUser(String name) throws UserAlreadyExistsException;

    /**
     * Reports whether a user record exists for the UUID.
     *
     * @param uuid user UUID
     * @return {@code true} if persisted or already materialized in memory
     */
    boolean exists(UUID uuid);

    /**
     * Reports whether a user record exists for the name.
     *
     * @param name user name
     * @return {@code true} if persisted or already materialized in memory
     */
    boolean exists(String name);

    /**
     * Returns the number of user records stored in the active backend.
     *
     * @return total persisted user count
     */
    int count();

    /**
     * Returns how many persisted users match {@code filter}.
     *
     * @param filter predicate applied to each stored user; must not be {@code null}
     * @return count of users for which the predicate is {@code true}
     */
    int count(Predicate<User> filter);
}
