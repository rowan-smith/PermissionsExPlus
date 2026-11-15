package dev.rono.permissions.core.context;

import lombok.var;

import dev.rono.permissions.api.context.ContextRegistry;
import dev.rono.permissions.api.platform.context.ContextRegistration;

import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import dev.rono.permissions.core.util.Immutables;

public final class RuntimeContextRegistry implements ContextRegistry {
    private static final Pattern KEY_PATTERN = Pattern.compile("[a-z0-9][a-z0-9._-]*");
    private static final String RESERVED_CONTEXT_KEY = "context";

    private final ConcurrentHashMap<String, Supplier<Collection<String>>> suppliers = new ConcurrentHashMap<>();

    @Override
    public ContextRegistration registerContextType(String key, Supplier<Collection<String>> valueSupplier) {
        var normalizedKey = normalizeKey(key);

        if (RESERVED_CONTEXT_KEY.equals(normalizedKey)) {
            throw new IllegalArgumentException("The universal --context flag is not supported");
        }

        var supplier = Objects.requireNonNull(valueSupplier, "valueSupplier");
        suppliers.put(normalizedKey, supplier);

        return () -> suppliers.remove(normalizedKey, supplier);
    }

    @Override
    public Set<String> registeredKeys() {
        return Collections.unmodifiableSet(new TreeSet<>(suppliers.keySet()));
    }

    @Override
    public Collection<String> validValues(String key) {
        var supplier = suppliers.get(normalizeKey(key));

        if (supplier == null) {
            return Immutables.listOf();
        }

        var values = Objects.requireNonNull(supplier.get(), "Context value supplier returned null");
        return values.stream().filter(Objects::nonNull).map(String::trim).filter(value -> !value.isEmpty()).distinct().sorted().collect(Immutables.toList());
    }

    static String normalizeKey(String key) {
        Objects.requireNonNull(key, "key");

        var normalized = key.trim().toLowerCase(Locale.ROOT);

        if (!KEY_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException("Invalid context key: " + key);
        }

        return normalized;
    }
}
