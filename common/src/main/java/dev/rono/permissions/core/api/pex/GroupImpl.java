package dev.rono.permissions.core.api.pex;

import dev.rono.permissions.api.group.Group;
import dev.rono.permissions.api.permission.PermissionContext;
import dev.rono.permissions.api.permission.PermissionHolder;
import dev.rono.permissions.api.subject.SubjectType;
import dev.rono.permissions.api.user.User;
import dev.rono.permissions.core.DefaultPermissionManager;
import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionUser;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class GroupImpl extends AbstractPermissionSubjectAdapter implements Group {

    private final String name;
    private final PermissionGroup group;
    private final PermissionHolder holder;

    public GroupImpl(String name, PermissionGroup group, DefaultPermissionManager manager) {
        super(group, manager);
        this.name = name;
        this.group = group;
        this.holder = new GroupPermissionHolder(name);
    }

    PermissionGroup delegate() {
        return group;
    }

    @Override
    public String getName() {
        return name();
    }

    @Override
    public String name() {
        String displayName = group.getName();
        return displayName != null && !displayName.isBlank() ? displayName : name;
    }

    @Override
    public PermissionHolder asHolder() {
        return holder;
    }

    @Override
    public SubjectType type() {
        return SubjectType.GROUP;
    }

    @Override
    public int weight() {
        return group.getWeight();
    }

    @Override
    public void setWeight(int weight) {
        group.setWeight(weight);
    }

    @Override
    public boolean isDefault(PermissionContext context) {
        return group.isDefault(storageRealm(context));
    }

    @Override
    public void setDefault(boolean value, PermissionContext context) {
        group.setDefault(value, storageRealm(context));
    }

    @Override
    public List<String> parents(PermissionContext context) {
        return group.getOwnParentIdentifiers(storageRealm(context));
    }

    @Override
    public List<String> parentTree(PermissionContext context) {
        return GroupHierarchyEngine.resolveParentTree(manager, group, storageRealm(context));
    }

    @Override
    public void addParent(String parentName, PermissionContext context) {
        group.addParent(parentName, storageRealm(context));
    }

    @Override
    public void removeParent(String parentName, PermissionContext context) {
        group.removeParent(parentName, storageRealm(context));
    }

    @Override
    public void setParents(List<String> parentNames, PermissionContext context) {
        group.setParentsIdentifier(parentNames, storageRealm(context));
    }

    @Override
    public boolean isChildOf(String groupName, PermissionContext context, boolean inherit) {
        return group.isChildOf(groupName, storageRealm(context), inherit);
    }

    @Override
    public int rank() {
        return group.getRank();
    }

    @Override
    public String rankLadder() {
        return group.getRankLadder();
    }

    @Override
    public void setRank(int rank, String ladder) {
        group.setRank(rank);
        group.setRankLadder(ladder);
    }

    @Override
    public Set<String> memberIdentifiers(PermissionContext context) {
        return GroupHierarchyEngine.resolveMemberIdentifiers(
                manager, group.getIdentifier(), storageRealm(context), false);
    }

    @Override
    public List<User> members(PermissionContext context, boolean inherit) {
        var legacyWorld = storageRealm(context);
        var seen = new LinkedHashSet<String>();
        var members = new ArrayList<User>();
        collectMembers(seen, members, manager.getUsers(group.getIdentifier(), legacyWorld, inherit));
        return List.copyOf(members);
    }

    private void collectMembers(LinkedHashSet<String> seen, List<User> members, Set<PermissionUser> source) {
        for (PermissionUser member : source) {
            if (seen.add(member.getIdentifier())) {
                members.add(SubjectSupport.wrapUser(manager, member));
            }
        }
    }

    @Override
    public List<Group> children(PermissionContext context, boolean inherit) {
        var children = new ArrayList<Group>();
        for (PermissionGroup child : manager.getGroups(group.getIdentifier(), storageRealm(context), inherit)) {
            children.add(new GroupImpl(child.getIdentifier(), child, manager));
        }
        return List.copyOf(children);
    }

    @Override
    public List<String> childIdentifiers(PermissionContext context, boolean inherit) {
        return GroupHierarchyEngine.resolveChildIdentifiers(
                manager, group.getIdentifier(), storageRealm(context), inherit);
    }

    @Override
    public List<User> activeMembers() {
        return activeMembers(false);
    }

    @Override
    public List<User> activeMembers(boolean inherit) {
        var members = new ArrayList<User>();
        for (PermissionUser member : group.getActiveUsers(inherit)) {
            members.add(SubjectSupport.wrapUser(manager, member));
        }
        return List.copyOf(members);
    }

    @Override
    public void delete() {
        var identifier = group.getIdentifier();
        group.remove();
        manager.resetGroup(identifier);
    }
}
