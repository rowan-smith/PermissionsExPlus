package dev.rono.permissions.core.storage;

import dev.rono.permissions.api.permission.PermissionContext;
import dev.rono.permissions.core.storage.model.Group;
import dev.rono.permissions.core.storage.model.Ladder;
import dev.rono.permissions.core.storage.model.User;
import dev.rono.permissions.core.storage.resolution.EffectiveUser;
import dev.rono.permissions.core.storage.resolution.PermissionResolver;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Caches resolved {@link EffectiveUser} snapshots and invalidates on data mutations.
 */
public final class EffectiveUserCache {

    private final ConcurrentHashMap<CacheKey, CachedEntry> cache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, Set<UUID>> groupMembers = new ConcurrentHashMap<>();
    private volatile long generation;

    public EffectiveUser getOrResolve(UUID userId,
                                    PermissionContext context,
                                    Supplier<User> userLoader,
                                    Supplier<Map<Integer, Group>> groupsLoader,
                                    Supplier<List<Ladder>> laddersLoader) {
        purgeExpired();
        CacheKey key = new CacheKey(userId, ContextKeyCodec.encode(context), generation);
        CachedEntry cached = cache.get(key);
        if (cached != null && !cached.isExpired()) {
            return cached.effectiveUser();
        }
        EffectiveUser resolved = PermissionResolver.resolve(
                userLoader.get(),
                groupsLoader.get(),
                laddersLoader.get(),
                context,
                Instant.now());
        cache.put(key, new CachedEntry(resolved, Instant.now().plusSeconds(30)));
        indexMembership(userId, resolved.getGroupIds());
        return resolved;
    }

    public void invalidateUser(UUID userId) {
        generation++;
        cache.keySet().removeIf(key -> key.userId().equals(userId));
        groupMembers.values().forEach(set -> set.remove(userId));
    }

    public void invalidateGroup(int groupId) {
        generation++;
        Set<UUID> members = groupMembers.get(groupId);
        if (members == null || members.isEmpty()) {
            cache.clear();
            return;
        }
        cache.keySet().removeIf(key -> members.contains(key.userId()));
    }

    public void invalidateAll() {
        generation++;
        cache.clear();
        groupMembers.clear();
    }

    public int size() {
        return cache.size();
    }

    public long generation() {
        return generation;
    }

    private void purgeExpired() {
        cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    private void indexMembership(UUID userId, Set<Integer> groupIds) {
        for (Integer groupId : groupIds) {
            groupMembers.computeIfAbsent(groupId, ignored -> ConcurrentHashMap.newKeySet()).add(userId);
        }
    }

    private record CacheKey(UUID userId, String contextKey, long generation) {}

    private record CachedEntry(EffectiveUser effectiveUser, Instant expiresAt) {
        boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }
}
