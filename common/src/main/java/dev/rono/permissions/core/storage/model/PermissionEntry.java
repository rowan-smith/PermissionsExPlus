package dev.rono.permissions.core.storage.model;

import java.time.Instant;

public abstract class PermissionEntry {

    protected final String permission;
    protected final boolean allow;
    protected final String contextKey;
    protected final Instant expiresAt;

    protected PermissionEntry(String permission,
                              boolean allow,
                              String contextKey,
                              Instant expiresAt) {
        this.permission = permission;
        this.allow = allow;
        this.contextKey = contextKey;
        this.expiresAt = expiresAt;
    }

    public String getPermission() { return permission; }
    public boolean isAllow() { return allow; }
    public String getContextKey() { return contextKey; }
    public Instant getExpiresAt() { return expiresAt; }
}
