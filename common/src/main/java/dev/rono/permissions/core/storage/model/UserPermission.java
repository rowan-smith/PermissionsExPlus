package dev.rono.permissions.core.storage.model;

import java.time.Instant;
import java.util.UUID;

public final class UserPermission extends PermissionEntry {

    private final UUID userId;

    public UserPermission(UUID userId,
                          String permission,
                          boolean allow,
                          String contextKey,
                          Instant expiresAt) {
        super(permission, allow, contextKey, expiresAt);
        this.userId = userId;
    }

    public UUID getUserId() { return userId; }
}
