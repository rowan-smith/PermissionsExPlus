package dev.rono.permissions.core.migration;

import lombok.var;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Data read from an original PermissionsEx installation, independent of the
 * backend (file or SQL) it came from. The world {@code ""} means global.
 */
public final class LegacyPexData {
    public static final String GLOBAL = "";

    final Map<String, Entity> groups = new LinkedHashMap<>();
    final Map<String, Entity> users = new LinkedHashMap<>();
    /** World name to the worlds it inherits from. */
    final Map<String, List<String>> worldInheritance = new LinkedHashMap<>();

    Entity group(String name) {
        return groups.computeIfAbsent(name, ignored -> new Entity());
    }

    Entity user(String identifier) {
        return users.computeIfAbsent(identifier, ignored -> new Entity());
    }

    public int groupCount() {
        return groups.size();
    }

    public int userCount() {
        return users.size();
    }

    /** A PermissionsEx group or user. */
    static final class Entity {
        /** Permissions per world, in PermissionsEx order; {@code -node} negates. */
        final Map<String, List<String>> permissions = new LinkedHashMap<>();
        final Map<String, Map<String, String>> options = new LinkedHashMap<>();
        /** Parent groups (for a group) or group memberships (for a user), per world. */
        final Map<String, List<String>> parents = new LinkedHashMap<>();
        boolean isDefault;

        void permission(String world, String permission) {
            permissions.computeIfAbsent(world, ignored -> new ArrayList<>()).add(permission);
        }

        void option(String world, String key, String value) {
            options.computeIfAbsent(world, ignored -> new LinkedHashMap<>()).put(key, value);
        }

        void parent(String world, String group) {
            var list = parents.computeIfAbsent(world, ignored -> new ArrayList<>());

            if (!list.contains(group)) {
                list.add(group);
            }
        }

        String option(String key) {
            return options.getOrDefault(GLOBAL, new LinkedHashMap<>()).get(key);
        }
    }
}
