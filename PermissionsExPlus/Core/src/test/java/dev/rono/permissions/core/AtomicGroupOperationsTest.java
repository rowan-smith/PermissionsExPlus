package dev.rono.permissions.core;

import lombok.var;

import static dev.rono.permissions.core.RuntimeFixture.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import dev.rono.permissions.api.event.group.GroupDeletedEvent;
import dev.rono.permissions.api.event.group.GroupModifiedEvent;
import dev.rono.permissions.api.event.user.UserModifiedEvent;
import dev.rono.permissions.api.parent.ParentNode;
import dev.rono.permissions.core.store.DataStore;
import dev.rono.permissions.core.store.MemoryDataStore;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionException;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

/** A group rename or delete either changes every reference or none. */
class AtomicGroupOperationsTest {
    private final FailingStore store = new FailingStore();
    private final RuntimeFixture runtime = new RuntimeFixture(store);
    private final UUID first = UUID.randomUUID();
    private final UUID second = UUID.randomUUID();
    private final ArrayList<Object> events = new ArrayList<>();

    AtomicGroupOperationsTest() {
        await(runtime.groups.create("vip"));
        await(runtime.groups.create("child"));
        await(runtime.groups.modify("child", modifier -> modifier.addParent(ParentNode.builder().group("vip").build())));
        await(runtime.users.create(first, "First"));
        await(runtime.users.create(second, "Second"));
        await(runtime.users.modify(first, modifier -> modifier.addGroup("vip")));
        await(runtime.users.modify(second, modifier -> modifier.addGroup("vip")));

        runtime.events.subscribe(GroupModifiedEvent.class, events::add);
        runtime.events.subscribe(GroupDeletedEvent.class, events::add);
        runtime.events.subscribe(UserModifiedEvent.class, events::add);
    }

    @Test
    void renameThatFailsPartWayChangesNothing() {
        store.failOnUser = second.toString();

        assertThrows(CompletionException.class, () -> await(runtime.groups.rename("vip", "gold")));

        assertTrue(store.getGroup("vip").isPresent());
        assertFalse(store.getGroup("gold").isPresent());
        assertEquals("vip", store.getGroup("child").get().parents().iterator().next().group());
        assertEquals("vip", store.getUser(first).get().groups().iterator().next().group());

        assertTrue(runtime.groups.cache().get("vip").isPresent());
        assertFalse(runtime.groups.cache().get("gold").isPresent());
        assertEquals("vip", runtime.users.cache().get(first).get().groups().iterator().next().group());
        assertEquals(0, events.size());
    }

    @Test
    void renameMovesEveryReference() {
        await(runtime.groups.rename("vip", "gold"));

        assertFalse(store.getGroup("vip").isPresent());
        assertEquals("gold", store.getGroup("child").get().parents().iterator().next().group());
        assertEquals("gold", store.getUser(second).get().groups().iterator().next().group());
    }

    @Test
    void deleteThatFailsPartWayChangesNothing() {
        store.failOnUser = second.toString();

        assertThrows(CompletionException.class, () -> await(runtime.groups.delete("vip")));

        assertTrue(store.getGroup("vip").isPresent());
        assertEquals(1, store.getGroup("child").get().parents().size());
        assertEquals(1, store.getUser(first).get().groups().size());
        assertTrue(runtime.groups.cache().get("vip").isPresent());
        assertEquals(0, events.size());
    }

    /** Memory storage that fails to save one chosen user. */
    private static final class FailingStore extends DelegatingStore {
        volatile String failOnUser;

        @Override
        public void put(String category, String key, String payload) {
            if (category.equals("users") && key.equals(failOnUser)) {
                throw new IllegalStateException("Simulated storage failure");
            }

            super.put(category, key, payload);
        }
    }

    /** Wraps the memory store so a test can intercept its writes. */
    private static class DelegatingStore implements DataStore {
        private final MemoryDataStore delegate = new MemoryDataStore();

        @Override
        public void open() {
            delegate.open();
        }

        @Override
        public <T> T transaction(Supplier<T> work) {
            return delegate.transaction(work);
        }

        @Override
        public Optional<String> get(String category, String key) {
            return delegate.get(category, key);
        }

        @Override
        public Map<String, String> all(String category) {
            return delegate.all(category);
        }

        @Override
        public void put(String category, String key, String payload) {
            delegate.put(category, key, payload);
        }

        @Override
        public boolean remove(String category, String key) {
            return delegate.remove(category, key);
        }

        @Override
        public String name() {
            return "Failing memory";
        }

        @Override
        public boolean persistent() {
            return false;
        }

        @Override
        public void close() {}
    }
}
