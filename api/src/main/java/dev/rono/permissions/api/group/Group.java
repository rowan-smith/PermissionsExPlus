package dev.rono.permissions.api.group;

import dev.rono.permissions.api.permission.PermissionContext;
import dev.rono.permissions.api.permission.PermissionHolder;
import dev.rono.permissions.api.subject.GroupContext;
import dev.rono.permissions.api.subject.PermissionSubject;
import dev.rono.permissions.api.subject.SubjectContexts;
import dev.rono.permissions.api.subject.SubjectType;
import dev.rono.permissions.api.user.User;
import java.util.List;
import java.util.Set;

/**
 * Modern view of a permission group.
 *
 * <p>Extends {@link PermissionSubject} with weight, default-group flags, parent/child relationships,
 * rank-ladder metadata, and membership queries.</p>
 *
 * <p><strong>Identifier vs entity naming:</strong> methods ending in {@code Identifiers} or returning
 * {@code List<String>} / {@code Set<String>} of names return stable backend identifiers only.
 * Methods returning {@link User} or {@link Group} resolve live adapter objects (may materialize
 * cached engine entities). Prefer identifier methods for bulk queries; use entity methods when you
 * need to mutate or inspect full subject state.</p>
 */
public interface Group extends PermissionSubject {

    String getName();

    PermissionHolder asHolder();

    @Override
    default SubjectType type() {
        return SubjectType.GROUP;
    }

    @Override
    default GroupContext inContext(PermissionContext context) {
        return SubjectContexts.group(this, context);
    }

    @Override
    default GroupContext global() {
        return inContext(PermissionContext.global());
    }

    int weight();

    void setWeight(int weight);

    boolean isDefault(PermissionContext context);

    default boolean isDefault() {
        return isDefault(PermissionContext.global());
    }

    void setDefault(boolean value, PermissionContext context);

    default void setDefault(boolean value) {
        setDefault(value, PermissionContext.global());
    }

    List<String> parents(PermissionContext context);

    default List<String> parents() {
        return parents(PermissionContext.global());
    }

    List<String> parentTree(PermissionContext context);

    default List<String> parentTree() {
        return parentTree(PermissionContext.global());
    }

    void addParent(String parentName, PermissionContext context);

    default void addParent(String parentName) {
        addParent(parentName, PermissionContext.global());
    }

    void removeParent(String parentName, PermissionContext context);

    default void removeParent(String parentName) {
        removeParent(parentName, PermissionContext.global());
    }

    void setParents(List<String> parentNames, PermissionContext context);

    default void setParents(List<String> parentNames) {
        setParents(parentNames, PermissionContext.global());
    }

    boolean isChildOf(String groupName, PermissionContext context, boolean inherit);

    default boolean isChildOf(String groupName, PermissionContext context) {
        return isChildOf(groupName, context, true);
    }

    default boolean isChildOf(String groupName) {
        return isChildOf(groupName, PermissionContext.global(), true);
    }

    int rank();

    String rankLadder();

    void setRank(int rank, String ladder);

    Set<String> memberIdentifiers(PermissionContext context);

    default Set<String> memberIdentifiers() {
        return memberIdentifiers(PermissionContext.global());
    }

    List<User> members(PermissionContext context, boolean inherit);

    default List<User> members(PermissionContext context) {
        return members(context, false);
    }

    default List<User> members() {
        return members(PermissionContext.global());
    }

    List<Group> children(PermissionContext context, boolean inherit);

    default List<Group> children(PermissionContext context) {
        return children(context, false);
    }

    default List<Group> children() {
        return children(PermissionContext.global());
    }

    default List<Group> descendants(PermissionContext context) {
        return children(context, true);
    }

    default List<Group> descendants() {
        return descendants(PermissionContext.global());
    }

    List<String> childIdentifiers(PermissionContext context, boolean inherit);

    default List<String> childIdentifiers(PermissionContext context) {
        return childIdentifiers(context, false);
    }

    default List<String> childIdentifiers() {
        return childIdentifiers(PermissionContext.global());
    }

    default List<String> descendantIdentifiers(PermissionContext context) {
        return childIdentifiers(context, true);
    }

    default List<String> descendantIdentifiers() {
        return descendantIdentifiers(PermissionContext.global());
    }

    List<User> activeMembers();

    List<User> activeMembers(boolean inherit);
}
