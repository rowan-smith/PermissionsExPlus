package dev.rono.permissions.core.store;

import lombok.var;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FlatDataStoreTest {
    @TempDir
    Path directory;

    @Test
    void writesAreOnDiskBeforeTheyReturn() {
        var store = new FlatDataStore(directory, true);
        store.open();

        store.put("groups", "vip", "payload");
        store.put("groups", "staff", "other");
        store.remove("groups", "staff");

        assertFalse(store.dirty());

        // Read back without closing or checkpointing, as after a crash.
        var reopened = new FlatDataStore(directory, true);
        reopened.open();

        assertEquals("payload", reopened.get("groups", "vip").get());
        assertFalse(reopened.get("groups", "staff").isPresent());
    }

    @Test
    void failedTransactionsLeaveMemoryAndDiskUnchanged() {
        var store = new FlatDataStore(directory, false);
        store.open();
        store.put("groups", "vip", "before");

        assertThrows(IllegalStateException.class, () -> store.transaction(() -> {
            store.put("groups", "vip", "after");
            store.put("groups", "staff", "new");
            throw new IllegalStateException("fails part-way");
        }));

        assertEquals("before", store.get("groups", "vip").get());
        assertFalse(store.get("groups", "staff").isPresent());

        var reopened = new FlatDataStore(directory, false);
        reopened.open();
        assertEquals("before", reopened.get("groups", "vip").get());
    }

    @Test
    void transactionsSaveOnceWhenTheyEnd() {
        var store = new FlatDataStore(directory, true);
        store.open();

        store.transaction(() -> {
            store.put("groups", "vip", "one");
            store.put("groups", "staff", "two");

            assertTrue(store.dirty());
            return null;
        });

        assertFalse(store.dirty());

        var reopened = new FlatDataStore(directory, true);
        reopened.open();
        assertEquals("two", reopened.get("groups", "staff").get());
    }
}
