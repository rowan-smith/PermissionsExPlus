package dev.rono.permissions.core.storage.resolution;

import dev.rono.permissions.api.permission.PermissionContext;
import dev.rono.permissions.core.api.ContextPermissionEvaluator;
import dev.rono.permissions.core.storage.ContextKeyCodec;
import dev.rono.permissions.core.storage.backend.LocalSqlBackend;
import ru.tehkode.permissions.PermissionEntity;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.PermissionMatcher;
import ru.tehkode.permissions.backends.PermissionBackend;

import java.util.ArrayList;
import java.util.List;

/** Bridges runtime permission checks to {@link PermissionResolver} for the local H2 backend. */
public final class LocalPermissionEvaluator {

    private LocalPermissionEvaluator() {}

    public static boolean usesLocalBackend(PermissionManager manager) {
        return manager != null && manager.getBackend() instanceof LocalSqlBackend;
    }

    public static LocalSqlBackend localBackend(PermissionManager manager) {
        PermissionBackend backend = manager.getBackend();
        if (backend instanceof LocalSqlBackend local) {
            return local;
        }
        throw new IllegalStateException("Active backend is not local: " + backend);
    }

    public static boolean supportsEntity(PermissionEntity entity, PermissionManager manager) {
        return entity != null
                && entity.getType() == PermissionEntity.Type.USER
                && usesLocalBackend(manager);
    }

    public static boolean hasUser(
            PermissionManager manager, String userName, String permission, PermissionContext context) {
        if (!usesLocalBackend(manager)) {
            return false;
        }
        try {
            EffectiveUser effective = localBackend(manager).resolveEffectiveUser(userName, context);
            PermissionMatcher matcher = manager.getPermissionMatcher();
            String requestKey = ContextKeyCodec.encode(context);
            return effective.hasPermission(matcher, permission, requestKey);
        } catch (Exception ex) {
            manager.getLogger().warning(
                    "Local permission check failed for " + userName + ": " + ex.getMessage());
            return false;
        }
    }

    public static boolean hasUserLegacyWorld(
            PermissionManager manager, String userName, String permission, String world) {
        return hasUser(manager, userName, permission, ContextPermissionEvaluator.fromLegacyWorld(world));
    }

    public static List<ResolvedPermissionViewAdapter> resolvedPermissions(
            PermissionManager manager, String userName, PermissionContext context) {
        if (!usesLocalBackend(manager)) {
            return List.of();
        }
        try {
            EffectiveUser effective = localBackend(manager).resolveEffectiveUser(userName, context);
            List<ResolvedPermissionViewAdapter> out = new ArrayList<>(effective.getPermissions().size());
            for (ResolvedPermission permission : effective.getPermissions()) {
                out.add(new ResolvedPermissionViewAdapter(permission));
            }
            return List.copyOf(out);
        } catch (Exception ex) {
            manager.getLogger().warning(
                    "Failed to resolve permissions for " + userName + ": " + ex.getMessage());
            return List.of();
        }
    }
}
