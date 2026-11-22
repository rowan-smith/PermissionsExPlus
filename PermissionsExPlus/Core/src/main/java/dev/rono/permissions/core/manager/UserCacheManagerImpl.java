package dev.rono.permissions.core.manager;

import lombok.var;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import dev.rono.permissions.api.user.User;
import dev.rono.permissions.api.user.UserCacheManager;
import dev.rono.permissions.api.util.Identifiers;
import dev.rono.permissions.core.model.UserSnapshot;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import dev.rono.permissions.core.util.Immutables;

public final class UserCacheManagerImpl implements UserCacheManager {

    private final Map<UUID, UserSnapshot> onlineCache = new LinkedHashMap<>();
    private final Cache<UUID, UserSnapshot> offlineCache;
    private final Set<UUID> online = new HashSet<>();
    private final Queue<User> evicted = new ConcurrentLinkedQueue<>();

    public UserCacheManagerImpl(Duration offlineExpiry, int maximumOfflineUsers) {
        var builder = Caffeine.newBuilder()
                .expireAfterAccess(offlineExpiry)
                .executor(Runnable::run)
                .removalListener((UUID id, UserSnapshot user, RemovalCause cause) -> {
                    if (cause.wasEvicted()) {
                        evicted.add(user);
                    }
                });

        if (maximumOfflineUsers > 0) {
            builder.maximumSize(maximumOfflineUsers);
        }

        this.offlineCache = builder.build();
    }

    @Override
    public synchronized Optional<User> get(UUID id) {
        var onlineUser = onlineCache.get(id);

        if (onlineUser != null) {
            return Optional.of(onlineUser);
        }

        return Optional.ofNullable(offlineCache.getIfPresent(id));
    }

    @Override
    public synchronized Optional<User> get(String username) {
        var key = Identifiers.usernameLookup(username);

        return snapshots().stream()
                .filter(user -> Identifiers.usernameLookup(user.name()).equals(key))
                .map(User.class::cast)
                .findFirst();
    }

    @Override
    public Set<String> names() {
        return snapshots().stream()
                .map(User::name)
                .collect(Collectors.toSet());
    }

    @Override
    public synchronized Set<UUID> identifiers() {
        return snapshots().stream()
                .map(User::uniqueId)
                .collect(Collectors.toSet());
    }

    @Override
    public synchronized Set<User> all() {
        return snapshots().stream()
                .map(User.class::cast)
                .collect(Collectors.toSet());
    }

    @Override
    public synchronized boolean isCached(UUID key) {
        return onlineCache.containsKey(key) || offlineCache.getIfPresent(key) != null;
    }

    @Override
    public synchronized boolean unload(UUID key) {
        if (online.contains(key)) {
            return false;
        }

        var user = offlineCache.getIfPresent(key);
        offlineCache.invalidate(key);

        return user != null;
    }

    public synchronized void put(UserSnapshot user) {
        var id = user.uniqueId();

        if (online.contains(id)) {
            onlineCache.put(id, user);
            offlineCache.invalidate(id);
        } else {
            offlineCache.put(id, user);
        }
    }

    public synchronized void remove(UUID id) {
        online.remove(id);
        onlineCache.remove(id);
        offlineCache.invalidate(id);
    }

    /**
     * Returns an action that puts this user's entry back as it is now, for
     * undoing a change made by an operation that failed.
     */
    synchronized Runnable restorer(UUID id) {
        var wasOnline = online.contains(id);
        var before = onlineCache.containsKey(id) ? onlineCache.get(id) : offlineCache.getIfPresent(id);

        return () -> {
            synchronized (this) {
                if (wasOnline) {
                    online.add(id);
                }

                if (before != null) {
                    put(before);
                } else {
                    onlineCache.remove(id);
                    offlineCache.invalidate(id);
                }
            }
        };
    }

    public synchronized void markOnline(UUID id) {
        online.add(id);

        var user = offlineCache.getIfPresent(id);
        if (user != null) {
            offlineCache.invalidate(id);
            onlineCache.put(id, user);
        }
    }

    public synchronized void markOffline(UUID id) {
        online.remove(id);

        var user = onlineCache.remove(id);
        if (user != null) {
            offlineCache.put(id, user);
        }
    }

    public synchronized List<User> evictInactive() {
        offlineCache.cleanUp();

        var removed = new ArrayList<User>();
        User user;

        while ((user = evicted.poll()) != null) {
            removed.add(user);
        }

        return Immutables.copyOfList(removed);
    }

    private List<UserSnapshot> snapshots() {
        var users = new LinkedHashMap<UUID, UserSnapshot>();

        users.putAll(offlineCache.asMap());
        users.putAll(onlineCache);

        return Immutables.copyOfList(users.values());
    }
}
