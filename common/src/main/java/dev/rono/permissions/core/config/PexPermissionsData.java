package dev.rono.permissions.core.config;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * {@code permissions.yml} store root — {@link #fromRoot} / {@link #toRootYaml}. For {@code config.yml}'s {@code permissions:}
 * block use {@link PexConfigData#fromPermissionsMap} / {@link PexConfigData#toPermissionsMap}.
 */
public record PexPermissionsData(
        Map<String, Object> groups,
        Map<String, Object> users,
        Map<String, Object> worldInheritance,
        int schemaVersion)
        implements Serializable {

    public static final String DEFAULT_STORE_FILE = "permissions.yml";

    public static final String KEY_GROUPS = "groups";
    public static final String KEY_USERS = "users";
    public static final String KEY_WORLD_INHERITANCE = "world-inheritance";
    public static final String KEY_SCHEMA_VERSION = "schema-version";

    public PexPermissionsData {
        groups = shallowCopy(groups);
        users = shallowCopy(users);
        worldInheritance = shallowCopy(worldInheritance);
    }

    /** Matches a minimal empty PermissionsEx YAML document ({@code schema-version: 1}, empty sections). */
    public static PexPermissionsData vanillaEmpty() {
        return new PexPermissionsData(Map.of(), Map.of(), Map.of(), 1);
    }

    /** Read from a SnakeYAML root map ({@code load(...)} result). Ignores unrelated top-level keys. */
    public static PexPermissionsData fromRoot(Map<String, Object> root) {
        if (root == null) {
            return vanillaEmpty();
        }
        Map<String, Object> g = asMutableMap(root.get(KEY_GROUPS));
        Map<String, Object> u = asMutableMap(root.get(KEY_USERS));
        Map<String, Object> wi = asMutableMap(root.get(KEY_WORLD_INHERITANCE));
        int ver = coerceInt(root.get(KEY_SCHEMA_VERSION), 1);
        return new PexPermissionsData(g, u, wi, ver);
    }

    /** Build a YAML root suitable for SnakeYAML/file backends. */
    public Map<String, Object> toRootYaml() {
        LinkedHashMap<String, Object> root = new LinkedHashMap<>();
        root.put(KEY_GROUPS, new LinkedHashMap<>(groups));
        root.put(KEY_USERS, new LinkedHashMap<>(users));
        root.put(KEY_WORLD_INHERITANCE, new LinkedHashMap<>(worldInheritance));
        root.put(KEY_SCHEMA_VERSION, schemaVersion);
        return root;
    }

    private static Map<String, Object> shallowCopy(Map<String, Object> section) {
        return Collections.unmodifiableMap(new LinkedHashMap<>(section));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asMutableMap(Object o) {
        if (o instanceof Map<?, ?> mp) {
            return new LinkedHashMap<>((Map<String, Object>) mp);
        }
        return new LinkedHashMap<>();
    }

    private static int coerceInt(Object o, int def) {
        if (o instanceof Number n) {
            return n.intValue();
        }
        if (o == null) {
            return def;
        }
        try {
            return Integer.parseInt(String.valueOf(o));
        } catch (NumberFormatException ignored) {
            return def;
        }
    }
}
