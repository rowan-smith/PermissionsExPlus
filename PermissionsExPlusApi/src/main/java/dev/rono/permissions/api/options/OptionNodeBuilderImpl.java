package dev.rono.permissions.api.options;

import dev.rono.permissions.api.context.ContextSet;
import dev.rono.permissions.api.util.Identifiers;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

final class OptionNodeBuilderImpl implements OptionNodeBuilder {

    private String key;

    private String value;

    private ContextSet contexts = ContextSet.empty();

    private Instant expiry;

    @Override
    public OptionNodeBuilder option(String key, String value) {
        Objects.requireNonNull(value, "value");

        if (value.codePoints().allMatch(Character::isWhitespace)) {
            throw new IllegalArgumentException("value cannot be blank");
        }

        this.key = Identifiers.optionKey(key);

        this.value = value;

        return this;
    }

    @Override
    public OptionNodeBuilder key(String key) {
        this.key = Identifiers.optionKey(key);

        return this;
    }

    @Override
    public OptionNodeBuilder value(String value) {
        Objects.requireNonNull(value, "value");

        if (value.codePoints().allMatch(Character::isWhitespace)) {
            throw new IllegalArgumentException("value cannot be blank");
        }

        this.value = value;

        return this;
    }

    @Override
    public OptionNodeBuilder contexts(ContextSet contexts) {
        this.contexts = Objects.requireNonNull(contexts, "contexts");

        return this;
    }

    @Override
    public OptionNodeBuilder expiry(Instant expiry) {
        this.expiry = Objects.requireNonNull(expiry, "expiry");

        return this;
    }

    @Override
    public OptionNodeBuilder permanent() {
        this.expiry = null;

        return this;
    }

    @Override
    public OptionNode build() {
        if (key == null) {
            throw new IllegalStateException("key must be provided");
        }

        if (value == null) {
            throw new IllegalStateException("value must be provided");
        }

        return new OptionNodeImpl(key, value, contexts, Optional.ofNullable(expiry));
    }
}
