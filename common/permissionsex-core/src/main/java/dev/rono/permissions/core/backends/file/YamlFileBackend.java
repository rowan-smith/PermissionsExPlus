package dev.rono.permissions.core.backends.file;

import dev.rono.permissions.core.InternalPermissionManager;
import dev.rono.permissions.core.backends.AbstractPermissionBackend;
import dev.rono.permissions.core.backends.SchemaUpdate;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import ru.tehkode.permissions.PEXBackendConfiguration;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.PermissionsGroupData;
import ru.tehkode.permissions.PermissionsUserData;
import ru.tehkode.permissions.backends.PermissionBackend;
import ru.tehkode.permissions.exceptions.PermissionBackendException;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * YAML-backed permissions store compatible with PermissionsEx classic {@code permissions.yml}.
 */
public class YamlFileBackend extends AbstractPermissionBackend {

    private final Yaml yaml;
    private final Object lock = new Object();
    private volatile boolean suppressSave;
    /** Top-level YAML document tree. */
    private Map<String, Object> yamlRoot;
    private File permissionsFile;
    private final Map<String, List<String>> worldInheritanceCache = new ConcurrentHashMap<>();

    public YamlFileBackend(PermissionManager manager, PEXBackendConfiguration config)
            throws PermissionBackendException {
        super(manager, config);
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setIndent(2);
        this.yaml = new Yaml(options);
        String permissionFilename = getConfig().getString("file");
        if (permissionFilename == null) {
            permissionFilename = "permissions.yml";
            getConfig().set("file", permissionFilename);
        }
        File baseDirectory = sanitizeBaseDirectory();
        permissionsFile = new File(baseDirectory, permissionFilename);
        addSchemaUpdate(new SchemaUpdate(1) {
            @Override
            public void performUpdate() {
                Map<String, Object> users = YamlMaps.getSection(yamlRoot, YamlMaps.USERS);
                if (users != null) {
                    for (Object value : users.values()) {
                        if (value instanceof Map<?, ?> section) {
                            migrateLegacyEntityOptions(castStringObjectMap(section));
                        }
                    }
                }
                Map<String, Object> groups = YamlMaps.getSection(yamlRoot, YamlMaps.GROUPS);
                if (groups != null) {
                    for (Object value : groups.values()) {
                        if (value instanceof Map<?, ?> section) {
                            migrateLegacyEntityOptions(castStringObjectMap(section));
                        }
                    }
                }
            }

            private void migrateLegacyEntityOptions(Map<String, Object> section) {
                migrateLegacyWorldOptions(section);
                Map<String, Object> worldSection = YamlMaps.getSection(section, YamlMaps.WORLDS);
                if (worldSection != null) {
                    for (Object worldValue : worldSection.values()) {
                        if (worldValue instanceof Map<?, ?> worldNode) {
                            migrateLegacyWorldOptions(castStringObjectMap(worldNode));
                        }
                    }
                }
            }

            private void migrateLegacyWorldOptions(Map<String, Object> section) {
                migrateLegacyField(section, "prefix");
                migrateLegacyField(section, "suffix");
                migrateLegacyField(section, "default");
            }

            private void migrateLegacyField(Map<String, Object> section, String field) {
                if (!section.containsKey(field)) {
                    return;
                }
                Object value = section.remove(field);
                Map<String, Object> opts = YamlMaps.requireSection(section, YamlMaps.OPTIONS);
                opts.put(field, value);
            }
        });
        reload();
        performSchemaUpdate();
    }

    private File sanitizeBaseDirectory() throws PermissionBackendException {
        try {
            String baseDir =
                    Objects.requireNonNull(InternalPermissionManager.require(getManager()).getBasedir(), "basedir");
            if (baseDir.contains("\\") && !File.separator.equals("\\")) {
                baseDir = baseDir.replace("\\", File.separator);
            }
            File bd = new File(baseDir);
            if (!bd.exists() && !bd.mkdirs()) {
                throw new PermissionBackendException("Cannot create PermissionsEx base directory " + bd);
            }
            return bd;
        } catch (RuntimeException e) {
            throw new PermissionBackendException("Invalid basedir configuration", e);
        }
    }

