package dev.rono.permissions.bungee;

import dev.rono.permissions.core.PermissionsExConfig;
import dev.rono.permissions.core.config.PexConfigData;
import dev.rono.permissions.core.config.PexConfigFlavor;
import dev.rono.permissions.core.config.PexRef;
import dev.rono.permissions.core.configuration.PexYamlConfig;
import dev.rono.permissions.core.runtime.PexVolatileRef;
import org.bukkit.configuration.ConfigurationSection;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import ru.tehkode.permissions.PEXBackendConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

@SuppressWarnings("unchecked")
public class BungeePermissionsExConfig implements PermissionsExConfig, ru.tehkode.permissions.bukkit.PermissionsExConfig {
    private final Path configFile;
    private final Logger logger;
    private final Yaml yaml;
    private Map<String, Object> root;
    private PexVolatileRef<PexConfigData> loaded;

    public BungeePermissionsExConfig(java.io.File dataFolder, Logger logger) {
        this.logger = logger;
        this.configFile = dataFolder.toPath().resolve("config.yml");
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        this.yaml = new Yaml(options);
        reload();
    }

    void reload() {
        try {
            Files.createDirectories(configFile.getParent());
            if (!Files.exists(configFile)) {
                root = defaultConfig();
                ensureDefaults();
                rebind();
                save();
                return;
            }
            try (InputStream in = Files.newInputStream(configFile)) {
                Object parsed = yaml.load(in);
                if (parsed instanceof Map<?, ?> map) {
                    root = (Map<String, Object>) map;
                } else {
                    root = defaultConfig();
                }
            }
            ensureDefaults();
            rebind();
        } catch (IOException ex) {
            throw new RuntimeException("Unable to load bungee config", ex);
        }
    }

    private void rebind() {
        Map<String, Object> perms = PexYamlConfig.permissionsFrom(root);
        PexConfigData snap =
                PexYamlConfig.load(
                        perms,
                        () -> configFile.getParent().resolve("data").toString(),
                        PexConfigFlavor.BUNGEE);
        if (loaded == null) {
            loaded = new PexVolatileRef<>(snap);
        } else {
            loaded.replace(snap);
        }
    }

    @Override
    public PexRef<PexConfigData> options() {
        return loaded;
    }

    @Override
    public void setDefaultBackend(String backendName) {
        set("permissions.backend", backendName);
        rebind();
        save();
    }

    @Override
    public boolean isDebug() {
        return loaded.current().debug();
    }

    @Override
    public boolean allowOps() {
        return loaded.current().allowOps();
    }

    @Override
    public boolean userAddGroupsLast() {
        return loaded.current().userAddGroupsLast();
    }

    @Override
    public String getDefaultBackend() {
        return loaded.current().backend();
    }

    @Override
    public boolean shouldLogPlayers() {
        return loaded.current().logPlayers();
    }

    @Override
    public boolean createUserRecords() {
        return loaded.current().createUserRecords();
    }

    @Override
    public boolean saveDefaultGroup() {
        return loaded.current().saveDefaultGroup();
    }

    @Override
    public boolean informPlayers() {
        return loaded.current().informPlayerChanges();
    }

    @Override
    public String getBasedir() {
        return loaded.current().basedir();
    }

    @Override
    public PEXBackendConfiguration pexBackendConfiguration(String backend) {
        Map<String, Object> section = getOrCreateSection("permissions.backends." + backend);
        return new BungeeBackendConfiguration(backend, section);
    }

    @Override
    public void save() {
        try (Writer writer = Files.newBufferedWriter(configFile)) {
            yaml.dump(root, writer);
        } catch (IOException ex) {
            logger.warning("Failed to save PermissionsEx bungee config: " + ex.getMessage());
        }
    }

    Object getNode(String path) {
        return get(path);
    }

    void setNode(String path, Object value) {
        set(path, value);
    }

    private Object get(String path) {
        String[] parts = path.split("\\.");
        Object current = root;
        for (String part : parts) {
            if (!(current instanceof Map<?, ?> map)) {
                return null;
            }
            current = map.get(part);
            if (current == null) {
                return null;
            }
        }
        return current;
    }

    private void set(String path, Object value) {
        String[] parts = path.split("\\.");
        Map<String, Object> current = root;
        for (int i = 0; i < parts.length - 1; i++) {
            Object next = current.get(parts[i]);
            if (!(next instanceof Map<?, ?>)) {
                LinkedHashMap<String, Object> section = new LinkedHashMap<>();
                current.put(parts[i], section);
                current = section;
            } else {
                current = (Map<String, Object>) next;
            }
        }
        current.put(parts[parts.length - 1], value);
    }

    private Map<String, Object> getOrCreateSection(String path) {
        String[] parts = path.split("\\.");
        Map<String, Object> current = root;
        for (String part : parts) {
            Object next = current.get(part);
            if (!(next instanceof Map<?, ?>)) {
                LinkedHashMap<String, Object> section = new LinkedHashMap<>();
                current.put(part, section);
                current = section;
            } else {
                current = (Map<String, Object>) next;
            }
        }
        return current;
    }

    private void ensureDefaults() {
        Map<String, Object> perms = getOrCreateSection("permissions");
        perms.putIfAbsent("backend", "file");
        perms.putIfAbsent("basedir", configFile.getParent().resolve("data").toString());
        perms.putIfAbsent("debug", false);
        perms.putIfAbsent("allowOps", false);
        perms.putIfAbsent("user-add-groups-last", false);
        perms.putIfAbsent("log-players", false);
        perms.putIfAbsent("createUserRecords", true);
        perms.putIfAbsent("save-default-group", false);

        Map<String, Object> inform = getOrCreateSection("permissions.informplayers");
        inform.putIfAbsent("changes", false);

        Map<String, Object> backend = getOrCreateSection("permissions.backends.memory");
        backend.putIfAbsent("type", "memory");
        Map<String, Object> fileBackend = getOrCreateSection("permissions.backends.file");
        fileBackend.putIfAbsent("type", "file");
        fileBackend.putIfAbsent("file", "permissions.yml");
    }

    private static Map<String, Object> defaultConfig() {
        LinkedHashMap<String, Object> root = new LinkedHashMap<>();
        LinkedHashMap<String, Object> permissions = new LinkedHashMap<>();
        permissions.put("backend", "file");
        permissions.put("basedir", "plugins/PermissionsEx");
        permissions.put("debug", false);
        permissions.put("allowOps", false);
        permissions.put("user-add-groups-last", false);
        permissions.put("log-players", false);
        permissions.put("createUserRecords", true);
        permissions.put("save-default-group", false);
        LinkedHashMap<String, Object> inform = new LinkedHashMap<>();
        inform.put("changes", false);
        permissions.put("informplayers", inform);
        LinkedHashMap<String, Object> backends = new LinkedHashMap<>();
        LinkedHashMap<String, Object> memory = new LinkedHashMap<>();
        memory.put("type", "memory");
        backends.put("memory", memory);
        LinkedHashMap<String, Object> file = new LinkedHashMap<>();
        file.put("type", "file");
        file.put("file", "permissions.yml");
        backends.put("file", file);
        permissions.put("backends", backends);
        root.put("permissions", permissions);
        return root;
    }

    @Override
    public boolean useNetEvents() {
        return false;
    }

    @Override
    public boolean updaterEnabled() {
        return false;
    }

    @Override
    public boolean alwaysUpdate() {
        return false;
    }

    @Override
    public java.util.List<String> getServerTags() {
        return java.util.Collections.emptyList();
    }

    @Override
    public ConfigurationSection getBackendConfig(String backend) {
        return null;
    }
}
