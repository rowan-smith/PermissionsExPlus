package dev.rono.permissions.core;

import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionUser;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Canonical graph traversal for group hierarchy and membership queries.
 *
 * <p>Modern API adapters ({@code UserImpl}, {@code GroupImpl}) must delegate multi-hop group graph
 * operations here or to {@link DefaultPermissionManager} engine methods — not reimplement traversal.</p>
 *
 * <p>The {@code inherit} flag semantics are unified:</p>
 * <ul>
 *   <li>{@code false} — direct edges only (own parents, direct members/children)</li>
 *   <li>{@code true} — transitive closure in the group graph</li>
 * </ul>
 */
final class GroupHierarchyEngine {

    private GroupHierarchyEngine() {}

    /**
     * Returns transitive parent group identifiers for {@code group} in {@code legacyWorld}.
     */
    static List<String> resolveParentTree(DefaultPermissionManager manager, PermissionGroup group, String legacyWorld) {
        var tree = new ArrayList<String>();
        Deque<String> pending = new ArrayDeque<>(group.getOwnParentIdentifiers(legacyWorld));
        var seen = new HashSet<String>();
        while (!pending.isEmpty()) {
            var parentName = pending.poll();
            if (!seen.add(parentName)) {
                continue;
            }
            tree.add(parentName);
            pending.addAll(manager.getGroup(parentName).getOwnParentIdentifiers(legacyWorld));
        }
        return List.copyOf(tree);
    }

    /**
     * Returns child group identifiers of {@code groupName} in {@code legacyWorld}.
     */
    static List<String> resolveChildIdentifiers(
            DefaultPermissionManager manager,
            String groupName,
            String legacyWorld,
            boolean inherit) {
        var identifiers = new ArrayList<String>();
        for (PermissionGroup child : manager.getGroups(groupName, legacyWorld, inherit)) {
            identifiers.add(child.getIdentifier());
        }
        return List.copyOf(identifiers);
    }

    /**
     * Returns member user identifiers for {@code groupName} in {@code legacyWorld}.
     */
    static Set<String> resolveMemberIdentifiers(
            DefaultPermissionManager manager,
            String groupName,
            String legacyWorld,
            boolean inherit) {
        var identifiers = new LinkedHashSet<String>();
        for (PermissionUser member : manager.getUsers(groupName, legacyWorld, inherit)) {
            identifiers.add(member.getIdentifier());
        }
        return Set.copyOf(identifiers);
    }
}