    /** Used by YAML entity lookups (users are keyed lowercase like classic file backend). */
    protected boolean userKeysLowercase() {
        return true;
    }

    protected Object getLock() {
        return lock;
    }

    @Override
    public int getSchemaVersion() {
        synchronized (lock) {
            Object v = yamlRoot.get(YamlMaps.SCHEMA_VERSION);
            if (v instanceof Number num) {
                return num.intValue();
            }
            if (v != null) {
                try {
                    return Integer.parseInt(String.valueOf(v));
                } catch (NumberFormatException ignored) {
                    return -1;
                }
            }
            return -1;
        }
    }

    @Override
    protected void setSchemaVersion(int version) {
        synchronized (lock) {
            yamlRoot.put(YamlMaps.SCHEMA_VERSION, version);
            persistIfNeeded();
        }
    }

    @Override
    public void reload() throws PermissionBackendException {
        synchronized (lock) {
            yamlRoot = new LinkedHashMap<>();
            worldInheritanceCache.clear();

            try {
                ensureParentDirectory();
                if (permissionsFile.exists() && Files.isReadable(permissionsFile.toPath())) {
                    boolean mutated = false;
                    try (Reader reader = Files.newBufferedReader(
                            permissionsFile.toPath(), StandardCharsets.UTF_8)) {
                        Object loaded = yaml.load(reader);
                        if (loaded instanceof Map<?, ?> map) {
                            yamlRoot.putAll(castStringObjectMap(map));
                            PexYamlValidator.validateRoot(yamlRoot);
                        } else {
                            mutated = true;
                        }
                    }
                    mutated |= ensureDefaultGroupsAndSchemaPresent();
                    hydrateWorldInheritance();
                    if (mutated) {
                        persistIfNeeded();
                    }
                    getManager().getLogger()
                            .info("Permissions file successfully reloaded: " + permissionsFile.getPath());
                    return;
                }

                initializeMissingPermissionsFileTree();
                persistIfNeeded();
            } catch (IOException e) {
                throw new PermissionBackendException("Error loading permissions file!", e);
            }
        }
    }

    /** @return {@code true} if the YAML root was mutated and should be written out */
    private boolean ensureDefaultGroupsAndSchemaPresent() {
        boolean mutated = false;
        Map<String, Object> groups = YamlMaps.getSection(yamlRoot, YamlMaps.GROUPS);
        if (groups == null || groups.isEmpty()) {
            yamlRoot.put(YamlMaps.GROUPS, newDefaultGroupsTree());
            mutated = true;
        }
        if (!yamlRoot.containsKey(YamlMaps.SCHEMA_VERSION)) {
            yamlRoot.put(YamlMaps.SCHEMA_VERSION, Math.max(getLatestSchemaVersion(), 1));
            mutated = true;
        }
        return mutated;
    }

    /** Default group scaffold when no permissions file existed yet */
    private void initializeMissingPermissionsFileTree() throws IOException {
        if (!permissionsFile.exists()) {
            ensureParentDirectory();
            if (!permissionsFile.createNewFile() && !permissionsFile.exists()) {
                throw new IOException("Could not create " + permissionsFile);
            }
        }
        yamlRoot.put(YamlMaps.GROUPS, newDefaultGroupsTree());
        yamlRoot.put(YamlMaps.SCHEMA_VERSION, Math.max(getLatestSchemaVersion(), 1));
    }

    private static Map<String, Object> newDefaultGroupsTree() {
        Map<String, Object> groups = new LinkedHashMap<>();
        Map<String, Object> defGrp = new LinkedHashMap<>();

        Map<String, Object> defOpts = new LinkedHashMap<>();
        defOpts.put("default", true);
        defGrp.put(YamlMaps.OPTIONS, defOpts);
        List<String> defaultPermissions = new ArrayList<>();
        defaultPermissions.add("modifyworld.*");
        defGrp.put(YamlMaps.PERMISSIONS, defaultPermissions);

        groups.put("default", defGrp);
        return groups;
    }

    private void ensureParentDirectory() throws IOException {
        File parentFile = permissionsFile.getParentFile();
        if (parentFile != null && !parentFile.exists()) {
            parentFile.mkdirs();
        }
    }

