package ru.tehkode.permissions;

import dev.rono.permissions.api.permission.PermissionAddRequest;
import dev.rono.permissions.api.permission.PermissionHolder;
import dev.rono.permissions.api.permission.PermissionNode;
import org.bukkit.entity.Player;
import ru.tehkode.permissions.backends.PermissionBackend;
import ru.tehkode.permissions.bukkit.PermissionsExConfig;
import ru.tehkode.permissions.exceptions.PermissionBackendException;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Logger;

/**
 * Classic PermissionsEx permission manager contract ({@code ru.tehkode.permissions}, baseline {@code 628215f}).
 *
 * <p>Registered on Bukkit {@code ServicesManager} as {@code PermissionManager.class} on game servers.
 * Runtime extensions ({@code PlatformAdapter}, entity bus dispatches, internal scheduling) are implemented on
 * {@code dev.rono.permissions.core.InternalPermissionManager} and are not part of this surface.</p>
 *
 * <p>Classic methods remain for binary compatibility but are deprecated — use
 * {@code PermissionsExApi} via {@code PermissionsEx.getApi()}.</p>
 *
 * <h2>World scope</h2>
 * <p>A {@code null} world name denotes the <em>global</em> (common) scope shared across worlds unless
 * overridden by world-specific data. World inheritance ({@link #getWorldInheritance(String)}) further
 * links named worlds to parent worlds when resolving effective permissions.</p>
 *
 * <h2>Inheritance flag overloads</h2>
 * <p>{@link #getUsers(String, String, boolean)} and {@link #getGroups(String, String, boolean)} accept an
 * {@code inheritance} parameter: when {@code true}, descendant groups of the named group are included;
 * when {@code false}, only direct members or child groups are returned. Two-argument overloads default
 * {@code inheritance} to {@code false}.</p>
 *
 * <h2>Cache and reset semantics</h2>
 * <ul>
 *   <li>{@link #cacheUser(String, String)} — pre-materializes a user during async login (thread-safe).</li>
 *   <li>{@link #clearUserCache(String)} — clears resolved permission state on a cached user without dropping
 *       the in-memory user object.</li>
 *   <li>{@link #resetUser(String)} — removes the in-memory user; the next lookup reloads from the backend.</li>
 *   <li>{@link #resetGroup(String)} — removes the in-memory group; the next lookup reloads from the backend.</li>
 *   <li>{@link #reset()} — clears all in-memory users and groups, reloads the backend, and optionally fires
 *       a reload event.</li>
 * </ul>
 *
 * <h2>Timed permissions</h2>
 * <p>{@link #initTimer()} starts the scheduler used to expire timed grants. {@link #TRANSIENT_PERMISSION}
 * ({@code 0}) marks a timed grant as non-persisted (in-memory only until reload).</p>
 *
 * <h2>Holder-based permissions</h2>
 * <p>The {@link #addPermission(PermissionHolder, String)}, {@link #removePermission(PermissionHolder, String)},
 * {@link #hasPermission(PermissionHolder, String)}, and {@link #getPermissions(PermissionHolder)} methods operate on
 * {@link PermissionHolder} identities (for example {@code User#asHolder()} from the modern user API, group/world/ladder
 * holders, etc.). Resolve the manager via {@code PermissionsEx.getApi().getPermissionManager()}.
 * World context for advanced adds is supplied through {@link PermissionAddRequest}.</p>
 *
 * @see PermissionEntity
 * @see PermissionUser
 * @see PermissionGroup
 */
// Suppressed deprecation warnings are intentional, binary compatibility.
@SuppressWarnings("DeprecatedIsStillUsed")
public interface PermissionManager {

    /**
     * Lifetime value ({@code 0}) indicating a timed permission is transient: held in memory only and not
     * persisted to the backend.
     *
     * <p>Passed to {@link PermissionEntity#addTimedPermission(String, String, int)} as {@code lifeTime}.</p>
     */
    int TRANSIENT_PERMISSION = 0;

    /**
     * Grants a permission to the given holder in the <em>global</em> scope (not world-specific).
     *
     * <p>The grant is persisted when the underlying entity is saved. For timed or world-scoped grants, use
     * {@link #addPermission(PermissionHolder, String, Duration)} or {@link #addPermission(PermissionAddRequest)}.</p>
     *
     * @param holder     permission target; must not be {@code null}
     * @param permission permission node to grant; must not be {@code null} or empty
     * @return metadata describing the added node
     * @see PermissionHolder
     * @see PermissionNode
     */
    PermissionNode addPermission(PermissionHolder holder, String permission);

