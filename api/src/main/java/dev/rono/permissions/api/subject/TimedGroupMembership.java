package dev.rono.permissions.api.subject;

import dev.rono.permissions.api.permission.PermissionContext;

/**
 * A timed group membership on a user.
 *
 * @param groupName          group identifier
 * @param context            scope where the membership applies
 * @param remainingSeconds   seconds until membership expires
 */
public record TimedGroupMembership(String groupName, PermissionContext context, int remainingSeconds) {}