    @SuppressWarnings("unchecked")
    private void hydrateWorldInheritance() {
        worldInheritanceCache.clear();

        Map<String, Object> legacy =
                YamlMaps.getSection(yamlRoot, YamlMaps.WORLD_INHERITANCE_LEGACY);
        if (legacy != null) {
            mergeLegacyWorldInheritance(legacy);
        }

        Map<String, Object> topWorlds = YamlMaps.getSection(yamlRoot, YamlMaps.WORLDS);
        if (topWorlds != null) {
            mergeTopWorldsInheritance(topWorlds);
        }
    }

    private void mergeLegacyWorldInheritance(Map<String, Object> legacy) {
        for (Map.Entry<String, Object> e : legacy.entrySet()) {
            putWorldInheritanceHydrate(e.getKey(), YamlMaps.coerceToStringList(e.getValue()));
        }
    }

    @SuppressWarnings("unchecked")
    private void mergeTopWorldsInheritance(Map<String, Object> topWorlds) {
        for (Map.Entry<String, Object> e : topWorlds.entrySet()) {
            if (!(e.getValue() instanceof Map)) {
                continue;
            }
            Map<String, Object> wSec = castStringObjectMap((Map<?, ?>) e.getValue());
            Object inh = wSec.get("inheritance");
            putWorldInheritanceHydrate(e.getKey(), YamlMaps.coerceToStringList(inh));
        }
    }

    private void putWorldInheritanceHydrate(String world, List<String> list) {
        if (world == null || world.isEmpty()) {
            return;
        }
        if (list.isEmpty()) {
            worldInheritanceCache.remove(world);
        } else {
            worldInheritanceCache.put(world, Collections.unmodifiableList(new ArrayList<>(list)));
        }
    }

    /**
     * Writes the flat {@code world-inheritance} block from {@link #worldInheritanceCache} (same idea as
     * legacy Spigot FileBackend). Nested {@code worlds/&lt;name&gt;/inheritance} is updated in
     * {@link #setWorldInheritance}.
     */
    private void persistLegacyWorldInheritanceBlock() {
        if (worldInheritanceCache.isEmpty()) {
            yamlRoot.remove(YamlMaps.WORLD_INHERITANCE_LEGACY);
            return;
        }
        Map<String, Object> wi =
                YamlMaps.requireSection(yamlRoot, YamlMaps.WORLD_INHERITANCE_LEGACY);
        wi.clear();
        for (Map.Entry<String, List<String>> e : worldInheritanceCache.entrySet()) {
            if (!e.getValue().isEmpty()) {
                wi.put(e.getKey(), new ArrayList<>(e.getValue()));
            }
        }
        if (wi.isEmpty()) {
            yamlRoot.remove(YamlMaps.WORLD_INHERITANCE_LEGACY);
        }
    }

    private void persistIfNeeded() {
        if (suppressSave) {
            return;
        }
        try {
            ensureParentDirectory();
            persistLegacyWorldInheritanceBlock();
            try (Writer w = Files.newBufferedWriter(permissionsFile.toPath(), StandardCharsets.UTF_8)) {
                yaml.dump(yamlRoot, w);
            }
        } catch (IOException ex) {
            getManager().getLogger().severe("Error while saving permissions file: " + ex.getMessage());
        }
    }

    protected void saveLocked() {
        synchronized (lock) {
            persistIfNeeded();
        }
    }

    @Override
    public PermissionsUserData getUserData(String userName) {
        synchronized (lock) {
            Map<String, Object> users = YamlMaps.requireSection(yamlRoot, YamlMaps.USERS);
            var data = YamlEntityData.forUser(this, users, userName);
            data.load();
            return data;
        }
    }

    @Override
    public PermissionsGroupData getGroupData(String groupName) {
        synchronized (lock) {
            Map<String, Object> groups =
                    YamlMaps.requireSection(yamlRoot, YamlMaps.GROUPS);
            var data = YamlEntityData.forGroup(this, groups, groupName);
            data.load();
            return data;
        }
    }

    @Override
    public boolean hasUser(String userName) {
        synchronized (lock) {
            Map<String, Object> users =
                    YamlMaps.getSection(yamlRoot, YamlMaps.USERS);
            return users != null && users.containsKey(userName.toLowerCase(Locale.ROOT));
        }
    }

