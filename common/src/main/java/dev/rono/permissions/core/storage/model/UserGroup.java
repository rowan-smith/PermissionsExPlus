package dev.rono.permissions.core.storage.model;

import java.time.Instant;
import java.util.UUID;

public final class UserGroup {

    private final UUID userId;
    private final int groupId;
    private final Instant expiresAt;

    public UserGroup(UUID userId, int groupId, Instant expiresAt) {
        this.userId = userId;
        this.groupId = groupId;
        this.expiresAt = expiresAt;
    }

    public UUID getUserId() { return userId; }
    public int getGroupId() { return groupId; }
    public Instant getExpiresAt() { return expiresAt; }
}
