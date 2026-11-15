package dev.rono.permissions.api.parent;

import dev.rono.permissions.api.context.ContextSet;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

final class ParentNodeImpl implements ParentNode {
    private final String group;
    private final ContextSet contexts;
    private final Optional<Instant> expiry;

    ParentNodeImpl(String group, ContextSet contexts, Optional<Instant> expiry) {
        Objects.requireNonNull(group, "group");
        Objects.requireNonNull(contexts, "contexts");
        Objects.requireNonNull(expiry, "expiry");

        if (group.codePoints().allMatch(Character::isWhitespace)) {
            throw new IllegalArgumentException("group cannot be blank");
        }

        this.group = group;
        this.contexts = contexts;
        this.expiry = expiry;
    }

    @Override
    public String group() {
        return group;
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

        if (!(other instanceof ParentNodeImpl)) {
            return false;
        }

        ParentNodeImpl that = (ParentNodeImpl) other;

        return group.equals(that.group)
                && contexts.equals(that.contexts)
                && expiry.equals(that.expiry);
    }

    @Override
    public int hashCode() {
        int result = group.hashCode();
        result = 31 * result + contexts.hashCode();
        result = 31 * result + expiry.hashCode();

        return result;
    }

    @Override
    public String toString() {
        return "ParentNodeImpl[group=" + group + ", contexts=" + contexts + ", expiry=" + expiry + "]";
    }
}
