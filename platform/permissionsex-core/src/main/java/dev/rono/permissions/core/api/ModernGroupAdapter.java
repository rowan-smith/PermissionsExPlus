package dev.rono.permissions.core.api;

import dev.rono.permissions.api.subject.PexGroup;
import dev.rono.permissions.api.subject.PexSubjectType;
import dev.rono.permissions.api.subject.PexUser;
import dev.rono.permissions.core.DefaultPermissionManager;
import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionUser;

import java.util.*;

public final class ModernGroupAdapter extends AbstractModernSubjectAdapter implements PexGroup {
    private final PermissionGroup group;

    public ModernGroupAdapter(PermissionGroup group, DefaultPermissionManager manager) {
        super(group, manager);
        this.group = group;
    }

    @Override
    public PexSubjectType type() {
        return PexSubjectType.GROUP;
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
        String legacyWorld = ModernWorlds.toLegacy(world);
        List<String> tree = new ArrayList<>();
        Deque<String> pending = new ArrayDeque<>(group.getOwnParentIdentifiers(legacyWorld));
        Set<String> seen = new HashSet<>();
        while (!pending.isEmpty()) {
            String parentName = pending.poll();
            if (!seen.add(parentName)) {
                continue;
            }
            tree.add(parentName);
            pending.addAll(manager.getGroup(parentName).getOwnParentIdentifiers(legacyWorld));
        }
        return List.copyOf(tree);
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
        LinkedHashSet<String> identifiers = new LinkedHashSet<>();
        for (PermissionUser member : group.getUsers(ModernWorlds.toLegacy(world))) {
            identifiers.add(member.getIdentifier());
        }
        return Set.copyOf(identifiers);
    }

    @Override
    public List<PexUser> members(String world, boolean inherit) {
        String legacyWorld = ModernWorlds.toLegacy(world);
        LinkedHashSet<String> seen = new LinkedHashSet<>();
        List<PexUser> members = new ArrayList<>();
        collectMembers(seen, members, manager.getUsers(group.getIdentifier(), legacyWorld, false));
        if (inherit) {
            collectMembers(seen, members, manager.getUsers(group.getIdentifier(), legacyWorld, true));
        }
        return List.copyOf(members);
    }

    private void collectMembers(LinkedHashSet<String> seen, List<PexUser> members, Set<PermissionUser> source) {
        for (PermissionUser member : source) {
            if (seen.add(member.getIdentifier())) {
                members.add(new ModernUserAdapter(member, manager));
            }
        }
    }

    @Override
    public List<PexUser> members(String world) {
        return members(world, false);
    }

    @Override
    public List<PexGroup> children(String world, boolean inherit) {
        List<PexGroup> children = new ArrayList<>();
        for (PermissionGroup child : manager.getGroups(group.getIdentifier(), ModernWorlds.toLegacy(world), inherit)) {
            children.add(new ModernGroupAdapter(child, manager));
        }
        return List.copyOf(children);
    }

    @Override
    public List<PexUser> activeMembers() {
        return activeMembers(false);
    }

    @Override
    public List<PexUser> activeMembers(boolean inherit) {
        List<PexUser> members = new ArrayList<>();
        for (PermissionUser member : group.getActiveUsers(inherit)) {
            members.add(new ModernUserAdapter(member, manager));
        }
        return List.copyOf(members);
    }

    @Override
    public void delete() {
        String id = group.getIdentifier();
        group.remove();
        manager.resetGroup(id);
    }
}
