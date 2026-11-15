package dev.rono.permissions.core.threading;

import lombok.var;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import dev.rono.permissions.core.event.EventBusImpl;
import dev.rono.permissions.core.manager.GroupManagerImpl;
import dev.rono.permissions.core.store.DataStore;
import dev.rono.permissions.core.store.MemoryDataStore;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class CoreTaskExecutorTest {
    @Test
    void fixedPoolNeverExceedsConfiguredConcurrency() throws Exception {
        var executor = new CoreTaskExecutor(2);
        var entered = new CountDownLatch(2);
        var release = new CountDownLatch(1);
        var active = new AtomicInteger();
        var maximum = new AtomicInteger();
        var futures = new ArrayList<CompletionStage<String>>();

        for (var index = 0; index < 8; index++) {
            futures.add(executor.submit(() -> {
                var current = active.incrementAndGet();

                maximum.accumulateAndGet(current, Math::max);
                entered.countDown();

                try {
                    assertTrue(release.await(5, TimeUnit.SECONDS));

                    return Thread.currentThread().getName();
                } finally {
                    active.decrementAndGet();
                }
            }));
        }

        assertTrue(entered.await(5, TimeUnit.SECONDS));
        assertEquals(2, maximum.get());

        release.countDown();

        for (var future : futures) {
            assertTrue(future.toCompletableFuture().join().startsWith("permissionsexplus-worker-"));
        }

        executor.close();

        assertTrue(executor.isShutdown());
        assertThrows(RejectedExecutionException.class, () -> executor.execute(() -> {}));
    }

    @Test
    void submittedFailuresCompleteTheirStagesExceptionally() {
        try (var executor = new CoreTaskExecutor(1)) {
            var stage = executor.submit(() -> {
                throw new IllegalStateException("broken");
            });

            var error = assertThrows(CompletionException.class, () -> stage.toCompletableFuture().join());

            assertInstanceOf(IllegalStateException.class, error.getCause());
        }
    }

    @Test
    void nestedWorkRunsInlineWithoutDeadlockingASingleWorkerPool() {
        try (var executor = new CoreTaskExecutor(1)) {
            var result = executor.submit(() -> executor.submit(() -> "complete").toCompletableFuture().join())
                    .toCompletableFuture().join();

            assertEquals("complete", result);
        }
    }

    @Test
    void managerPersistenceRunsOnTheManagedWorker() {
        try (var executor = new CoreTaskExecutor(1)) {
            var store = new RecordingStore();
            store.open();

            var manager = new GroupManagerImpl(store, new EventBusImpl(error -> fail(error)), 10, executor::execute);
            manager.create("staff").toCompletableFuture().join();

            assertTrue(store.writeThread.startsWith("permissionsexplus-worker-"));
        }
    }

    @Test
    void rejectsInvalidPoolSizes() {
        assertThrows(IllegalArgumentException.class, () -> new CoreTaskExecutor(0));
    }

    private static final class RecordingStore implements DataStore {
        private final MemoryDataStore delegate = new MemoryDataStore();
        private String writeThread;

        @Override
        public void open() {
            delegate.open();
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
            writeThread = Thread.currentThread().getName();
            delegate.put(category, key, payload);
        }

        @Override
        public boolean remove(String category, String key) {
            return delegate.remove(category, key);
        }

        @Override
        public String name() {
            return delegate.name();
        }

        @Override
        public boolean persistent() {
            return delegate.persistent();
        }

        @Override
        public void close() {
            delegate.close();
        }
    }
}
