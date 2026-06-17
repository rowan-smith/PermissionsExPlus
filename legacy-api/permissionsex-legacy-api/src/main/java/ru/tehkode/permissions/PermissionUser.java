package ru.tehkode.permissions;

import ru.tehkode.permissions.exceptions.RankingException;

import java.util.List;
import java.util.Map;

/**
 * Classic online-subject facade for a PermissionsEx user.
 *
 * <p>Extends {@link PermissionEntity} and adds group membership, rank-ladder promotion/demotion, and
 * timed group assignment. Concrete implementations live in {@code permissionsex-core}
 * ({@code DefaultPermissionUser}).</p>
 *
 * <p>Inherited {@link PermissionEntity} capabilities include (non-exhaustive): identity and lifecycle
 * ({@link PermissionEntity#getIdentifier()}, {@link PermissionEntity#getName()},
 * {@link PermissionEntity#initialize()}, {@link PermissionEntity#save()}, {@link PermissionEntity#remove()}),
 * permission checks and mutation ({@link PermissionEntity#has(String)},
 * {@link PermissionEntity#addPermission(String)}, {@link PermissionEntity#setPermissions(List, String)}),
 * prefix/suffix/options ({@link PermissionEntity#getPrefix(String)}, {@link PermissionEntity#getOption(String, String)}),
 * timed permissions ({@link PermissionEntity#addTimedPermission(String, String, int)}),
 * and parent/group resolution ({@link PermissionEntity#getParents(String)},
 * {@link PermissionEntity#getOwnParents(String)}, {@link PermissionEntity#setParents(List, String)}).</p>
 *
 * <p>World-scoped overloads use {@code null} for the global (common) context when no world is specified.</p>
 */
public interface PermissionUser extends PermissionEntity {

    /**
     * Returns all parent groups for this user, keyed by world name.
     *
     * @return map of world name to ordered list of parent groups (including inherited parents)
     * @deprecated Use {@link PermissionEntity#getAllParents()} instead.
     */
    @Deprecated(since = "3.0.0")    Map<String, List<PermissionGroup>> getAllGroups();

    /**
     * Adds this user to a group in the given world context.
     *
     * <p>The assignment is direct (own parent); it does not modify group inheritance trees.</p>
     *
     * @param groupName name of the group to join
     * @param worldName world context, or {@code null} for the global context
     */
    void addGroup(String groupName, String worldName);

    /**
     * Adds this user to a group in the global context.
     *
     * @param groupName name of the group to join
     * @see #addGroup(String, String)
     */
    void addGroup(String groupName);

    /**
     * Adds this user to a group in the given world context.
     *
     * @param group     group to join; no-op if {@code null}
     * @param worldName world context, or {@code null} for the global context
     * @see #addGroup(String, String)
     */
    void addGroup(PermissionGroup group, String worldName);

    /**
     * Adds this user to a group in the global context.
     *
     * @param group group to join; no-op if {@code null}
     * @see #addGroup(String, String)
     */
    void addGroup(PermissionGroup group);

    /**
     * Adds this user to a group for a limited time in the given world context.
     *
     * <p>When {@code lifetime} is greater than zero, membership expires after that many seconds.
     * Expiration is tracked via a {@code group-&lt;name&gt;-until} option and enforced by
     * {@link #updateTimedGroups()}. A {@code lifetime} of zero behaves like a permanent
     * {@link #addGroup(String, String)} call.</p>
     *
     * @param groupName name of the group to join
     * @param worldName world context, or {@code null} for the global context
     * @param lifetime  membership duration in seconds; {@code 0} for no expiration
     */
    void addGroup(String groupName, String worldName, long lifetime);

    /**
     * Removes this user's direct membership in a group for the given world context.
     *
     * @param groupName name of the group to leave
     * @param worldName world context, or {@code null} for the global context
     */
    void removeGroup(String groupName, String worldName);

    /**
     * Removes this user's direct membership in a group across all world contexts and the global context.
     *
     * @param groupName name of the group to leave
     * @see #removeGroup(String, String)
     */
    void removeGroup(String groupName);

    /**
     * Removes this user's direct membership in a group for the given world context.
     *
     * @param group     group to leave; no-op if {@code null}
     * @param worldName world context, or {@code null} for the global context
     * @see #removeGroup(String, String)
     */
    void removeGroup(PermissionGroup group, String worldName);

    /**
     * Removes this user's direct membership in a group across all world contexts and the global context.
     *
     * @param group group to leave; no-op if {@code null}
     * @see #removeGroup(String, String)
     */
    void removeGroup(PermissionGroup group);

    /**
     * Checks whether this user belongs to the given group in a world context, optionally considering
     * inherited (indirect) membership.
     *
     * <p>When {@code checkInheritance} is {@code true}, membership is satisfied if the user is directly
     * assigned to {@code group} or to any group that is a descendant of {@code group} in the inheritance
     * tree. When {@code false}, only a direct parent assignment to {@code group} counts.</p>
     *
     * @param group            group to test
     * @param worldName        world context, or {@code null} for the global context
     * @param checkInheritance {@code true} to match descendant groups; {@code false} for direct membership only
     * @return {@code true} if the user is in the group under the given rules
     */
    boolean inGroup(PermissionGroup group, String worldName, boolean checkInheritance);

