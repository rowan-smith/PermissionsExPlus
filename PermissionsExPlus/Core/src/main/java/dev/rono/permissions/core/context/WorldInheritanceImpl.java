package dev.rono.permissions.core.context;

import lombok.var;

import dev.rono.permissions.api.context.ContextKeys;
import dev.rono.permissions.api.context.ContextSet;
import dev.rono.permissions.api.context.WorldInheritance;
import dev.rono.permissions.core.util.Immutables;
import dev.rono.permissions.core.util.Strings;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayDeque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

/**
 * World inheritance stored in {@code worlds.yml}, in the same shape as the
 * {@code worlds} section of a PermissionsEx {@code permissions.yml}:
 *
 * <pre>
 * worlds:
 *   world_nether:
 *     inheritance:
 *     - world
 * </pre>
 */
public final class WorldInheritanceImpl implements WorldInheritance {
    private static final String WORLD = ContextKeys.WORLD.key();
    private static final String HEADER = "World inheritance, as in PermissionsEx. A world listed here also gets the\n"
            + "permissions, options and groups set for the worlds it inherits from, and\n"
            + "inheritance is transitive. Example:\n"
            + "\n"
            + "worlds:\n"
            + "  world_nether:\n"
            + "    inheritance:\n"
            + "    - world\n"
            + "\n"
            + "When a world and a world it inherits from set the same node differently, the\n"
            + "inheritance.conflict-resolution setting in advanced.yml decides, as for any other\n"
            + "conflict between equally specific nodes.";

    private final Path file;
    private final Runnable onChange;
    private volatile Map<String, List<String>> inheritance;

    public WorldInheritanceImpl(Path file, Runnable onChange) {
        this.file = Objects.requireNonNull(file, "file");
        this.onChange = Objects.requireNonNull(onChange, "onChange");
        this.inheritance = load(file);
    }

    /** In-memory inheritance that is never saved; for tests and tools. */
    public static WorldInheritanceImpl inMemory(Map<String, List<String>> inheritance) {
        return new WorldInheritanceImpl(inheritance);
    }

    private WorldInheritanceImpl(Map<String, List<String>> inheritance) {
        this.file = null;
        this.onChange = () -> {};
        this.inheritance = copy(inheritance);
    }

    @Override
    public List<String> parents(String world) {
        return inheritance.getOrDefault(Objects.requireNonNull(world, "world"), Immutables.listOf());
    }

    @Override
    public Map<String, List<String>> all() {
        return inheritance;
    }

    @Override
    public synchronized void set(String world, List<String> parents) {
        Objects.requireNonNull(world, "world");
        Objects.requireNonNull(parents, "parents");

        if (Strings.isBlank(world)) {
            throw new IllegalArgumentException("World name is blank");
        }

        var cleaned = new LinkedHashSet<String>();

        for (var parent : parents) {
            if (parent == null || Strings.isBlank(parent)) {
                throw new IllegalArgumentException("Parent world name is blank");
            }

            if (!parent.equals(world)) {
                cleaned.add(parent);
            }
        }

        var updated = new LinkedHashMap<>(inheritance);

        if (cleaned.isEmpty()) {
            updated.remove(world);
        } else {
            updated.put(world, Immutables.copyOfList(cleaned));
        }

        if (file != null) {
            save(file, updated);
        }

        inheritance = copy(updated);
        onChange.run();
    }

    @Override
    public ContextSet expand(ContextSet contexts) {
        Objects.requireNonNull(contexts, "contexts");

        var current = inheritance;
        var worlds = contexts.values(WORLD);

        if (current.isEmpty() || worlds.isEmpty()) {
            return contexts;
        }

        var all = new LinkedHashSet<String>(worlds);
        var queue = new ArrayDeque<String>(worlds);

        while (!queue.isEmpty()) {
            for (var parent : current.getOrDefault(queue.poll(), Immutables.listOf())) {
                if (all.add(parent)) {
                    queue.add(parent);
                }
            }
        }

        if (all.size() == worlds.size()) {
            return contexts;
        }

        var builder = ContextSet.builder(contexts);
        all.forEach(world -> builder.add(WORLD, world));

        return builder.build();
    }

    private static Map<String, List<String>> load(Path file) {
        if (Files.notExists(file)) {
            // Write a commented, empty file so the setting is easy to find.
            save(file, Immutables.mapOf());
            return Immutables.mapOf();
        }

        try {
            var root = YamlConfigurationLoader.builder().path(file).build().load();
            var loaded = new LinkedHashMap<String, List<String>>();

            for (Map.Entry<Object, ? extends ConfigurationNode> entry : root.node("worlds").childrenMap().entrySet()) {
                var parents = entry.getValue().node("inheritance").getList(String.class, Immutables.listOf());

                if (!parents.isEmpty()) {
                    loaded.put(String.valueOf(entry.getKey()), parents);
                }
            }

            return copy(loaded);
        } catch (Exception error) {
            throw new IllegalStateException("Unable to load " + file, error);
        }
    }

    private static void save(Path file, Map<String, List<String>> inheritance) {
        try {
            Files.createDirectories(file.toAbsolutePath().getParent());

            var temporary = file.resolveSibling(file.getFileName() + ".tmp");
            var loader = YamlConfigurationLoader.builder().path(temporary).nodeStyle(NodeStyle.BLOCK)
                    .defaultOptions(options -> options.header(HEADER)).build();
            var root = loader.createNode();

            root.node("worlds").raw(java.util.Collections.emptyMap());

            for (var entry : inheritance.entrySet()) {
                root.node("worlds", entry.getKey(), "inheritance").setList(String.class, entry.getValue());
            }

            loader.save(root);

            try {
                Files.move(temporary, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException error) {
            throw new IllegalStateException("Unable to save " + file, error);
        }
    }

    private static Map<String, List<String>> copy(Map<String, List<String>> source) {
        var copy = new LinkedHashMap<String, List<String>>();
        source.forEach((world, parents) -> copy.put(world, Immutables.copyOfList(parents)));
        return Immutables.copyOfMap(copy);
    }
}
