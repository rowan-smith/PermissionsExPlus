package dev.rono.permissions.api.subject;

import dev.rono.permissions.api.permission.PermissionContext;

/**
 * A timed (temporary) permission assignment.
 *
 * @param permission       permission node
 * @param context          scope where the permission applies
 * @param remainingSeconds seconds until expiry; {@code 0} for transient/no expiry metadata
 */
public record TimedPermissionEntry(String permission, PermissionContext context, int remainingSeconds) {}
