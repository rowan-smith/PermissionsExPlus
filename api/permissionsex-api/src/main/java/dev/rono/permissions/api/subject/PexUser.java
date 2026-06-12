package dev.rono.permissions.api.subject;

import dev.rono.permissions.api.PexRankingException;
import dev.rono.permissions.api.world.PexWorlds;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Modern view of a permission user.
 *
 * <p>Extends {@link PexPermissionSubject} with group membership, rank-ladder promotion/demotion, and
 * timed group assignment.</p>
 */
public interface PexUser extends PexPermissionSubject {

    /**
     * Returns {@link PexSubjectType#USER}.
     *
     * @return {@link PexSubjectType#USER}
     */
    @Override
    default PexSubjectType type() {
        return PexSubjectType.USER;
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
     * @param world world name, or {@link PexWorlds#GLOBAL} for the global namespace
     * @return world-bound user context
     */
    @Override
    default PexUserWorldContext inWorld(String world) {
        return SubjectWorldContexts.user(this, world);
    }

    /**
     * Returns a view of this user bound to the global namespace.
     *
     * <p>Equivalent to {@link #inWorld(String)} with {@link PexWorlds#GLOBAL}.</p>
     *
     * @return global world context for this user
     */
    @Override
    default PexUserWorldContext global() {
        return inWorld(PexWorlds.GLOBAL);
    }

    /**
     * Returns group identifiers the user inherits in the given world.
     *
     * <p>When {@code inherit} is {@code true}, includes parents of parents; when {@code false}, returns
     * only direct group assignments.</p>
     *
     * @param world   world name, or {@link PexWorlds#GLOBAL} for the global namespace
     * @param inherit when {@code true}, expand transitive group inheritance
     * @return list of group identifiers
     */
    List<String> groups(String world, boolean inherit);

    /**
     * Returns group identifiers the user inherits in the given world, including transitive inheritance.
     *
     * <p>Delegates to {@link #groups(String, boolean)} with {@code inherit = true}.</p>
     *
     * @param world world name, or {@link PexWorlds#GLOBAL} for the global namespace
     * @return list of group identifiers
     */
    default List<String> groups(String world) {
        return groups(world, true);
    }

    /**
     * Returns group identifiers the user inherits in the global namespace, including transitive inheritance.
     *
     * <p>Delegates to {@link #groups(String, boolean)} with {@link PexWorlds#GLOBAL} and {@code inherit = true}.</p>
     *
     * @return list of group identifiers
     */
    default List<String> groups() {
        return groups(PexWorlds.GLOBAL, true);
    }

    /**
     * Returns whether this user belongs to the named group in the given world.
     *
     * @param groupName group identifier to test
     * @param world     world name, or {@link PexWorlds#GLOBAL} for the global namespace
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
     * @param world     world name, or {@link PexWorlds#GLOBAL} for the global namespace
     * @return {@code true} if the user is in the group, {@code false} otherwise
     */
    default boolean inGroup(String groupName, String world) {
        return inGroup(groupName, world, true);
    }

    /**
     * Returns whether this user belongs to the named group in the global namespace, including transitive membership.
     *
     * <p>Delegates to {@link #inGroup(String, String, boolean)} with {@link PexWorlds#GLOBAL} and
     * {@code inherit = true}.</p>
     *
     * @param groupName group identifier to test
     * @return {@code true} if the user is in the group, {@code false} otherwise
     */
    default boolean inGroup(String groupName) {
        return inGroup(groupName, PexWorlds.GLOBAL, true);
    }

    /**
     * Adds this user to a group in the given world.
     *
     * @param groupName group identifier to join
     * @param world     world name, or {@link PexWorlds#GLOBAL} for the global namespace
     */
    void addGroup(String groupName, String world);

    /**
     * Adds this user to a group in the given world with a timed membership.
     *
     * @param groupName        group identifier to join
     * @param world            world name, or {@link PexWorlds#GLOBAL} for the global namespace
     * @param lifetimeSeconds  seconds until membership expires; {@code 0} for transient (in-memory only)
     */
    void addGroup(String groupName, String world, int lifetimeSeconds);

    /**
     * Adds this user to a group in the global namespace.
     *
     * <p>Delegates to {@link #addGroup(String, String)} with {@link PexWorlds#GLOBAL}.</p>
     *
     * @param groupName group identifier to join
     */
    default void addGroup(String groupName) {
        addGroup(groupName, PexWorlds.GLOBAL);
    }

    /**
     * Adds this user to a group in the global namespace with a timed membership.
     *
     * <p>Delegates to {@link #addGroup(String, String, int)} with {@link PexWorlds#GLOBAL}.</p>
     *
     * @param groupName       group identifier to join
     * @param lifetimeSeconds seconds until membership expires; {@code 0} for transient (in-memory only)
     */
    default void addGroup(String groupName, int lifetimeSeconds) {
        addGroup(groupName, PexWorlds.GLOBAL, lifetimeSeconds);
    }

    /**
     * Removes this user from a group in the given world.
     *
     * @param groupName group identifier to leave
     * @param world     world name, or {@link PexWorlds#GLOBAL} for the global namespace
     */
    void removeGroup(String groupName, String world);

    /**
     * Removes this user from a group in the global namespace.
     *
     * <p>Delegates to {@link #removeGroup(String, String)} with {@link PexWorlds#GLOBAL}.</p>
     *
     * @param groupName group identifier to leave
     */
    default void removeGroup(String groupName) {
        removeGroup(groupName, PexWorlds.GLOBAL);
    }

    /**
     * Returns timed group memberships in the given world.
     *
     * @param world world name, or {@link PexWorlds#GLOBAL} for the global namespace
     * @return list of timed group membership entries
     */
    List<PexTimedGroupMembership> timedGroupMemberships(String world);

    /**
     * Returns timed group memberships across every configured world, including global.
     *
     * @return aggregated timed group memberships from all worlds with data
     */
    default List<PexTimedGroupMembership> allTimedGroupMemberships() {
        java.util.LinkedHashSet<String> worlds = new java.util.LinkedHashSet<>();
        worlds.add(PexWorlds.GLOBAL);
        worlds.addAll(configuredWorlds());
        return worlds.stream().flatMap(world -> timedGroupMemberships(world).stream()).toList();
    }

    /**
     * Returns seconds until a timed group membership expires in the given world.
     *
     * @param groupName group identifier
     * @param world     world name, or {@link PexWorlds#GLOBAL} for the global namespace
     * @return seconds until expiry; {@code 0} if membership is not timed
     */
    int groupMembershipRemainingSeconds(String groupName, String world);

    /**
     * Returns seconds until a timed group membership expires in the global namespace.
     *
     * <p>Delegates to {@link #groupMembershipRemainingSeconds(String, String)} with {@link PexWorlds#GLOBAL}.</p>
     *
     * @param groupName group identifier
     * @return seconds until expiry; {@code 0} if membership is not timed
     */
    default int groupMembershipRemainingSeconds(String groupName) {
        return groupMembershipRemainingSeconds(groupName, PexWorlds.GLOBAL);
    }

    /**
     * Promotes this user one step up on the specified rank ladder without rank restrictions.
     *
     * <p>Equivalent to {@link #promote(PexUser, String)} with a {@code null} promoter (console/plugin).</p>
     *
     * @param ladderName rank ladder name; implementations typically default empty values to {@code "default"}
     * @return the group this user was promoted into
     * @throws PexRankingException if this user is not on the ladder or no higher group exists
     */
    PexGroup promote(String ladderName) throws PexRankingException;

    /**
     * Promotes this user one step up on the specified rank ladder.
     *
     * <p>Replaces the user's current ladder group with the next higher-ranked group on the same ladder.
     * If {@code promoter} is non-null and ranked on the ladder, their rank must be strictly higher
     * (numerically lower) than this user's rank. Pass {@code null} when the action is performed from the
     * console or by a plugin without rank restrictions.</p>
     *
     * @param promoter   user authorizing the promotion, or {@code null} for unrestricted promotion
     * @param ladderName rank ladder name; implementations typically default empty values to {@code "default"}
     * @return the group this user was promoted into
     * @throws PexRankingException if this user is not on the ladder, the promoter lacks sufficient rank,
     *                          or no higher group exists on the ladder
     */
    PexGroup promote(PexUser promoter, String ladderName) throws PexRankingException;

    /**
     * Demotes this user one step down on the specified rank ladder without rank restrictions.
     *
     * <p>Equivalent to {@link #demote(PexUser, String)} with a {@code null} demoter (console/plugin).</p>
     *
     * @param ladderName rank ladder name; implementations typically default empty values to {@code "default"}
     * @return the group this user was demoted into
     * @throws PexRankingException if this user is not on the ladder or no lower group exists
     */
    PexGroup demote(String ladderName) throws PexRankingException;

    /**
     * Demotes this user one step down on the specified rank ladder.
     *
     * <p>Replaces the user's current ladder group with the next lower-ranked group on the same ladder.
     * If {@code demoter} is non-null and ranked on the ladder, their rank must be strictly higher
     * (numerically lower) than this user's rank. Pass {@code null} when the action is performed from the
     * console or by a plugin without rank restrictions.</p>
     *
     * @param demoter    user authorizing the demotion, or {@code null} for unrestricted demotion
     * @param ladderName rank ladder name; implementations typically default empty values to {@code "default"}
     * @return the group this user was demoted into
     * @throws PexRankingException if this user is not on the ladder, the demoter lacks sufficient rank,
     *                          or no lower group exists on the ladder
     */
    PexGroup demote(PexUser demoter, String ladderName) throws PexRankingException;

    /**
     * Returns whether this user holds a ranked group on the given ladder.
     *
     * @param ladderName rank ladder name
     * @return {@code true} if {@link #rank(String)} would return a value greater than zero
     */
    boolean isRanked(String ladderName);

    /**
     * Returns this user's numeric rank on the given ladder.
     *
     * <p>Lower numbers denote higher standing on the ladder. Returns {@code 0} when the user is not
     * ranked on that ladder.</p>
     *
     * @param ladderName rank ladder name
     * @return rank value, or {@code 0} if not ranked
     */
    int rank(String ladderName);
}
