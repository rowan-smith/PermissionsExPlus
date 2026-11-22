package dev.rono.permissions.core.store;

import lombok.var;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import dev.rono.permissions.core.util.Immutables;

/** Non-persistent {@link DataStore} for tests and throwaway servers. */
public final class MemoryDataStore implements DataStore {
    private final Map<String, Map<String, String>> data = new LinkedHashMap<>();

    private int transactionDepth;

    @Override
    public void open() {}

    @Override
    public synchronized <T> T transaction(Supplier<T> work) {
        if (transactionDepth > 0) {
            return work.get();
        }

        var saved = copy(data);

        transactionDepth++;

        try {
            return work.get();
        } catch (RuntimeException | Error error) {
            data.clear();
            data.putAll(saved);

            throw error;
        } finally {
            transactionDepth--;
        }
    }

    @Override
    public synchronized Optional<String> get(String category, String key) {
        return Optional.ofNullable(data.getOrDefault(category, Immutables.mapOf()).get(key));
    }

    @Override
    public synchronized Map<String, String> all(String category) {
        return Immutables.copyOfMap(data.getOrDefault(category, Immutables.mapOf()));
    }

    @Override
    public synchronized void put(String category, String key, String payload) {
        data.computeIfAbsent(category, ignored -> new LinkedHashMap<>()).put(key, payload);
    }

    @Override
    public synchronized boolean remove(String category, String key) {
        var values = data.get(category);
        return values != null && values.remove(key) != null;
    }

    @Override
    public String name() {
        return "Memory";
    }

    @Override
    public boolean persistent() {
        return false;
    }

    @Override
    public void close() {}

    static Map<String, Map<String, String>> copy(Map<String, Map<String, String>> data) {
        var copy = new LinkedHashMap<String, Map<String, String>>();

        data.forEach((category, values) -> copy.put(category, new LinkedHashMap<>(values)));

        return copy;
    }
}
