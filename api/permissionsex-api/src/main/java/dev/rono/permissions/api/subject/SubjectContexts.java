package dev.rono.permissions.api.subject;

import dev.rono.permissions.api.group.Group;
import dev.rono.permissions.api.permission.PermissionContext;
import dev.rono.permissions.api.user.User;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Factory for {@link PermissionContext}-bound subject facades. Pure delegation only. */
public final class SubjectContexts {
    private SubjectContexts() {}

    public static SubjectContext subject(PermissionSubject subject, PermissionContext context) {
        PermissionContext bound = context == null ? PermissionContext.global() : context;
        return new SubjectContext() {
            @Override
            public PermissionContext context() {
                return bound;
            }

            @Override
            public PermissionSubject subject() {
                return subject;
            }

            @Override
            public boolean has(String permission) {
                return subject.has(permission, bound);
            }

            @Override
            public List<String> permissions() {
                return subject.permissions(bound);
            }

            @Override
            public List<String> effectivePermissions() {
                return subject.effectivePermissions(bound);
            }

            @Override
            public void addPermission(String permission) {
                subject.addPermission(permission, bound);
            }

            @Override
            public void removePermission(String permission) {
                subject.removePermission(permission, bound);
            }

            @Override
            public void setPermissions(List<String> permissions) {
                subject.setPermissions(permissions, bound);
            }

            @Override
            public void addTimedPermission(String permission, int lifetimeSeconds) {
                subject.addTimedPermission(permission, bound, lifetimeSeconds);
            }

            @Override
            public void removeTimedPermission(String permission) {
                subject.removeTimedPermission(permission, bound);
            }

            @Override
            public List<String> timedPermissions() {
                return subject.timedPermissions(bound);
            }

            @Override
            public List<TimedPermissionEntry> timedPermissionEntries() {
                return subject.timedPermissionEntries(bound);
            }

            @Override
            public int timedPermissionRemainingSeconds(String permission) {
                return subject.timedPermissionRemainingSeconds(permission, bound);
            }

            @Override
            public boolean hasTimedPermission(String permission) {
                return subject.hasTimedPermission(permission, bound);
            }

            @Override
            public String prefix() {
                return subject.prefix(bound);
            }

            @Override
            public String suffix() {
                return subject.suffix(bound);
            }

            @Override
            public void setPrefix(String prefix) {
                subject.setPrefix(prefix, bound);
            }

            @Override
            public void setSuffix(String suffix) {
                subject.setSuffix(suffix, bound);
            }

            @Override
            public String option(String key) {
                return subject.option(key, bound);
            }

            @Override
            public void setOption(String key, String value) {
                subject.setOption(key, value, bound);
            }

            @Override
            public Map<String, String> options() {
                return subject.options(bound);
            }
        };
    }

    public static UserContext user(User user, PermissionContext context) {
        PermissionContext bound = context == null ? PermissionContext.global() : context;
        SubjectContext base = subject(user, bound);
        return new UserContext() {
            @Override
            public PermissionContext context() {
                return bound;
            }

            @Override
            public User subject() {
                return user;
            }

            @Override
            public boolean has(String permission) {
                return base.has(permission);
            }

            @Override
            public List<String> permissions() {
                return base.permissions();
            }

            @Override
            public List<String> effectivePermissions() {
                return base.effectivePermissions();
            }

            @Override
            public void addPermission(String permission) {
                base.addPermission(permission);
            }

            @Override
            public void removePermission(String permission) {
                base.removePermission(permission);
            }

            @Override
            public void setPermissions(List<String> permissions) {
                base.setPermissions(permissions);
            }

            @Override
            public void addTimedPermission(String permission, int lifetimeSeconds) {
                base.addTimedPermission(permission, lifetimeSeconds);
            }

            @Override
            public void removeTimedPermission(String permission) {
                base.removeTimedPermission(permission);
            }

            @Override
            public List<String> timedPermissions() {
                return base.timedPermissions();
            }

            @Override
            public List<TimedPermissionEntry> timedPermissionEntries() {
                return base.timedPermissionEntries();
            }

            @Override
            public int timedPermissionRemainingSeconds(String permission) {
                return base.timedPermissionRemainingSeconds(permission);
            }

            @Override
            public boolean hasTimedPermission(String permission) {
                return base.hasTimedPermission(permission);
            }

            @Override
            public String prefix() {
                return base.prefix();
            }

            @Override
            public String suffix() {
                return base.suffix();
            }

            @Override
            public void setPrefix(String prefix) {
                base.setPrefix(prefix);
            }

            @Override
            public void setSuffix(String suffix) {
                base.setSuffix(suffix);
            }

            @Override
            public String option(String key) {
                return base.option(key);
            }

            @Override
            public void setOption(String key, String value) {
                base.setOption(key, value);
            }

            @Override
            public Map<String, String> options() {
                return base.options();
            }

            @Override
            public List<String> groups(boolean inherit) {
                return user.groups(bound, inherit);
            }

            @Override
            public boolean inGroup(String groupName, boolean inherit) {
                return user.inGroup(groupName, bound, inherit);
            }

            @Override
            public void addGroup(String groupName) {
                user.addGroup(groupName, bound);
            }

            @Override
            public void addGroup(String groupName, int lifetimeSeconds) {
                user.addGroup(groupName, bound, lifetimeSeconds);
            }

            @Override
            public void removeGroup(String groupName) {
                user.removeGroup(groupName, bound);
            }

            @Override
            public List<TimedGroupMembership> timedGroupMemberships() {
                return user.timedGroupMemberships(bound);
            }

            @Override
            public int groupMembershipRemainingSeconds(String groupName) {
                return user.groupMembershipRemainingSeconds(groupName, bound);
            }
        };
    }

