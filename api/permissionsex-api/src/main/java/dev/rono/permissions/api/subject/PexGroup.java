package dev.rono.permissions.api.subject;

import dev.rono.permissions.api.world.PexWorlds;
import java.util.List;
import java.util.Set;

/**
 * Modern view of a permission group.
 *
 * <p>Extends {@link PexPermissionSubject} with weight, default-group flags, parent/child relationships,
 * rank-ladder metadata, and membership queries.</p>
 */
public interface PexGroup extends PexPermissionSubject {

    /**
     * Returns {@link PexSubjectType#GROUP}.
     *
     * @return {@link PexSubjectType#GROUP}
     */
    @Override
    default PexSubjectType type() {
        return PexSubjectType.GROUP;
    }

    /**
     * Returns a world-scoped view of this group for permission, hierarchy, and metadata operations.
     *
     * <p>Methods on the returned context apply to {@code world} without repeating the world argument.</p>
     *
     * @param world world name, or {@link PexWorlds#GLOBAL} for the global namespace
     * @return world-bound group context
     */
    @Override
    default PexGroupWorldContext inWorld(String world) {
        return SubjectWorldContexts.group(this, world);
    }

    /**
     * Returns a view of this group bound to the global namespace.
     *
     * <p>Equivalent to {@link #inWorld(String)} with {@link PexWorlds#GLOBAL}.</p>
     *
     * @return global world context for this group
     */
    @Override
    default PexGroupWorldContext global() {
        return inWorld(PexWorlds.GLOBAL);
    }

    /**
     * Returns the sort weight for this group.
     *
     * <p>Higher weights typically take precedence when resolving conflicting inherited values.</p>
     *
     * @return group weight
     */
    int weight();

    /**
     * Sets the sort weight for this group.
     *
     * @param weight new weight value
     */
    void setWeight(int weight);

    /**
     * Returns whether this group is marked as the default group in the given world.
     *
     * @param world world name, or {@link PexWorlds#GLOBAL} for the global namespace
     * @return {@code true} if the group is a default group for new users in that context
     */
    boolean isDefault(String world);

    /**
     * Returns whether this group is marked as the default group in the global namespace.
     *
     * <p>Delegates to {@link #isDefault(String)} with {@link PexWorlds#GLOBAL}.</p>
     *
     * @return {@code true} if the group is a default group for new users globally
     */
    default boolean isDefault() {
        return isDefault(PexWorlds.GLOBAL);
    }

    /**
     * Marks or clears this group as the default group in the given world.
     *
     * @param value {@code true} to mark as default; {@code false} to clear
     * @param world world name, or {@link PexWorlds#GLOBAL} for the global namespace
     */
    void setDefault(boolean value, String world);

    /**
     * Marks or clears this group as the default group in the global namespace.
     *
     * <p>Delegates to {@link #setDefault(boolean, String)} with {@link PexWorlds#GLOBAL}.</p>
     *
     * @param value {@code true} to mark as default; {@code false} to clear
     */
    default void setDefault(boolean value) {
        setDefault(value, PexWorlds.GLOBAL);
    }

    /**
     * Returns direct parent group identifiers in the given world.
     *
     * @param world world name, or {@link PexWorlds#GLOBAL} for the global namespace
     * @return list of own parent group identifiers
     */
    List<String> parents(String world);

    /**
     * Returns direct parent group identifiers in the global namespace.
     *
     * <p>Delegates to {@link #parents(String)} with {@link PexWorlds#GLOBAL}.</p>
     *
     * @return list of own parent group identifiers
     */
    default List<String> parents() {
        return parents(PexWorlds.GLOBAL);
    }

    /**
     * Returns effective parent group identifiers in the given world (inheritance expanded).
     *
     * @param world world name, or {@link PexWorlds#GLOBAL} for the global namespace
     * @return list of parent group identifiers including transitive parents
     */
    List<String> parentTree(String world);

