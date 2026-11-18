package dev.rono.permissions.core;

import lombok.var;

import cloud.commandframework.CommandManager;
import dev.rono.permissions.core.platform.Platform;
import dev.rono.permissions.core.platform.PlatformConfiguration;
import dev.rono.permissions.core.platform.PlatformLogger;
import dev.rono.permissions.core.platform.PlatformScheduler;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

/** A started PermissionsExPlus runtime with in-memory storage, for tests. */
public final class TestRuntime {
    private TestRuntime() {}

    public static PexApiImpl<Object> start(Path directory) throws IOException {
        java.nio.file.Files.createDirectories(directory);
        TestFiles.writeString(directory.resolve("config.yml"), "default-group: default\n");
        TestFiles.writeString(directory.resolve("advanced.yml"), "commands:\n  register-base-commands: false\nadvanced-version: 1\n");
        TestFiles.writeString(directory.resolve("database.yml"), "type: memory\n"
                + "local:\n  filename: permissions\n"
                + "credentials:\n  host: localhost\n  database: permissions\n  username: test\n"
                + "pool:\n  maximum-pool-size: 2\n  minimum-idle: 0\n"
                + "data-version: 1\n");

        var core = new PexApiImpl<>(new TestPlatform(directory));
        core.start();
        return core;
    }

    private static final class TestPlatform implements Platform<Object> {
        private final Path directory;

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
                    throw new AssertionError(message, error);
                }
            };
        }

        @Override
        public PlatformScheduler scheduler() {
            return new PlatformScheduler() {
                private final AtomicInteger ids = new AtomicInteger();

                @Override
                public void execute(Runnable task) {
                    task.run();
                }

                @Override
                public void executeAsync(Runnable task) {
                    task.run();
                }

                @Override
                public void executeLater(Runnable task, Duration delay) {}

                @Override
                public void executeLaterAsync(Runnable task, Duration delay) {}

                @Override
                public void executeRepeating(Runnable task, Duration interval) {}

                @Override
                public void executeRepeatingAsync(Runnable task, Duration interval) {}

                @Override
                public int scheduleRepeating(Runnable task, Duration interval) {
                    return ids.incrementAndGet();
                }

                @Override
                public boolean isMainThread() {
                    return true;
                }

                @Override
                public void cancelTask(int taskId) {}

                @Override
                public void cancelTasks() {}
            };
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
                    throw new AssertionError("Unexpected resource request: " + resource);
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
}
