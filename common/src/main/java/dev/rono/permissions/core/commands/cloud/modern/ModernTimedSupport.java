package dev.rono.permissions.core.commands.cloud.modern;

import dev.rono.permissions.core.commands.CoreCommandService;
import dev.rono.permissions.core.commands.PexCompactDuration;

final class ModernTimedSupport {
    private ModernTimedSupport() {}

    static String addUserTimedPermission(
            CoreCommandService service, String user, String permission, String duration, String world) {
        int seconds = PexCompactDuration.parseSeconds(duration);
        if (seconds < 0) {
            return service.userAddPermission(user, permission, world);
        }
        return service.userAddTimedPermissionSeconds(user, permission, seconds, world);
    }

    static String addGroupTimedPermission(
            CoreCommandService service, String group, String permission, String duration, String world) {
        int seconds = PexCompactDuration.parseSeconds(duration);
        if (seconds < 0) {
            return service.groupAddPermission(group, permission, world);
        }
        return service.groupAddTimedPermissionSeconds(group, permission, seconds, world);
    }

    static String addUserTimedGroup(
            CoreCommandService service, String user, String group, String duration, String world) {
        int seconds = PexCompactDuration.parseSeconds(duration);
        return service.userAddGroupSeconds(user, group, world, seconds);
    }
}