    /**
     * Returns effective parent group identifiers in the global namespace (inheritance expanded).
     *
     * <p>Delegates to {@link #parentTree(String)} with {@link PexWorlds#GLOBAL}.</p>
     *
     * @return list of parent group identifiers including transitive parents
     */
    default List<String> parentTree() {
        return parentTree(PexWorlds.GLOBAL);
    }

    /**
     * Adds a direct parent group in the given world.
     *
     * @param parentName parent group identifier to add
     * @param world      world name, or {@link PexWorlds#GLOBAL} for the global namespace
     */
    void addParent(String parentName, String world);

    /**
     * Adds a direct parent group in the global namespace.
     *
     * <p>Delegates to {@link #addParent(String, String)} with {@link PexWorlds#GLOBAL}.</p>
     *
     * @param parentName parent group identifier to add
     */
    default void addParent(String parentName) {
        addParent(parentName, PexWorlds.GLOBAL);
    }

    /**
     * Removes a direct parent group in the given world.
     *
     * @param parentName parent group identifier to remove
     * @param world      world name, or {@link PexWorlds#GLOBAL} for the global namespace
     */
    void removeParent(String parentName, String world);

    /**
     * Removes a direct parent group in the global namespace.
     *
     * <p>Delegates to {@link #removeParent(String, String)} with {@link PexWorlds#GLOBAL}.</p>
     *
     * @param parentName parent group identifier to remove
     */
    default void removeParent(String parentName) {
        removeParent(parentName, PexWorlds.GLOBAL);
    }

    /**
     * Replaces direct parent groups in the given world.
     *
     * @param parentNames new parent group identifiers
     * @param world       world name, or {@link PexWorlds#GLOBAL} for the global namespace
     */
    void setParents(List<String> parentNames, String world);

    /**
     * Replaces direct parent groups in the global namespace.
     *
     * <p>Delegates to {@link #setParents(List, String)} with {@link PexWorlds#GLOBAL}.</p>
     *
     * @param parentNames new parent group identifiers
     */
    default void setParents(List<String> parentNames) {
        setParents(parentNames, PexWorlds.GLOBAL);
    }

    /**
     * Returns whether this group is a child of the named group in the given world.
     *
     * @param groupName group identifier to test as a potential ancestor
     * @param world     world name, or {@link PexWorlds#GLOBAL} for the global namespace
     * @param inherit   when {@code true}, match transitive parent relationships
     * @return {@code true} if this group is a direct or inherited child of {@code groupName}
     */
    boolean isChildOf(String groupName, String world, boolean inherit);

    /**
     * Returns whether this group is a child of the named group in the given world, including transitive parents.
     *
     * <p>Delegates to {@link #isChildOf(String, String, boolean)} with {@code inherit = true}.</p>
     *
     * @param groupName group identifier to test as a potential ancestor
     * @param world     world name, or {@link PexWorlds#GLOBAL} for the global namespace
     * @return {@code true} if this group is a direct or inherited child of {@code groupName}
     */
    default boolean isChildOf(String groupName, String world) {
        return isChildOf(groupName, world, true);
    }

    /**
     * Returns whether this group is a child of the named group in the global namespace, including transitive parents.
     *
     * <p>Delegates to {@link #isChildOf(String, String, boolean)} with {@link PexWorlds#GLOBAL} and
     * {@code inherit = true}.</p>
     *
     * @param groupName group identifier to test as a potential ancestor
     * @return {@code true} if this group is a direct or inherited child of {@code groupName}
     */
    default boolean isChildOf(String groupName) {
        return isChildOf(groupName, PexWorlds.GLOBAL, true);
    }

    /**
     * Returns this group's numeric rank on its assigned rank ladder.
     *
     * <p>Lower numbers denote higher standing. Returns {@code 0} when the group is not on a ladder.</p>
     *
     * @return rank value, or {@code 0} if not ranked
     */
    int rank();

    /**
     * Returns the rank ladder this group belongs to.
     *
     * @return ladder name, or {@code null} if the group is not on a ladder
     */
    String rankLadder();