    /**
     * Checks whether this user belongs to the given group in any known world or the global context.
     *
     * @param group            group to test
     * @param checkInheritance {@code true} to match descendant groups; {@code false} for direct membership only
     * @return {@code true} if the user is in the group in at least one context
     * @see #inGroup(PermissionGroup, String, boolean)
     */
    boolean inGroup(PermissionGroup group, boolean checkInheritance);

    /**
     * Checks whether this user belongs to the named group in a world context.
     *
     * @param groupName        name of the group to test
     * @param worldName        world context, or {@code null} for the global context
     * @param checkInheritance {@code true} to match descendant groups; {@code false} for direct membership only
     * @return {@code true} if the user is in the group under the given rules
     * @see #inGroup(PermissionGroup, String, boolean)
     */
    boolean inGroup(String groupName, String worldName, boolean checkInheritance);

    /**
     * Checks whether this user belongs to the named group in any known world or the global context.
     *
     * @param groupName        name of the group to test
     * @param checkInheritance {@code true} to match descendant groups; {@code false} for direct membership only
     * @return {@code true} if the user is in the group in at least one context
     * @see #inGroup(PermissionGroup, String, boolean)
     */
    boolean inGroup(String groupName, boolean checkInheritance);

    /**
     * Checks whether this user belongs to the given group in a world context, including inherited membership.
     *
     * @param group     group to test
     * @param worldName world context, or {@code null} for the global context
     * @return {@code true} if the user is directly or indirectly in the group
     * @see #inGroup(PermissionGroup, String, boolean)
     */
    boolean inGroup(PermissionGroup group, String worldName);

    /**
     * Checks whether this user belongs to the given group in any context, including inherited membership.
     *
     * @param group group to test
     * @return {@code true} if the user is directly or indirectly in the group
     * @see #inGroup(PermissionGroup, boolean)
     */
    boolean inGroup(PermissionGroup group);

    /**
     * Checks whether this user belongs to the named group in a world context, including inherited membership.
     *
     * @param groupName name of the group to test
     * @param worldName world context, or {@code null} for the global context
     * @return {@code true} if the user is directly or indirectly in the group
     * @see #inGroup(String, String, boolean)
     */
    boolean inGroup(String groupName, String worldName);

    /**
     * Checks whether this user belongs to the named group in any context, including inherited membership.
     *
     * @param groupName name of the group to test
     * @return {@code true} if the user is directly or indirectly in the group
     * @see #inGroup(String, boolean)
     */
    boolean inGroup(String groupName);

    /**
     * Promotes this user one step up on the specified rank ladder.
     *
     * <p>Replaces the user's current ladder group with the next higher-ranked group on the same ladder.
     * Only direct group assignments are changed; inherited parents are not modified.</p>
     *
     * <p>If {@code promoter} is non-null and ranked on the ladder, their rank must be strictly higher
     * (numerically lower) than this user's rank. Pass {@code null} when the action is performed from the
     * console or by a plugin without rank restrictions.</p>
     *
     * @param promoter   user authorizing the promotion, or {@code null} for unrestricted promotion
     * @param ladderName rank ladder name; implementations typically default empty values to {@code "default"}
     * @return the group this user was promoted into
     * @throws RankingException if this user is not on the ladder, the promoter lacks sufficient rank,
     *                          or no higher group exists on the ladder
     * @deprecated Prefer {@code PermissionsEx.getApi().getLadderManager().promote(user, ladderName)}
     *             (modern {@code dev.rono.permissions.api.ladder.LadderManager}). This method remains for
     *             binary compatibility with hook plugins compiled against older releases.
     */
    @Deprecated(since = "3.0.0")
    PermissionGroup promote(PermissionUser promoter, String ladderName) throws RankingException;

    /**
     * Demotes this user one step down on the specified rank ladder.
     *
     * <p>Replaces the user's current ladder group with the next lower-ranked group on the same ladder.
     * Only direct group assignments are changed; inherited parents are not modified.</p>
     *
     * <p>If {@code demoter} is non-null and ranked on the ladder, their rank must be strictly higher
     * (numerically lower) than this user's rank. Pass {@code null} when the action is performed from the
     * console or by a plugin without rank restrictions.</p>
     *
     * @param demoter    user authorizing the demotion, or {@code null} for unrestricted demotion
     * @param ladderName rank ladder name; implementations typically default empty values to {@code "default"}
     * @return the group this user was demoted into
     * @throws RankingException if this user is not on the ladder, the demoter lacks sufficient rank,
     *                          or no lower group exists on the ladder
     * @deprecated Prefer {@code PermissionsEx.getApi().getLadderManager().demote(user, ladderName)}
     *             (modern {@code dev.rono.permissions.api.ladder.LadderManager}). This method remains for
     *             binary compatibility with hook plugins compiled against older releases.
     */
    @Deprecated(since = "3.0.0")
    PermissionGroup demote(PermissionUser demoter, String ladderName) throws RankingException;

