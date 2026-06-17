package dev.rono.permissions.core.storage.model;

import java.time.Instant;

public final class GroupPermission extends PermissionEntry {

    private final int groupId;

    public GroupPermission(int groupId,
                           String permission,
                           boolean allow,
                           String contextKey,
                           Instant expiresAt) {
        super(permission, allow, contextKey, expiresAt);
        this.groupId = groupId;
    }

    public int getGroupId() { return groupId; }
}