    @Override
    public boolean hasGroup(String group) {
        synchronized (lock) {
            Map<String, Object> groups =
                    YamlMaps.getSection(yamlRoot, YamlMaps.GROUPS);
            if (groups == null) {
                return false;
            }
            if (groups.containsKey(group)) {
                return true;
            }
            for (String k : groups.keySet()) {
                if (k.equalsIgnoreCase(group)) {
                    return true;
                }
            }
            return false;
        }
    }

    @Override
    public Collection<String> getUserIdentifiers() {
        synchronized (lock) {
            Map<String, Object> users =
                    YamlMaps.getSection(yamlRoot, YamlMaps.USERS);
            return users == null ? List.of() : List.copyOf(users.keySet());
        }
    }

    @Override
    public Collection<String> getUserNames() {
        synchronized (lock) {
            Map<String, Object> users =
                    YamlMaps.getSection(yamlRoot, YamlMaps.USERS);
            if (users == null) {
                return List.of();
            }
            Collection<String> out = new ArrayList<>();
            for (Object v : users.values()) {
                if (!(v instanceof Map)) {
                    continue;
                }
                Map<String, Object> uSec = castStringObjectMap((Map<?, ?>) v);
                String name = nestedOptionLeaf(uSec, null, "name");
                if (name != null) {
                    out.add(name);
                }
            }
            return Collections.unmodifiableCollection(out);
        }
    }

    private static String nestedOptionLeaf(Map<String, Object> entityRoot, String world,
            String optionKey) {
        Map<String, Object> bucket =
                world == null || world.isEmpty() ? entityRoot
                        : YamlMaps.worldBucket(entityRoot, world);
        Map<String, Object> opts = YamlMaps.getSection(bucket, YamlMaps.OPTIONS);
        if (opts != null && opts.containsKey(optionKey)) {
            Object leaf = opts.get(optionKey);
            return leaf != null ? String.valueOf(leaf) : null;
        }
        return null;
    }

    @Override
    public Collection<String> getGroupNames() {
        synchronized (lock) {
            Map<String, Object> groups =
                    YamlMaps.getSection(yamlRoot, YamlMaps.GROUPS);
            return groups == null ? List.of() : List.copyOf(groups.keySet());
        }
    }

    @Override
    public List<String> getWorldInheritance(String world) {
        if (world == null || world.isEmpty()) {
            return List.of();
        }
        List<String> fromCache = worldInheritanceCache.get(world);
        if (fromCache != null) {
            return fromCache;
        }
        synchronized (lock) {
            List<String> fromYaml = readWorldInheritanceFromYamlUnsynced(world);
            if (!fromYaml.isEmpty()) {
                worldInheritanceCache.put(world, List.copyOf(fromYaml));
            }
            return fromYaml;
        }
    }

    private List<String> readWorldInheritanceFromYamlUnsynced(String world) {
        Map<String, Object> topWorlds = YamlMaps.getSection(yamlRoot, YamlMaps.WORLDS);
        if (topWorlds == null) {
            return List.of();
        }
        Object wObj = topWorlds.get(world);
        if (!(wObj instanceof Map)) {
            return List.of();
        }
        Map<String, Object> wSec = castStringObjectMap((Map<?, ?>) wObj);
        return List.copyOf(YamlMaps.coerceToStringList(wSec.get("inheritance")));
    }

    @Override
    public Map<String, List<String>> getAllWorldInheritance() {
        synchronized (lock) {
            Map<String, List<String>> merged = new LinkedHashMap<>();
            for (String w : yamlWorldKeys()) {
                merged.put(w, getWorldInheritance(w));
            }
            for (String w : worldInheritanceCache.keySet()) {
                merged.putIfAbsent(w, worldInheritanceCache.get(w));
            }
            return Collections.unmodifiableMap(merged);
        }
    }