    /**
     * Returns whether this user holds a ranked group on the given ladder.
     *
     * @param ladder rank ladder name
     * @return {@code true} if {@link #getRank(String)} would return a value greater than zero
     */
    boolean isRanked(String ladder);

    /**
     * Returns this user's numeric rank on the given ladder.
     *
     * <p>Lower numbers denote higher standing on the ladder. Returns {@code 0} when the user is not
     * ranked on that ladder.</p>
     *
     * @param ladder rank ladder name
     * @return rank value, or {@code 0} if not ranked
     */
    int getRank(String ladder);

    /**
     * Returns the group that places this user on the given rank ladder.
     *
     * @param ladder rank ladder name; implementations typically default empty values to {@code "default"}
     * @return the ranked group, or {@code null} if the user is not on the ladder
     */
    PermissionGroup getRankLadderGroup(String ladder);

    /**
     * Returns every rank ladder this user participates in and the corresponding group for each.
     *
     * @return map of ladder name to the user's group on that ladder; never {@code null}
     */
    Map<String, PermissionGroup> getRankLadders();

    /**
     * Removes expired timed group memberships and schedules the next expiration check.
     *
     * <p>Scans {@code group-&lt;name&gt;-until} options set by {@link #addGroup(String, String, long)}.
     * Called automatically during {@link PermissionEntity#initialize()} and after timed group assignment;
     * plugins normally do not need to invoke this directly.</p>
     */
    void updateTimedGroups();

    /**
     * Returns parent group names for this user in the global context.
     *
     * @return array of group names (including inherited parents)
     * @deprecated Use {@link PermissionEntity#getParentIdentifiers()} instead.
     */
    @Deprecated(since = "3.0.0")    String[] getGroupsNames();

    /**
     * Returns parent group names for this user in the given world context.
     *
     * @param world world context, or {@code null} for the global context
     * @return array of group names (including inherited parents)
     * @deprecated Use {@link PermissionEntity#getParentIdentifiers(String)} instead.
     */
    @Deprecated(since = "3.0.0")    String[] getGroupsNames(String world);

    /**
     * Returns parent groups for this user in the global context.
     *
     * @return array of parent groups (including inherited parents)
     * @deprecated Use {@link PermissionEntity#getParents()} instead.
     */
    @Deprecated(since = "3.0.0")    PermissionGroup[] getGroups();

    /**
     * Returns parent groups for this user in the given world context.
     *
     * @param worldName world context, or {@code null} for the global context
     * @return array of parent groups (including inherited parents)
     * @deprecated Use {@link PermissionEntity#getParents(String)} instead.
     */
    @Deprecated(since = "3.0.0")    PermissionGroup[] getGroups(String worldName);

    /**
     * Returns own (direct) parent group names in the global context.
     *
     * @return array of directly assigned group names
     * @deprecated Use {@link PermissionEntity#getOwnParentIdentifiers()} instead.
     */
    @Deprecated(since = "3.0.0")    String[] getGroupNames();

    /**
     * Returns own (direct) parent group names in the given world context.
     *
     * @param worldName world context, or {@code null} for the global context
     * @return array of directly assigned group names
     * @deprecated Use {@link PermissionEntity#getOwnParentIdentifiers(String)} instead.
     */
    @Deprecated(since = "3.0.0")    String[] getGroupNames(String worldName);

    /**
     * Replaces this user's direct parent groups in the given world context.
     *
     * @param groups    new parent group names
     * @param worldName world context, or {@code null} for the global context
     * @deprecated Use {@link PermissionEntity#setParentsIdentifier(List, String)} instead.
     */
    @Deprecated(since = "3.0.0")    void setGroups(String[] groups, String worldName);

    /**
     * Replaces this user's direct parent groups in the global context.
     *
     * @param groups new parent group names
     * @deprecated Use {@link PermissionEntity#setParentsIdentifier(List)} instead.
     */
    @Deprecated(since = "3.0.0")    void setGroups(String[] groups);

    /**
     * Replaces this user's direct parent groups in the given world context.
     *
     * @param parentGroups new parent group objects
     * @param worldName    world context, or {@code null} for the global context
     * @deprecated Use {@link PermissionEntity#setParents(List, String)} instead.
     */
    @Deprecated(since = "3.0.0")    void setGroups(PermissionGroup[] parentGroups, String worldName);

    /**
     * Replaces this user's direct parent groups in the global context.
     *
     * @param parentGroups new parent group objects
     * @deprecated Use {@link PermissionEntity#setParents(List)} instead.
     */
    @Deprecated(since = "3.0.0")    void setGroups(PermissionGroup[] parentGroups);
}
