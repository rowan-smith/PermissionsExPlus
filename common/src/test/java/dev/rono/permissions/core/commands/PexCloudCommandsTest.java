package dev.rono.permissions.core.commands;

import dev.rono.permissions.core.config.CommandFramework;
import org.junit.jupiter.api.Test;
import ru.tehkode.permissions.PEXTestBase;

import static org.junit.jupiter.api.Assertions.*;

class PexCloudCommandsTest extends PEXTestBase {

    @Test
    void installReturnsCommandServiceBoundToManager() {
        PexCloudCommandTestSupport.TestHarness harness =
                PexCloudCommandTestSupport.install(manager, CommandFramework.MODERN, CoreCloudPlatform.GAME_SERVER);

        assertSame(manager, harness.permissionManager());
        assertNotNull(harness.service());
    }

    @Test
    void installRegistersModernRootCommand() {
        PexCloudCommandTestSupport.TestHarness harness =
                PexCloudCommandTestSupport.install(manager, CommandFramework.MODERN, CoreCloudPlatform.GAME_SERVER);

        assertEquals("PEX help text", harness.execute("pex").getFirst());
    }

    @Test
    void installRegistersClassicRootCommand() {
        PexCloudCommandTestSupport.TestHarness harness =
                PexCloudCommandTestSupport.install(manager, CommandFramework.CLASSIC, CoreCloudPlatform.GAME_SERVER);

        assertEquals("PEX help text", harness.execute("pex").getFirst());
    }

    @Test
    void installWithImportBridgeStillRegistersCommands() {
        PexCloudCommandTestSupport.TestCommandManager cloudManager =
                new PexCloudCommandTestSupport.TestCommandManager();
        PexCloudCommandTestSupport.RecordingSenderAdapter adapter =
                new PexCloudCommandTestSupport.RecordingSenderAdapter();
        CoreCommandService service = PexCloudCommands.install(new PexCloudCommands.InstallRequest<>(
                        cloudManager,
                        String.class,
                        manager,
                        adapter,
                        () -> {},
                        new PexCloudCommandTestSupport.InMemoryConfigBridge(),
                        force -> "ok",
                        CoreCloudPlatform.PROXY,
                        CommandFramework.CLASSIC)
                .withImportBridge(new CoreCommandService.ImportBridge() {
                    @Override
                    public boolean supports(String source) {
                        return true;
                    }

                    @Override
                    public String importIntoPex(String source) {
                        return "imported:" + source;
                    }
                }));

        assertNotNull(service);
        cloudManager.executeCommand("console", "pex help").join();
        assertEquals("PEX help text", adapter.messages.getFirst());
    }
}
