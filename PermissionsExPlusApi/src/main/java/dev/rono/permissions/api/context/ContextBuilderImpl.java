package dev.rono.permissions.api.context;

import lombok.var;

import dev.rono.permissions.api.util.Identifiers;

import java.util.*;

final class ContextBuilderImpl implements ContextBuilder {

    private final Map<String, Set<String>> contexts = new LinkedHashMap<>();

    @Override
    public ContextBuilder add(String key, String value) {
        Objects.requireNonNull(value, "value");

        if (value.codePoints().allMatch(Character::isWhitespace)) {
            throw new IllegalArgumentException("value cannot be blank");
        }

        contexts.computeIfAbsent(Identifiers.contextKey(key), ignored -> new LinkedHashSet<>()).add(value);

        return this;
    }

    @Override
    public ContextBuilder remove(String key, String value) {
        key = Identifiers.contextKey(key);

        Objects.requireNonNull(value, "value");

        if (value.codePoints().allMatch(Character::isWhitespace)) {
            throw new IllegalArgumentException("value cannot be blank");
        }

        var values = contexts.get(key);

        if (values != null) {
            values.remove(value);

            if (values.isEmpty()) {
                contexts.remove(key);
            }
        }

        return this;
    }

    @Override
    public ContextBuilder remove(String key) {
        contexts.remove(Identifiers.contextKey(key));

        return this;
    }

    @Override
    public ContextBuilder set(String key, String value) {
        remove(key);

        return add(key, value);
    }

    @Override
    public ContextBuilder clear() {
        contexts.clear();

        return this;
    }

    @Override
    public ContextSet build() {
        if (contexts.isEmpty()) {
            return ContextSet.empty();
        }

        return new ImmutableContextSet(contexts);
    }
}
