package dev.rono.permissions.api.service;

import dev.rono.permissions.api.PermissionsExException;
import dev.rono.permissions.api.backend.BackendHandle;
import dev.rono.permissions.api.backend.BackendInfo;
import dev.rono.permissions.api.data.ImportMode;
import dev.rono.permissions.api.event.PermissionEventBus;
import dev.rono.permissions.api.session.PermissionEditSession;
import dev.rono.permissions.api.subject.Group;
import dev.rono.permissions.api.subject.User;
import dev.rono.permissions.api.world.Worlds;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Modern PermissionsEx integration API ({@code dev.rono.permissions.api}).
 *
 * <p>Registered on Spigot/Paper {@code ServicesManager} under this type. Implemented by the runtime
 * manager alongside legacy {@code ru.tehkode.permissions.PermissionManager}.</p>
 */
public interface PermissionService {

    // --- Introspection (legacy aliases retained) ---

    default int registeredUserNameCount() {
        return userCount();
    }

    default int registeredGroupCount() {
        return groupCount();
    }

    default String activeBackendSimpleName() {
        return backend().simpleName();
    }

    BackendInfo backend();

    /** Subscribe to entity/system permission notifications. */
    PermissionEventBus events();

    int userCount();

    int groupCount();

    Collection<String> worlds();

    boolean isDebug();

    // --- World inheritance ---

    /**
     * Parent worlds whose permissions/options inherit into {@code world}.
     * {@link Worlds#GLOBAL} returns inheritance for the global namespace when configured.
     */
    List<String> worldInheritance(String world);

    void setWorldInheritance(String world, List<String> parentWorlds);

    /** All configured world-inheritance mappings ({@link Worlds#GLOBAL} key = global). */
    Map<String, List<String>> worldInheritanceMap();

    /** Default groups for {@code world} (includes global defaults when applicable). */
    List<Group> defaultGroups(String world);

    /** Rank-ordered groups on a ladder (key = rank, value = group). */
    Map<Integer, Group> rankLadder(String ladderName);

    /** Direct child groups of {@code groupName} in {@code world}. */
    List<Group> childGroups(String groupName, String world, boolean inherit);

    default List<Group> childGroups(String groupName, String world) {
        return childGroups(groupName, world, false);
    }

    default List<Group> childGroups(String groupName) {
        return childGroups(groupName, Worlds.GLOBAL, false);
    }

    /** All descendant groups of {@code groupName} in {@code world}. */
    default List<Group> descendantGroups(String groupName, String world) {
        return childGroups(groupName, world, true);
    }

    default List<Group> descendantGroups(String groupName) {
        return descendantGroups(groupName, Worlds.GLOBAL);
    }

    // --- Permission checks ---

    boolean has(UUID playerId, String permission);

    boolean has(UUID playerId, String permission, String world);

    boolean has(String playerName, String permission, String world);

    default boolean has(String playerName, String permission) {
        return has(playerName, permission, Worlds.GLOBAL);
    }

    // --- Users ---

    Optional<User> findUser(String identifier);

    Optional<User> findUser(UUID uuid);

    /** Resolves or materializes a user (classic {@code getUser} semantics). */
    User user(String identifier);

    User user(UUID uuid);

    Set<String> userIdentifiers();

    void deleteUser(String identifier);

    // --- Groups ---

    Optional<Group> findGroup(String name);

    /** Resolves a persisted group; throws if the group does not exist in the backend. */
    Group group(String name);

    Set<String> groupNames();

    void deleteGroup(String name);

    /**
     * Users belonging to {@code groupName} in {@code world}.
     *
     * @param inherit when {@code true}, includes users in descendant groups
     */
    List<User> usersInGroup(String groupName, String world, boolean inherit);

    default List<User> usersInGroup(String groupName, String world) {
        return usersInGroup(groupName, world, false);
    }

    default List<User> usersInGroup(String groupName) {
        return usersInGroup(groupName, Worlds.GLOBAL, false);
    }

    // --- Backend administration ---

    void setActiveBackend(String alias) throws PermissionsExException;

    /** Create a backend from configuration without activating it. */
    BackendHandle createBackendHandle(String alias) throws PermissionsExException;

    /** Copy data from another configured backend alias into the active backend. */
    void importFromBackend(String backendAlias) throws PermissionsExException;

    /** Export active backend document (YAML for file backends). */
    String exportData() throws PermissionsExException;

    /** Import a backend document into the active backend. */
    void importData(String document, ImportMode mode) throws PermissionsExException;

    // --- Maintenance ---

    void reload() throws PermissionsExException;

    /** Reload backend data asynchronously on the PEX executor. */
    CompletableFuture<Void> reloadAsync();

    /** Open a batch edit session (call {@link PermissionEditSession#save()} when done). */
    PermissionEditSession openEditSession();
}
