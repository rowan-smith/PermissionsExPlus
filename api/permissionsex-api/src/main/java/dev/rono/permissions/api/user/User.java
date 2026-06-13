package dev.rono.permissions.api.user;

import dev.rono.permissions.api.permission.PermissionHolder;
import dev.rono.permissions.api.subject.PermissionSubject;
import dev.rono.permissions.api.subject.SubjectType;
import dev.rono.permissions.api.subject.SubjectWorldContexts;
import dev.rono.permissions.api.subject.TimedGroupMembership;
import dev.rono.permissions.api.subject.UserWorldContext;
import dev.rono.permissions.api.world.Worlds;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Modern view of a permission user.
 *
 * <p>Extends {@link PermissionSubject} with group membership and timed group assignment.
 * Rank-ladder promotion/demotion lives on {@link dev.rono.permissions.api.ladder.LadderManager}.</p>
 *
 * <p><strong>Identifier vs entity naming:</strong> {@link #groups(String, boolean)} returns group
 * identifiers ({@code String}). For resolved {@link dev.rono.permissions.api.group.Group} objects,
 * use {@code getGroupManager().getGroup(name)}. This mirrors {@link dev.rono.permissions.api.group.Group#members(String, boolean)}
 * returning {@link User} entities while {@link dev.rono.permissions.api.group.Group#memberIdentifiers(String)}
 * returns identifiers.</p>
 */
public interface User extends PermissionSubject {


    UUID getId();

    String getName();

    PermissionHolder asHolder();

    /**
     * Returns {@link SubjectType#USER}.
     *
     * @return {@link SubjectType#USER}
     */
    @Override
    default SubjectType type() {
        return SubjectType.USER;
    }

    /**
     * Returns the parsed UUID when the identifier is UUID-shaped.
     *
     * @return UUID when the identifier is a valid UUID string; empty for name-only records
     */
    Optional<UUID> uniqueId();

    /**
     * Returns a world-scoped view of this user for permission, group, and metadata operations.
     *
     * <p>Methods on the returned context apply to {@code world} without repeating the world argument.</p>
     *
     * @param world world name, or {@link Worlds#GLOBAL} for the global namespace
     * @return world-bound user context
     */
    @Override
    default UserWorldContext inWorld(String world) {
        return SubjectWorldContexts.user(this, world);
    }

    /**
     * Returns a view of this user bound to the global namespace.
     *
     * <p>Equivalent to {@link #inWorld(String)} with {@link Worlds#GLOBAL}.</p>
     *
     * @return global world context for this user
     */
    @Override
    default UserWorldContext global() {
        return inWorld(Worlds.GLOBAL);
    }

    /**
     * Returns group identifiers the user inherits in the given world.
     *
     * <p>When {@code inherit} is {@code true}, includes parents of parents; when {@code false}, returns
     * only direct group assignments.</p>
     *
     * @param world   world name, or {@link Worlds#GLOBAL} for the global namespace
     * @param inherit when {@code true}, expand transitive group inheritance
     * @return list of group identifiers
     */
    List<String> groups(String world, boolean inherit);

    /**
     * Returns group identifiers the user inherits in the given world, including transitive inheritance.
     *
     * <p>Delegates to {@link #groups(String, boolean)} with {@code inherit = true}.</p>
     *
     * @param world world name, or {@link Worlds#GLOBAL} for the global namespace
     * @return list of group identifiers
     */
    default List<String> groups(String world) {
        return groups(world, true);
    }

    /**
     * Returns group identifiers the user inherits in the global namespace, including transitive inheritance.
     *
     * <p>Delegates to {@link #groups(String, boolean)} with {@link Worlds#GLOBAL} and {@code inherit = true}.</p>
     *
     * @return list of group identifiers
     */
    default List<String> groups() {
        return groups(Worlds.GLOBAL, true);
    }

    /**
     * Returns whether this user belongs to the named group in the given world.
     *
     * @param groupName group identifier to test
     * @param world     world name, or {@link Worlds#GLOBAL} for the global namespace
     * @param inherit   when {@code true}, match transitive group membership
     * @return {@code true} if the user is in the group, {@code false} otherwise
     */
    boolean inGroup(String groupName, String world, boolean inherit);

    /**
     * Returns whether this user belongs to the named group in the given world, including transitive membership.
     *
     * <p>Delegates to {@link #inGroup(String, String, boolean)} with {@code inherit = true}.</p>
     *
     * @param groupName group identifier to test
     * @param world     world name, or {@link Worlds#GLOBAL} for the global namespace
     * @return {@code true} if the user is in the group, {@code false} otherwise
     */
    default boolean inGroup(String groupName, String world) {
        return inGroup(groupName, world, true);
    }

    /**
     * Returns whether this user belongs to the named group in the global namespace, including transitive membership.
     *
     * <p>Delegates to {@link #inGroup(String, String, boolean)} with {@link Worlds#GLOBAL} and
     * {@code inherit = true}.</p>
     *
     * @param groupName group identifier to test
     * @return {@code true} if the user is in the group, {@code false} otherwise
     */
    default boolean inGroup(String groupName) {
        return inGroup(groupName, Worlds.GLOBAL, true);
    }

    /**
     * Adds this user to a group in the given world.
     *
     * @param groupName group identifier to join
     * @param world     world name, or {@link Worlds#GLOBAL} for the global namespace
     */
    void addGroup(String groupName, String world);

    /**
     * Adds this user to a group in the given world with a timed membership.
     *
     * @param groupName        group identifier to join
     * @param world            world name, or {@link Worlds#GLOBAL} for the global namespace
     * @param lifetimeSeconds  seconds until membership expires; {@code 0} for transient (in-memory only)
     */
    void addGroup(String groupName, String world, int lifetimeSeconds);

    /**
     * Adds this user to a group in the global namespace.
     *
     * <p>Delegates to {@link #addGroup(String, String)} with {@link Worlds#GLOBAL}.</p>
     *
     * @param groupName group identifier to join
     */
    default void addGroup(String groupName) {
        addGroup(groupName, Worlds.GLOBAL);
    }

    /**
     * Adds this user to a group in the global namespace with a timed membership.
     *
     * <p>Delegates to {@link #addGroup(String, String, int)} with {@link Worlds#GLOBAL}.</p>
     *
     * @param groupName       group identifier to join
     * @param lifetimeSeconds seconds until membership expires; {@code 0} for transient (in-memory only)
     */
    default void addGroup(String groupName, int lifetimeSeconds) {
        addGroup(groupName, Worlds.GLOBAL, lifetimeSeconds);
    }

    /**
     * Removes this user from a group in the given world.
     *
     * @param groupName group identifier to leave
     * @param world     world name, or {@link Worlds#GLOBAL} for the global namespace
     */
    void removeGroup(String groupName, String world);

    /**
     * Removes this user from a group in the global namespace.
     *
     * <p>Delegates to {@link #removeGroup(String, String)} with {@link Worlds#GLOBAL}.</p>
     *
     * @param groupName group identifier to leave
     */
    default void removeGroup(String groupName) {
        removeGroup(groupName, Worlds.GLOBAL);
    }

    /**
     * Returns timed group memberships in the given world.
     *
     * @param world world name, or {@link Worlds#GLOBAL} for the global namespace
     * @return list of timed group membership entries
     */
    List<TimedGroupMembership> timedGroupMemberships(String world);

    /**
     * Returns timed group memberships across every configured world, including global.
     *
     * @return aggregated timed group memberships from all worlds with data
     */
    default List<TimedGroupMembership> allTimedGroupMemberships() {
        java.util.LinkedHashSet<String> worlds = new java.util.LinkedHashSet<>();
        worlds.add(Worlds.GLOBAL);
        worlds.addAll(configuredWorlds());
        return worlds.stream().flatMap(world -> timedGroupMemberships(world).stream()).toList();
    }

    /**
     * Returns seconds until a timed group membership expires in the given world.
     *
     * @param groupName group identifier
     * @param world     world name, or {@link Worlds#GLOBAL} for the global namespace
     * @return seconds until expiry; {@code 0} if membership is not timed
     */
    int groupMembershipRemainingSeconds(String groupName, String world);

    /**
     * Returns seconds until a timed group membership expires in the global namespace.
     *
     * <p>Delegates to {@link #groupMembershipRemainingSeconds(String, String)} with {@link Worlds#GLOBAL}.</p>
     *
     * @param groupName group identifier
     * @return seconds until expiry; {@code 0} if membership is not timed
     */
    default int groupMembershipRemainingSeconds(String groupName) {
        return groupMembershipRemainingSeconds(groupName, Worlds.GLOBAL);
    }
}