    /**
     * Assigns this group to a rank ladder at the given rank.
     *
     * @param rank   numeric rank on the ladder (lower = higher standing)
     * @param ladder rank ladder name
     */
    void setRank(int rank, String ladder);

    /**
     * Returns user identifiers with direct membership in this group for the given world.
     *
     * @param world world name, or {@link PexWorlds#GLOBAL} for the global namespace
     * @return set of user identifiers with direct membership
     */
    Set<String> memberIdentifiers(String world);

    /**
     * Returns user identifiers with direct membership in this group in the global namespace.
     *
     * <p>Delegates to {@link #memberIdentifiers(String)} with {@link PexWorlds#GLOBAL}.</p>
     *
     * @return set of user identifiers with direct membership
     */
    default Set<String> memberIdentifiers() {
        return memberIdentifiers(PexWorlds.GLOBAL);
    }

    /**
     * Returns users belonging to this group in the given world.
     *
     * @param world   world name, or {@link PexWorlds#GLOBAL} for the global namespace
     * @param inherit when {@code true}, includes users in descendant groups
     * @return list of users in this group
     */
    List<PexUser> members(String world, boolean inherit);

    /**
     * Returns users with direct membership in this group for the given world.
     *
     * <p>Delegates to {@link #members(String, boolean)} with {@code inherit = false}.</p>
     *
     * @param world world name, or {@link PexWorlds#GLOBAL} for the global namespace
     * @return list of users with direct membership
     */
    default List<PexUser> members(String world) {
        return members(world, false);
    }

    /**
     * Returns users with direct membership in this group in the global namespace.
     *
     * <p>Delegates to {@link #members(String)} with {@link PexWorlds#GLOBAL}.</p>
     *
     * @return list of users with direct membership
     */
    default List<PexUser> members() {
        return members(PexWorlds.GLOBAL);
    }

    /**
     * Returns child groups of this group in the given world.
     *
     * @param world   world name, or {@link PexWorlds#GLOBAL} for the global namespace
     * @param inherit when {@code true}, includes all descendant groups
     * @return list of child groups
     */
    List<PexGroup> children(String world, boolean inherit);

    /**
     * Returns direct child groups of this group in the given world.
     *
     * <p>Delegates to {@link #children(String, boolean)} with {@code inherit = false}.</p>
     *
     * @param world world name, or {@link PexWorlds#GLOBAL} for the global namespace
     * @return list of direct child groups
     */
    default List<PexGroup> children(String world) {
        return children(world, false);
    }

    /**
     * Returns direct child groups of this group in the global namespace.
     *
     * <p>Delegates to {@link #children(String)} with {@link PexWorlds#GLOBAL}.</p>
     *
     * @return list of direct child groups
     */
    default List<PexGroup> children() {
        return children(PexWorlds.GLOBAL);
    }

    /**
     * Returns all descendant groups of this group in the given world.
     *
     * <p>Delegates to {@link #children(String, boolean)} with {@code inherit = true}.</p>
     *
     * @param world world name, or {@link PexWorlds#GLOBAL} for the global namespace
     * @return list of all descendant groups
     */
    default List<PexGroup> descendants(String world) {
        return children(world, true);
    }

    /**
     * Returns all descendant groups of this group in the global namespace.
     *
     * <p>Delegates to {@link #descendants(String)} with {@link PexWorlds#GLOBAL}.</p>
     *
     * @return list of all descendant groups
     */
    default List<PexGroup> descendants() {
        return descendants(PexWorlds.GLOBAL);
    }

    /**
     * Returns currently online users with direct membership in this group.
     *
     * @return list of online users with direct membership
     */
    List<PexUser> activeMembers();

    /**
     * Returns currently online users in this group.
     *
     * @param inherit when {@code true}, includes online users in descendant groups
     * @return list of online users in this group
     */
    List<PexUser> activeMembers(boolean inherit);
}
