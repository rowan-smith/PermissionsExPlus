package ru.tehkode.permissions;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Classic group node in the PermissionsEx inheritance graph.
 *
 * <p>Extends {@link PermissionEntity} and adds group hierarchy, weight-based ordering, rank-ladder metadata,
 * membership listing, and default-group flags. Concrete implementations live in {@code permissionsex-core}
 * ({@code DefaultPermissionGroup}). Groups are ordered by {@link #getWeight()} via
 * {@link Comparable}.</p>
 *
 * <p>Inherited {@link PermissionEntity} capabilities include (non-exhaustive): identity and lifecycle
 * ({@link PermissionEntity#getIdentifier()}, {@link PermissionEntity#getName()},
 * {@link PermissionEntity#initialize()}, {@link PermissionEntity#save()}, {@link PermissionEntity#remove()}),
 * permission checks and mutation ({@link PermissionEntity#has(String)},
 * {@link PermissionEntity#addPermission(String, String)}, {@link PermissionEntity#setPermissions(List, String)}),
 * prefix/suffix/options ({@link PermissionEntity#getPrefix(String)}, {@link PermissionEntity#setOption(String, String, String)}),
 * and parent resolution ({@link PermissionEntity#getParents(String)},
 * {@link PermissionEntity#getOwnParents(String)}, {@link PermissionEntity#setParents(List, String)}).</p>
 *
 * <p><strong>Child vs. descendant groups:</strong> {@link #getChildGroups(String)} returns groups that list
 * this group as a direct parent. {@link #getDescendantGroups(String)} returns all groups reachable through
 * the inheritance tree below this group (children, grandchildren, and so on).</p>
 *
 * <p>World-scoped overloads use {@code null} for the global (common) context when no world is specified.</p>
 */
public interface PermissionGroup extends PermissionEntity, Comparable<PermissionGroup> {

    /**
     * Returns the sort weight of this group.
     *
     * <p>Higher weights sort later when groups are ordered for inheritance resolution. The weight is
     * typically stored as a {@code weight} option on the group.</p>
     *
     * @return group weight; {@code 0} when unset
     */
    int getWeight();

    /**
     * Sets the sort weight of this group.
     *
     * @param weight new weight value
     */
    void setWeight(int weight);

    /**
     * Returns whether this group participates in a rank ladder.
     *
     * @return {@code true} if {@link #getRank()} is greater than zero
     */
    boolean isRanked();

    /**
     * Returns this group's numeric rank on its ladder.
     *
     * <p>Lower numbers denote higher standing. Returns {@code 0} when the group is not ranked.</p>
     *
     * @return rank value, or {@code 0} if not ranked
     */
    int getRank();

    /**
     * Sets this group's rank on its ladder.
     *
     * <p>Specify {@code 0} to remove the group from ranking.</p>
     *
     * @param rank rank value; {@code 0} clears ranking
     */
    void setRank(int rank);

    /**
     * Returns the name of the rank ladder this group belongs to.
     *
     * @return ladder name; implementations typically default unset values to {@code "default"}
     */
    String getRankLadder();

    /**
     * Sets the rank ladder for this group.
     *
     * @param rankLadder ladder name; empty or {@code "default"} typically clears to the default ladder
     */
    void setRankLadder(String rankLadder);

    /**
     * Checks whether this group inherits from the given group in a world context.
     *
     * <p>When {@code checkInheritance} is {@code true}, any ancestor in the parent chain matches.
     * When {@code false}, only a direct parent assignment matches.</p>
     *
     * @param group            candidate parent group
     * @param worldName        world context, or {@code null} for the global context
     * @param checkInheritance {@code true} to walk the full parent chain; {@code false} for direct parent only
     * @return {@code true} if this group is a child (direct or indirect) of {@code group}
     */
    boolean isChildOf(PermissionGroup group, String worldName, boolean checkInheritance);

    /**
     * Checks whether this group inherits from the given group in any known world or the global context.
     *
     * @param group            candidate parent group
     * @param checkInheritance {@code true} to walk the full parent chain; {@code false} for direct parent only
     * @return {@code true} if this group is a child of {@code group} in at least one context
     * @see #isChildOf(PermissionGroup, String, boolean)
     */
    boolean isChildOf(PermissionGroup group, boolean checkInheritance);

    /**
     * Checks whether the given group is a direct parent of this group in a world context.
     *
     * @param group     candidate parent group
     * @param worldName world context, or {@code null} for the global context
     * @return {@code true} if {@code group} is an immediate parent
     * @see #isChildOf(PermissionGroup, String, boolean)
     */
    boolean isChildOf(PermissionGroup group, String worldName);

    /**
     * Checks whether the given group is a direct parent of this group in the global context.
     *
     * @param group candidate parent group
     * @return {@code true} if {@code group} is an immediate parent
     * @see #isChildOf(PermissionGroup, String)
     */
    boolean isChildOf(PermissionGroup group);

    /**
     * Checks whether this group inherits from the named group in a world context.
     *
     * @param groupName        name of the candidate parent group
     * @param worldName        world context, or {@code null} for the global context
     * @param checkInheritance {@code true} to walk the full parent chain; {@code false} for direct parent only
     * @return {@code true} if this group is a child (direct or indirect) of the named group
     * @see #isChildOf(PermissionGroup, String, boolean)
     */
    boolean isChildOf(String groupName, String worldName, boolean checkInheritance);

    /**
     * Checks whether this group inherits from the named group in any known world or the global context.
     *
     * @param groupName        name of the candidate parent group
     * @param checkInheritance {@code true} to walk the full parent chain; {@code false} for direct parent only
     * @return {@code true} if this group is a child of the named group in at least one context
     * @see #isChildOf(PermissionGroup, String, boolean)
     */
    boolean isChildOf(String groupName, boolean checkInheritance);

    /**
     * Checks whether the named group is a direct parent of this group in a world context.
     *
     * @param groupName name of the candidate parent group
     * @param worldName world context, or {@code null} for the global context
     * @return {@code true} if the named group is an immediate parent
     * @see #isChildOf(String, String, boolean)
     */
    boolean isChildOf(String groupName, String worldName);

    /**
     * Checks whether the named group is a direct parent of this group in the global context.
     *
     * @param groupName name of the candidate parent group
     * @return {@code true} if the named group is an immediate parent
     * @see #isChildOf(String, String)
     */
    boolean isChildOf(String groupName);

    /**
     * Returns groups that have this group as a direct parent in the given world context.
     *
     * <p>Does not include deeper descendants; use {@link #getDescendantGroups(String)} for the full subtree.</p>
     *
     * @param worldName world context, or {@code null} for the global context
     * @return list of immediate child groups; never {@code null}
     */
    List<PermissionGroup> getChildGroups(String worldName);

    /**
     * Returns groups that have this group as a direct parent in the global context.
     *
     * @return list of immediate child groups; never {@code null}
     * @see #getChildGroups(String)
     */
    List<PermissionGroup> getChildGroups();

    /**
     * Returns all groups below this one in the inheritance tree for the given world context.
     *
     * <p>Includes direct children and every group reachable through nested parent links (descendants).</p>
     *
     * @param worldName world context, or {@code null} for the global context
     * @return list of descendant groups; never {@code null}
     * @see #getChildGroups(String)
     */
    List<PermissionGroup> getDescendantGroups(String worldName);

    /**
     * Returns all groups below this one in the inheritance tree in the global context.
     *
     * @return list of descendant groups; never {@code null}
     * @see #getDescendantGroups(String)
     */
    List<PermissionGroup> getDescendantGroups();

    /**
     * Returns users directly assigned to this group in the given world context.
     *
     * <p>Loads membership from the backend; includes users that may not currently be loaded in memory.
     * Does not include users who inherit this group only through another group.</p>
     *
     * @param worldName world context, or {@code null} for the global context
     * @return set of direct member users; never {@code null}
     */
    Set<PermissionUser> getUsers(String worldName);

    /**
     * Returns users directly assigned to this group across all contexts.
     *
     * @return set of direct member users; never {@code null}
     * @see #getUsers(String)
     */
    Set<PermissionUser> getUsers();

    /**
     * Returns users currently loaded in memory who belong to this group.
     *
     * <p>Only considers direct membership (same as {@link #getActiveUsers(boolean)} with
     * {@code inheritance = false}). For users assigned to descendant groups, use
     * {@link #getActiveUsers(boolean)} with {@code inheritance = true}.</p>
     *
     * @return set of active, directly assigned users; never {@code null}
     * @see #getActiveUsers(boolean)
     */
    Set<PermissionUser> getActiveUsers();

    /**
     * Returns users currently loaded in memory who belong to this group.
     *
     * <p>When {@code inheritance} is {@code false}, only users with a direct parent assignment to this
     * group are included. When {@code true}, users assigned to any descendant group of this group are
     * also included (matching {@link PermissionUser#inGroup(String, boolean)} semantics).</p>
     *
     * @param inheritance {@code true} to include users in descendant groups; {@code false} for direct members only
     * @return set of matching active users; never {@code null}
     */
    Set<PermissionUser> getActiveUsers(boolean inheritance);

    /**
     * Returns whether this group is marked as the default group in the given world context.
     *
     * @param worldName world context, or {@code null} for the global context
     * @return {@code true} if the group is a default group for new users in that context
     */
    boolean isDefault(String worldName);

    /**
     * Marks or clears this group as the default group in the given world context.
     *
     * @param def       {@code true} to mark as default; {@code false} to clear
     * @param worldName world context, or {@code null} for the global context
     */
    void setDefault(boolean def, String worldName);

    /**
     * Adds a direct parent group in the given world context.
     *
     * <p>No-op if the parent is already assigned. Does not create duplicate entries.</p>
     *
     * @param parent    parent group to add; no-op if {@code null}
     * @param worldName world context, or {@code null} for the global context
     */
    void addParent(PermissionGroup parent, String worldName);

    /**
     * Adds a direct parent group in the global context.
     *
     * @param parent parent group to add; no-op if {@code null}
     * @see #addParent(PermissionGroup, String)
     */
    void addParent(PermissionGroup parent);

    /**
     * Adds a direct parent group by name in the given world context.
     *
     * @param parentName name of the parent group
     * @param worldName  world context, or {@code null} for the global context
     * @see #addParent(PermissionGroup, String)
     */
    void addParent(String parentName, String worldName);

    /**
     * Adds a direct parent group by name in the global context.
     *
     * @param parentName name of the parent group
     * @see #addParent(PermissionGroup, String)
     */
    void addParent(String parentName);

    /**
     * Removes a direct parent group in the given world context.
     *
     * @param parent    parent group to remove; no-op if {@code null}
     * @param worldName world context, or {@code null} for the global context
     */
    void removeParent(PermissionGroup parent, String worldName);

    /**
     * Removes a direct parent group in the global context.
     *
     * @param parent parent group to remove; no-op if {@code null}
     * @see #removeParent(PermissionGroup, String)
     */
    void removeParent(PermissionGroup parent);

    /**
     * Removes a direct parent group by name in the given world context.
     *
     * @param parentName name of the parent group to remove
     * @param worldName  world context, or {@code null} for the global context
     * @see #removeParent(PermissionGroup, String)
     */
    void removeParent(String parentName, String worldName);

    /**
     * Removes a direct parent group by name in the global context.
     *
     * @param parentName name of the parent group to remove
     * @see #removeParent(PermissionGroup, String)
     */
    void removeParent(String parentName);

    /**
     * Returns direct parent group names in the given world context.
     *
     * @param worldName world context, or {@code null} for the global context
     * @return array of own parent group names
     * @deprecated Use {@link PermissionEntity#getOwnParentIdentifiers(String)} instead.
     */
    @Deprecated(since = "3.0.0")    String[] getParentGroupsNames(String worldName);

    /**
     * Returns direct parent group names in the global context.
     *
     * @return array of own parent group names
     * @deprecated Use {@link PermissionEntity#getOwnParentIdentifiers()} instead.
     */
    @Deprecated(since = "3.0.0")    String[] getParentGroupsNames();

    /**
     * Replaces direct parent groups by name in the given world context.
     *
     * @param parentGroups new parent group names
     * @param worldName    world context, or {@code null} for the global context
     * @deprecated Use {@link PermissionEntity#setParentsIdentifier(List, String)} instead.
     */
    @Deprecated(since = "3.0.0")    void setParentGroups(List<String> parentGroups, String worldName);

    /**
     * Replaces direct parent groups by name in the global context.
     *
     * @param parentGroups new parent group names
     * @deprecated Use {@link PermissionEntity#setParentsIdentifier(List)} instead.
     */
    @Deprecated(since = "3.0.0")    void setParentGroups(List<String> parentGroups);

    /**
     * Replaces direct parent groups in the given world context.
     *
     * @param parentGroups new parent group objects
     * @param worldName    world context, or {@code null} for the global context
     * @deprecated Use {@link PermissionEntity#setParents(List, String)} instead.
     */
    @Deprecated(since = "3.0.0")    void setParentGroupObjects(List<PermissionGroup> parentGroups, String worldName);

    /**
     * Replaces direct parent groups in the global context.
     *
     * @param parentGroups new parent group objects
     * @deprecated Use {@link PermissionEntity#setParents(List)} instead.
     */
    @Deprecated(since = "3.0.0")    void setParentGroupObjects(List<PermissionGroup> parentGroups);

    /**
     * Returns parent groups in the given world context, including inherited parents.
     *
     * @param worldName world context, or {@code null} for the global context
     * @return list of parent groups
     * @deprecated Use {@link PermissionEntity#getParents(String)} instead.
     */
    @Deprecated(since = "3.0.0")    List<PermissionGroup> getParentGroups(String worldName);

    /**
     * Returns parent groups in the global context, including inherited parents.
     *
     * @return list of parent groups
     * @deprecated Use {@link PermissionEntity#getParents()} instead.
     */
    @Deprecated(since = "3.0.0")    List<PermissionGroup> getParentGroups();

    /**
     * Returns all parent groups keyed by world name, including inherited parents.
     *
     * @return map of world name to parent group lists
     * @deprecated Use {@link PermissionEntity#getAllParents()} instead.
     */
    @Deprecated(since = "3.0.0")    Map<String, List<PermissionGroup>> getAllParentGroups();
}
