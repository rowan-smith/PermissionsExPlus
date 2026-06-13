package dev.rono.permissions.api.user;

import dev.rono.permissions.api.permission.PermissionContext;
import dev.rono.permissions.api.permission.PermissionHolder;
import dev.rono.permissions.api.subject.PermissionSubject;
import dev.rono.permissions.api.subject.SubjectContexts;
import dev.rono.permissions.api.subject.SubjectType;
import dev.rono.permissions.api.subject.TimedGroupMembership;
import dev.rono.permissions.api.subject.UserContext;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Modern view of a permission user. */
public interface User extends PermissionSubject {

    UUID getId();

    String getName();

    PermissionHolder asHolder();

    @Override
    default SubjectType type() {
        return SubjectType.USER;
    }

    Optional<UUID> uniqueId();

    @Override
    default UserContext inContext(PermissionContext context) {
        return SubjectContexts.user(this, context);
    }

    @Override
    default UserContext global() {
        return inContext(PermissionContext.global());
    }

    List<String> groups(PermissionContext context, boolean inherit);

    default List<String> groups(PermissionContext context) {
        return groups(context, true);
    }

    default List<String> groups() {
        return groups(PermissionContext.global());
    }

    boolean inGroup(String groupName, PermissionContext context, boolean inherit);

    default boolean inGroup(String groupName, PermissionContext context) {
        return inGroup(groupName, context, true);
    }

    default boolean inGroup(String groupName) {
        return inGroup(groupName, PermissionContext.global());
    }

    void addGroup(String groupName, PermissionContext context);

    void addGroup(String groupName, PermissionContext context, int lifetimeSeconds);

    default void addGroup(String groupName) {
        addGroup(groupName, PermissionContext.global());
    }

    default void addGroup(String groupName, int lifetimeSeconds) {
        addGroup(groupName, PermissionContext.global(), lifetimeSeconds);
    }

    void removeGroup(String groupName, PermissionContext context);

    default void removeGroup(String groupName) {
        removeGroup(groupName, PermissionContext.global());
    }

    List<TimedGroupMembership> timedGroupMemberships(PermissionContext context);

    default List<TimedGroupMembership> timedGroupMemberships() {
        return timedGroupMemberships(PermissionContext.global());
    }

    List<TimedGroupMembership> allTimedGroupMemberships();

    int groupMembershipRemainingSeconds(String groupName, PermissionContext context);

    default int groupMembershipRemainingSeconds(String groupName) {
        return groupMembershipRemainingSeconds(groupName, PermissionContext.global());
    }
}