    public static GroupContext group(Group group, PermissionContext context) {
        PermissionContext bound = context == null ? PermissionContext.global() : context;
        SubjectContext base = subject(group, bound);
        return new GroupContext() {
            @Override
            public PermissionContext context() {
                return bound;
            }

            @Override
            public Group subject() {
                return group;
            }

            @Override
            public boolean has(String permission) {
                return base.has(permission);
            }

            @Override
            public List<String> permissions() {
                return base.permissions();
            }

            @Override
            public List<String> effectivePermissions() {
                return base.effectivePermissions();
            }

            @Override
            public void addPermission(String permission) {
                base.addPermission(permission);
            }

            @Override
            public void removePermission(String permission) {
                base.removePermission(permission);
            }

            @Override
            public void setPermissions(List<String> permissions) {
                base.setPermissions(permissions);
            }

            @Override
            public void addTimedPermission(String permission, int lifetimeSeconds) {
                base.addTimedPermission(permission, lifetimeSeconds);
            }

            @Override
            public void removeTimedPermission(String permission) {
                base.removeTimedPermission(permission);
            }

            @Override
            public List<String> timedPermissions() {
                return base.timedPermissions();
            }

            @Override
            public List<TimedPermissionEntry> timedPermissionEntries() {
                return base.timedPermissionEntries();
            }

            @Override
            public int timedPermissionRemainingSeconds(String permission) {
                return base.timedPermissionRemainingSeconds(permission);
            }

            @Override
            public boolean hasTimedPermission(String permission) {
                return base.hasTimedPermission(permission);
            }

            @Override
            public String prefix() {
                return base.prefix();
            }

            @Override
            public String suffix() {
                return base.suffix();
            }

            @Override
            public void setPrefix(String prefix) {
                base.setPrefix(prefix);
            }

            @Override
            public void setSuffix(String suffix) {
                base.setSuffix(suffix);
            }

            @Override
            public String option(String key) {
                return base.option(key);
            }

            @Override
            public void setOption(String key, String value) {
                base.setOption(key, value);
            }

            @Override
            public Map<String, String> options() {
                return base.options();
            }

            @Override
            public boolean isDefault() {
                return group.isDefault(bound);
            }

            @Override
            public void setDefault(boolean value) {
                group.setDefault(value, bound);
            }

            @Override
            public List<String> parents() {
                return group.parents(bound);
            }

            @Override
            public List<String> parentTree() {
                return group.parentTree(bound);
            }

            @Override
            public void addParent(String parentName) {
                group.addParent(parentName, bound);
            }

            @Override
            public void removeParent(String parentName) {
                group.removeParent(parentName, bound);
            }

            @Override
            public void setParents(List<String> parentNames) {
                group.setParents(parentNames, bound);
            }

            @Override
            public boolean isChildOf(String groupName, boolean inherit) {
                return group.isChildOf(groupName, bound, inherit);
            }

            @Override
            public Set<String> memberIdentifiers() {
                return group.memberIdentifiers(bound);
            }

            @Override
            public List<User> members() {
                return group.members(bound);
            }

            @Override
            public List<User> members(boolean inherit) {
                return group.members(bound, inherit);
            }

            @Override
            public List<Group> children() {
                return group.children(bound);
            }

            @Override
            public List<Group> children(boolean inherit) {
                return group.children(bound, inherit);
            }

            @Override
            public List<Group> descendants() {
                return group.descendants(bound);
            }

            @Override
            public List<String> childIdentifiers() {
                return group.childIdentifiers(bound);
            }

            @Override
            public List<String> childIdentifiers(boolean inherit) {
                return group.childIdentifiers(bound, inherit);
            }

            @Override
            public List<String> descendantIdentifiers() {
                return group.descendantIdentifiers(bound);
            }

            @Override
            public List<User> activeMembers() {
                return group.activeMembers(false);
            }

            @Override
            public List<User> activeMembers(boolean inherit) {
                return group.activeMembers(inherit);
            }
        };
    }
}
