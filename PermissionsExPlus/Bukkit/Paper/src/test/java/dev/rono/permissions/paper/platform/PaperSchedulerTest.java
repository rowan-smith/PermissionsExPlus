package dev.rono.permissions.paper.platform;

import dev.rono.permissions.bukkit.platform.BukkitTaskScheduler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.plugin.PluginMock;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaperSchedulerTest {
    private ServerMock server;

    private PluginMock plugin;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();

        plugin = MockBukkit.createMockPlugin();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void delayedExecutionConvertsMillisecondsToBukkitTicks() {
        var calls = new AtomicInteger();

        new BukkitTaskScheduler(plugin).executeLater(calls::incrementAndGet, Duration.ofMillis(100));

        server.getScheduler().performTicks(1);

        assertEquals(0, calls.get());

        server.getScheduler().performTicks(1);

        assertEquals(1, calls.get());
    }

    @Test
    void platformExposesThePluginConfigurationAndDetectsNoOptionalIntegrationsByDefault() {
        var platform = new PaperPlatform(plugin);

        assertEquals(plugin.getDataPath(), platform.configuration().dataDirectory());

        assertEquals(org.bukkit.command.CommandSender.class, platform.senderType());

        assertTrue(platform.integrations().isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Permissions updated", "§aGranted", "", "A message with spaces", "unicode: β"})
    void platformSendsMessagesToBukkitCommandSenders(String message) {
        var player = server.addPlayer("alex");

        new PaperPlatform(plugin).sendMessage(player, message);

        assertEquals(message, player.nextMessage());
    }

    @Test
    void cancellingPlatformTasksPreventsQueuedDelayedWork() {
        var calls = new AtomicInteger();

        var scheduler = new BukkitTaskScheduler(plugin);

        scheduler.executeLater(calls::incrementAndGet, Duration.ofMillis(100));

        scheduler.cancelTasks();

        server.getScheduler().performTicks(2);

        assertEquals(0, calls.get());
    }

    @Test
    void mainThreadExecutionRunsOnTheMockBukkitScheduler() {
        var calls = new AtomicInteger();

        var scheduler = new BukkitTaskScheduler(plugin);

        assertTrue(scheduler.isMainThread());

        scheduler.execute(calls::incrementAndGet);

        server.getScheduler().performOneTick();

        assertEquals(1, calls.get());
    }

    @ParameterizedTest
    @CsvSource({"50,1", "100,2", "150,3", "250,5", "500,10", "750,15", "1000,20", "1500,30"})
    void delayedExecutionUsesTheEquivalentBukkitTickCount(long milliseconds, long ticks) {
        var calls = new AtomicInteger();

        new BukkitTaskScheduler(plugin).executeLater(calls::incrementAndGet, Duration.ofMillis(milliseconds));

        if (ticks > 1) {
            server.getScheduler().performTicks(ticks - 1);
        }

        assertEquals(0, calls.get());

        server.getScheduler().performOneTick();

        assertEquals(1, calls.get());
    }

    @ParameterizedTest
    @CsvSource({"config.yml,config.yml", "nested/config.yml,nested/config.yml",
            "data/permissions.json,data/permissions.json", "audit.log,audit.log",
            "worlds/survival.yml,worlds/survival.yml"})
    void configurationResolveKeepsPathsWithinThePluginDataDirectory(String child, String expectedSuffix) {
        var resolved = new PaperPlatform(plugin).configuration().resolve(child);

        assertEquals(plugin.getDataPath().resolve(expectedSuffix), resolved);
    }
}
