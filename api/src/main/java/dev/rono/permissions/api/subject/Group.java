package dev.rono.permissions.api.subject;

import dev.rono.permissions.api.world.Worlds;
import java.util.List;
import java.util.Set;

/** Modern view of a permission group. */
public interface Group extends PermissionSubject {

    @Override
    default SubjectType type() {
        return SubjectType.GROUP;
    }

    @Override
    default GroupWorldContext inWorld(String world) {
        return SubjectWorldContexts.group(this, world);
    }

    @Override
    default GroupWorldContext global() {
        return inWorld(Worlds.GLOBAL);
    }

    int weight();

    void setWeight(int weight);

    boolean isDefault(String world);

    default boolean isDefault() {
        return isDefault(Worlds.GLOBAL);
    }

    void setDefault(boolean value, String world);

    default void setDefault(boolean value) {
        setDefault(value, Worlds.GLOBAL);
    }

    /** Direct parent group identifiers in {@code world}. */
    List<String> parents(String world);

    default List<String> parents() {
        return parents(Worlds.GLOBAL);
    }

    /** Effective parent groups (inheritance expanded) in {@code world}. */
    List<String> parentTree(String world);

    default List<String> parentTree() {
        return parentTree(Worlds.GLOBAL);
    }

    void addParent(String parentName, String world);

    default void addParent(String parentName) {
        addParent(parentName, Worlds.GLOBAL);
    }

    void removeParent(String parentName, String world);

    default void removeParent(String parentName) {
        removeParent(parentName, Worlds.GLOBAL);
    }

    void setParents(List<String> parentNames, String world);

    default void setParents(List<String> parentNames) {
        setParents(parentNames, Worlds.GLOBAL);
    }

    boolean isChildOf(String groupName, String world, boolean inherit);

    default boolean isChildOf(String groupName, String world) {
        return isChildOf(groupName, world, true);
    }

    default boolean isChildOf(String groupName) {
        return isChildOf(groupName, Worlds.GLOBAL, true);
    }

    int rank();

    String rankLadder();

    void setRank(int rank, String ladder);

    /** User identifiers with direct membership in this group for {@code world}. */
    Set<String> memberIdentifiers(String world);

    default Set<String> memberIdentifiers() {
        return memberIdentifiers(Worlds.GLOBAL);
    }

    /**
     * Users belonging to this group in {@code world}.
     *
     * @param inherit when {@code true}, includes users in descendant groups
     */
    List<User> members(String world, boolean inherit);

    /** Users with direct membership in this group for {@code world}. */
    default List<User> members(String world) {
        return members(world, false);
    }

    default List<User> members() {
        return members(Worlds.GLOBAL);
    }

    /** Direct child groups of this group in {@code world}. */
    List<Group> children(String world, boolean inherit);

    default List<Group> children(String world) {
        return children(world, false);
    }

    default List<Group> children() {
        return children(Worlds.GLOBAL);
    }

    /** All descendant groups of this group in {@code world}. */
    default List<Group> descendants(String world) {
        return children(world, true);
    }

    default List<Group> descendants() {
        return descendants(Worlds.GLOBAL);
    }

    /** Currently online users in this group (direct membership). */
    List<User> activeMembers();

    /** Currently online users in this group, optionally including descendant groups. */
    List<User> activeMembers(boolean inherit);
}
