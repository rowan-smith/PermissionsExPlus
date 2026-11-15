package dev.rono.permissions.core.audit;

import dev.rono.permissions.core.TestFiles;
import lombok.var;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import dev.rono.permissions.core.logger.AuditLogger;
import dev.rono.permissions.core.platform.PlatformConfiguration;
import dev.rono.permissions.core.platform.PlatformLogger;
import dev.rono.permissions.core.platform.PlatformScheduler;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AuditLoggerTest {
    @TempDir
    Path directory;

    @Test
    void appendsAuditEntriesAsynchronously() throws Exception {
        var logger = new AuditLogger(new PlatformConfiguration() {
            @Override
            public Path dataDirectory() {
                return directory;
            }

            @Override
            public void saveResource(String resource, boolean replace) {}
        }, new DirectScheduler(), new SilentLogger());

        logger.log("console", "created group 'default'");
        logger.log("console", "added permission '*'");

        var contents = TestFiles.readString(directory.resolve("audit.log"));

        assertTrue(contents.contains("Staff 'console' created group 'default'"));
        assertTrue(contents.contains("Staff 'console' added permission '*'"));
    }

    @Test
    void doesNotCreateAFileWhenAuditLoggingIsDisabled() {
        var configuration = new PlatformConfiguration() {
            @Override
            public Path dataDirectory() {
                return directory;
            }

            @Override
            public void saveResource(String resource, boolean replace) {}
        };

        new AuditLogger(configuration, new DirectScheduler(), new SilentLogger(), () -> false).log("console", "must not be written");

        assertFalse(Files.exists(directory.resolve("audit.log")));
    }

    @Test
    void broadcastsLocallyAndPublishesNetworkAuditWhenConfigured() {
        var local = new ArrayList<String>();
        var network = new ArrayList<String>();
        var logger = new AuditLogger(configuration(), new DirectScheduler(), new SilentLogger(), () -> false, () -> true, () -> true, local::add);

        logger.attachNetworkPublisher(network::add);
        logger.log("console", "promoted PlayerOne");

        assertTrue(local.get(0).contains("promoted PlayerOne"));
        assertTrue(network.get(0).contains("promoted PlayerOne"));
        assertFalse(Files.exists(directory.resolve("audit.log")));
    }

    private PlatformConfiguration configuration() {
        return new PlatformConfiguration() {
            @Override
            public Path dataDirectory() {
                return directory;
            }

            @Override
            public void saveResource(String resource, boolean replace) {}
        };
    }

    private static final class DirectScheduler implements PlatformScheduler {
        @Override
        public void execute(Runnable task) {
            task.run();
        }

        @Override
        public void executeAsync(Runnable task) {
            task.run();
        }

        @Override
        public void executeLater(Runnable task, Duration delay) {
            task.run();
        }

        @Override
        public void executeLaterAsync(Runnable task, Duration delay) {
            task.run();
        }

        @Override
        public void executeRepeating(Runnable task, Duration interval) {}

        @Override
        public void executeRepeatingAsync(Runnable task, Duration interval) {}

        @Override
        public boolean isMainThread() {
            return true;
        }

        @Override
        public void cancelTask(int taskId) {}

        @Override
        public void cancelTasks() {}
    }

    private static final class SilentLogger implements PlatformLogger {
        @Override
        public void info(String message) {}

        @Override
        public void warn(String message) {}

        @Override
        public void error(String message, Throwable error) {}
    }
}
