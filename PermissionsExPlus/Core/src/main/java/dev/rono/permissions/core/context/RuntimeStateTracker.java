package dev.rono.permissions.core.context;

import lombok.var;

import dev.rono.permissions.api.context.ContextSet;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import dev.rono.permissions.core.util.Immutables;

public final class RuntimeStateTracker implements CoreStateTracker {
    private final ConcurrentHashMap<UUID, ConcurrentHashMap<String, String>> states = new ConcurrentHashMap<>();
    private volatile ContextPolicy policy;

    public RuntimeStateTracker() {}

    public RuntimeStateTracker(ContextPolicy policy) {
        configure(policy);
    }

    public void configure(ContextPolicy policy) {
        this.policy = Objects.requireNonNull(policy, "policy");

        states.replaceAll((subject, values) -> new ConcurrentHashMap<>(policy.select(values)));
        states.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }

    @Override
    public void updateState(UUID subject, String key, String value) {
        Objects.requireNonNull(subject, "subject");
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");

        var normalizedKey = key.toLowerCase(Locale.ROOT);

        var selected = currentPolicy().select(Immutables.mapOf(normalizedKey, value));
        if (selected.isEmpty()) {
            states.computeIfPresent(subject, (ignored, current) -> {
                current.remove(normalizedKey);
                return current.isEmpty() ? null : current;
            });

            return;
        }

        states.computeIfAbsent(subject, ignored -> new ConcurrentHashMap<>()).put(normalizedKey, value);
    }

    @Override
    public void replaceState(UUID subject, Map<String, String> values) {
        Objects.requireNonNull(subject, "subject");
        Objects.requireNonNull(values, "values");

        var normalized = new HashMap<String, String>();

        values.forEach((key, value) -> normalized.put(Objects.requireNonNull(key, "state key").toLowerCase(Locale.ROOT), Objects.requireNonNull(value, "state value")));

        var selected = currentPolicy().select(normalized);
        if (selected.isEmpty()) {
            states.remove(subject);
            return;
        }

        states.put(subject, new ConcurrentHashMap<>(selected));
    }

    @Override
    public void clearState(UUID subject) {
        states.remove(Objects.requireNonNull(subject, "subject"));
    }

    ContextSet contexts(UUID subject) {
        var values = states.get(subject);
        if (values == null) {
            return ContextSet.empty();
        }

        var builder = ContextSet.builder();

        values.forEach(builder::add);

        return builder.build();
    }

    private ContextPolicy currentPolicy() {
        return Objects.requireNonNull(policy, "State tracker has not been configured");
    }
}
