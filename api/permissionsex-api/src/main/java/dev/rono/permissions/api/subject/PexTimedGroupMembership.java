package dev.rono.permissions.api.subject;

import dev.rono.permissions.api.world.PexWorlds;

/**
 * A timed group membership on a user.
 *
 * @param groupName         group identifier
 * @param world             {@link PexWorlds#GLOBAL} or a specific world name
 * @param remainingSeconds  seconds until membership expires
 */
public record PexTimedGroupMembership(String groupName, String world, int remainingSeconds) {}
