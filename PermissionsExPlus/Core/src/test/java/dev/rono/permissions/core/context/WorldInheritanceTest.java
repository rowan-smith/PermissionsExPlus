package dev.rono.permissions.core.context;

import lombok.var;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import dev.rono.permissions.api.context.ContextSet;
import dev.rono.permissions.core.TestFiles;
import dev.rono.permissions.core.util.Immutables;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class WorldInheritanceTest {
    @TempDir
    Path directory;

    @Test
    void expandsTransitivelyAndSurvivesCycles() {
        var worlds = WorldInheritanceImpl.inMemory(Immutables.mapOf(
                "world_the_end", Immutables.listOf("world_nether"),
                "world_nether", Immutables.listOf("world"),
                "world", Immutables.listOf("world_the_end")));

        var expanded = worlds.expand(ContextSet.builder().add("world", "world_the_end").add("server", "lobby").build());

        assertEquals(Immutables.setOf("world_the_end", "world_nether", "world"), expanded.values("world"));
        assertEquals(Immutables.setOf("lobby"), expanded.values("server"));
    }

    @Test
    void leavesContextsWithoutAnInheritingWorldUntouched() {
        var worlds = WorldInheritanceImpl.inMemory(Immutables.mapOf("world_nether", Immutables.listOf("world")));
        var global = ContextSet.empty();
        var overworld = ContextSet.builder().add("world", "world").build();

        assertSame(global, worlds.expand(global));
        assertSame(overworld, worlds.expand(overworld));
    }

    @Test
    void writesACommentedDefaultFile() throws Exception {
        var file = directory.resolve("worlds.yml");

        new WorldInheritanceImpl(file, () -> {});

        assertTrue(Files.exists(file));
        assertTrue(TestFiles.readString(file).contains("# World inheritance, as in PermissionsEx"));
        assertTrue(new WorldInheritanceImpl(file, () -> {}).all().isEmpty());
    }

    @Test
    void savesChangesAndReadsThemBack() {
        var file = directory.resolve("worlds.yml");
        var changes = new AtomicInteger();
        var worlds = new WorldInheritanceImpl(file, changes::incrementAndGet);

        worlds.set("world_nether", Immutables.listOf("world", "world_nether", "world"));
        worlds.set("world_the_end", Immutables.listOf("world"));
        worlds.set("world_the_end", Immutables.listOf());

        assertEquals(3, changes.get());
        // A world never inherits from itself, and duplicates collapse.
        assertEquals(Immutables.mapOf("world_nether", Immutables.listOf("world")), worlds.all());
        assertEquals(worlds.all(), new WorldInheritanceImpl(file, () -> {}).all());
    }

    @Test
    void readsThePermissionsExShape() throws Exception {
        var file = directory.resolve("worlds.yml");
        TestFiles.writeString(file, "worlds:\n  world_nether:\n    inheritance:\n    - world\n    - creative\n");

        assertEquals(Immutables.listOf("world", "creative"), new WorldInheritanceImpl(file, () -> {}).parents("world_nether"));
    }
}
