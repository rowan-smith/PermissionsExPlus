package dev.rono.permissions.core.config;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * {@code permissions:} subtree of {@code config.yml}, shaped like {@link PexPermissionsData}: typed leaves plus map
 * sections and round-trip helpers.
 */
public record PexConfigData(
        boolean debug,
        boolean allowOps,
        boolean userAddGroupsLast,
        boolean logPlayers,
        boolean createUserRecords,
        boolean saveDefaultGroup,
        String backend,
        Map<String, Object> informPlayers,
        String basedir,
        Map<String, Map<String, Object>> backends,
        CommandFramework commandFramework)
        implements Serializable {

    public static final String KEY_DEBUG = "debug";
    public static final String KEY_ALLOW_OPS = "allowOps";
    public static final String KEY_USER_ADD_GROUPS_LAST = "user-add-groups-last";
    public static final String KEY_LOG_PLAYERS = "log-players";
    public static final String KEY_CREATE_USER_RECORDS = "createUserRecords";
    public static final String KEY_SAVE_DEFAULT_GROUP = "save-default-group";
    public static final String KEY_BACKEND = "backend";
    public static final String KEY_BASEDIR = "basedir";
    public static final String KEY_INFORMPLAYERS = "informplayers";
    public static final String KEY_INFORM_CHANGES = "changes";
    public static final String KEY_BACKENDS = "backends";
    public static final String FILE_BACKEND = "file";
    public static final String H2_BACKEND = "h2";
    public static final String KEY_BACKEND_TYPE = "type";
    /** Path leaf inside {@code backends.file}. */
    public static final String KEY_BACKEND_FILE_LEAF = "file";
    public static final String KEY_DATABASE = "database";
    public static final String KEY_MIGRATION_SOURCE = "migration-source";
    public static final String KEY_COMMAND_FRAMEWORK = CommandFramework.CONFIG_KEY;

    private static final String FALLBACK_BACKEND = H2_BACKEND;

    public PexConfigData {
        Objects.requireNonNull(informPlayers, "informPlayers");
        Objects.requireNonNull(backend, "backend");
        Objects.requireNonNull(basedir, "basedir");
        Objects.requireNonNull(backends, "backends");
        informPlayers = shallowCopyLeafMap(informPlayers);
        backends = freezeBackends(backends);
    }

    /** Matches {@link PexPermissionsData#fromRoot} — read SnackYAML-loaded {@code permissions:} subtree. */
    public static PexConfigData fromPermissionsMap(
            Map<String, Object> permissionRoot,
            Supplier<String> fallbackBasedir,
            PexConfigFlavor flavor) {
        Objects.requireNonNull(fallbackBasedir, "fallbackBasedir");
        Objects.requireNonNull(flavor, "flavor");
        Map<String, Object> src = Objects.requireNonNullElse(permissionRoot, Map.of());
        LinkedHashMap<String, Object> copy = deepCopyFlatKeys(src);
        applyYamlDefaults(copy, flavor);

        boolean debug = parseBoolean(copy.get(KEY_DEBUG), false);
        boolean allowOps = parseBoolean(copy.get(KEY_ALLOW_OPS), false);
        boolean userAddGroupsLast = parseBoolean(copy.get(KEY_USER_ADD_GROUPS_LAST), false);
        boolean logPlayers = parseBoolean(copy.get(KEY_LOG_PLAYERS), false);
        boolean createUserRecords =
                parseBoolean(copy.get(KEY_CREATE_USER_RECORDS), flavor.defaultCreateUserRecords());
        boolean saveDefaultGroup = parseBoolean(copy.get(KEY_SAVE_DEFAULT_GROUP), false);

        Map<String, Object> inform = asMutableNested(copy.get(KEY_INFORMPLAYERS));
        String bk = stringify(copy.get(KEY_BACKEND), flavor.defaultBackend());
        if (bk.isBlank()) {
            bk = flavor.defaultBackend();
        }
        LinkedHashMap<String, Map<String, Object>> backends =
                new LinkedHashMap<>(coerceBackendMap(copy.get(KEY_BACKENDS)));
        bk = normalizeActiveBackendAlias(bk, backends);

        String basedirLocal = stringify(copy.get(KEY_BASEDIR), "");
        if (basedirLocal.isBlank()) {
            basedirLocal = fallbackBasedir.get();
        }
        Objects.requireNonNull(basedirLocal, "basedir");

        CommandFramework commandFramework =
                CommandFramework.fromConfig(copy.get(KEY_COMMAND_FRAMEWORK));

        return new PexConfigData(
                debug,
                allowOps,
                userAddGroupsLast,
                logPlayers,
                createUserRecords,
                saveDefaultGroup,
                bk,
                inform,
                basedirLocal,
                backends,
                commandFramework);
    }

    /** Matches {@link PexPermissionsData#toRootYaml} — writable map suitable for SnakeYAML ({@code permissions:}). */
    public Map<String, Object> toPermissionsMap() {
        LinkedHashMap<String, Object> m = new LinkedHashMap<>();
        m.put(KEY_DEBUG, debug);
        m.put(KEY_ALLOW_OPS, allowOps);
        m.put(KEY_USER_ADD_GROUPS_LAST, userAddGroupsLast);
        m.put(KEY_LOG_PLAYERS, logPlayers);
        m.put(KEY_CREATE_USER_RECORDS, createUserRecords);
        m.put(KEY_SAVE_DEFAULT_GROUP, saveDefaultGroup);
        m.put(KEY_BACKEND, backend);
        m.put(KEY_INFORMPLAYERS, new LinkedHashMap<>(informPlayers));
        m.put(KEY_BASEDIR, basedir);
        LinkedHashMap<String, Object> be = new LinkedHashMap<>();
        for (Map.Entry<String, Map<String, Object>> e : backends.entrySet()) {
            be.put(e.getKey(), new LinkedHashMap<>(e.getValue()));
        }
        m.put(KEY_BACKENDS, be);
        m.put(KEY_COMMAND_FRAMEWORK, commandFramework.name().toLowerCase());
        return m;
    }

    public static PexConfigData testDefaults(String backendAlias, String basedir) {
        LinkedHashMap<String, Object> inf = new LinkedHashMap<>();
        inf.put(KEY_INFORM_CHANGES, false);
        LinkedHashMap<String, Map<String, Object>> b = new LinkedHashMap<>();
        LinkedHashMap<String, Object> one = new LinkedHashMap<>();
        one.put(KEY_BACKEND_TYPE, backendAlias);
        b.put(backendAlias, one);
        return new PexConfigData(
                false,
                false,
                false,
                false,
                false,
                false,
                backendAlias,
                inf,
                basedir,
                b,
                CommandFramework.MODERN);
    }

    public boolean informPlayerChanges() {
        return parseBoolean(informPlayers.get(KEY_INFORM_CHANGES), false);
    }

    public PexConfigData withBackend(String next) {
        String nb = next == null || next.isBlank() ? FALLBACK_BACKEND : next.strip();
        return new PexConfigData(
                debug,
                allowOps,
                userAddGroupsLast,
                logPlayers,
                createUserRecords,
                saveDefaultGroup,
                nb,
                informPlayers,
                basedir,
                backends,
                commandFramework);
    }

    public String storeRelative() {
        Map<String, Object> active = backends.get(backend);
        if (active != null) {
            String type = stringify(active.get(KEY_BACKEND_TYPE), backend);
            if (H2_BACKEND.equals(type)) {
                Object migration = active.get(KEY_MIGRATION_SOURCE);
                if (migration != null) {
                    String s = String.valueOf(migration).trim();
                    if (!s.isEmpty()) {
                        return s;
                    }
                }
                Object database = active.get(KEY_DATABASE);
                if (database != null) {
                    String db = String.valueOf(database).trim();
                    if (!db.isEmpty()) {
                        return db + ".mv.db";
                    }
                }
            }
            Object leaf = active.get(KEY_BACKEND_FILE_LEAF);
            if (leaf != null) {
                String s = String.valueOf(leaf).trim();
                if (!s.isEmpty()) {
                    return s;
                }
            }
        }
        Map<String, Object> fileBk = backends.get(FILE_BACKEND);
        if (fileBk == null) {
            return PexPermissionsData.DEFAULT_STORE_FILE;
        }
        Object leaf = fileBk.get(KEY_BACKEND_FILE_LEAF);
        if (leaf == null) {
            return PexPermissionsData.DEFAULT_STORE_FILE;
        }
        String s = String.valueOf(leaf).trim();
        return s.isEmpty() ? PexPermissionsData.DEFAULT_STORE_FILE : s;
    }

    public String storePathSlash() {
        String normBase = basedir.replace('\\', '/');
        while (normBase.endsWith("/")) {
            normBase = normBase.substring(0, normBase.length() - 1);
        }
        String leaf = storeRelative().replace('\\', '/');
        if (leaf.startsWith("/") || leaf.length() >= 2 && leaf.charAt(1) == ':') {
            return leaf.replace('\\', '/');
        }
        return normBase + "/" + leaf;
    }

    private static Map<String, Object> shallowCopyLeafMap(Map<String, Object> in) {
        return Collections.unmodifiableMap(new LinkedHashMap<>(in));
    }

    private static Map<String, Map<String, Object>> freezeBackends(Map<String, Map<String, Object>> raw) {
        LinkedHashMap<String, Map<String, Object>> shallow = new LinkedHashMap<>();
        for (Map.Entry<String, Map<String, Object>> e : raw.entrySet()) {
            shallow.put(e.getKey(), Collections.unmodifiableMap(new LinkedHashMap<>(e.getValue())));
        }
        return Collections.unmodifiableMap(shallow);
    }

    /**
     * Maps legacy active {@code backend: file} configs to {@code h2} while preserving the YAML path
     * for one-time import via {@code migration-source}.
     */
    private static String normalizeActiveBackendAlias(
            String backend, LinkedHashMap<String, Map<String, Object>> backends) {
        if (!FILE_BACKEND.equalsIgnoreCase(backend)) {
            return backend;
        }
        Map<String, Object> h2 = backends.computeIfAbsent(H2_BACKEND, ignored -> new LinkedHashMap<>());
        h2.putIfAbsent(KEY_BACKEND_TYPE, H2_BACKEND);
        h2.putIfAbsent(KEY_DATABASE, "permissions");
        Map<String, Object> fileSection = backends.get(FILE_BACKEND);
        if (fileSection != null) {
            Object yamlPath = fileSection.get(KEY_BACKEND_FILE_LEAF);
            if (yamlPath != null && !String.valueOf(yamlPath).isBlank()) {
                h2.putIfAbsent(KEY_MIGRATION_SOURCE, String.valueOf(yamlPath).trim());
            }
        }
        h2.putIfAbsent(KEY_MIGRATION_SOURCE, PexPermissionsData.DEFAULT_STORE_FILE);
        return H2_BACKEND;
    }

    private static void applyYamlDefaults(Map<String, Object> permissionsMap, PexConfigFlavor flavor) {
        Objects.requireNonNull(permissionsMap, "permissionsMap");
        putBooleanIfUnset(permissionsMap, KEY_DEBUG, false);
        putBooleanIfUnset(permissionsMap, KEY_ALLOW_OPS, false);
        putBooleanIfUnset(permissionsMap, KEY_USER_ADD_GROUPS_LAST, false);
        putBooleanIfUnset(permissionsMap, KEY_LOG_PLAYERS, false);
        putBooleanIfUnset(
                permissionsMap, KEY_CREATE_USER_RECORDS, flavor.defaultCreateUserRecords());
        putBooleanIfUnset(permissionsMap, KEY_SAVE_DEFAULT_GROUP, false);

        Map<String, Object> inform = getOrCreateNestedMap(permissionsMap, KEY_INFORMPLAYERS);
        putBooleanIfUnset(inform, KEY_INFORM_CHANGES, false);

        putIfUnset(permissionsMap, KEY_BACKEND, flavor.defaultBackend());
        putIfUnset(permissionsMap, KEY_COMMAND_FRAMEWORK, CommandFramework.MODERN.name().toLowerCase());
    }

    @SuppressWarnings("unchecked")
    private static LinkedHashMap<String, Object> deepCopyFlatKeys(Map<String, Object> src) {
        LinkedHashMap<String, Object> out = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : src.entrySet()) {
            String k = e.getKey();
            if (KEY_INFORMPLAYERS.equals(k) && e.getValue() instanceof Map<?, ?> nm) {
                out.put(k, new LinkedHashMap<>((Map<String, Object>) nm));
            } else if (KEY_BACKENDS.equals(k) && e.getValue() instanceof Map<?, ?> bm) {
                out.put(k, shallowCopyBackendsInner((Map<String, Object>) bm));
            } else {
                out.put(k, e.getValue());
            }
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> shallowCopyBackendsInner(Map<String, Object> backends) {
        LinkedHashMap<String, Object> out = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : backends.entrySet()) {
            Object v = e.getValue();
            if (v instanceof Map<?, ?> m) {
                out.put(e.getKey(), new LinkedHashMap<>((Map<String, Object>) m));
            } else {
                out.put(e.getKey(), v);
            }
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    private static LinkedHashMap<String, Map<String, Object>> coerceBackendMap(Object raw) {
        LinkedHashMap<String, Map<String, Object>> out = new LinkedHashMap<>();
        if (!(raw instanceof Map<?, ?> bm)) {
            return out;
        }
        for (Map.Entry<?, ?> e : bm.entrySet()) {
            Object v = e.getValue();
            if (v instanceof Map<?, ?> sec) {
                out.put(String.valueOf(e.getKey()), new LinkedHashMap<>((Map<String, Object>) sec));
            }
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asMutableNested(Object o) {
        if (o instanceof Map<?, ?> nm) {
            return new LinkedHashMap<>((Map<String, Object>) nm);
        }
        return new LinkedHashMap<>();
    }

    private static void putBooleanIfUnset(Map<String, Object> map, String key, boolean val) {
        if (!map.containsKey(key)) {
            map.put(key, val);
        }
    }

    private static void putIfUnset(Map<String, Object> map, String key, String val) {
        if (!map.containsKey(key)) {
            map.put(key, val);
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> getOrCreateNestedMap(Map<String, Object> parent, String key) {
        Object cur = parent.get(key);
        if (cur instanceof Map<?, ?> nm) {
            return (Map<String, Object>) nm;
        }
        LinkedHashMap<String, Object> created = new LinkedHashMap<>();
        parent.put(key, created);
        return created;
    }

    private static boolean parseBoolean(Object val, boolean def) {
        if (val == null) {
            return def;
        }
        if (val instanceof Boolean bool) {
            return bool;
        }
        return Boolean.parseBoolean(String.valueOf(val));
    }

    private static String stringify(Object val, String def) {
        if (val == null) {
            return def == null ? "" : def;
        }
        return String.valueOf(val);
    }
}
