package dev.rono.permissions.api.context;

import lombok.var;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

public interface ContextSet {

    Set<String> values(String key);

    Map<String, Set<String>> asMap();

    boolean contains(String key, String value);

    default boolean contains(ContextKeys key, String value) {
        return contains(key.key(), value);
    }

    boolean isEmpty();

    static ContextSet empty() {
        return ContextSets.empty();
    }

    static ContextBuilder builder() {
        return ContextSets.builder();
    }

    static ContextBuilder builder(ContextSet contexts) {
        Objects.requireNonNull(contexts, "contexts");

        var builder = builder();

        contexts.asMap().forEach((key, values) -> values.forEach(value -> builder.add(key, value)));

        return builder;
    }
}
