package dev.rono.permissions.core.store.jdbc;

import lombok.var;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import dev.rono.permissions.api.permission.PermissionNode;
import dev.rono.permissions.core.config.DatabasePool;
import dev.rono.permissions.core.config.DdlGeneration;
import dev.rono.permissions.core.event.EventBusImpl;
import dev.rono.permissions.core.manager.UserManagerImpl;
import dev.rono.permissions.core.model.UserSnapshot;
import dev.rono.permissions.core.store.StorageConflictException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import dev.rono.permissions.core.util.Immutables;

/** Two stores on one database stand in for two servers sharing MySQL. */
class JdbcConcurrencyTest {
    private static final DatabasePool POOL = new DatabasePool(2, 1, 5_000L, 30_000L);

    private final String url = "jdbc:h2:mem:pex-concurrency-" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1";
    private final JdbcDataStore first = store();
    private final JdbcDataStore second = store();

    private JdbcDataStore store() {
        var store = new JdbcDataStore("H2", url, "org.h2.Driver", null, null, POOL, DdlGeneration.UPDATE, JdbcDialect.H2, false);
        store.open();
        return store;
    }

    @AfterEach
    void close() {
        first.close();
        second.close();
    }

    @Test
    void savesIncrementTheRevision() {
        var id = UUID.randomUUID();

        first.putUser(new UserSnapshot(id, "Alex", Immutables.setOf(), Immutables.setOf(), Immutables.setOf()));
        assertEquals(1, first.getUser(id).get().revision());

        first.putUser(first.getUser(id).get());
        assertEquals(2, first.getUser(id).get().revision());
    }

    @Test
    void savingAStaleSnapshotIsRejected() {
        var id = UUID.randomUUID();
        first.putUser(new UserSnapshot(id, "Alex", Immutables.setOf(), Immutables.setOf(), Immutables.setOf()));

        var readByFirst = first.getUser(id).get();
        var readBySecond = second.getUser(id).get();

        second.putUser(withPermission(readBySecond, "second.change"));

        assertThrows(StorageConflictException.class, () -> first.putUser(withPermission(readByFirst, "first.change")));
        assertTrue(first.getUser(id).get().explicitPermissions().stream().anyMatch(node -> node.permission().equals("second.change")));
    }

    @Test
    void failedTransactionsLeaveNothingBehind() {
        var id = UUID.randomUUID();

        assertThrows(IllegalStateException.class, () -> first.transaction(() -> {
            first.putUser(new UserSnapshot(id, "Alex", Immutables.setOf(), Immutables.setOf(), Immutables.setOf()));
            throw new IllegalStateException("fails after the write");
        }));

        assertFalse(second.getUser(id).isPresent());
    }

    @Test
    void concurrentEditsOnTwoServersBothSurvive() {
        var id = UUID.randomUUID();
        var firstServer = new UserManagerImpl(first, new EventBusImpl(error -> {}));
        var secondServer = new UserManagerImpl(second, new EventBusImpl(error -> {}));
        firstServer.create(id, "Alex").toCompletableFuture().join();

        var raced = new AtomicBoolean();

        // While the first server is part-way through its edit, the second one
        // saves a different edit to the same user.
        firstServer.modify(id, modifier -> {
            if (raced.compareAndSet(false, true)) {
                CompletableFuture.runAsync(() -> secondServer.modify(id, other -> other.allowPermission("second.change")).toCompletableFuture().join()).join();
            }

            modifier.allowPermission("first.change");
        }).toCompletableFuture().join();

        var stored = first.getUser(id).get().explicitPermissions();

        assertTrue(stored.stream().anyMatch(node -> node.permission().equals("first.change")));
        assertTrue(stored.stream().anyMatch(node -> node.permission().equals("second.change")));
    }

    private static UserSnapshot withPermission(UserSnapshot user, String permission) {
        return new UserSnapshot(user.uniqueId(), user.name(), Immutables.setOf(PermissionNode.builder().permission(permission).build()), user.explicitOptions(), user.groups(), user.revision());
    }
}
