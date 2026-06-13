package dev.rono.permissions.core.api.pex;

import dev.rono.permissions.api.group.Group;
import dev.rono.permissions.api.permission.PermissionHolder;
import dev.rono.permissions.api.subject.SubjectType;
import dev.rono.permissions.api.user.User;
import dev.rono.permissions.core.DefaultPermissionManager;
import dev.rono.permissions.core.api.ModernWorlds;
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
        return name;
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
    public boolean isDefault(String world) {
        return group.isDefault(ModernWorlds.toLegacy(world));
    }

    @Override
    public void setDefault(boolean value, String world) {
        group.setDefault(value, ModernWorlds.toLegacy(world));
    }

    @Override
    public List<String> parents(String world) {
        return group.getOwnParentIdentifiers(ModernWorlds.toLegacy(world));
    }

    @Override
    public List<String> parentTree(String world) {
        return GroupHierarchyEngine.resolveParentTree(manager, group, ModernWorlds.toLegacy(world));
    }

    @Override
    public void addParent(String parentName, String world) {
        group.addParent(parentName, ModernWorlds.toLegacy(world));
    }

    @Override
    public void removeParent(String parentName, String world) {
        group.removeParent(parentName, ModernWorlds.toLegacy(world));
    }

    @Override
    public void setParents(List<String> parentNames, String world) {
        group.setParentsIdentifier(parentNames, ModernWorlds.toLegacy(world));
    }

    @Override
    public boolean isChildOf(String groupName, String world, boolean inherit) {
        return group.isChildOf(groupName, ModernWorlds.toLegacy(world), inherit);
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
    public Set<String> memberIdentifiers(String world) {
        return GroupHierarchyEngine.resolveMemberIdentifiers(
                manager, group.getIdentifier(), ModernWorlds.toLegacy(world), false);
    }

    @Override
    public List<User> members(String world, boolean inherit) {
        var legacyWorld = ModernWorlds.toLegacy(world);
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
    public List<Group> children(String world, boolean inherit) {
        var children = new ArrayList<Group>();
        for (PermissionGroup child : manager.getGroups(group.getIdentifier(), ModernWorlds.toLegacy(world), inherit)) {
            children.add(new GroupImpl(child.getIdentifier(), child, manager));
        }
        return List.copyOf(children);
    }

    @Override
    public List<String> childIdentifiers(String world, boolean inherit) {
        return GroupHierarchyEngine.resolveChildIdentifiers(
                manager, group.getIdentifier(), ModernWorlds.toLegacy(world), inherit);
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
