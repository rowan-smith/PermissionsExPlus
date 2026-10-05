package ru.tehkode.permissions.backends.data;

import static org.junit.jupiter.api.Assertions.fail;
import cloud.commandframework.CommandManager;
import dev.rono.permissions.api.PexRegistration;
import dev.rono.permissions.core.PexApiImpl;
import dev.rono.permissions.core.platform.Platform;
import dev.rono.permissions.core.platform.PlatformConfiguration;
import dev.rono.permissions.core.platform.PlatformLogger;
import dev.rono.permissions.core.platform.PlatformScheduler;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Boots a real in-memory PermissionsExPlus runtime for ApiAdapter data-bridge tests.
 */
public final class PlusTestFixture implements AutoCloseable {
    private final PexApiImpl<?> api;

    public PlusTestFixture(Path directory) throws IOException {
        Files.writeString(directory.resolve("config.yml"), "default-group: default\n");
        Files.writeString(directory.resolve("advanced.yml"), """
                cache:
                  preload-on-join: false
                  cache-failure-fallback: deny
                threading:
                  worker-pool-size: 2
                commands:
                  register-base-commands: false
                temporary-permissions:
                  expiry-check-interval: 30
                  log-expiry: false
                  log-expiry-mode: total
                advanced-version: 1
                """);
        Files.writeString(directory.resolve("database.yml"), """
                type: memory
                local:
                  filename: permissions
                credentials:
                  host: localhost
                  database: permissions
                  username: test
                pool:
                  maximum-pool-size: 2
                  minimum-idle: 0
                data-version: 1
                """);

        api = new PexApiImpl<>(new TestPlatform(directory));
        api.start();
        PexRegistration.register(api);
    }

    public PexApiImpl<?> api() {
        return api;
    }

    @Override
    public void close() {
        PexRegistration.unregister(api);
        api.stop();
    }

    private static final class TestPlatform implements Platform<Object> {
        private final Path directory;
        private final TestScheduler scheduler = new TestScheduler();

        private TestPlatform(Path directory) {
            this.directory = directory;
        }

        @Override
        public PlatformLogger logger() {
            return new PlatformLogger() {
                @Override
                public void info(String message) {}

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
            return null;
        }
    }

    private static final class TestScheduler implements PlatformScheduler {
        private final AtomicInteger taskIds = new AtomicInteger();

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
            return taskIds.incrementAndGet();
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
