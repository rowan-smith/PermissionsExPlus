package dev.rono.permissions.core.commands;

import cloud.commandframework.CommandManager;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.internal.CommandRegistrationHandler;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.meta.SimpleCommandMeta;
import dev.rono.permissions.core.config.CommandFramework;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.PermissionUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared Cloud command wiring for core command integration tests.
 */
final class PexCloudCommandTestSupport {
    private PexCloudCommandTestSupport() {}

    static TestHarness install(
            PermissionManager manager,
            CommandFramework framework,
            CoreCloudPlatform platform) {
        TestCommandManager cloudManager = new TestCommandManager();
        RecordingSenderAdapter adapter = new RecordingSenderAdapter();
        CoreCommandService service = PexCloudCommands.install(new PexCloudCommands.InstallRequest<>(
                cloudManager,
                String.class,
                manager,
                adapter,
                () -> {},
                new InMemoryConfigBridge(),
                force -> force ? "forced" : "normal",
                platform,
                framework));
        return new TestHarness(cloudManager, adapter, service, manager);
    }

    static final class TestCommandManager extends CommandManager<String> {
        TestCommandManager() {
            super(
                    CommandExecutionCoordinator.simpleCoordinator(),
                    CommandRegistrationHandler.nullCommandRegistrationHandler());
        }

        @Override
        public boolean hasPermission(String sender, String permission) {
            return true;
        }

        @Override
        public CommandMeta createDefaultCommandMeta() {
            return SimpleCommandMeta.empty();
        }
    }

    static final class RecordingSenderAdapter implements CoreCloudCommandRegistrar.SenderAdapter<String> {
        final List<String> messages = new ArrayList<>();
        String defaultWorld = "survival";

        @Override
        public void reply(String sender, String message) {
            messages.add(message);
        }

        @Override
        public String defaultWorld(String sender) {
            return defaultWorld;
        }

        @Override
        public PermissionUser actor(String sender) {
            return null;
        }

        @Override
        public String helpText() {
            return "PEX help text";
        }

        @Override
        public String pluginVersion() {
            return "test";
        }

        @Override
        public String reportText() {
            return "report-url";
        }
    }

    static final class InMemoryConfigBridge implements CoreCommandService.ConfigBridge {
        private final java.util.Map<String, Object> nodes = new java.util.LinkedHashMap<>();

        @Override
        public Object get(String path) {
            return nodes.get(path);
        }

        @Override
        public void set(String path, Object value) {
            nodes.put(path, value);
        }

        @Override
        public void save() {
        }
    }

    record TestHarness(
            TestCommandManager manager,
            RecordingSenderAdapter adapter,
            CoreCommandService service,
            PermissionManager permissionManager) {

        List<String> execute(String input) {
            adapter.messages.clear();
            manager.executeCommand("console", input).join();
            return List.copyOf(adapter.messages);
        }

        List<String> suggest(String input) {
            return manager.suggest("console", input);
        }

        void setDefaultWorld(String world) {
            adapter.defaultWorld = world;
        }
    }
}