    /**
     * Grants a permission to the holder in the <em>global</em> scope for a limited duration.
     *
     * <p>When {@code duration} is {@code null}, behaves like {@link #addPermission(PermissionHolder, String)}.
     * Timed grants expire automatically after the scheduler runs ({@link #initTimer()}).</p>
     *
     * @param holder     permission target; must not be {@code null}
     * @param permission permission node to grant; must not be {@code null} or empty
     * @param duration   lifetime of the grant, or {@code null} for a permanent direct assignment
     * @return metadata describing the added node (including expiry when timed)
     * @see PermissionHolder
     * @see PermissionNode
     */
    PermissionNode addPermission(PermissionHolder holder, String permission, Duration duration);

    /**
     * Grants a permission using a fully specified add request (world context, expiry, source metadata).
     *
     * <p>Build requests with {@link PermissionAddRequest#builder()}. World keys in the request context map to
     * world-scoped storage; an empty context uses the global scope.</p>
     *
     * @param request add specification; must not be {@code null}
     * @return metadata describing the added node
     * @see PermissionAddRequest
     * @see PermissionNode
     */
    PermissionNode addPermission(PermissionAddRequest request);

    /**
     * Removes a <em>direct</em> permission assignment from the holder in the <em>global</em> scope.
     *
     * <p>Does not remove inherited permissions. Does not clear timed nodes in world-specific scopes; use
     * {@link PermissionEntity#removeTimedPermission(String, String)} on the resolved classic entity when needed.</p>
     *
     * @param holder     permission target; must not be {@code null}
     * @param permission permission node to remove; must not be {@code null}
     */
    void removePermission(PermissionHolder holder, String permission);

    /**
     * Checks whether the holder effectively holds the permission in the <em>global</em> scope.
     *
     * <p>Includes inheritance and parent groups for user/group holders. For per-world effective checks, use the
     * classic {@link #has(Player, String, String)} API or {@link #hasPermission(PermissionHolder, String, Map)}
     * with a {@code world} context entry.</p>
     *
     * @param holder     permission target; must not be {@code null}
     * @param permission permission node to check; must not be {@code null}
     * @return {@code true} if granted after inheritance, {@code false} otherwise
     * @see PermissionHolder
     */
    boolean hasPermission(PermissionHolder holder, String permission);

    /**
     * Checks whether the holder effectively holds the permission in the scope described by {@code context}.
     *
     * <p>The {@code world} entry in {@code context} selects the world/realm namespace (same as classic
     * per-world checks). An empty or missing context uses the global namespace.</p>
     *
     * @param holder     permission target; must not be {@code null}
     * @param permission permission node to check; must not be {@code null}
     * @param context    optional context map (for example {@code Map.of("world", player.getWorld().getName())})
     * @return {@code true} if granted after inheritance, {@code false} otherwise
     * @see PermissionHolder
     */
    boolean hasPermission(PermissionHolder holder, String permission, Map<String, String> context);

    /**
     * Returns <em>direct</em> permission assignments for the holder in the <em>global</em> scope.
     *
     * <p>Does not include inherited nodes or world-specific assignments. Each entry is a {@link PermissionNode}
     * without expiry metadata for permanent grants.</p>
     *
     * @param holder permission target; must not be {@code null}
     * @return immutable list of direct global permissions (may be empty)
     * @see PermissionHolder
     * @see PermissionNode
     */
    List<PermissionNode> getPermissions(PermissionHolder holder);

    // |----------------------------------------------------|
    // |  Legacy methods (deprecated binary compatability)  |
    // |----------------------------------------------------|

    /**
     * Returns whether the engine should create user records automatically when unknown players are resolved.
     *
     * @return {@code true} if new user records should be created (config-driven)
     * @see PermissionsExConfig#createUserRecords()
     */
    @Deprecated()
    boolean shouldCreateUserRecords();

    /**
     * Returns the read-only PermissionsEx configuration view.
     *
     * @return configuration instance
     */
    @Deprecated()
    PermissionsExConfig getConfiguration();

    /**
     * Checks whether the given online player holds a permission in their current world.
     *
     * @param player     online player
     * @param permission permission node to check
     * @return {@code true} if granted, {@code false} otherwise
     */
    @Deprecated()
    boolean has(Player player, String permission);

