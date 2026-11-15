package dev.rono.permissions.core.manager;

import dev.rono.permissions.api.ladder.Ladder;
import dev.rono.permissions.api.ladder.LadderCacheManager;
import dev.rono.permissions.api.util.Identifiers;
import dev.rono.permissions.core.model.LadderSnapshot;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import dev.rono.permissions.core.util.Immutables;

public final class LadderCacheManagerImpl implements LadderCacheManager {
    private final Map<String, LadderSnapshot> cache = new LinkedHashMap<>();

    @Override
    public synchronized Optional<Ladder> get(String name) {
        return Optional.ofNullable(cache.get(Identifiers.ladder(name)));
    }

    @Override
    public synchronized Set<String> identifiers() {
        return Immutables.copyOfSet(cache.keySet());
    }

    @Override
    public synchronized Collection<Ladder> all() {
        return Immutables.copyOfList(cache.values());
    }

    @Override
    public synchronized boolean isCached(String name) {
        return cache.containsKey(Identifiers.ladder(name));
    }

    @Override
    public synchronized boolean unload(String name) {
        return cache.remove(Identifiers.ladder(name)) != null;
    }

    synchronized void put(LadderSnapshot ladder) {
        cache.put(ladder.name(), ladder);
    }

    synchronized void remove(String name) {
        cache.remove(Identifiers.ladder(name));
    }

    synchronized void clear() {
        cache.clear();
    }

    synchronized List<LadderSnapshot> snapshots() {
        return Immutables.copyOfList(cache.values());
    }
}
