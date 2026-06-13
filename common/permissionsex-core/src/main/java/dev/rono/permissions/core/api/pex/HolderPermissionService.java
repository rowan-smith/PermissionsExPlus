package dev.rono.permissions.core.api.pex;

import dev.rono.permissions.api.permission.PermissionAddRequest;
import dev.rono.permissions.api.permission.PermissionContext;
import dev.rono.permissions.api.permission.PermissionHolder;
import dev.rono.permissions.api.permission.PermissionNode;
import dev.rono.permissions.api.permission.PermissionSource;
import dev.rono.permissions.core.DefaultPermissionManager;
import dev.rono.permissions.core.api.ContextPermissionEvaluator;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class HolderPermissionService {

    private final DefaultPermissionManager manager;
    private final HolderEntityResolver resolver;

    public HolderPermissionService(DefaultPermissionManager manager) {
        this.manager = manager;
        this.resolver = new HolderEntityResolver(manager);
    }

    public PermissionNode addPermission(PermissionHolder holder, String permission) {
        return addPermission(holder, permission, null);
    }

    public PermissionNode addPermission(PermissionHolder holder, String permission, Duration duration) {
        var entity = resolver.resolve(holder);
        String world = null;
        if (duration == null) {
            entity.addPermission(permission, world);
            return new PermissionNodeImpl(holder, permission, null, Map.of(), PermissionSource.SYSTEM);
        }
        var seconds = (int) Math.min(Integer.MAX_VALUE, duration.getSeconds());
        entity.addTimedPermission(permission, world, seconds);
        var expiresAt = Instant.now().plus(duration);
        return new PermissionNodeImpl(holder, permission, expiresAt, Map.of(), PermissionSource.SYSTEM);
    }

    public PermissionNode addPermission(PermissionAddRequest request) {
        var entity = resolver.resolve(request.holder());
        var world = resolver.storageRealm(request.context());
        var expiresAt = request.expiresAt();
        if (expiresAt != null) {
            var seconds = (int) Math.max(1, expiresAt.getEpochSecond() - Instant.now().getEpochSecond());
            entity.addTimedPermission(request.permission(), world, seconds);
        } else {
            entity.addPermission(request.permission(), world);
        }
        return new PermissionNodeImpl(
                request.holder(),
                request.permission(),
                expiresAt,
                request.contextMap(),
                request.source());
    }

    public void removePermission(PermissionHolder holder, String permission) {
        var entity = resolver.resolve(holder);
        entity.removePermission(permission, null);
    }

    public boolean hasPermission(PermissionHolder holder, String permission) {
        return hasPermission(holder, permission, Map.of());
    }

    public boolean hasPermission(PermissionHolder holder, String permission, Map<String, String> context) {
        return hasPermission(holder, permission, PermissionContext.fromMap(context));
    }

    public boolean hasPermission(PermissionHolder holder, String permission, PermissionContext context) {
        var entity = resolver.resolve(holder);
        return ContextPermissionEvaluator.has(entity, permission, context, manager.getPlatform());
    }

    public List<PermissionNode> getPermissions(PermissionHolder holder) {
        var entity = resolver.resolve(holder);
        var nodes = new ArrayList<PermissionNode>();
        for (String perm : entity.getOwnPermissions(null)) {
            nodes.add(new PermissionNodeImpl(holder, perm, null, Map.of(), PermissionSource.SYSTEM));
        }
        return List.copyOf(nodes);
    }
}
