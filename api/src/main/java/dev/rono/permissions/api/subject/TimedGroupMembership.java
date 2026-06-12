package dev.rono.permissions.api.subject;

import dev.rono.permissions.api.world.Worlds;

/**
 * A timed group membership on a user.
 *
 * @param groupName         group identifier
 * @param world             {@link Worlds#GLOBAL} or a specific world name
 * @param remainingSeconds  seconds until membership expires
 */
public record TimedGroupMembership(String groupName, String world, int remainingSeconds) {}
