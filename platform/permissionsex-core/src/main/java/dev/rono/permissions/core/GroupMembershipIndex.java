package dev.rono.permissions.core;

import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.PermissionsUserData;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Reverse index of direct group membership for fast {@code getUsers(group)} queries.
 */
final class GroupMembershipIndex {
    private static String worldKey(String world) {
        return world == null ? "" : world;
    }

    /** worldKey -> groupId -> user identifiers with direct membership */
    private final Map<String, Map<String, Set<String>>> directMembers = new ConcurrentHashMap<>();
    private volatile boolean dirty = true;

    void markDirty() {
        dirty = true;
    }

    void clear() {
        directMembers.clear();
        dirty = true;
    }

    void rebuild(DefaultPermissionManager manager) {
        directMembers.clear();
        for (String userId : manager.getBackend().getUserIdentifiers()) {
            PermissionsUserData data = manager.getBackend().getUserData(userId);
            if (data == null) {
                continue;
            }
            trackFromData(userId, data);
        }
        dirty = false;
    }

    void trackFromData(String userId, PermissionsUserData data) {
        trackParents(userId, null, data.getParents(null));
        for (String world : data.getWorlds()) {
            trackParents(userId, world, data.getParents(world));
        }
    }

    void onUserMembershipChanged(PermissionUser user, String world) {
        String userId = user.getIdentifier();
        untrackUser(userId);
        trackParents(userId, world, user.getOwnParentIdentifiers(world));
        for (String w : user.getWorlds()) {
            if (world == null || !w.equals(world)) {
                trackParents(userId, w, user.getOwnParentIdentifiers(w));
            }
        }
        trackParents(userId, null, user.getOwnParentIdentifiers(null));
    }

    void untrackUser(String userId) {
        for (Map<String, Set<String>> byGroup : directMembers.values()) {
            for (Set<String> members : byGroup.values()) {
                members.remove(userId);
            }
        }
    }

    private void trackParents(String userId, String world, List<String> parents) {
        if (parents == null || parents.isEmpty()) {
            return;
        }
        String wk = worldKey(world);
        Map<String, Set<String>> byGroup = directMembers.computeIfAbsent(wk, k -> new ConcurrentHashMap<>());
        for (String groupId : parents) {
            if (groupId == null || groupId.isEmpty()) {
                continue;
            }
            byGroup.computeIfAbsent(groupId, g -> ConcurrentHashMap.newKeySet()).add(userId);
        }
    }

    Set<String> resolveUserIds(DefaultPermissionManager manager, String groupName, String worldName,
            boolean inheritance) {
        if (dirty) {
            rebuild(manager);
        }
        Set<String> result = new HashSet<>();
        String wk = worldKey(worldName);
        if (!inheritance) {
            collectDirect(result, wk, groupName);
            if (worldName != null) {
                collectDirect(result, worldKey(null), groupName);
            }
            return result;
        }
        for (PermissionGroup group : manager.getGroups(groupName, worldName, true)) {
            collectDirect(result, wk, group.getIdentifier());
            if (worldName != null) {
                collectDirect(result, worldKey(null), group.getIdentifier());
            }
        }
        return result;
    }

    private void collectDirect(Set<String> result, String worldKey, String groupId) {
        Map<String, Set<String>> byGroup = directMembers.get(worldKey);
        if (byGroup == null) {
            return;
        }
        Set<String> members = byGroup.get(groupId);
        if (members != null) {
            result.addAll(members);
        }
        for (Map.Entry<String, Set<String>> e : byGroup.entrySet()) {
            if (e.getKey().equalsIgnoreCase(groupId)) {
                result.addAll(e.getValue());
            }
        }
    }

    Set<PermissionUser> resolveUsers(DefaultPermissionManager manager, String groupName, String worldName,
            boolean inheritance) {
        Set<String> ids = resolveUserIds(manager, groupName, worldName, inheritance);
        if (ids.isEmpty()) {
            return Collections.emptySet();
        }
        Set<PermissionUser> users = new HashSet<>();
        for (String id : ids) {
            try {
                users.add(manager.getUser(id));
            } catch (RuntimeException ignored) {
                // PexUser removed between index read and load
            }
        }
        return Collections.unmodifiableSet(users);
    }
}