    private Collection<String> yamlWorldKeys() {
        Map<String, Object> legacy =
                YamlMaps.getSection(yamlRoot, YamlMaps.WORLD_INHERITANCE_LEGACY);
        Map<String, Object> topWorlds = YamlMaps.getSection(yamlRoot, YamlMaps.WORLDS);

        LinkedHashMap<String, Object> keys = new LinkedHashMap<>();
        if (legacy != null) {
            keys.putAll(legacy);
        }
        if (topWorlds != null) {
            for (String k : topWorlds.keySet()) {
                keys.put(k, "");
            }
        }
        return keys.keySet();
    }

    @Override
    public void setWorldInheritance(String world, List<String> rawParentWorlds) {
        if (world == null || world.isEmpty()) {
            return;
        }
        synchronized (lock) {
            final List<String> parentWorlds = new ArrayList<>(
                    rawParentWorlds == null ? Collections.emptyList() : rawParentWorlds);

            if (parentWorlds.isEmpty()) {
                worldInheritanceCache.remove(world);

                Map<String, Object> tw = YamlMaps.getSection(yamlRoot, YamlMaps.WORLDS);
                if (tw != null) {
                    tw.remove(world);
                    if (tw.isEmpty()) {
                        yamlRoot.remove(YamlMaps.WORLDS);
                    }
                }

                Map<String, Object> legacyWi =
                        YamlMaps.getSection(yamlRoot, YamlMaps.WORLD_INHERITANCE_LEGACY);
                if (legacyWi != null) {
                    legacyWi.remove(world);
                    if (legacyWi.isEmpty()) {
                        yamlRoot.remove(YamlMaps.WORLD_INHERITANCE_LEGACY);
                    }
                }
            } else {
                worldInheritanceCache.put(world, Collections.unmodifiableList(parentWorlds));
                Map<String, Object> tw =
                        YamlMaps.requireSection(yamlRoot, YamlMaps.WORLDS);
                Map<String, Object> wSec = YamlMaps.requireSection(tw, world);
                wSec.put("inheritance", new ArrayList<>(parentWorlds));
            }

            persistIfNeeded();
        }
    }

    @Override
    public void writeContents(Writer writer) throws IOException {
        synchronized (lock) {
            LinkedHashMap<String, Object> copy = duplicateRootForDump();
            persistWorldMirrorsForDump(copy);
            yaml.dump(copy, writer);
        }
    }

    private LinkedHashMap<String, Object> duplicateRootForDump() {
        return copyMap(yamlRoot);
    }

    @SuppressWarnings("unchecked")
    private LinkedHashMap<String, Object> copyMap(Map<String, Object> src) {
        LinkedHashMap<String, Object> out = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : src.entrySet()) {
            if (e.getValue() instanceof Map) {
                out.put(e.getKey(), copyMap(castStringObjectMap((Map<?, ?>) e.getValue())));
            } else if (e.getValue() instanceof List<?> list) {
                out.put(e.getKey(), new ArrayList<>(list));
            } else {
                out.put(e.getKey(), e.getValue());
            }
        }
        return out;
    }

    private void persistWorldMirrorsForDump(LinkedHashMap<String, Object> rootCopy) {
        Map<String, Object> wi =
                YamlMaps.requireSection(rootCopy, YamlMaps.WORLD_INHERITANCE_LEGACY);
        wi.clear();
        for (Map.Entry<String, List<String>> e : worldInheritanceCache.entrySet()) {
            if (!e.getValue().isEmpty()) {
                wi.put(e.getKey(), new ArrayList<>(e.getValue()));
            }
        }
        if (wi.isEmpty()) {
            rootCopy.remove(YamlMaps.WORLD_INHERITANCE_LEGACY);
        }
    }

    @Override
    public void loadFrom(PermissionBackend backend) {
        setPersistent(false);
        try {
            super.loadFrom(backend);
        } finally {
            setPersistent(true);
        }
    }

    @Override
    public void setPersistent(boolean persistent) {
        super.setPersistent(persistent);
        suppressSave = !persistent;
        if (persistent) {
            saveLocked();
        }
    }

    @SuppressWarnings("unchecked")
    private static LinkedHashMap<String, Object> castStringObjectMap(Map<?, ?> raw) {
        LinkedHashMap<String, Object> m = new LinkedHashMap<>();
        for (Map.Entry<?, ?> e : raw.entrySet()) {
            m.put(String.valueOf(e.getKey()), e.getValue());
        }
        return m;
    }
}
