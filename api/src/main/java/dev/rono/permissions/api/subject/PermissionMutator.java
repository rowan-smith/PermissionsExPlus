package dev.rono.permissions.api.subject;

import dev.rono.permissions.api.permission.PermissionContext;
import java.util.List;

/** Write operations for a permission subject. All scoped operations use {@link PermissionContext}. */
public interface PermissionMutator {

    void addPermission(String permission, PermissionContext context);

    default void addPermission(String permission) {
        addPermission(permission, PermissionContext.global());
    }

    void removePermission(String permission, PermissionContext context);

    default void removePermission(String permission) {
        removePermission(permission, PermissionContext.global());
    }

    void setPermissions(List<String> permissions, PermissionContext context);

    default void setPermissions(List<String> permissions) {
        setPermissions(permissions, PermissionContext.global());
    }

    void addTimedPermission(String permission, PermissionContext context, int lifetimeSeconds);

    default void addTimedPermission(String permission, int lifetimeSeconds) {
        addTimedPermission(permission, PermissionContext.global(), lifetimeSeconds);
    }

    void removeTimedPermission(String permission, PermissionContext context);

    default void removeTimedPermission(String permission) {
        removeTimedPermission(permission, PermissionContext.global());
    }

    void setPrefix(String prefix, PermissionContext context);

    default void setPrefix(String prefix) {
        setPrefix(prefix, PermissionContext.global());
    }

    void setSuffix(String suffix, PermissionContext context);

    default void setSuffix(String suffix) {
        setSuffix(suffix, PermissionContext.global());
    }

    void setOption(String key, String value, PermissionContext context);

    default void setOption(String key, String value) {
        setOption(key, value, PermissionContext.global());
    }

    void save();

    void delete();
}
