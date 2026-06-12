package dev.rono.permissions.core.backends.file;

import ru.tehkode.permissions.PermissionsGroupData;
import ru.tehkode.permissions.PermissionsUserData;

import java.util.*;

/**
 * PexUser/group subtree stored in YAML-compatible nested maps (Spigot FileBackend-compatible layout).
 */
public final class YamlEntityData implements PermissionsGroupData, PermissionsUserData {

    private final YamlFileBackend backend;
    private final Map<String, Object> entitiesRoot;

    /** Key under {@link #entitiesRoot}; user keys match {@link YamlFileBackend#userKeysLowercase()}. */
    private String storageKey;

    /** Live backing map for this entity. */
    private final Map<String, Object> node;
    /** True until this node's map is inserted into {@link #entitiesRoot} (parity with legacy FileData). */
    private boolean virtual;

    /** PexUser entries use {@code group}; groups use {@code inheritance}. */
    private final String parentListKey;

    private YamlEntityData(YamlFileBackend backend, Map<String, Object> entitiesRoot, String lookupName,
            String parentListKey, boolean lowercaseStorageKey) {
        this.backend = backend;
        this.entitiesRoot = entitiesRoot;
        this.parentListKey = parentListKey;
        Objects.requireNonNull(lookupName, "lookupName");

        LocateResult lr = locate(entitiesRoot, lookupName, lowercaseStorageKey);
        this.storageKey = lr.storageKey();
        this.node = lr.node();
        this.virtual = lr.needsInsert();
    }

    public static YamlEntityData forUser(YamlFileBackend backend, Map<String, Object> entitiesRoot,
            String userName) {
        return new YamlEntityData(backend, entitiesRoot, userName, YamlMaps.USER_PARENT_LIST,
                backend.userKeysLowercase());
    }

    public static YamlEntityData forGroup(YamlFileBackend backend, Map<String, Object> entitiesRoot,
            String groupName) {
        return new YamlEntityData(backend, entitiesRoot, groupName, YamlMaps.GROUP_PARENT_LIST, false);
    }

    private record LocateResult(Map<String, Object> node, String storageKey, boolean needsInsert) {}

