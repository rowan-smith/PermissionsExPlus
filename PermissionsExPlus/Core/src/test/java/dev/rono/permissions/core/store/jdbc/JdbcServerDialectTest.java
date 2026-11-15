package dev.rono.permissions.core.store.jdbc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.OptionalInt;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Runs the store against real MySQL and Postgres servers. Skipped when Docker
 * is unavailable. Expiries carry microseconds and are checked for exact
 * equality, which catches drivers applying the JVM time zone.
 */
@Testcontainers(disabledWithoutDocker = true)
class JdbcServerDialectTest {
    private static final DatabasePool POOL = new DatabasePool(4, 1, 30_000L, 1_800_000L);
    private static final Instant EXPIRY = Instant.parse("2031-06-15T12:30:45.123456Z");

    @Container
    private static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.4");

    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @Test
    void mysql() {
        exercise(MYSQL, "com.mysql.cj.jdbc.Driver", JdbcDialect.MYSQL);
    }

    @Test
    void postgres() {
        exercise(POSTGRES, "org.postgresql.Driver", JdbcDialect.POSTGRES);
    }

    private static JdbcDataStore store(JdbcDatabaseContainer<?> container, String driver, JdbcDialect dialect, DdlGeneration mode) {
        return new JdbcDataStore(dialect.name(), container.getJdbcUrl(), driver, container.getUsername(), container.getPassword(), POOL, mode, dialect, true);
    }

    private static void exercise(JdbcDatabaseContainer<?> container, String driver, JdbcDialect dialect) {
        JdbcDataStore store = store(container, driver, dialect, DdlGeneration.UPDATE);
        store.open();

        try {
            ContextSet nether = ContextSet.builder().add("world", "nether").add("server", "survival").build();
            UserSnapshot user = new UserSnapshot(UUID.randomUUID(), "Alex",
                    set(PermissionNode.builder().permission("example.fly").contexts(nether).build(),
                            PermissionNode.builder().permission("example.timed").expiry(EXPIRY).build(),
                            PermissionNode.builder().permission("example.denied").value(PermissionValue.DENY).build()),
                    set(OptionNode.builder().key("prefix").value("[Admin]").expiry(EXPIRY).build()),
                    set(ParentNode.builder().group("staff").contexts(nether).expiry(EXPIRY).build()));

            store.putUser(user);
            assertEquals(user, store.getUser(user.uniqueId()).get());
            assertEquals(user, store.allUsers().get(user.uniqueId().toString()));

            GroupSnapshot group = new GroupSnapshot("staff", OptionalInt.of(100),
                    set(PermissionNode.builder().permission("a").contexts(nether).build()),
                    Collections.<OptionNode>emptySet(), Collections.<ParentNode>emptySet());
            store.putGroup(group);

            GroupSnapshot replaced = new GroupSnapshot("staff", OptionalInt.empty(),
                    set(PermissionNode.builder().permission("b").build()),
                    Collections.<OptionNode>emptySet(), Collections.<ParentNode>emptySet());
            store.putGroup(replaced);
            assertEquals(replaced, store.getGroup("staff").get());

            LadderSnapshot ladder = new LadderSnapshot("staff", Arrays.asList("helper", "moderator", "admin"));
            store.putLadder(ladder);
            assertEquals(ladder, store.getLadder("staff").get());

            assertTrue(store.removeUser(user.uniqueId()));
            assertFalse(store.getUser(user.uniqueId()).isPresent());
            assertTrue(store.removeGroup("staff"));
            assertTrue(store.removeLadder("staff"));
        } finally {
            store.close();
        }

        // Reopening must be idempotent (MySQL has no CREATE INDEX IF NOT EXISTS).
        JdbcDataStore reopened = store(container, driver, dialect, DdlGeneration.UPDATE);
        reopened.open();
        reopened.close();

        JdbcDataStore validating = store(container, driver, dialect, DdlGeneration.VALIDATE);
        validating.open();
        validating.close();
    }

    @SafeVarargs
    private static <T> Set<T> set(T... values) {
        return new HashSet<>(Arrays.asList(values));
    }
}
