package ru.tehkode.permissions.backends;

import lombok.var;

import ru.tehkode.permissions.PermissionsData;
import ru.tehkode.permissions.PermissionsGroupData;
import ru.tehkode.permissions.PermissionsUserData;

/**
 * Helper class to hold static methods relating to import/export between
 * backends.
 * Should be refactored to be interface methods in jdk8
 */
public class BackendDataTransfer {
    private BackendDataTransfer() {
        // NO NO NO
    }

    private static void transferBase(PermissionsData from, PermissionsData to) {
        for (var entry : from.getPermissionsMap().entrySet()) {
            to.setPermissions(entry.getValue(), entry.getKey());
        }

        for (var entry : from.getOptionsMap().entrySet()) {
            for (var option : entry.getValue().entrySet()) {
                to.setOption(option.getKey(), option.getValue(), entry.getKey());
            }
        }

        to.setParents(from.getParents(null), null);

        for (var world : from.getWorlds()) {
            var groups = from.getParents(world);

            if (groups == null || groups.isEmpty()) {
                continue;
            }

            to.setParents(groups, world);
        }
    }

    public static void transferGroup(PermissionsGroupData from, PermissionsGroupData to) {
        transferBase(from, to);
    }

    public static void transferUser(PermissionsUserData from, PermissionsUserData to) {
        transferBase(from, to);
    }
}
