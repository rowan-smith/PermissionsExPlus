package dev.rono.permissions.core.manager;

import lombok.var;

import dev.rono.permissions.api.group.Group;
import dev.rono.permissions.api.group.GroupCacheManager;
import dev.rono.permissions.api.util.Identifiers;
import dev.rono.permissions.core.model.GroupSnapshot;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import dev.rono.permissions.core.util.Immutables;

public final class GroupCacheManagerImpl implements GroupCacheManager {
    private final Map<String, GroupSnapshot> cache = new LinkedHashMap<>();

    @Override
    public synchronized Optional<Group> get(String name) {
        return Optional.ofNullable(cache.get(Identifiers.group(name)));
    }

    @Override
    public synchronized Set<String> identifiers() {
        return Immutables.copyOfSet(cache.keySet());
    }

    @Override
    public synchronized Collection<Group> all() {
        return Immutables.copyOfList(cache.values());
    }

    @Override
    public synchronized boolean isCached(String name) {
        return cache.containsKey(Identifiers.group(name));
    }

    @Override
    public synchronized boolean unload(String name) {
        return cache.remove(Identifiers.group(name)) != null;
    }

    synchronized void put(GroupSnapshot group) {
        cache.put(group.name(), group);
    }

    synchronized void remove(String name) {
        cache.remove(Identifiers.group(name));
    }

    /**
     * Returns an action that puts this entry back as it is now, for undoing a
     * change made by an operation that failed.
     */
    synchronized Runnable restorer(String name) {
        var before = cache.get(name);

        return () -> {
            synchronized (this) {
                if (before != null) {
                    cache.put(name, before);
                } else {
                    cache.remove(name);
                }
            }
        };
    }

    synchronized void clear() {
        cache.clear();
    }

    synchronized List<GroupSnapshot> snapshots() {
        return Immutables.copyOfList(cache.values());
    }
}
