package dev.rono.permissions.core.store;

import lombok.var;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.gson.GsonConfigurationLoader;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;
import dev.rono.permissions.core.util.Immutables;

/**
 * Flat-file {@link DataStore}.
 *
 * <p>
 * Every write is saved to disk before it returns, by writing a temporary file
 * and moving it over the old one. Writes inside a {@link #transaction} are
 * saved
 * once, when the transaction ends, and are undone in memory if it fails.
 * </p>
 */
public final class FlatDataStore implements DataStore {
    private final Path file;
    private final boolean yaml;

    private final Map<String, Map<String, String>> data = new LinkedHashMap<>();
    private boolean dirty;
    private int transactionDepth;

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
        return Optional.ofNullable(data.getOrDefault(category, Immutables.mapOf()).get(key));
    }

    @Override
    public synchronized Map<String, String> all(String category) {
        return Immutables.copyOfMap(data.getOrDefault(category, Immutables.mapOf()));
    }

    @Override
    public synchronized <T> T transaction(Supplier<T> work) {
        if (transactionDepth > 0) {
            return work.get();
        }

        var saved = MemoryDataStore.copy(data);
        T result;

        transactionDepth++;

        try {
            result = work.get();
        } catch (RuntimeException | Error error) {
            transactionDepth--;
            restore(saved);

            throw error;
        }

        transactionDepth--;
        persist(saved);

        return result;
    }

    @Override
    public synchronized void put(String category, String key, String payload) {
        var saved = transactionDepth > 0 ? null : MemoryDataStore.copy(data);

        data.computeIfAbsent(category, ignored -> new LinkedHashMap<>()).put(key, payload);
        dirty = true;

        persist(saved);
    }

    @Override
    public synchronized boolean remove(String category, String key) {
        var saved = transactionDepth > 0 ? null : MemoryDataStore.copy(data);
        var values = data.get(category);
        var changed = values != null && values.remove(key) != null;

        if (changed) {
            if (values.isEmpty()) {
                data.remove(category);
            }

            dirty = true;

            persist(saved);
        }

        return changed;
    }

    /**
     * Saves pending writes unless a transaction is still open. If the save
     * fails, memory goes back to {@code saved}, the state on disk.
     */
    private void persist(Map<String, Map<String, String>> saved) {
        if (transactionDepth > 0 || !dirty) {
            return;
        }

        try {
            save();
            dirty = false;
        } catch (RuntimeException error) {
            restore(saved);

            throw error;
        }
    }

    private void restore(Map<String, Map<String, String>> saved) {
        data.clear();
        data.putAll(saved);
        dirty = false;
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
