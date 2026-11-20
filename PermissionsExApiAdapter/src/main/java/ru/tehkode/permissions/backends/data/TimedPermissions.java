package ru.tehkode.permissions.backends.data;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import ru.tehkode.permissions.PermissionsData;

/**
 * Timed permissions for data backed by PermissionsExPlus, stored as permission
 * nodes with an expiry, so they survive restarts and PermissionsExPlus removes
 * them when they expire. Other data (such as the memory backend) is not
 * supported, and callers fall back to PermissionsEx's in-memory timed
 * permissions.
 */
public final class TimedPermissions {
    private TimedPermissions() {}

    public static boolean supported(PermissionsData data) {
        return data instanceof AbstractData;
    }

    /** @return false if {@code data} is not backed by PermissionsExPlus */
    public static boolean add(PermissionsData data, String permission, String world, Instant expiry) {
        if (!supported(data)) {
            return false;
        }

        ((AbstractData) data).addTimedPermission(permission, world, expiry);
        return true;
    }

    /** @return whether a stored timed permission was removed */
    public static boolean remove(PermissionsData data, String permission, String world) {
        return supported(data) && ((AbstractData) data).removeTimedPermission(permission, world);
    }

    /** Stored timed permissions in {@code world}, with their expiry. */
    public static Map<String, Instant> list(PermissionsData data, String world) {
        return supported(data) ? ((AbstractData) data).getTimedPermissions(world) : Collections.<String, Instant>emptyMap();
    }

    /** Stored timed permissions in every world; the global world is {@code ""}. */
    public static Map<String, Map<String, Instant>> all(PermissionsData data) {
        return supported(data) ? ((AbstractData) data).getAllTimedPermissions() : Collections.<String, Map<String, Instant>>emptyMap();
    }
}
