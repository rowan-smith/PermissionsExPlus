package dev.rono.permissions.api.subject;

import dev.rono.permissions.api.world.Worlds;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class SubjectWorldContexts {
    private SubjectWorldContexts() {}

    static SubjectWorldContext subject(PermissionSubject subject, String world) {
        String normalized = Worlds.normalize(world);
        return new SubjectWorldContext() {
            @Override
            public String world() {
                return normalized;
            }

            @Override
            public PermissionSubject subject() {
                return subject;
            }

            @Override
            public boolean hasPermission(String permission) {
                return subject.has(permission, normalized);
            }

            @Override
            public boolean has(String permission) {
                return hasPermission(permission);
            }

            @Override
            public List<String> permissions() {
                return subject.permissions(normalized);
            }

            @Override
            public List<String> effectivePermissions() {
                return subject.effectivePermissions(normalized);
            }

            @Override
            public void addPermission(String permission) {
                subject.addPermission(permission, normalized);
            }

            @Override
            public void removePermission(String permission) {
                subject.removePermission(permission, normalized);
            }

            @Override
            public void setPermissions(List<String> permissions) {
                subject.setPermissions(permissions, normalized);
            }

            @Override
            public void addTimedPermission(String permission, int lifetimeSeconds) {
                subject.addTimedPermission(permission, normalized, lifetimeSeconds);
            }

            @Override
            public void removeTimedPermission(String permission) {
                subject.removeTimedPermission(permission, normalized);
            }

            @Override
            public List<String> timedPermissions() {
                return subject.timedPermissions(normalized);
            }

            @Override
            public List<TimedPermissionEntry> timedPermissionEntries() {
                return subject.timedPermissionEntries(normalized);
            }

            @Override
            public int timedPermissionRemainingSeconds(String permission) {
                return subject.timedPermissionRemainingSeconds(permission, normalized);
            }

            @Override
            public boolean hasTimedPermission(String permission) {
                return subject.hasTimedPermission(permission, normalized);
            }

            @Override
            public String prefix() {
                return subject.prefix(normalized);
            }

            @Override
            public String suffix() {
                return subject.suffix(normalized);
            }

            @Override
            public void setPrefix(String prefix) {
                subject.setPrefix(prefix, normalized);
            }

            @Override
            public void setSuffix(String suffix) {
                subject.setSuffix(suffix, normalized);
            }

            @Override
            public String option(String key) {
                return subject.option(key, normalized);
            }

            @Override
            public void setOption(String key, String value) {
                subject.setOption(key, value, normalized);
            }

            @Override
            public Map<String, String> options() {
                return subject.options(normalized);
            }
        };
    }

    static UserWorldContext user(User user, String world) {
        String normalized = Worlds.normalize(world);
        SubjectWorldContext base = subject(user, normalized);
        return new UserWorldContext() {
            @Override
            public String world() {
                return base.world();
            }

            @Override
            public User subject() {
                return user;
            }

            @Override
            public boolean hasPermission(String permission) {
                return base.hasPermission(permission);
            }

            @Override
            public boolean has(String permission) {
                return hasPermission(permission);
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
                return user.groups(normalized, inherit);
            }

            @Override
            public boolean inGroup(String groupName, boolean inherit) {
                return user.inGroup(groupName, normalized, inherit);
            }

            @Override
            public void addGroup(String groupName) {
                user.addGroup(groupName, normalized);
            }

            @Override
            public void addGroup(String groupName, int lifetimeSeconds) {
                user.addGroup(groupName, normalized, lifetimeSeconds);
            }

            @Override
            public void removeGroup(String groupName) {
                user.removeGroup(groupName, normalized);
            }

            @Override
            public List<TimedGroupMembership> timedGroupMemberships() {
                return user.timedGroupMemberships(normalized);
            }

            @Override
            public int groupMembershipRemainingSeconds(String groupName) {
                return user.groupMembershipRemainingSeconds(groupName, normalized);
            }
        };
    }

    static GroupWorldContext group(Group group, String world) {
        String normalized = Worlds.normalize(world);
        SubjectWorldContext base = subject(group, normalized);
        return new GroupWorldContext() {
            @Override
            public String world() {
                return base.world();
            }

            @Override
            public Group subject() {
                return group;
            }

            @Override
            public boolean hasPermission(String permission) {
                return base.hasPermission(permission);
            }

            @Override
            public boolean has(String permission) {
                return hasPermission(permission);
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
                return group.isDefault(normalized);
            }

            @Override
            public void setDefault(boolean value) {
                group.setDefault(value, normalized);
            }

            @Override
            public List<String> parents() {
                return group.parents(normalized);
            }

            @Override
            public List<String> parentTree() {
                return group.parentTree(normalized);
            }

            @Override
            public void addParent(String parentName) {
                group.addParent(parentName, normalized);
            }

            @Override
            public void removeParent(String parentName) {
                group.removeParent(parentName, normalized);
            }

            @Override
            public void setParents(List<String> parentNames) {
                group.setParents(parentNames, normalized);
            }

            @Override
            public boolean isChildOf(String groupName, boolean inherit) {
                return group.isChildOf(groupName, normalized, inherit);
            }

            @Override
            public Set<String> memberIdentifiers() {
                return group.memberIdentifiers(normalized);
            }

            @Override
            public List<User> members() {
                return group.members(normalized);
            }

            @Override
            public List<User> members(boolean inherit) {
                return group.members(normalized, inherit);
            }

            @Override
            public List<Group> children() {
                return group.children(normalized);
            }

            @Override
            public List<Group> children(boolean inherit) {
                return group.children(normalized, inherit);
            }

            @Override
            public List<Group> descendants() {
                return group.descendants(normalized);
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
