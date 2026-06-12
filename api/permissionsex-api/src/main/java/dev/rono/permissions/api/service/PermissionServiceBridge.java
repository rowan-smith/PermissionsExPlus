package dev.rono.permissions.api.service;

import dev.rono.permissions.api.PermissionsExException;
import dev.rono.permissions.api.backend.BackendHandle;
import dev.rono.permissions.api.backend.BackendInfo;
import dev.rono.permissions.api.data.ImportMode;
import dev.rono.permissions.api.event.PermissionEventBus;
import dev.rono.permissions.api.session.PermissionEditSession;
import dev.rono.permissions.api.subject.Group;
import dev.rono.permissions.api.subject.User;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Runtime operations backing {@link PermissionService}.
 *
 * <p>Implemented by the core manager; not intended for direct use in plugins. Prefer the fluent
 * {@link PermissionService} flat entry points, which wrap this bridge.</p>
 *
 * <h2>Resolve vs find</h2>
 * <ul>
 *   <li><strong>Resolve</strong> ({@link #user(String)}, {@link #user(UUID)}, {@link #group(String)}):
 *       returns a live subject handle, creating or materializing a record when none exists yet.</li>
 *   <li><strong>Find</strong> ({@link PermissionService#findUser(String)},
 *       {@link PermissionService#findUser(UUID)}, {@link PermissionService#findGroup(String)} via
 *       {@link #lookupUser(String)}, {@link #lookupUser(UUID)}, {@link #lookupGroup(String)}):
 *       returns {@link Optional#empty()} when the subject is not persisted in the active backend.</li>
 * </ul>
 */
public interface PermissionServiceBridge {

    /**
     * Returns the number of user records stored in the active backend.
     *
     * @return persisted user count
     */
    int userCount();

    /**
     * Returns the number of group records stored in the active backend.
     *
     * @return persisted group count
     */
    int groupCount();

    /**
     * Returns a snapshot describing the currently active permission backend.
     *
     * @return active backend metadata
     */
    BackendInfo activeBackend();

    /**
     * Switches the active backend to the configured alias.
     *
     * @param alias configured backend alias (for example {@code "file"} or {@code "sql"})
     * @throws PermissionsExException if the alias is unknown or activation fails
     */
    void setActiveBackend(String alias) throws PermissionsExException;

    /**
     * Opens a handle to a non-active backend for inspection or data transfer.
     *
     * @param alias configured backend alias
     * @return a {@link BackendHandle} for the requested backend
     * @throws PermissionsExException if the alias is unknown or the handle cannot be created
     */
    BackendHandle createBackendHandle(String alias) throws PermissionsExException;

    /**
     * Replaces active-backend data with the contents of another configured backend.
     *
     * @param backendAlias source backend alias to import from
     * @throws PermissionsExException if the alias is unknown or import fails
     */
    void importFromBackend(String backendAlias) throws PermissionsExException;

    /**
     * Serializes all users, groups, and world inheritance from the active backend.
     *
     * @return exported document (format depends on the active backend)
     * @throws PermissionsExException if export fails
     */
    String exportData() throws PermissionsExException;

    /**
     * Merges or replaces active-backend data from a serialized document.
     *
     * @param document serialized permission data
     * @param mode merge strategy ({@link ImportMode#MERGE} or {@link ImportMode#REPLACE})
     * @throws PermissionsExException if the document is invalid or import fails
     */
    void importData(String document, ImportMode mode) throws PermissionsExException;

    /**
     * Returns the permission-domain event bus for subscribing to entity and system dispatches.
     *
     * @return the shared {@link PermissionEventBus}
     */
    PermissionEventBus events();

    /**
     * Returns the names of worlds registered on the platform.
     *
     * @return immutable view of known world names
     */
    Collection<String> registeredWorlds();

    /**
     * Reports whether PermissionsEx debug logging is enabled.
     *
     * @return {@code true} when debug mode is active
     */
    boolean isDebug();

    /**
     * Returns the inheritance chain for a world (parent worlds whose permissions apply).
     *
     * @param world world name, or {@code null} for the global namespace
     * @return ordered list of parent world names
     */
    List<String> worldInheritance(String world);

    /**
     * Sets the inheritance chain for a world.
     *
     * @param world world name, or {@code null} for the global namespace
     * @param parentWorlds ordered list of parent world names
     */
    void setWorldInheritance(String world, List<String> parentWorlds);

    /**
     * Returns the inheritance map for every configured world.
     *
     * @return map from world name to ordered parent-world lists
     */
    Map<String, List<String>> worldInheritanceMap();

    /**
     * Returns groups marked as default for new users in the given world.
     *
     * @param world world name, or {@code null} for the global namespace
     * @return default groups for the world
     */
    List<Group> defaultGroups(String world);

    /**
     * Returns the rank ladder mapping for a named ladder.
     *
     * @param ladderName ladder identifier
     * @return map from rank index to group at that rank
     */
    Map<Integer, Group> rankLadder(String ladderName);

    /**
     * Looks up a persisted user by string identifier without materializing a new record.
     *
     * @param identifier user name or UUID string
     * @return the user when present in the backend, otherwise empty
     */
    Optional<User> lookupUser(String identifier);

    /**
     * Looks up a persisted user by UUID without materializing a new record.
     *
     * @param uuid player UUID
     * @return the user when present in the backend, otherwise empty
     */
    Optional<User> lookupUser(UUID uuid);

    /**
     * Resolves a user by string identifier, materializing a record when none exists yet.
     *
     * @param identifier user name or UUID string
     * @return a live {@link User} handle (never {@code null})
     */
    User user(String identifier);

    /**
     * Resolves a user by UUID, materializing a record when none exists yet.
     *
     * @param uuid player UUID
     * @return a live {@link User} handle (never {@code null})
     */
    User user(UUID uuid);

    /**
     * Returns every user identifier known to the active backend.
     *
     * @return set of persisted user identifiers
     */
    Set<String> userIdentifiers();

    /**
     * Deletes a user record from the active backend.
     *
     * @param identifier user name or UUID string to remove
     */
    void deleteUser(String identifier);

    /**
     * Looks up a persisted group by name without materializing a new record.
     *
     * @param name group name
     * @return the group when present in the backend, otherwise empty
     */
    Optional<Group> lookupGroup(String name);

    /**
     * Resolves a group by name, materializing a record when none exists yet.
     *
     * @param name group name
     * @return a live {@link Group} handle (never {@code null})
     */
    Group group(String name);

    /**
     * Returns every group name known to the active backend.
     *
     * @return set of persisted group names
     */
    Set<String> groupNames();

    /**
     * Deletes a group record from the active backend.
     *
     * @param name group name to remove
     */
    void deleteGroup(String name);

    /**
     * Reloads permission data from the active backend synchronously.
     *
     * @throws PermissionsExException if reload fails
     */
    void reload() throws PermissionsExException;

    /**
     * Reloads permission data from the active backend asynchronously.
     *
     * @return a future that completes when reload finishes (normally or exceptionally)
     */
    CompletableFuture<Void> reloadAsync();

    /**
     * Opens a batch edit session that tracks touched subjects until {@link PermissionEditSession#save()}.
     *
     * @return a new {@link PermissionEditSession}
     */
    PermissionEditSession openEditSession();
}
