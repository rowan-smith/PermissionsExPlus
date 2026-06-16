package dev.rono.permissions.core.storage.resolution;

import dev.rono.permissions.core.storage.model.UserOptions;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class EffectiveUser {

    private final UUID userId;
    private final String name;
    private final Set<Integer> groupIds;
    private final List<ResolvedPermission> permissions;
    private final UserOptions options;

    public EffectiveUser(UUID userId,
                         String name,
                         Set<Integer> groupIds,
                         List<ResolvedPermission> permissions,
                         UserOptions options) {
        this.userId = userId;
        this.name = name;
        this.groupIds = Set.copyOf(groupIds);
        this.permissions = List.copyOf(permissions);
        this.options = options;
    }

    public UUID getUserId() { return userId; }
    public String getName() { return name; }
    public Set<Integer> getGroupIds() { return groupIds; }
    public List<ResolvedPermission> getPermissions() { return permissions; }
    public UserOptions getOptions() { return options; }

    public boolean hasPermission(String permission) {
        return hasPermission(permission, null);
    }

    public boolean hasPermission(String permission, String contextKey) {
        ResolvedPermission resolved = resolve(permission, contextKey);
        return resolved != null && resolved.isValue();
    }

    public boolean hasPermission(ru.tehkode.permissions.PermissionMatcher matcher,
                                 String permission,
                                 String contextKey) {
        ResolvedPermission resolved = resolveMatching(matcher, permission, contextKey);
        return resolved != null && resolved.isValue();
    }

    public ResolvedPermission resolveMatching(ru.tehkode.permissions.PermissionMatcher matcher,
                                              String permission,
                                              String contextKey) {
        ResolvedPermission best = null;
        for (ResolvedPermission candidate : permissions) {
            if (!contextMatches(contextKey, candidate.getContextKey())) {
                continue;
            }
            String expression = candidate.isValue()
                    ? candidate.getPermission()
                    : "-" + candidate.getPermission();
            if (!matcher.isMatches(expression, permission)) {
                continue;
            }
            if (best == null || isBetter(candidate, best)) {
                best = candidate;
            }
        }
        return best;
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

    public ResolvedPermission resolve(String permission, String contextKey) {
        ResolvedPermission best = null;
        for (ResolvedPermission candidate : permissions) {
            if (!candidate.getPermission().equals(permission)) {
                continue;
            }
            if (!contextMatches(contextKey, candidate.getContextKey())) {
                continue;
            }
            if (best == null || candidate.getPriority() > best.getPriority()
                    || (candidate.getPriority() == best.getPriority() && !candidate.isValue() && best.isValue())) {
                best = candidate;
            }
        }
        return best;
    }

    private static boolean contextMatches(String requestKey, String entryKey) {
        if (requestKey == null || requestKey.isEmpty()) {
            return entryKey == null || entryKey.isEmpty();
        }
        if (entryKey == null || entryKey.isEmpty()) {
            return true;
        }
        return requestKey.startsWith(entryKey) || entryKey.startsWith(requestKey);
    }
}
