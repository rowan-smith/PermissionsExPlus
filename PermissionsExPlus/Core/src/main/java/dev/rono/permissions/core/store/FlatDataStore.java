package dev.rono.permissions.core.store;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.gson.GsonConfigurationLoader;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

/**
 * Flat-file {@link DataStore} with write-behind persistence.
 *
 * <p>
 * Mutations update the in-memory map immediately and mark the store dirty.
 * Durable writes happen on {@link #checkpoint()}, {@link #close()}, or an
 * explicit flush. The API scheduler already invokes {@code checkpoint()} on
 * {@code auto-save-interval}.
 * </p>
 */
public final class FlatDataStore implements DataStore {
    private final Path file;
    private final boolean yaml;

    private final Map<String, Map<String, String>> data = new LinkedHashMap<>();
    private boolean dirty;

    public FlatDataStore(Path directory, boolean yaml) {
        this.yaml = yaml;
        file = directory.resolve("permissions." + (yaml ? "yml" : "json"));
    }

    @Override
    public synchronized void open() {
        try {
            Files.createDirectories(file.getParent());

            var root = loader(file).load();

            root.childrenMap().forEach((category, node) -> {
                var values = new LinkedHashMap<String, String>();

                node.childrenMap().forEach((key, value) -> values.put(String.valueOf(key), value.getString("")));

                data.put(String.valueOf(category), values);
            });

            dirty = false;
        } catch (IOException error) {
            throw new IllegalStateException("Unable to open API flat-file storage", error);
        }
    }

    @Override
    public synchronized Optional<String> get(String category, String key) {
        return Optional.ofNullable(data.getOrDefault(category, Map.of()).get(key));
    }

    @Override
    public synchronized Map<String, String> all(String category) {
        return Map.copyOf(data.getOrDefault(category, Map.of()));
    }

    @Override
    public synchronized void put(String category, String key, String payload) {
        data.computeIfAbsent(category, ignored -> new LinkedHashMap<>()).put(key, payload);
        dirty = true;
    }

    @Override
    public synchronized boolean remove(String category, String key) {
        var values = data.get(category);
        var changed = values != null && values.remove(key) != null;

        if (changed) {
            if (values.isEmpty()) {
                data.remove(category);
            }

            dirty = true;
        }

        return changed;
    }

    @Override
    public synchronized void checkpoint() {
        if (dirty) {
            save();
            dirty = false;
        }
    }

    @Override
    public boolean supportsCheckpoints() {
        return true;
    }

    @Override
    public String name() {
        return yaml ? "YAML" : "JSON";
    }

    @Override
    public boolean persistent() {
        return true;
    }

    @Override
    public synchronized void close() {
        if (dirty) {
            save();
            dirty = false;
        }

        data.clear();
    }

    /** Whether mutations are pending a durable write. */
    public synchronized boolean dirty() {
        return dirty;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private ConfigurationLoader<ConfigurationNode> loader(Path path) {
        return (ConfigurationLoader) (yaml
                ? YamlConfigurationLoader.builder().path(path).build()
                : GsonConfigurationLoader.builder().path(path).build());
    }

    private void save() {
        try {
            var root = loader(file).createNode();

            data.forEach((category, values) -> values.forEach((key, payload) -> {
                try {
                    root.node(category, key).set(payload);
                } catch (Exception error) {
                    throw new IllegalStateException(error);
                }
            }));

            var temporary = file.resolveSibling(file.getFileName() + ".tmp");
            loader(temporary).save(root);

            try {
                Files.move(temporary, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException error) {
            throw new IllegalStateException("Unable to save API flat-file storage", error);
        }
    }
}
