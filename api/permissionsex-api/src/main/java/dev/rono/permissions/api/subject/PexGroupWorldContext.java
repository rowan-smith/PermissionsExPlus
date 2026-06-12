package dev.rono.permissions.api.subject;

import java.util.List;
import java.util.Set;

/**
 * World-scoped view of a {@link PexGroup}.
 *
 * <p>Every method applies to the bound world from {@link #world()} (see {@link PexSubjectWorldContext}).</p>
 */
public interface PexGroupWorldContext extends PexSubjectWorldContext {

    /**
     * Returns the underlying group.
     *
     * @return the group this context wraps
     */
    @Override
    PexGroup subject();

    /**
     * Returns whether the group is marked as the default group in this context's world.
     *
     * <p>Equivalent to {@link PexGroup#isDefault(String)} with {@link #world()}.</p>
     *
     * @return {@code true} if the group is a default group for new users in this context
     */
    boolean isDefault();

    /**
     * Marks or clears the group as the default group in this context's world.
     *
     * <p>Equivalent to {@link PexGroup#setDefault(boolean, String)} with {@link #world()}.</p>
     *
     * @param value {@code true} to mark as default; {@code false} to clear
     */
    void setDefault(boolean value);

    /**
     * Returns direct parent group identifiers in this context's world.
     *
     * <p>Equivalent to {@link PexGroup#parents(String)} with {@link #world()}.</p>
     *
     * @return list of own parent group identifiers
     */
    List<String> parents();

    /**
     * Returns effective parent group identifiers in this context's world (inheritance expanded).
     *
     * <p>Equivalent to {@link PexGroup#parentTree(String)} with {@link #world()}.</p>
     *
     * @return list of parent group identifiers including transitive parents
     */
    List<String> parentTree();

    /**
     * Adds a direct parent group in this context's world.
     *
     * <p>Equivalent to {@link PexGroup#addParent(String, String)} with {@link #world()}.</p>
     *
     * @param parentName parent group identifier to add
     */
    void addParent(String parentName);

    /**
     * Removes a direct parent group in this context's world.
     *
     * <p>Equivalent to {@link PexGroup#removeParent(String, String)} with {@link #world()}.</p>
     *
     * @param parentName parent group identifier to remove
     */
    void removeParent(String parentName);

    /**
     * Replaces direct parent groups in this context's world.
     *
     * <p>Equivalent to {@link PexGroup#setParents(List, String)} with {@link #world()}.</p>
     *
     * @param parentNames new parent group identifiers
     */
    void setParents(List<String> parentNames);

    /**
     * Returns whether the group is a child of the named group in this context's world.
     *
     * <p>Equivalent to {@link PexGroup#isChildOf(String, String, boolean)} with {@link #world()}.</p>
     *
     * @param groupName group identifier to test as a potential ancestor
     * @param inherit   when {@code true}, match transitive parent relationships
     * @return {@code true} if this group is a direct or inherited child of {@code groupName}
     */
    boolean isChildOf(String groupName, boolean inherit);

    /**
     * Returns whether the group is a child of the named group in this context's world, including transitive parents.
     *
     * <p>Delegates to {@link #isChildOf(String, boolean)} with {@code inherit = true}.</p>
     *
     * @param groupName group identifier to test as a potential ancestor
     * @return {@code true} if this group is a direct or inherited child of {@code groupName}
     */
    default boolean isChildOf(String groupName) {
        return isChildOf(groupName, true);
    }

    /**
     * Returns user identifiers with direct membership in this group for this context's world.
     *
     * <p>Equivalent to {@link PexGroup#memberIdentifiers(String)} with {@link #world()}.</p>
     *
     * @return set of user identifiers with direct membership
     */
    Set<String> memberIdentifiers();

    /**
     * Returns users with direct membership in this group for this context's world.
     *
     * <p>Equivalent to {@link PexGroup#members(String)} with {@link #world()}.</p>
     *
     * @return list of users with direct membership
     */
    List<PexUser> members();

    /**
     * Returns users belonging to this group in this context's world.
     *
     * <p>Equivalent to {@link PexGroup#members(String, boolean)} with {@link #world()}.</p>
     *
     * @param inherit when {@code true}, includes users in descendant groups
     * @return list of users in this group
     */
    List<PexUser> members(boolean inherit);

    /**
     * Returns direct child groups of this group in this context's world.
     *
     * <p>Equivalent to {@link PexGroup#children(String)} with {@link #world()}.</p>
     *
     * @return list of direct child groups
     */
    List<PexGroup> children();

    /**
     * Returns child groups of this group in this context's world.
     *
     * <p>Equivalent to {@link PexGroup#children(String, boolean)} with {@link #world()}.</p>
     *
     * @param inherit when {@code true}, includes all descendant groups
     * @return list of child groups
     */
    List<PexGroup> children(boolean inherit);

    /**
     * Returns all descendant groups of this group in this context's world.
     *
     * <p>Equivalent to {@link PexGroup#descendants(String)} with {@link #world()}.</p>
     *
     * @return list of all descendant groups
     */
    List<PexGroup> descendants();

    /**
     * Returns currently online users with direct membership in this group.
     *
     * <p>Equivalent to {@link PexGroup#activeMembers()}.</p>
     *
     * @return list of online users with direct membership
     */
    List<PexUser> activeMembers();

    /**
     * Returns currently online users in this group.
     *
     * <p>Equivalent to {@link PexGroup#activeMembers(boolean)}.</p>
     *
     * @param inherit when {@code true}, includes online users in descendant groups
     * @return list of online users in this group
     */
    List<PexUser> activeMembers(boolean inherit);
}
