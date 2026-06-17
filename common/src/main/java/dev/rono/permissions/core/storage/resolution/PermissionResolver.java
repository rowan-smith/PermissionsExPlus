package dev.rono.permissions.core.storage.resolution;

import dev.rono.permissions.api.permission.PermissionContext;
import dev.rono.permissions.core.storage.ContextKeyCodec;
import dev.rono.permissions.core.storage.model.Group;
import dev.rono.permissions.core.storage.model.GroupInheritance;
import dev.rono.permissions.core.storage.model.GroupPermission;
import dev.rono.permissions.core.storage.model.Ladder;
import dev.rono.permissions.core.storage.model.LadderGroup;
import dev.rono.permissions.core.storage.model.PermissionEntry;
import dev.rono.permissions.core.storage.model.User;
import dev.rono.permissions.core.storage.model.UserGroup;
import dev.rono.permissions.core.storage.model.UserOptions;
import dev.rono.permissions.core.storage.model.UserPermission;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Permission resolution algorithm v1.0.
 *
 * <p>Evaluation order:</p>
 * <ol>
 *   <li>Drop expired group memberships and permission rows.</li>
 *   <li>Collect user-direct permissions (highest base priority).</li>
 *   <li>Collect permissions from direct groups ordered by ladder position then weight.</li>
 *   <li>Collect inherited group permissions breadth-first with depth penalty.</li>
 *   <li>Filter by context specificity (entry context must be a subset of the request context).</li>
 *   <li>Resolve conflicts by priority; deny wins over allow at equal priority.</li>
 * </ol>
 */
public final class PermissionResolver {

    public static final int VERSION = 1;

    private static final int USER_BASE = 100_000;
    private static final int GROUP_BASE = 50_000;
    private static final int INHERIT_BASE = 10_000;
    private static final int CONTEXT_MULTIPLIER = 1_000;
    private static final int DEPTH_PENALTY = 500;

    private PermissionResolver() {}

    public static EffectiveUser resolve(User user,
                                        Map<Integer, Group> groupsById,
                                        List<Ladder> ladders,
                                        PermissionContext context,
                                        Instant now) {
        String requestKey = ContextKeyCodec.encode(context);
        Set<Integer> activeGroupIds = activeGroups(user, now);
        Map<Integer, Integer> ladderPositions = ladderPositions(ladders);
        List<ResolvedPermission> candidates = new ArrayList<>();

        for (UserPermission permission : user.getPermissions()) {
            if (isExpired(permission, now) || !ContextKeyCodec.matches(requestKey, permission.getContextKey())) {
                continue;
            }
            candidates.add(new ResolvedPermission(
                    permission.getPermission(),
                    permission.isAllow(),
                    priority(USER_BASE, permission.getContextKey(), 0),
                    permission.getContextKey(),
                    "user"));
        }

        List<Integer> orderedGroups = orderGroups(activeGroupIds, groupsById, ladderPositions);
        for (int groupId : orderedGroups) {
            Group group = groupsById.get(groupId);
            if (group == null) {
                continue;
            }
            int ladderBonus = ladderPositions.getOrDefault(groupId, 0);
            for (GroupPermission permission : group.getPermissions()) {
                if (isExpired(permission, now) || !ContextKeyCodec.matches(requestKey, permission.getContextKey())) {
                    continue;
                }
                candidates.add(new ResolvedPermission(
                        permission.getPermission(),
                        permission.isAllow(),
                        priority(GROUP_BASE, permission.getContextKey(), ladderBonus * 100 + group.getWeight()),
                        permission.getContextKey(),
                        "group"));
            }
        }

        Map<Integer, Integer> depths = inheritanceDepths(orderedGroups, groupsById);
        for (Map.Entry<Integer, Integer> entry : depths.entrySet()) {
            if (entry.getValue() == 0) {
                continue;
            }
            Group group = groupsById.get(entry.getKey());
            if (group == null) {
                continue;
            }
            for (GroupPermission permission : group.getPermissions()) {
                if (isExpired(permission, now) || !ContextKeyCodec.matches(requestKey, permission.getContextKey())) {
                    continue;
                }
                candidates.add(new ResolvedPermission(
                        permission.getPermission(),
                        permission.isAllow(),
                        priority(INHERIT_BASE, permission.getContextKey(),
                                group.getWeight() - entry.getValue() * DEPTH_PENALTY),
                        permission.getContextKey(),
                        "inheritance"));
            }
        }

        List<ResolvedPermission> resolved = collapse(candidates);
        UserOptions options = resolveOptions(user, orderedGroups, groupsById);
        return new EffectiveUser(user.getId(), user.getName(), activeGroupIds, resolved, options);
    }

