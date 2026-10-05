package dev.rono.permissions.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import dev.rono.permissions.api.context.ContextSet;
import dev.rono.permissions.api.options.OptionNode;
import dev.rono.permissions.api.parent.ParentNode;
import dev.rono.permissions.api.permission.PermissionNode;
import dev.rono.permissions.core.config.DatabasePool;
import dev.rono.permissions.core.config.DdlGeneration;
import dev.rono.permissions.core.model.GroupSnapshot;
import dev.rono.permissions.core.store.FlatDataStore;
import dev.rono.permissions.core.store.HibernateDataStore;
import dev.rono.permissions.core.store.SnapshotCodec;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.util.OptionalInt;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class StorageTest {
    @TempDir
    Path directory;

    @Test
    void yamlRoundTripIsDurable() {
        var first = new FlatDataStore(directory, true);

        first.open();

        assertFalse(first.remove("missing", "id"));

        first.put("users", "id", "{\"name\":\"Rono\"}");
        assertTrue(first.dirty());
        first.checkpoint();
        assertFalse(first.dirty());
        first.close();

        var second = new FlatDataStore(directory, true);

        second.open();

        assertEquals("{\"name\":\"Rono\"}", second.get("users", "id").orElseThrow());
        assertTrue(second.remove("users", "id"));
        second.checkpoint();

        second.close();
    }

    @Test
    void hibernateTypedPathSkipsJsonRoundTrip() {
        var pool = new DatabasePool(2, 1, 30_000);
        var url = "jdbc:h2:mem:permissions-typed-" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1";
        var store = new HibernateDataStore("Memory", url, "org.h2.Driver", null, null, pool, false);

        store.open();

        var contexts = ContextSet.builder().add("world", "nether").build();
        var group = new GroupSnapshot("staff", OptionalInt.of(100),
                Set.of(PermissionNode.builder().permission("example.fly").contexts(contexts).build()),
                Set.of(OptionNode.builder().key("prefix").value("Admin").contexts(contexts).build()),
                Set.of(ParentNode.builder().group("default").contexts(contexts).build()));

        store.putGroup(group);

        assertEquals(group, store.getGroup("staff").orElseThrow());
        assertEquals(group, store.allGroups().get("staff"));
        assertTrue(store.removeGroup("staff"));

        store.close();
    }

    @Test
    void hibernateMemoryRoundTripUsesTypedTablesAndRepositories() throws Exception {
        var pool = new DatabasePool(2, 1, 30_000);
        var url = "jdbc:h2:mem:permissions-store-" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1";
        var store = new HibernateDataStore("Memory", url, "org.h2.Driver", null, null, pool, false);

        store.open();

        var contexts = ContextSet.builder().add("world", "nether").build();
        var group = new GroupSnapshot("staff", OptionalInt.of(100),
                Set.of(PermissionNode.builder().permission("example.fly").contexts(contexts).build()),
                Set.of(OptionNode.builder().key("prefix").value("Admin").contexts(contexts).build()),
                Set.of(ParentNode.builder().group("default").contexts(contexts).build()));

        var payload = SnapshotCodec.group(group);

        store.put("groups", "staff", payload);

        assertEquals(group, SnapshotCodec.group(store.get("groups", "staff").orElseThrow()));
        assertEquals(group, SnapshotCodec.group(store.all("groups").get("staff")));

        try (var connection = DriverManager.getConnection(url); var tables = connection.getMetaData().getTables(null, null, "PEX_%", new String[]{"TABLE"})) {
            var names = new java.util.HashSet<String>();

            while (tables.next()) {
                names.add(tables.getString("TABLE_NAME"));
            }

            assertTrue(names.containsAll(Set.of("PEX_USERS", "PEX_GROUPS", "PEX_LADDERS", "PEX_PERMISSIONS", "PEX_OPTIONS", "PEX_PARENTS", "PEX_CONTEXTS", "PEX_LADDER_GROUPS")));
            assertFalse(names.contains("PEX_DATA"));
        }

        assertTrue(store.remove("groups", "staff"));

        store.close();

        var validating = new HibernateDataStore("Memory", url, "org.h2.Driver", null, null, new DatabasePool(2, 1, 4_321L, 30_000L), DdlGeneration.VALIDATE, false);
        validating.open();

        assertTrue(validating.all("groups").isEmpty());

        validating.close();
    }

    @Test
    void ddlGenerationNoneDoesNotCreateMissingTables() {
        var pool = new DatabasePool(2, 1, 5_000L, 30_000L);
        var url = "jdbc:h2:mem:permissions-no-ddl-" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1";
        var store = new HibernateDataStore("Memory", url, "org.h2.Driver", null, null, pool, DdlGeneration.NONE, false);

        store.open();

        assertThrows(RuntimeException.class, () -> store.all("groups"));

        store.close();
    }
}
