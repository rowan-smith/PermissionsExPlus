package dev.rono.permissions.api.context;

import lombok.var;

import dev.rono.permissions.api.util.Identifiers;

import java.util.*;

final class ImmutableContextSet implements ContextSet {

    private final Map<String, Set<String>> contexts;

    ImmutableContextSet(Map<String, Set<String>> contexts) {
        Objects.requireNonNull(contexts, "contexts");

        var snapshot = new LinkedHashMap<String, Set<String>>(contexts.size());

        contexts.forEach((key, values) -> {
            Objects.requireNonNull(key, "context key");

            Objects.requireNonNull(values, "context values");

            snapshot.put(key, Collections.unmodifiableSet(new LinkedHashSet<>(values)));
        });

        this.contexts = Collections.unmodifiableMap(snapshot);
    }

    @Override
    public Set<String> values(String key) {
        Objects.requireNonNull(key, "key");

        return contexts.getOrDefault(Identifiers.contextKey(key), Collections.<String>emptySet());
    }

    @Override
    public Map<String, Set<String>> asMap() {
        return contexts;
    }

    @Override
    public boolean contains(String key, String value) {
        Objects.requireNonNull(key, "key");

        Objects.requireNonNull(value, "value");

        return values(key).contains(value);
    }

    @Override
    public boolean isEmpty() {
        return contexts.isEmpty();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        return other instanceof ContextSet && contexts.equals(((ContextSet) other).asMap());
    }

    @Override
    public int hashCode() {
        return contexts.hashCode();
    }

    @Override
    public String toString() {
        return contexts.toString();
    }
}
