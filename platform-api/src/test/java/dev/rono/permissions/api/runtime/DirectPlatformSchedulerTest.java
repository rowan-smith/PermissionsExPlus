package dev.rono.permissions.api.runtime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class DirectPlatformSchedulerTest {

    private final DirectPlatformScheduler scheduler = DirectPlatformScheduler.INSTANCE;

    @AfterEach
    void tearDown() {
        scheduler.shutdown();
    }

    @Test
    void runSyncExecutesInline() {
        var ran = new AtomicBoolean(false);
        scheduler.runSync(() -> ran.set(true));
        assertTrue(ran.get());
    }

    @Test
    void runAsyncEventuallyExecutes() throws InterruptedException {
        var latch = new CountDownLatch(1);
        scheduler.runAsync(latch::countDown);
        assertTrue(latch.await(2, TimeUnit.SECONDS));
    }

    @Test
    void shutdownRecreatesExecutor() throws InterruptedException {
        scheduler.shutdown();
        var latch = new CountDownLatch(1);
        scheduler.runAsync(latch::countDown);
        assertTrue(latch.await(2, TimeUnit.SECONDS));
    }
}
