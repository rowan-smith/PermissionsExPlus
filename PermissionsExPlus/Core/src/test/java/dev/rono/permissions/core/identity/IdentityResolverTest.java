package dev.rono.permissions.core.identity;

import lombok.var;

import static org.junit.jupiter.api.Assertions.assertEquals;
import com.sun.net.httpserver.HttpServer;
import dev.rono.permissions.core.event.EventBusImpl;
import dev.rono.permissions.core.logger.AuditLogger;
import dev.rono.permissions.core.logger.DebugLogger;
import dev.rono.permissions.core.logger.LoggerManagerImpl;
import dev.rono.permissions.core.manager.UserManagerImpl;
import dev.rono.permissions.core.platform.PlatformConfiguration;
import dev.rono.permissions.core.platform.PlatformLogger;
import dev.rono.permissions.core.platform.PlatformScheduler;
import dev.rono.permissions.core.store.MemoryDataStore;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import java.nio.file.Paths;

class IdentityResolverTest {
    @Test
    void offlineAndMixedModesRetainTheirV1Semantics() {
        var users = users();
        var offline = new AtomicReference<Optional<UUID>>();

        new IdentityResolver(users, new DirectScheduler(), "offline", logger()).resolveAsync("Alex", offline::set);

        assertEquals(Optional.of(UUID.nameUUIDFromBytes("OfflinePlayer:Alex".getBytes(StandardCharsets.UTF_8))), offline.get());

        var stored = users.create(UUID.randomUUID(), "Rono").toCompletableFuture().join();
        var mixed = new AtomicReference<Optional<UUID>>();

        new IdentityResolver(users, new DirectScheduler(), "mix_mode", logger()).resolveAsync("RONO", mixed::set);

        assertEquals(Optional.of(stored.uniqueId()), mixed.get());
    }

    @Test
    void onlineModeParsesMojangIdsAndRejectsBadResponses() throws Exception {
        var server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);

        server.createContext("/valid", exchange -> respond(exchange, 200, "{\"id\":\"0123456789abcdef0123456789abcdef\"}"));
        server.createContext("/missing", exchange -> respond(exchange, 404, "missing"));

        server.start();

        try {
            var endpoint = URI.create("http://localhost:" + server.getAddress().getPort() + "/");

            assertEquals(Optional.of(UUID.fromString("01234567-89ab-cdef-0123-456789abcdef")), online(endpoint, "valid"));
            assertEquals(Optional.empty(), online(endpoint, "missing"));
        } finally {
            server.stop(0);
        }
    }

    private static Optional<UUID> online(URI endpoint, String name) {
        var result = new AtomicReference<Optional<UUID>>();

        new IdentityResolver(users(), new DirectScheduler(), "online_only", logger(), endpoint).resolveAsync(name, result::set);

        return result.get();
    }

    private static UserManagerImpl users() {
        var store = new MemoryDataStore();

        store.open();

        return new UserManagerImpl(store, new EventBusImpl(error -> {
            throw new AssertionError(error);
        }));
    }

    private static void respond(com.sun.net.httpserver.HttpExchange exchange, int status, String body) throws IOException {
        var bytes = body.getBytes(StandardCharsets.UTF_8);

        exchange.sendResponseHeaders(status, bytes.length);

        try (var output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }

    private static LoggerManagerImpl logger() {
        var platform = new SilentDebugLogger();
        var config = new PlatformConfiguration() {
            @Override
            public Path dataDirectory() {
                return Paths.get("");
            }

            @Override
            public void saveResource(String resource, boolean replace) {}
        };

        var debug = new DebugLogger(platform, () -> false);
        var audit = new AuditLogger(config, new DirectScheduler(), platform);

        return new LoggerManagerImpl(platform, debug, audit);
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

    private static final class SilentDebugLogger implements PlatformLogger {
        @Override
        public void info(String message) {}

        @Override
        public void warn(String message) {}

        @Override
        public void error(String message, Throwable error) {}
    }
}
