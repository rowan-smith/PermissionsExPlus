package dev.rono.permissions.core;

import lombok.var;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import cloud.commandframework.CommandManager;
import dev.rono.permissions.api.permission.PermissionNode;
import dev.rono.permissions.api.permission.PermissionResult;
import dev.rono.permissions.core.platform.Platform;
import dev.rono.permissions.core.platform.PlatformConfiguration;
import dev.rono.permissions.core.platform.PlatformLogger;
import dev.rono.permissions.core.platform.PlatformScheduler;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CoreRuntimeControlsTest {
    @TempDir
    Path directory;

    @Test
    void disabledCommandsLeaveTheApiAndFallbackEvaluationAvailable() throws IOException {
        writeConfiguration(false, false, "allow", 2);

        var platform = new TestPlatform(directory);
        var core = new PexApiImpl<>(platform);
        core.start();

        assertEquals(0, platform.commandManagerCreations.get());
        assertFalse(core.config().preloadOnJoin());

        core.groups().modify("default", modifier -> {
            modifier.allowPermission("example.use");

            modifier.denyPermission("example.blocked");
        }).toCompletableFuture().join();

        var unknown = UUID.randomUUID();

        assertEquals(PermissionResult.ALLOW, core.cacheFailureFallback(unknown, "example.use"));
        assertEquals(PermissionResult.DENY, core.cacheFailureFallback(unknown, "example.blocked"));
        assertEquals(PermissionResult.DENY, core.cacheFailureFallback(unknown, "example.undefined"));

        core.stop();
    }

    @Test
    void denyFallbackNeverUsesDefaultGroupGrants() throws IOException {
        writeConfiguration(false, true, "deny", 1);

        var core = new PexApiImpl<>(new TestPlatform(directory));
        core.start();

        core.groups().modify("default", modifier -> modifier.allowPermission("example.use")).toCompletableFuture().join();

        assertEquals(PermissionResult.DENY, core.cacheFailureFallback(UUID.randomUUID(), "example.use"));

        core.stop();
    }

    @Test
    void managedPreloadCreatesAndCachesTheProfile() throws IOException {
        writeConfiguration(false, true, "deny", 2);

        var core = new PexApiImpl<>(new TestPlatform(directory));
        core.start();

        var id = UUID.randomUUID();
        var user = core.users().loadOrCreateUser(id, "Alex").toCompletableFuture().join();

        assertEquals(id, user.uniqueId());
        assertTrue(core.users().cache().isCached(id));
        assertEquals(id, core.users().loadOrCreateUser(id, "Renamed").toCompletableFuture().join().uniqueId());
        assertEquals("Alex", core.users().cache().get(id).get().name());

        core.stop();
    }

    @Test
    void totalExpiryLoggingReportsTheRemovedNodeCount() throws IOException {
        writeConfiguration(false, true, "deny", 2, true, "total");

        var platform = new TestPlatform(directory);
        var core = new PexApiImpl<>(platform);

        core.start();

        var id = UUID.randomUUID();

        core.users().create(id, "Alex").toCompletableFuture().join();
        core.users().modify(id, modifier -> modifier.setPermission(PermissionNode.builder()
                .permission("example.expired").expiry(Instant.now().minusSeconds(1)).build())).toCompletableFuture()
                .join();

        platform.scheduler.runMaintenance();

        core.stop();

        assertTrue(platform.information.contains("Cleaned up 1 expired node across the network"));
    }

    @Test
    void individualExpiryLoggingNamesTheNodeAndUser() throws IOException {
        writeConfiguration(false, true, "deny", 2, true, "individual");

        var platform = new TestPlatform(directory);
        var core = new PexApiImpl<>(platform);

        core.start();

        var id = UUID.randomUUID();

        core.users().create(id, "Alex").toCompletableFuture().join();
        core.users().modify(id, modifier -> modifier.setPermission(PermissionNode.builder()
                .permission("example.expired").expiry(Instant.now().minusSeconds(1)).build())).toCompletableFuture()
                .join();

        platform.scheduler.runMaintenance();

        core.stop();

        assertTrue(platform.information.stream().anyMatch(message -> message.equals("Removed expired permission " + "'example.expired' from user Alex (" + id + ")")));
    }

    private void writeConfiguration(boolean commands, boolean preload, String fallback, int workers) throws IOException {
        writeConfiguration(commands, preload, fallback, workers, false, "total");
    }

    private void writeConfiguration(boolean commands, boolean preload, String fallback, int workers, boolean logExpiry, String expiryMode) throws IOException {
        TestFiles.writeString(directory.resolve("config.yml"), "default-group: default\n");

        TestFiles.writeString(directory.resolve("advanced.yml"), String.format(("cache:\n"
                + "  preload-on-join: %s\n"
                + "  cache-failure-fallback: %s\n"
                + "threading:\n"
                + "  worker-pool-size: %d\n"
                + "commands:\n"
                + "  register-base-commands: %s\n"
                + "temporary-permissions:\n"
                + "  expiry-check-interval: 30\n"
                + "  log-expiry: %s\n"
                + "  log-expiry-mode: %s\n"
                + "advanced-version: 1\n"), preload, fallback, workers, commands, logExpiry, expiryMode));

        TestFiles.writeString(directory.resolve("database.yml"), ("type: memory\n"
                + "local:\n"
                + "  filename: permissions\n"
                + "credentials:\n"
                + "  host: localhost\n"
                + "  database: permissions\n"
                + "  username: test\n"
                + "pool:\n"
                + "  maximum-pool-size: 2\n"
                + "  minimum-idle: 0\n"
                + "data-version: 1\n"));
    }

    private static final class TestPlatform implements Platform<Object> {
        private final Path directory;
        private final AtomicInteger commandManagerCreations = new AtomicInteger();
        private final TestScheduler scheduler = new TestScheduler();
        private final List<String> information = new CopyOnWriteArrayList<>();

        private TestPlatform(Path directory) {
            this.directory = directory;
        }

        @Override
        public PlatformLogger logger() {
            return new PlatformLogger() {
                @Override
                public void info(String message) {
                    information.add(message);
                }

                @Override
                public void warn(String message) {}

                @Override
                public void error(String message, Throwable error) {
                    fail(message, error);
                }
            };
        }

        @Override
        public PlatformScheduler scheduler() {
            return scheduler;
        }

        @Override
        public PlatformConfiguration configuration() {
            return new PlatformConfiguration() {
                @Override
                public Path dataDirectory() {
                    return directory;
                }

                @Override
                public void saveResource(String resource, boolean replace) {
                    fail("Unexpected resource request: " + resource);
                }
            };
        }

        @Override
        public Class<Object> senderType() {
            return Object.class;
        }

        @Override
        public void sendMessage(Object sender, String message) {}

        @Override
        public CommandManager<Object> createCommandManager() {
            commandManagerCreations.incrementAndGet();
            return null;
        }
    }

    private static final class TestScheduler implements PlatformScheduler {
        private final AtomicInteger taskIds = new AtomicInteger();
        private Runnable maintenance;

        @Override
        public void execute(Runnable task) {
            task.run();
        }

        @Override
        public void executeAsync(Runnable task) {
            throw new AssertionError("Core work must use the managed executor");
        }

        @Override
        public void executeLater(Runnable task, Duration delay) {}

        @Override
        public void executeLaterAsync(Runnable task, Duration delay) {
            throw new AssertionError("Core work must use the managed executor");
        }

        @Override
        public void executeRepeating(Runnable task, Duration interval) {}

        @Override
        public void executeRepeatingAsync(Runnable task, Duration interval) {
            throw new AssertionError("Core work must use the managed executor");
        }

        @Override
        public int scheduleRepeating(Runnable task, Duration interval) {
            if (maintenance == null) {
                maintenance = task;
            }

            return taskIds.incrementAndGet();
        }

        private void runMaintenance() {
            assertNotNull(maintenance);
            maintenance.run();
        }

        @Override
        public boolean isMainThread() {
            return true;
        }

        @Override
        public void cancelTask(int taskId) {}

        @Override
        public void cancelTasks() {}
    }
}