    /**
     * Checks whether the given online player holds a permission in the named world.
     *
     * @param player     online player
     * @param permission permission node to check
     * @param world      world name
     * @return {@code true} if granted, {@code false} otherwise
     */
    @Deprecated()
    boolean has(Player player, String permission, String world);

    /**
     * Checks whether the user identified by name holds a permission in the named world.
     *
     * <p>Resolves offline and online users via the active backend. Returns {@code false} when the user
     * cannot be resolved.</p>
     *
     * @param playerName player name or UUID string
     * @param permission permission node to check
     * @param world      world name, or {@code null} for global scope
     * @return {@code true} if granted, {@code false} otherwise
     */
    @Deprecated()
    boolean has(String playerName, String permission, String world);

    /**
     * Checks whether the user identified by UUID holds a permission in the named world.
     *
     * @param playerId   player UUID
     * @param permission permission node to check
     * @param world      world name, or {@code null} for global scope
     * @return {@code true} if granted, {@code false} if denied or the user is unknown
     */
    @Deprecated()
    boolean has(UUID playerId, String permission, String world);

    /**
     * Resolves or materializes a {@link PermissionUser} by identifier (UUID string or legacy name).
     *
     * @param username user identifier or display name
     * @return user instance
     * @throws IllegalArgumentException if {@code username} is null or empty
     * @throws IllegalStateException    if the user does not exist in the backend and cannot be created
     */
    @Deprecated()
    PermissionUser getUser(String username);

    /**
     * Pre-caches a user during asynchronous login before the player is fully online.
     *
     * <p>Thread-safe; stores the resolved user in the manager's in-memory cache using {@code ident} as key
     * and {@code fallbackName} for UUID conversion fallbacks.</p>
     *
     * @param ident        stable user identifier (typically UUID string)
     * @param fallbackName player name used when converting legacy name-based records
     */
    @Deprecated()
    void cacheUser(String ident, String fallbackName);

    /**
     * Resolves or materializes a {@link PermissionUser} for an online player.
     *
     * @param player online player
     * @return user instance
     */
    @Deprecated()
    PermissionUser getUser(Player player);

    /**
     * Resolves or materializes a {@link PermissionUser} by UUID.
     *
     * @param uid player UUID
     * @return user instance
     */
    @Deprecated()
    PermissionUser getUser(UUID uid);

    /**
     * Returns all users known to the backend, materializing instances as needed.
     *
     * @return unmodifiable set of users
     */
    @Deprecated()
    Set<PermissionUser> getUsers();

    /**
     * Returns users currently held in the manager's in-memory cache.
     *
     * @return copy of cached users (may be a subset of {@link #getUsers()})
     */
    @Deprecated()
    Set<PermissionUser> getActiveUsers();

    /**
     * Returns stable user identifiers stored in the backend.
     *
     * @return collection of user identifiers (typically UUID strings)
     */
    @Deprecated()
    Collection<String> getUserIdentifiers();

    /**
     * Returns display names stored in the backend.
     *
     * @return collection of user names
     */
    @Deprecated()
    Collection<String> getUserNames();

    /**
     * Returns users that are direct members of the named group in the given world.
     *
     * <p>Equivalent to {@link #getUsers(String, String, boolean)} with {@code inheritance == false}.</p>
     *
     * @param groupName group identifier
     * @param worldName world name, or {@code null} for global scope
     * @return unmodifiable set of matching users
     */
    @Deprecated()
    Set<PermissionUser> getUsers(String groupName, String worldName);

    /**
     * Returns users that are direct members of the named group in global scope.
     *
     * <p>Equivalent to {@link #getUsers(String, boolean)} with {@code inheritance == false}.</p>
     *
     * @param groupName group identifier
     * @return unmodifiable set of matching users
     */
    @Deprecated()
    Set<PermissionUser> getUsers(String groupName);

    /**
     * Returns users in the named group for the given world, optionally including descendant groups.
     *
     * @param groupName   group identifier
     * @param worldName   world name, or {@code null} for global scope
     * @param inheritance when {@code true}, includes users in child/descendant groups
     * @return unmodifiable set of matching users
     */
    @Deprecated()
    Set<PermissionUser> getUsers(String groupName, String worldName, boolean inheritance);

