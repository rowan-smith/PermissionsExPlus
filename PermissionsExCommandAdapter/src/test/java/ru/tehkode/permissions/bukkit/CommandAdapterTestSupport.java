package ru.tehkode.permissions.bukkit;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import cloud.commandframework.CommandManager;
import dev.rono.permissions.api.PexRegistration;
import dev.rono.permissions.api.context.ContextSet;
import dev.rono.permissions.api.user.User;
import dev.rono.permissions.core.PexApiImpl;
import dev.rono.permissions.core.platform.Platform;
import dev.rono.permissions.core.platform.PlatformConfiguration;
import dev.rono.permissions.core.platform.PlatformLogger;
import dev.rono.permissions.core.platform.PlatformScheduler;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicInteger;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.command.ConsoleCommandSenderMock;

/**
 * Boots an in-memory PermissionsExPlus runtime, registers it with
 * {@link PexRegistration}, then loads the CommandAdapter plugin under MockBukkit
 * so legacy {@code /pex} commands exercise the real handler path.
 */
abstract class CommandAdapterTestSupport {
    @TempDir
    Path directory;

    protected ServerMock server;
    protected PexApiImpl<Object> core;
    protected PermissionsEx adapter;
    protected ConsoleCommandSenderMock console;
    protected List<String> lastMessages = List.of();

    @BeforeEach
    void setUpHarness() throws IOException {
        writeConfiguration(directory);
        server = MockBukkit.mock();
        MockBukkit.createMockPlugin("PermissionsExPlus");

        core = new PexApiImpl<>(new TestPlatform(directory));
        core.start();
        PexRegistration.register(core);

        adapter = MockBukkit.load(PermissionsEx.class);
        assertTrue(adapter.isEnabled(), "CommandAdapter failed to enable");
        assertNotNull(adapter.getCommandsManager());

        console = server.getConsoleSender();
        drain(console);
        lastMessages = List.of();
    }

    @AfterEach
    void tearDownHarness() {
        try {
            if (adapter != null && adapter.isEnabled()) {
                server.getPluginManager().disablePlugin(adapter);
            }
        } finally {
            MockBukkit.unmock();

            if (core != null) {
                PexRegistration.unregister(core);
                core.stop();
            }
        }
    }

    protected boolean run(String commandLine) {
        drain(console);
        boolean result = server.dispatchCommand(console, commandLine);
        lastMessages = captureMessages();
        return result;
    }

    protected boolean run(CommandSender sender, String commandLine) {
        return server.dispatchCommand(sender, commandLine);
    }

    protected void drain(ConsoleCommandSenderMock sender) {
        while (sender.nextMessage() != null) {
            // discard startup noise
        }
    }

    protected List<String> captureMessages() {
        java.util.ArrayList<String> collected = new java.util.ArrayList<>();
        String message;

        while ((message = console.nextMessage()) != null) {
            collected.add(ChatColor.stripColor(message));
        }

        return List.copyOf(collected);
    }

    protected List<String> messages() {
        return lastMessages;
    }

    protected String joinedMessages() {
        return String.join("\n", lastMessages);
    }

    protected void assertMessageContains(String expected) {
        String joined = joinedMessages();
        assertTrue(joined.contains(expected), () -> "Expected message containing '" + expected + "' but got:\n" + joined);
    }

    protected void assertExactMessages(String... expected) {
        org.junit.jupiter.api.Assertions.assertEquals(List.of(expected), lastMessages,
                () -> "Unexpected command output:\n" + joinedMessages());
    }

    protected void assertExactMessage(String expected) {
        assertExactMessages(expected);
    }

    protected void assertMessagesStartWith(String... expectedPrefix) {
        List<String> expected = List.of(expectedPrefix);
        org.junit.jupiter.api.Assertions.assertTrue(
                lastMessages.size() >= expected.size(),
                () -> "Expected at least " + expected.size() + " messages but got:\n" + joinedMessages());

        for (int index = 0; index < expected.size(); index++) {
            String actual = lastMessages.get(index);
            String prefix = expected.get(index);
            org.junit.jupiter.api.Assertions.assertTrue(
                    actual.startsWith(prefix),
                    () -> "Expected message starting with '" + prefix + "' but got:\n" + joinedMessages());
        }
    }

    protected User createUser(String name) {
        UUID id = UUID.nameUUIDFromBytes(("offline:" + name.toLowerCase()).getBytes());
        return await(core.users().create(id, name));
    }

    protected User requireUser(String name) {
        return await(core.users().find(name)).orElseThrow();
    }

    protected static ContextSet world(String name) {
        return ContextSet.builder().add("world", name).build();
    }

    protected static <T> T await(CompletionStage<T> stage) {
        return stage.toCompletableFuture().join();
    }

    private static void writeConfiguration(Path directory) throws IOException {
        Files.writeString(directory.resolve("config.yml"), """
                default-group: default
                """);

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
            fail("Cloud command manager should not be created when register-base-commands is false");
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
