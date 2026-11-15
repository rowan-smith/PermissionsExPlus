package dev.rono.permissions.api.permission;

import dev.rono.permissions.api.context.ContextSet;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

final class PermissionNodeImpl implements PermissionNode {
    private final String permission;
    private final PermissionValue value;
    private final ContextSet contexts;
    private final Optional<Instant> expiry;

    PermissionNodeImpl(String permission, PermissionValue value, ContextSet contexts, Optional<Instant> expiry) {
        Objects.requireNonNull(permission, "permission");
        Objects.requireNonNull(value, "value");
        Objects.requireNonNull(contexts, "contexts");
        Objects.requireNonNull(expiry, "expiry");

        if (permission.codePoints().allMatch(Character::isWhitespace)) {
            throw new IllegalArgumentException("permission cannot be blank");
        }

        this.permission = permission;
        this.value = value;
        this.contexts = contexts;
        this.expiry = expiry;
    }

    @Override
    public String permission() {
        return permission;
    }

    @Override
    public PermissionValue value() {
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

        if (!(other instanceof PermissionNodeImpl)) {
            return false;
        }

        PermissionNodeImpl that = (PermissionNodeImpl) other;

        return permission.equals(that.permission)
                && value.equals(that.value)
                && contexts.equals(that.contexts)
                && expiry.equals(that.expiry);
    }

    @Override
    public int hashCode() {
        int result = permission.hashCode();
        result = 31 * result + value.hashCode();
        result = 31 * result + contexts.hashCode();
        result = 31 * result + expiry.hashCode();

        return result;
    }

    @Override
    public String toString() {
        return "PermissionNodeImpl[permission=" + permission + ", value=" + value + ", contexts=" + contexts + ", expiry=" + expiry + "]";
    }
}
