package dev.rono.permissions.core.backends.file;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Nested-map helpers aligned with PermissionsEx YAML layout (parity with legacy FileBackend paths).
 */
public final class YamlMaps {
    public static final String USERS = "users";
    public static final String GROUPS = "groups";
    public static final String WORLDS = "worlds";
    public static final String PERMISSIONS = "permissions";
    public static final String OPTIONS = "options";
    public static final String SCHEMA_VERSION = "schema-version";

    public static final String USER_PARENT_LIST = "group";
    public static final String GROUP_PARENT_LIST = "inheritance";

    /** Legacy top-level world inheritance block (still written for exports / Spigot compatibility). */
    public static final String WORLD_INHERITANCE_LEGACY = "world-inheritance";

    private YamlMaps() {}

    @SuppressWarnings("unchecked")
    public static Map<String, Object> requireSection(Map<String, Object> parent, String key) {
        Object cur = parent.get(key);
        if (cur instanceof Map) {
            return (Map<String, Object>) cur;
        }
        LinkedHashMap<String, Object> sec = new LinkedHashMap<>();
        parent.put(key, sec);
        return sec;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> getSection(Map<String, Object> parent, String key) {
        Object cur = parent.get(key);
        if (cur instanceof Map) {
            return (Map<String, Object>) cur;
        }
        return null;
    }

    public static Map<String, Object> worldBucket(Map<String, Object> entityRoot, String worldName) {
        Map<String, Object> worldsSec = requireSection(entityRoot, WORLDS);
        return requireSection(worldsSec, worldName);
    }

    /** Path for users: {@code worlds/<w>/permissions}; common: {@code permissions}. */
    public static Map<String, Object> bucketForPermissions(Map<String, Object> entityRoot, String worldName) {
        if (worldName == null || worldName.isEmpty()) {
            return entityRoot;
        }
        return worldBucket(entityRoot, worldName);
    }

    public static List<String> getStringList(Map<String, Object> bucket, String listKey) {
        Object raw = bucket.get(listKey);
        return coerceToStringList(raw);
    }

    public static void putStringList(Map<String, Object> bucket, String listKey, List<String> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            bucket.remove(listKey);
        } else {
            bucket.put(listKey, new ArrayList<>(permissions));
        }
    }

    public static List<String> coerceToStringList(Object raw) {
        if (raw == null) {
            return List.of();
        }
        if (raw instanceof List<?> list) {
            List<String> out = new ArrayList<>(list.size());
            for (Object o : list) {
                if (o != null) {
                    out.add(String.valueOf(o));
                }
            }
            return List.copyOf(out);
        }
        return List.of();
    }

    public static String getNestedString(Map<String, Object> entityRoot, String worldName, String optionsKey,
            String optName) {
        Map<String, Object> opts = optionsMap(entityRoot, worldName);
        Object v = opts.get(optName);
        return v == null ? null : String.valueOf(v);
    }

    /**
     * Returns the {@code options} map for global or world bucket (mutable).
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> optionsMap(Map<String, Object> entityRoot, String worldName) {
        Map<String, Object> bucket = bucketForPermissions(entityRoot, worldName);
        Object cur = bucket.get(OPTIONS);
        if (!(cur instanceof Map)) {
            LinkedHashMap<String, Object> m = new LinkedHashMap<>();
            bucket.put(OPTIONS, m);
            return m;
        }
        return (Map<String, Object>) cur;
    }

    public static void setNestedOption(Map<String, Object> entityRoot, String worldName, String optionsKeyUnused,
            String optName, String value) {
        Map<String, Object> bucket = bucketForPermissions(entityRoot, worldName);
        Map<String, Object> opts = requireSection(bucket, OPTIONS);
        if (value == null) {
            opts.remove(optName);
            if (opts.isEmpty()) {
                bucket.remove(OPTIONS);
            }
            pruneWorldIfEmpty(entityRoot, worldName);
        } else {
            opts.put(optName, value);
        }
    }

    public static Map<String, String> collectLeafOptions(Map<String, Object> optionsSection) {
        Map<String, String> out = new LinkedHashMap<>();
        if (optionsSection == null) {
            return out;
        }
        collectLeafOptionsRecursive(optionsSection, "", out);
        return out;
    }

    @SuppressWarnings("unchecked")
    private static void collectLeafOptionsRecursive(Map<String, Object> section, String prefix,
            Map<String, String> out) {
        for (Map.Entry<String, Object> e : section.entrySet()) {
            String k = e.getKey();
            Object v = e.getValue();
            String path = prefix.isEmpty() ? k : prefix + '.' + k;
            if (v instanceof Map<?, ?> nested) {
                collectLeafOptionsRecursive((Map<String, Object>) nested, path, out);
            } else if (v != null) {
                out.put(path, String.valueOf(v));
            }
        }
    }

    public static void pruneWorldIfEmpty(Map<String, Object> entityRoot, String worldName) {
        if (worldName == null || worldName.isEmpty()) {
            return;
        }
        Map<String, Object> worldsSec = getSection(entityRoot, WORLDS);
        if (worldsSec == null) {
            return;
        }
        Object wObj = worldsSec.get(worldName);
        if (!(wObj instanceof Map)) {
            worldsSec.remove(worldName);
            if (worldsSec.isEmpty()) {
                entityRoot.remove(WORLDS);
            }
            return;
        }
        Map<String, Object> worldNode = (Map<String, Object>) wObj;
        if (worldNode.isEmpty()) {
            worldsSec.remove(worldName);
        }
        if (worldsSec.isEmpty()) {
            entityRoot.remove(WORLDS);
        }
    }
}