    /**
     * Returns users in the named group across all worlds (including global scope), optionally including
     * descendant groups.
     *
     * @param groupName   group identifier
     * @param inheritance when {@code true}, includes users in child/descendant groups
     * @return unmodifiable set of matching users
     */
    @Deprecated()
    Set<PermissionUser> getUsers(String groupName, boolean inheritance);

    /**
     * Removes the in-memory {@link PermissionUser} for the given identifier.
     *
     * <p>Does not delete backend data; the next {@link #getUser(String)} reloads from storage.
     * Differs from {@link #clearUserCache(String)}, which retains the cached object and only clears
     * resolved permission state.</p>
     *
     * @param userName user identifier or name
     */
    @Deprecated()
    void resetUser(String userName);

    /**
     * Removes the in-memory {@link PermissionUser} for the given player.
     *
     * @param player online player whose cached user object should be dropped
     * @see #resetUser(String)
     */
    @Deprecated()
    void resetUser(Player player);

    /**
     * Clears resolved permission cache for the user identified by name without removing the cached user object.
     *
     * @param userName user identifier or name
     * @see #resetUser(String)
     */
    @Deprecated()
    void clearUserCache(String userName);

    /**
     * Clears resolved permission cache for the user identified by UUID.
     *
     * @param uid player UUID
     * @see #clearUserCache(String)
     */
    @Deprecated()
    void clearUserCache(UUID uid);

    /**
     * Clears resolved permission cache for the given online player.
     *
     * @param player online player
     * @see #clearUserCache(String)
     */
    @Deprecated()
    void clearUserCache(Player player);

    /**
     * Resolves or materializes a {@link PermissionGroup} by name.
     *
     * @param groupname group identifier
     * @return group instance, or {@code null} if {@code groupname} is null or empty
     */
    @Deprecated()
    PermissionGroup getGroup(String groupname);

    /**
     * Returns all groups known to the backend.
     *
     * @return unmodifiable list of groups
     */
    @Deprecated()
    List<PermissionGroup> getGroupList();

    /**
     * Returns all groups as an array.
     *
     * @return array of all groups
     * @deprecated prefer {@link #getGroupList()}
     */
    @Deprecated
    PermissionGroup[] getGroups();

    /**
     * Returns names of all groups in the backend.
     *
     * @return collection of group names
     * @deprecated prefer resolving groups via {@link #getGroupList()} or the backend
     */
    @Deprecated
    Collection<String> getGroupNames();

    /**
     * Returns child groups of {@code groupName} in the given world (direct children only).
     *
     * <p>Equivalent to {@link #getGroups(String, String, boolean)} with {@code inheritance == false}.</p>
     *
     * @param groupName parent group identifier
     * @param worldName world name, or {@code null} for global scope
     * @return unmodifiable list of child groups
     */
    @Deprecated()
    List<PermissionGroup> getGroups(String groupName, String worldName);

    /**
     * Returns child groups of {@code groupName} in global scope (direct children only).
     *
     * @param groupName parent group identifier
     * @return unmodifiable list of child groups
     * @see #getGroups(String, String)
     */
    @Deprecated()
    List<PermissionGroup> getGroups(String groupName);

    /**
     * Returns child or descendant groups of {@code groupName} in the given world.
     *
     * @param groupName   parent group identifier
     * @param worldName   world name, or {@code null} for global scope
     * @param inheritance when {@code true}, includes all descendant groups; when {@code false}, direct children only
     * @return unmodifiable list of matching groups
     */
    @Deprecated()
    List<PermissionGroup> getGroups(String groupName, String worldName, boolean inheritance);

    /**
     * Returns child or descendant groups of {@code groupName} aggregated across all worlds and global scope.
     *
     * @param groupName   parent group identifier
     * @param inheritance when {@code true}, includes all descendant groups; when {@code false}, direct children only
     * @return unmodifiable, sorted list of matching groups
     */
    @Deprecated()
    List<PermissionGroup> getGroups(String groupName, boolean inheritance);

    /**
     * Returns groups marked as default for the given world.
     *
     * <p>Includes groups default in global scope ({@code worldName == null} on the group) when querying a
     * specific world.</p>
     *
     * @param worldName world name, or {@code null} to query global defaults only
     * @return unmodifiable list of default groups (may be empty)
     */
    @Deprecated()
    List<PermissionGroup> getDefaultGroups(String worldName);

