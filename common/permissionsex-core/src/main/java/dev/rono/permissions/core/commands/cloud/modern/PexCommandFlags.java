package dev.rono.permissions.core.commands.cloud.modern;

import java.util.LinkedHashMap;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;

/**
 * Optional trailing context flags ({@code --world}, {@code --server}, …) for modern commands.
 */
public final class PexCommandFlags {
    public static final PexCommandFlags EMPTY = new PexCommandFlags(Map.of());

    private final Map<String, String> values;

    public PexCommandFlags(Map<String, String> values) {
        this.values = Collections.unmodifiableMap(new LinkedHashMap<>(values));
    }

    public boolean has(String key) {
        return values.containsKey(key);
    }

    public Optional<String> get(String key) {
        return Optional.ofNullable(values.get(key)).filter(v -> !v.isBlank());
    }

    public Map<String, String> asMap() {
        return values;
    }

    public static PexCommandFlags parseOptional(Queue<String> input) {
        if (input.isEmpty() || !input.peek().startsWith("--")) {
            return EMPTY;
        }
        return parse(input);
    }

    public static PexCommandFlags parse(Queue<String> input) {
        Map<String, String> map = new LinkedHashMap<>();
        while (!input.isEmpty()) {
            String token = input.peek();
            if (!token.startsWith("--")) {
                break;
            }
            input.poll();
            String key = token.substring(2);
            if (key.isBlank()) {
                throw new IllegalArgumentException("Empty flag name");
            }
            if (input.isEmpty()) {
                throw new IllegalArgumentException("Flag --" + key + " requires a value");
            }
            String value = input.poll();
            if (value.startsWith("--")) {
                throw new IllegalArgumentException("Flag --" + key + " requires a value");
            }
            map.put(key, value);
        }
        return new PexCommandFlags(map);
    }
}
