package dev.rono.permissions.api.options;

import dev.rono.permissions.api.context.ContextSet;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

final class OptionNodeImpl implements OptionNode {
    private final String key;
    private final String value;
    private final ContextSet contexts;
    private final Optional<Instant> expiry;

    OptionNodeImpl(String key, String value, ContextSet contexts, Optional<Instant> expiry) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");
        Objects.requireNonNull(contexts, "contexts");
        Objects.requireNonNull(expiry, "expiry");

        if (key.codePoints().allMatch(Character::isWhitespace)) {
            throw new IllegalArgumentException("key cannot be blank");
        }

        if (value.codePoints().allMatch(Character::isWhitespace)) {
            throw new IllegalArgumentException("value cannot be blank");
        }

        this.key = key;
        this.value = value;
        this.contexts = contexts;
        this.expiry = expiry;
    }

    @Override
    public String key() {
        return key;
    }

    @Override
    public String value() {
        return value;
    }

    @Override
    public ContextSet contexts() {
        return contexts;
    }

    @Override
    public Optional<Instant> expiry() {
        return expiry;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof OptionNodeImpl)) {
            return false;
        }

        OptionNodeImpl that = (OptionNodeImpl) other;

        return key.equals(that.key)
                && value.equals(that.value)
                && contexts.equals(that.contexts)
                && expiry.equals(that.expiry);
    }

    @Override
    public int hashCode() {
        int result = key.hashCode();
        result = 31 * result + value.hashCode();
        result = 31 * result + contexts.hashCode();
        result = 31 * result + expiry.hashCode();

        return result;
    }

    @Override
    public String toString() {
        return "OptionNodeImpl[key=" + key + ", value=" + value + ", contexts=" + contexts + ", expiry=" + expiry + "]";
    }
}
