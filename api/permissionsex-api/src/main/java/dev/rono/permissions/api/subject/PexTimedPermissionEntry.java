package dev.rono.permissions.api.subject;

import dev.rono.permissions.api.world.PexWorlds;

/**
 * A timed (temporary) permission assignment.
 *
 * @param permission        permission node
 * @param world             {@link PexWorlds#GLOBAL} or a specific world name
 * @param remainingSeconds  seconds until expiry; {@code 0} for transient/no expiry metadata
 */
public record PexTimedPermissionEntry(String permission, String world, int remainingSeconds) {}
