package dev.rono.permissions.api.subject;

import dev.rono.permissions.api.permission.PermissionContext;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Read-only permission and metadata evaluation for a subject.
 *
 * <p>All scoped operations take a {@link PermissionContext}. Parameterless overloads use
 * {@link PermissionContext#global()}.</p>
 */
public interface PermissionView {

    boolean has(String permission, PermissionContext context);

    default boolean has(String permission) {
        return has(permission, PermissionContext.global());
    }

    List<String> permissions(PermissionContext context);

    default List<String> permissions() {
        return permissions(PermissionContext.global());
    }

    List<String> effectivePermissions(PermissionContext context);

    default List<String> effectivePermissions() {
        return effectivePermissions(PermissionContext.global());
    }

    List<String> timedPermissions(PermissionContext context);

    default List<String> timedPermissions() {
        return timedPermissions(PermissionContext.global());
    }

    List<TimedPermissionEntry> timedPermissionEntries(PermissionContext context);

    default List<TimedPermissionEntry> timedPermissionEntries() {
        return timedPermissionEntries(PermissionContext.global());
    }

    int timedPermissionRemainingSeconds(String permission, PermissionContext context);

    default int timedPermissionRemainingSeconds(String permission) {
        return timedPermissionRemainingSeconds(permission, PermissionContext.global());
    }

    default boolean hasTimedPermission(String permission, PermissionContext context) {
        return timedPermissions(context).contains(permission);
    }

    default boolean hasTimedPermission(String permission) {
        return hasTimedPermission(permission, PermissionContext.global());
    }

    /** @return backend realm keys where this subject has stored data */
    Set<String> configuredRealms();

    Map<String, List<String>> permissionsByRealm();

    Map<String, List<String>> effectivePermissionsByRealm();

    String prefix(PermissionContext context);

    default String prefix() {
        return prefix(PermissionContext.global());
    }

    String suffix(PermissionContext context);

    default String suffix() {
        return suffix(PermissionContext.global());
    }

    String option(String key, PermissionContext context);

    default String option(String key) {
        return option(key, PermissionContext.global());
    }

    Map<String, String> options(PermissionContext context);

    default Map<String, String> options() {
        return options(PermissionContext.global());
    }
}
