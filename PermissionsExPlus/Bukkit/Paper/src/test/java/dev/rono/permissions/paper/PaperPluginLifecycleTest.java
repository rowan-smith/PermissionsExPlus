package dev.rono.permissions.paper;

import dev.rono.permissions.api.PexProvider;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class PaperPluginLifecycleTest {
    @BeforeEach
    void setUp() {
        MockBukkit.mock();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();

        assertFalse(PexProvider.available());
    }

    @Test
    void registersAndUnregistersThePublicApiAcrossThePluginLifecycle() throws Exception {
        var plugin = loadPlugin();

        assertTrue(plugin.isEnabled());

        assertTrue(PexProvider.available());
    }

    @Test
    void platformEventsMaintainAndReleaseCachedPlayerState() throws Exception {
        loadPlugin();

        var server = MockBukkit.getMock();
        assertNotNull(server);
        var nether = server.addSimpleWorld("nether");
        var player = server.addPlayer("alex");

        assertFalse(player.isOp());
        assertTrue(Objects.requireNonNull(server.getCommandMap().getCommand("pex")).testPermissionSilent(player));

        player.setOp(true);

        assertTrue(server.getCommandTabComplete(player, "pex ").contains("user"));

        var contexts = PexProvider.get().contexts();

        assertTrue(PexProvider.get().users().cache().isCached(player.getUniqueId()));
        assertTrue(contexts.contexts(player.getUniqueId()).contains("world", player.getWorld().getName()));

        player.setGameMode(GameMode.CREATIVE);

        assertTrue(contexts.contexts(player.getUniqueId()).contains("gamemode", "creative"));

        player.teleport(new Location(nether, 0, 64, 0));

        assertTrue(contexts.contexts(player.getUniqueId()).contains("world", "nether"));

        player.disconnect();

        assertTrue(PexProvider.get().users().cache().isCached(player.getUniqueId()));
        assertTrue(contexts.contexts(player.getUniqueId()).values("world").isEmpty());
        assertTrue(contexts.contexts(player.getUniqueId()).values("gamemode").isEmpty());
    }

    private static PaperPlugin loadPlugin() throws Exception {
        var plugin = MockBukkit.load(PaperPlugin.class);

        assertNotNull(MockBukkit.getMock());
        assertNotNull(MockBukkit.getMock().getCommandMap().getCommand("pex"),
                "Command must be registered synchronously during plugin enable");

        var deadline = System.nanoTime() + java.time.Duration.ofSeconds(15).toNanos();

        while (!PexProvider.available() && plugin.isEnabled() && System.nanoTime() < deadline) {
            MockBukkit.getMock().getScheduler().performOneTick();

            Thread.sleep(10);
        }

        assertTrue(PexProvider.available(), "API did not become ready before the timeout");

        return plugin;
    }
}
