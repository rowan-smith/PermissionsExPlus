package dev.rono.permissions.core.store.jdbc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.rono.permissions.api.context.ContextSet;
import dev.rono.permissions.api.options.OptionNode;
import dev.rono.permissions.api.parent.ParentNode;
import dev.rono.permissions.api.permission.PermissionNode;
import dev.rono.permissions.api.permission.PermissionValue;
import dev.rono.permissions.core.config.DatabasePool;
import dev.rono.permissions.core.config.DdlGeneration;
import dev.rono.permissions.core.model.GroupSnapshot;
import dev.rono.permissions.core.model.LadderSnapshot;
import dev.rono.permissions.core.model.UserSnapshot;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.OptionalInt;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class JdbcDataStoreTest {
    private static final DatabasePool POOL = new DatabasePool(2, 1, 5_000L, 30_000L);
    private static final Instant EXPIRY = Instant.parse("2031-06-15T12:30:45.123456Z");

    private static String h2Url() {
        return "jdbc:h2:mem:pex-jdbc-" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1";
    }

    private static JdbcDataStore h2(String url, DdlGeneration mode) {
        return new JdbcDataStore("H2", url, "org.h2.Driver", null, null, POOL, mode, JdbcDialect.H2, false);
    }

    private static UserSnapshot user() {
        ContextSet nether = ContextSet.builder().add("world", "nether").add("server", "survival").build();

        return new UserSnapshot(UUID.randomUUID(), "Alex",
                set(PermissionNode.builder().permission("example.fly").contexts(nether).build(),
                        PermissionNode.builder().permission("example.timed").expiry(EXPIRY).build(),
                        PermissionNode.builder().permission("example.denied").value(PermissionValue.DENY).build()),
                set(OptionNode.builder().key("prefix").value("[Admin]").expiry(EXPIRY).build()),
                set(ParentNode.builder().group("staff").contexts(nether).expiry(EXPIRY).build()));
    }

    @Test
    void usersRoundTripWithContextsAndMicrosecondExpiries() {
        JdbcDataStore store = h2(h2Url(), DdlGeneration.UPDATE);
        store.open();

        try {
            UserSnapshot user = user();

            store.putUser(user);

            assertEquals(user, store.getUser(user.uniqueId()).get());
            assertEquals(user, store.allUsers().get(user.uniqueId().toString()));

            // Renaming updates the existing row instead of inserting a second one.
            UserSnapshot renamed = new UserSnapshot(user.uniqueId(), "Alexandra", user.explicitPermissions(), user.explicitOptions(), user.groups());
            store.putUser(renamed);

            assertEquals(1, store.allUsers().size());
            assertEquals("Alexandra", store.getUser(user.uniqueId()).get().name());

            assertTrue(store.removeUser(user.uniqueId()));
            assertFalse(store.removeUser(user.uniqueId()));
            assertFalse(store.getUser(user.uniqueId()).isPresent());
        } finally {
            store.close();
        }
    }

    @Test
    void savingReplacesNodesWithoutLeavingOrphanContexts() throws Exception {
        String url = h2Url();
        JdbcDataStore store = h2(url, DdlGeneration.UPDATE);
        store.open();

        try {
            ContextSet nether = ContextSet.builder().add("world", "nether").build();

            store.putGroup(new GroupSnapshot("staff", OptionalInt.of(10),
                    set(PermissionNode.builder().permission("a").contexts(nether).build(), PermissionNode.builder().permission("b").contexts(nether).build()),
                    Collections.<OptionNode>emptySet(), Collections.<ParentNode>emptySet()));

            GroupSnapshot replaced = new GroupSnapshot("staff", OptionalInt.empty(),
                    set(PermissionNode.builder().permission("c").build()),
                    Collections.<OptionNode>emptySet(), Collections.<ParentNode>emptySet());

            store.putGroup(replaced);

            assertEquals(replaced, store.getGroup("staff").get());
            assertEquals(1, count(url, "pex_permissions"));
            assertEquals(0, count(url, "pex_contexts"));

            assertTrue(store.removeGroup("staff"));
            assertEquals(0, count(url, "pex_permissions"));
        } finally {
            store.close();
        }
    }

    @Test
    void laddersKeepGroupOrder() {
        JdbcDataStore store = h2(h2Url(), DdlGeneration.UPDATE);
        store.open();

        try {
            LadderSnapshot ladder = new LadderSnapshot("staff", Arrays.asList("helper", "moderator", "admin"));

            store.putLadder(ladder);
            assertEquals(ladder, store.getLadder("staff").get());

            LadderSnapshot reordered = new LadderSnapshot("staff", Arrays.asList("admin", "helper"));

            store.putLadder(reordered);
            assertEquals(reordered, store.allLadders().get("staff"));

            assertTrue(store.removeLadder("staff"));
            assertFalse(store.getLadder("staff").isPresent());
        } finally {
            store.close();
        }
    }

    @Test
    void stampsSchemaVersionAndAdoptsUnversionedDatabases() throws Exception {
        String url = h2Url();
        JdbcDataStore store = h2(url, DdlGeneration.UPDATE);
        store.open();
        store.close();

        assertEquals(JdbcSchema.CURRENT_VERSION, version(url));

        // A database created by the earlier Hibernate store has the tables but no
        // version table.
        execute(url, "DROP TABLE pex_schema");

        JdbcDataStore validating = h2(url, DdlGeneration.VALIDATE);
        validating.open();
        validating.close();

        JdbcDataStore updating = h2(url, DdlGeneration.UPDATE);
        updating.open();
        updating.close();

        assertEquals(JdbcSchema.CURRENT_VERSION, version(url));
    }

    @Test
    void refusesDatabasesFromNewerSchemaVersions() throws Exception {
        String url = h2Url();
        JdbcDataStore store = h2(url, DdlGeneration.UPDATE);
        store.open();
        store.close();

        execute(url, "UPDATE pex_schema SET version = " + (JdbcSchema.CURRENT_VERSION + 1));

        assertThrows(IllegalStateException.class, () -> h2(url, DdlGeneration.UPDATE).open());
        assertThrows(IllegalStateException.class, () -> h2(url, DdlGeneration.VALIDATE).open());
    }

    @Test
    void validateReportsMissingTables() {
        assertThrows(IllegalStateException.class, () -> h2(h2Url(), DdlGeneration.VALIDATE).open());
    }

    @Test
    void sqliteRoundTripPersistsAcrossReopen(@TempDir Path directory) {
        String url = "jdbc:sqlite:" + directory.resolve("permissions.db");
        UserSnapshot user = user();

        JdbcDataStore first = new JdbcDataStore("SQLite", url, "org.sqlite.JDBC", null, null, POOL, DdlGeneration.UPDATE, JdbcDialect.SQLITE, true);
        first.open();

        try {
            first.putUser(user);
            first.putLadder(new LadderSnapshot("staff", Arrays.asList("helper", "admin")));
        } finally {
            first.close();
        }

        JdbcDataStore second = new JdbcDataStore("SQLite", url, "org.sqlite.JDBC", null, null, POOL, DdlGeneration.UPDATE, JdbcDialect.SQLITE, true);
        second.open();

        try {
            assertEquals(user, second.getUser(user.uniqueId()).get());
            assertEquals(Arrays.asList("helper", "admin"), second.getLadder("staff").get().groups());
        } finally {
            second.close();
        }
    }

    @SafeVarargs
    private static <T> Set<T> set(T... values) {
        return new HashSet<>(Arrays.asList(values));
    }

    private static int count(String url, String table) throws Exception {
        try (Connection connection = DriverManager.getConnection(url); Statement statement = connection.createStatement(); ResultSet rows = statement.executeQuery("SELECT COUNT(*) FROM " + table)) {
            rows.next();
            return rows.getInt(1);
        }
    }

    private static int version(String url) throws Exception {
        try (Connection connection = DriverManager.getConnection(url); Statement statement = connection.createStatement(); ResultSet rows = statement.executeQuery("SELECT MAX(version) FROM pex_schema")) {
            rows.next();
            return rows.getInt(1);
        }
    }

    private static void execute(String url, String sql) throws Exception {
        try (Connection connection = DriverManager.getConnection(url); Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }
}
