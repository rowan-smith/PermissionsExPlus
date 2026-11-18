package ru.tehkode.permissions.backends.data;

import java.time.Instant;
import ru.tehkode.permissions.PermissionsUserData;

/** Timed group memberships for user data backed by PermissionsExPlus. */
public final class TimedGroups {
    private TimedGroups() {}

    /**
     * Adds a membership that PermissionsExPlus expires by itself at {@code expiry}.
     *
     * @return false if {@code data} is not backed by PermissionsExPlus, in which
     *         case the caller falls back to the PermissionsEx option-only form
     */
    public static boolean add(PermissionsUserData data, String group, String world, Instant expiry) {
        if (!(data instanceof UserData)) {
            return false;
        }

        ((UserData) data).addTimedGroup(group, world, expiry);
        return true;
    }
}
