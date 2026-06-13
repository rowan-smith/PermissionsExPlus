package dev.rono.permissions.core.api.pex;

import dev.rono.permissions.api.permission.PermissionHolder;
import dev.rono.permissions.api.permission.PermissionNode;
import dev.rono.permissions.api.permission.PermissionSource;

import java.time.Instant;
import java.util.Map;

final class PermissionNodeImpl implements PermissionNode {

    private final PermissionHolder holder;
    private final String permission;
    private final Instant expiresAt;
    private final Map<String, String> context;
    private final PermissionSource source;

    PermissionNodeImpl(
            PermissionHolder holder,
            String permission,
            Instant expiresAt,
            Map<String, String> context,
            PermissionSource source) {
        this.holder = holder;
        this.permission = permission;
        this.expiresAt = expiresAt;
        this.context = context == null ? Map.of() : Map.copyOf(context);
        this.source = source;
    }

    @Override
    public PermissionHolder holder() {
        return holder;
    }

    @Override
    public String permission() {
        return permission;
    }

    @Override
    public Instant expiresAt() {
        return expiresAt;
    }

    @Override
    public Map<String, String> context() {
        return context;
    }

    @Override
    public PermissionSource source() {
        return source;
    }
}
