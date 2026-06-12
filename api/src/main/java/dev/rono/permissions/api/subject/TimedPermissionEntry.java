package dev.rono.permissions.api.subject;

import dev.rono.permissions.api.world.Worlds;

/**
 * A timed (temporary) permission assignment.
 *
 * @param permission        permission node
 * @param world             {@link Worlds#GLOBAL} or a specific world name
 * @param remainingSeconds  seconds until expiry; {@code 0} for transient/no expiry metadata
 */
public record TimedPermissionEntry(String permission, String world, int remainingSeconds) {}
