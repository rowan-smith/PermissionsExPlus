package dev.rono.permissions.api.permission;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Builder request for advanced permission additions (duration, expiry, context, source).
 *
 * <p>Preferred holder-based write path — use this instead of shorter {@code addPermission} overloads
 * when world scope, expiry, or source metadata matter.</p>
 */
public final class PermissionAddRequest {

    private final PermissionHolder holder;
    private final String permission;
    private final Duration duration;
    private final Instant expiresAt;
    private final Map<String, String> context;
    private final PermissionSource source;

    private PermissionAddRequest(Builder builder) {
        this.holder = builder.holder;
        this.permission = builder.permission;
        this.duration = builder.duration;
        this.expiresAt = builder.expiresAt;
        this.context = Map.copyOf(builder.context);
        this.source = builder.source;
    }

    public PermissionHolder holder() {
        return holder;
    }

    public String permission() {
        return permission;
    }

    public Duration duration() {
        return duration;
    }

    public Instant expiresAt() {
        return expiresAt;
    }

    public Map<String, String> context() {
        return context;
    }

    public PermissionSource source() {
        return source;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private PermissionHolder holder;
        private String permission;
        private Duration duration;
        private Instant expiresAt;
        private Map<String, String> context = new HashMap<>();
        private PermissionSource source = PermissionSource.SYSTEM;

        public Builder holder(PermissionHolder holder) {
            this.holder = holder;
            return this;
        }

        public Builder permission(String permission) {
            this.permission = permission;
            return this;
        }

        public Builder duration(Duration duration) {
            this.duration = duration;
            return this;
        }

        public Builder expiresAt(Instant expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }

        public Builder addContext(String key, String value) {
            this.context.put(key, value);
            return this;
        }

        public Builder source(PermissionSource source) {
            this.source = source;
            return this;
        }

        public PermissionAddRequest build() {
            if (holder == null) {
                throw new IllegalStateException("Holder cannot be null");
            }
            if (permission == null || permission.isEmpty()) {
                throw new IllegalStateException("Permission cannot be null or empty");
            }
            if (duration != null && expiresAt != null) {
                throw new IllegalStateException("Use either duration or expiresAt, not both");
            }
            Instant resolvedExpiresAt = expiresAt;
            if (duration != null && resolvedExpiresAt == null) {
                resolvedExpiresAt = Instant.now().plus(duration);
            }
            Builder copy = new Builder();
            copy.holder = holder;
            copy.permission = permission;
            copy.duration = duration;
            copy.expiresAt = resolvedExpiresAt;
            copy.context = new HashMap<>(context);
            copy.source = source;
            return new PermissionAddRequest(copy);
        }
    }
}