    private static List<ResolvedPermission> collapse(List<ResolvedPermission> candidates) {
        Map<String, ResolvedPermission> bestByPermission = new LinkedHashMap<>();
        for (ResolvedPermission candidate : candidates) {
            String key = candidate.getPermission() + "\0" + nullToEmpty(candidate.getContextKey());
            ResolvedPermission existing = bestByPermission.get(key);
            if (existing == null || isBetter(candidate, existing)) {
                bestByPermission.put(key, candidate);
            }
        }
        return new ArrayList<>(bestByPermission.values());
    }

    private static boolean isBetter(ResolvedPermission candidate, ResolvedPermission existing) {
        if (candidate.getPriority() != existing.getPriority()) {
            return candidate.getPriority() > existing.getPriority();
        }
        if (candidate.isValue() == existing.isValue()) {
            return false;
        }
        return !candidate.isValue();
    }

    private static int priority(int base, String contextKey, int bonus) {
        return base + ContextKeyCodec.specificity(contextKey) * CONTEXT_MULTIPLIER + bonus;
    }

    private static boolean isExpired(PermissionEntry entry, Instant now) {
        return entry.getExpiresAt() != null && entry.getExpiresAt().isBefore(now);
    }

    private static Set<Integer> activeGroups(User user, Instant now) {
        Set<Integer> out = new HashSet<>();
        for (UserGroup membership : user.getGroups()) {
            if (membership.getExpiresAt() != null && membership.getExpiresAt().isBefore(now)) {
                continue;
            }
            out.add(membership.getGroupId());
        }
        return out;
    }

    private static Map<Integer, Integer> ladderPositions(List<Ladder> ladders) {
        Map<Integer, Integer> out = new HashMap<>();
        for (Ladder ladder : ladders) {
            for (LadderGroup group : ladder.getGroups()) {
                out.merge(group.getGroupId(), group.getPosition(), Math::max);
            }
        }
        return out;
    }

    private static List<Integer> orderGroups(Set<Integer> groupIds,
                                             Map<Integer, Group> groupsById,
                                             Map<Integer, Integer> ladderPositions) {
        List<Integer> ordered = new ArrayList<>(groupIds);
        ordered.sort(Comparator
                .comparingInt((Integer id) -> ladderPositions.getOrDefault(id, -1)).reversed()
                .thenComparingInt(id -> {
                    Group group = groupsById.get(id);
                    return group != null ? group.getWeight() : 0;
                }).reversed()
                .thenComparingInt(id -> id));
        return ordered;
    }

    private static Map<Integer, Integer> inheritanceDepths(List<Integer> roots,
                                                           Map<Integer, Group> groupsById) {
        Map<Integer, Integer> depths = new HashMap<>();
        ArrayDeque<int[]> queue = new ArrayDeque<>();
        for (int root : roots) {
            depths.putIfAbsent(root, 0);
            queue.add(new int[] {root, 0});
        }
        while (!queue.isEmpty()) {
            int[] current = queue.removeFirst();
            Group group = groupsById.get(current[0]);
            if (group == null) {
                continue;
            }
            for (GroupInheritance inheritance : group.getParents()) {
                int depth = current[1] + 1;
                Integer existing = depths.get(inheritance.getParentId());
                if (existing != null && existing <= depth) {
                    continue;
                }
                depths.put(inheritance.getParentId(), depth);
                queue.add(new int[] {inheritance.getParentId(), depth});
            }
        }
        return depths;
    }

    private static UserOptions resolveOptions(User user,
                                              List<Integer> orderedGroups,
                                              Map<Integer, Group> groupsById) {
        String prefix = user.getOptions() != null ? user.getOptions().getPrefix() : null;
        String suffix = user.getOptions() != null ? user.getOptions().getSuffix() : null;
        if (prefix != null || suffix != null) {
            return new UserOptions(prefix, suffix);
        }
        for (int groupId : orderedGroups) {
            Group group = groupsById.get(groupId);
            if (group == null || group.getOptions() == null) {
                continue;
            }
            if (group.getOptions().getPrefix() != null || group.getOptions().getSuffix() != null) {
                return new UserOptions(group.getOptions().getPrefix(), group.getOptions().getSuffix());
            }
        }
        return new UserOptions(null, null);
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
