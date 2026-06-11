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

    /** Users with direct membership in this group for {@code world}. */
    List<User> members(String world);

    default List<User> members() {
        return members(Worlds.GLOBAL);
    }

    /** Currently online users in this group (direct membership). */
    List<User> activeMembers();

    /** Currently online users in this group, optionally including descendant groups. */
    List<User> activeMembers(boolean inherit);
}