    private static LocateResult locate(Map<String, Object> entitiesRoot, String name, boolean lowercaseStorageKey) {
        if (lowercaseStorageKey) {
            String key = name.toLowerCase(Locale.ROOT);
            Object raw = entitiesRoot.get(key);
            if (raw instanceof Map<?, ?> mp) {
                return new LocateResult(castMap(mp), key, false);
            }
            return new LocateResult(new LinkedHashMap<>(), key, true);
        }

        Object direct = entitiesRoot.get(name);
        if (direct instanceof Map<?, ?> mp) {
            return new LocateResult(castMap(mp), name, false);
        }

        for (Map.Entry<String, Object> e : entitiesRoot.entrySet()) {
            if (e.getKey().equalsIgnoreCase(name) && e.getValue() instanceof Map<?, ?> mp) {
                return new LocateResult(castMap(mp), e.getKey(), false);
            }
        }

        return new LocateResult(new LinkedHashMap<>(), name, true);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> castMap(Map<?, ?> mp) {
        return (Map<String, Object>) mp;
    }

    void flushVirtualCommitIfNeeded() {
        if (!virtual) {
            return;
        }
        entitiesRoot.put(storageKey, node);
        virtual = false;
    }

    @Override
    public void load() {}

    @Override
    public String getIdentifier() {
        return storageKey;
    }

    @Override
    public boolean setIdentifier(String identifier) {
        if (!(parentListKey.equals(YamlMaps.USER_PARENT_LIST))) {
            return false;
        }
        Objects.requireNonNull(identifier, "identifier");
        synchronized (backend.getLock()) {
            String newStored = backend.userKeysLowercase() ? identifier.toLowerCase(Locale.ROOT) : identifier;
            if (entitiesRoot.containsKey(newStored) && !newStored.equals(storageKey)) {
                return false;
            }
            flushVirtualCommitIfNeeded();
            if (entitiesRoot.containsKey(storageKey)) {
                entitiesRoot.remove(storageKey);
            }
            entitiesRoot.entrySet().removeIf(e -> e.getKey().equalsIgnoreCase(storageKey));

            entitiesRoot.put(newStored, node);
            storageKey = newStored;
            virtual = false;
            backend.saveLocked();
            return true;
        }
    }

    @Override
    public List<String> getPermissions(String worldName) {
        synchronized (backend.getLock()) {
            Map<String, Object> bucket = YamlMaps.bucketForPermissions(node, worldName);
            return List.copyOf(YamlMaps.getStringList(bucket, YamlMaps.PERMISSIONS));
        }
    }

    @Override
    public void setPermissions(List<String> permissions, String worldName) {
        synchronized (backend.getLock()) {
            Map<String, Object> bucket = YamlMaps.bucketForPermissions(node, worldName);
            YamlMaps.putStringList(bucket, YamlMaps.PERMISSIONS, permissions);
            pruneEmptyWorldBucket(bucket, worldName);
            flushVirtualCommitIfNeeded();
            backend.saveLocked();
        }
    }

    /** If a per-world subtree has no usable content, strip it so YAML stays tidy. */
    private void pruneEmptyWorldBucket(Map<String, Object> bucket, String worldName) {
        if (worldName == null || worldName.isEmpty()) {
            return;
        }
        if (bucket.isEmpty()) {
            YamlMaps.pruneWorldIfEmpty(node, worldName);
        }
    }

    @Override
    public Map<String, List<String>> getPermissionsMap() {
        synchronized (backend.getLock()) {
            Map<String, List<String>> all = new HashMap<>();
            List<String> common = YamlMaps.getStringList(node, YamlMaps.PERMISSIONS);
            if (!common.isEmpty()) {
                all.put(null, List.copyOf(common));
            }
            Map<String, Object> worldsSec = YamlMaps.getSection(node, YamlMaps.WORLDS);
            if (worldsSec != null) {
                for (String world : worldsSec.keySet()) {
                    Map<String, Object> wbucket = YamlMaps.getSection(worldsSec, world);
                    if (wbucket != null) {
                        List<String> wp = YamlMaps.getStringList(wbucket, YamlMaps.PERMISSIONS);
                        if (!wp.isEmpty()) {
                            all.put(world, List.copyOf(wp));
                        }
                    }
                }
            }
            return Collections.unmodifiableMap(all);
        }
    }

    @Override
    public Set<String> getWorlds() {
        synchronized (backend.getLock()) {
            Set<String> worlds = new HashSet<>();
            for (Map.Entry<String, List<String>> e : getPermissionsMap().entrySet()) {
                if (e.getKey() != null && !e.getValue().isEmpty()) {
                    worlds.add(e.getKey());
                }
            }
            worlds.addAll(optionsWorlds());
            worlds.addAll(parentWorldsKeys());
            return Collections.unmodifiableSet(worlds);
        }
    }

    private Set<String> optionsWorlds() {
        Map<String, Object> worldsSec = YamlMaps.getSection(node, YamlMaps.WORLDS);
        if (worldsSec == null) {
            return Set.of();
        }
        Set<String> out = new HashSet<>();
        for (Map.Entry<String, Object> e : worldsSec.entrySet()) {
            if (!(e.getValue() instanceof Map)) {
                continue;
            }
            Map<String, Object> w = castMap((Map<?, ?>) e.getValue());
            Map<String, Object> opts = YamlMaps.getSection(w, YamlMaps.OPTIONS);
            if (opts != null && !opts.isEmpty()) {
                out.add(e.getKey());
            }
        }
        return out;
    }

    private Set<String> parentWorldsKeys() {
        Map<String, Object> worldsSec = YamlMaps.getSection(node, YamlMaps.WORLDS);
        if (worldsSec == null) {
            return Set.of();
        }
        Set<String> out = new HashSet<>();
        for (Map.Entry<String, Object> e : worldsSec.entrySet()) {
            if (!(e.getValue() instanceof Map)) {
                continue;
            }
            Map<String, Object> w = castMap((Map<?, ?>) e.getValue());
            if (!YamlMaps.getStringList(w, parentListKey).isEmpty()) {
                out.add(e.getKey());
            }
        }
        return out;
    }

    @Override
    public String getOption(String option, String worldName) {
        synchronized (backend.getLock()) {
            Map<String, Object> optsBucket = bucketOptionsSection(worldName);
            if (optsBucket == null) {
                return null;
            }
            Object v = deepGet(optsBucket, option.split("\\."));
            return v == null ? null : String.valueOf(v);
        }
    }

    @SuppressWarnings("unchecked")
    private static Object deepGet(Map<String, Object> map, String[] parts) {
        Object cur = map;
        for (String part : parts) {
            if (!(cur instanceof Map)) {
                return null;
            }
            cur = ((Map<String, Object>) cur).get(part);
        }
        return cur;
    }

    private Map<String, Object> bucketOptionsSection(String worldName) {
        Map<String, Object> bucket = YamlMaps.bucketForPermissions(node, worldName);
        return YamlMaps.getSection(bucket, YamlMaps.OPTIONS);
    }

    @Override
    public void setOption(String option, String value, String worldName) {
        synchronized (backend.getLock()) {
            if (option == null || option.isEmpty()) {
                return;
            }
            Map<String, Object> bucket = YamlMaps.bucketForPermissions(node, worldName);
            Map<String, Object> opts = YamlMaps.requireSection(bucket, YamlMaps.OPTIONS);
            applyDeepSet(opts, option.split("\\."), value == null ? null : value);
            if (value == null) {
                pruneEmptyNested(opts);
                if (opts.isEmpty()) {
                    bucket.remove(YamlMaps.OPTIONS);
                }
            }
            pruneEmptyWorldBucket(bucket, worldName);
            flushVirtualCommitIfNeeded();
            backend.saveLocked();
        }
    }

    @SuppressWarnings("unchecked")
    private static void applyDeepSet(Map<String, Object> map, String[] parts, String value) {
        if (parts.length == 1) {
            if (value == null) {
                map.remove(parts[0]);
            } else {
                map.put(parts[0], value);
            }
            return;
        }
        Map<String, Object> next = requireSub(map, parts[0]);
        applyDeepSet(next, Arrays.copyOfRange(parts, 1, parts.length), value);
        if (value == null && next.isEmpty()) {
            map.remove(parts[0]);
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> requireSub(Map<String, Object> map, String key) {
        Object cur = map.get(key);
        if (cur instanceof Map) {
            return (Map<String, Object>) cur;
        }
        LinkedHashMap<String, Object> nm = new LinkedHashMap<>();
        map.put(key, nm);
        return nm;
    }

    private static void pruneEmptyNested(Map<String, Object> map) {
        Iterator<Map.Entry<String, Object>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Object> e = it.next();
            if (e.getValue() instanceof Map<?, ?> sub) {
                pruneEmptyNested((Map<String, Object>) sub);
                if (((Map<?, ?>) e.getValue()).isEmpty()) {
                    it.remove();
                }
            } else if (e.getValue() == null) {
                it.remove();
            }
        }
    }

    @Override
    public Map<String, String> getOptions(String worldName) {
        synchronized (backend.getLock()) {
            Map<String, Object> opts = bucketOptionsSection(worldName);
            if (opts == null) {
                return Map.of();
            }
            return Collections.unmodifiableMap(YamlMaps.collectLeafOptions(opts));
        }
    }

    @Override
    public Map<String, Map<String, String>> getOptionsMap() {
        synchronized (backend.getLock()) {
            Map<String, Map<String, String>> out = new HashMap<>();
            out.put(null, getOptions(null));
            Set<String> worldNames =
                    Optional.ofNullable(YamlMaps.getSection(node, YamlMaps.WORLDS))
                            .map(Map::keySet)
                            .orElse(Collections.emptySet());
            for (String w : worldNames) {
                Map<String, String> o = getOptions(w);
                if (!o.isEmpty()) {
                    out.put(w, o);
                }
            }
            return Collections.unmodifiableMap(out);
        }
    }

    @Override
    public List<String> getParents(String worldName) {
        synchronized (backend.getLock()) {
            Map<String, Object> bucket = YamlMaps.bucketForPermissions(node, worldName);
            return List.copyOf(cleanParents(YamlMaps.getStringList(bucket, parentListKey)));
        }
    }

    private static List<String> cleanParents(List<String> raw) {
        List<String> out = new ArrayList<>();
        for (String test : raw) {
            if (test != null && !test.isEmpty()) {
                out.add(test);
            }
        }
        return out;
    }

    @Override
    public void setParents(List<String> parents, String worldName) {
        synchronized (backend.getLock()) {
            Map<String, Object> bucket = YamlMaps.bucketForPermissions(node, worldName);
            if (parents == null || parents.isEmpty()) {
                bucket.remove(parentListKey);
            } else {
                YamlMaps.putStringList(bucket, parentListKey, parents);
            }
            pruneEmptyWorldBucket(bucket, worldName);
            flushVirtualCommitIfNeeded();
            backend.saveLocked();
        }
    }

    @Override
    public boolean isVirtual() {
        synchronized (backend.getLock()) {
            return virtual;
        }
    }

    @Override
    public void save() {
        synchronized (backend.getLock()) {
            flushVirtualCommitIfNeeded();
            backend.saveLocked();
        }
    }

    @Override
    public void remove() {
        synchronized (backend.getLock()) {
            entitiesRoot.remove(storageKey);
            entitiesRoot.entrySet().removeIf(e -> e.getKey().equalsIgnoreCase(storageKey));
            node.clear();
            virtual = false;
            backend.saveLocked();
        }
    }

    @Override
    public Map<String, List<String>> getParentsMap() {
        synchronized (backend.getLock()) {
            Map<String, List<String>> ret = new HashMap<>();
            ret.put(null, getParents(null));
            for (String w : parentWorldsKeys()) {
                ret.put(w, getParents(w));
            }
            return Collections.unmodifiableMap(ret);
        }
    }
}