    /**
     * Removes the in-memory {@link PermissionGroup} for the given name.
     *
     * <p>Does not delete backend data; the next {@link #getGroup(String)} reloads from storage.</p>
     *
     * @param groupName group identifier
     * @return the removed cached group, or {@code null} if none was cached
     */
    @Deprecated()
    PermissionGroup resetGroup(String groupName);

    /**
     * Enables or disables manager-wide debug logging.
     *
     * @param debug {@code true} to enable debug output
     */
    @Deprecated()
    void setDebug(boolean debug);

    /**
     * Returns whether manager-wide debug logging is enabled.
     *
     * @return {@code true} if debug mode is active
     */
    @Deprecated()
    boolean isDebug();

    /**
     * Returns groups on the named rank ladder keyed by rank number.
     *
     * @param ladderName ladder name (case-insensitive match)
     * @return map of rank to group; empty if the ladder does not exist
     */
    @Deprecated()
    Map<Integer, PermissionGroup> getRankLadder(String ladderName);

    /**
     * Returns parent worlds from which the named world inherits permissions and options.
     *
     * @param worldName world name
     * @return ordered list of parent world names; empty if none configured
     */
    @Deprecated()
    List<String> getWorldInheritance(String worldName);

    /**
     * Configures parent-world inheritance for the named world.
     *
     * <p>Clears cached permission state on all active users after the change.</p>
     *
     * @param world        world whose inheritance is being set
     * @param parentWorlds ordered list of parent world names
     */
    @Deprecated()
    void setWorldInheritance(String world, List<String> parentWorlds);

    /**
     * Returns the active permission storage backend.
     *
     * @return current {@link PermissionBackend} instance
     */
    @Deprecated()
    PermissionBackend getBackend();

    /**
     * Switches the active backend to the named type, reloading data.
     *
     * @param backendName backend type identifier (e.g. {@code file}, {@code sql})
     * @throws PermissionBackendException if the backend cannot be created or initialized
     */
    @Deprecated()
    void setBackend(String backendName) throws PermissionBackendException;

    /**
     * Creates a new {@link PermissionBackend} instance without activating it.
     *
     * @param backendName backend type identifier
     * @return newly created backend
     * @throws PermissionBackendException if the backend cannot be created
     */
    @Deprecated()
    PermissionBackend createBackend(String backendName) throws PermissionBackendException;

    /**
     * Clears all in-memory users and groups, reloads the active backend, and fires a reload event.
     *
     * @throws PermissionBackendException if the backend reload fails
     * @see #reset(boolean)
     */
    @Deprecated()
    void reset() throws PermissionBackendException;

    /**
     * Clears all in-memory users and groups and reloads the active backend.
     *
     * @param callEvent when {@code true}, publishes a system reload event after reload completes
     * @throws PermissionBackendException if the backend reload fails
     */
    @Deprecated()
    void reset(boolean callEvent) throws PermissionBackendException;

    /**
     * Shuts down the manager: closes the backend, clears caches, and stops the scheduler.
     */
    @Deprecated()
    void end();

    /**
     * (Re)initializes the scheduled executor used for timed permission and timed group expiration.
     *
     * <p>Called during startup and after {@link #reset()} cache clears. Tasks scheduled with
     * {@link PermissionManager#TRANSIENT_PERMISSION} lifetime are not persisted.</p>
     */
    @Deprecated()
    void initTimer();

    /**
     * Returns the permission expression matcher used for node checks.
     *
     * @return current {@link PermissionMatcher}
     */
    @Deprecated()
    PermissionMatcher getPermissionMatcher();

    /**
     * Replaces the permission expression matcher.
     *
     * @param matcher new matcher implementation
     */
    @Deprecated()
    void setPermissionMatcher(PermissionMatcher matcher);

    /**
     * Returns the engine logger.
     *
     * @return PermissionsEx logger instance
     */
    @Deprecated()
    Logger getLogger();

    /**
     * Returns the scheduled executor service used for timed tasks.
     *
     * @return scheduler, or {@code null} after {@link #end()}
     */
    @Deprecated()
    ScheduledExecutorService getExecutor();

    /**
     * Returns whether default group membership should be persisted when saving users.
     *
     * @return {@code true} if default groups are written to storage (config-driven)
     * @see PermissionsExConfig#saveDefaultGroup()
     */
    @Deprecated()
    boolean shouldSaveDefaultGroup();
}
