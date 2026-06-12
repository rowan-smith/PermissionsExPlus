package dev.rono.permissions.spigot.platform;

import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpigotPlatformBridgeTest {

    @Mock
    private JavaPlugin plugin;
    @Mock
    private Server server;
    @Mock
    private World world;
    @Mock
    private Player player;

    private SpigotPlatformBridge bridge;
    private final UUID worldId = UUID.randomUUID();
    private final UUID playerId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        when(plugin.getServer()).thenReturn(server);
        bridge = new SpigotPlatformBridge(plugin);
    }

    @Test
    void serverIdUsesFirstWorld() {
        when(server.getWorlds()).thenReturn(List.of(world));
        when(world.getUID()).thenReturn(worldId);
        assertEquals(worldId, bridge.serverId());
    }

    @Test
    void onlinePlayerResolved() {
        when(server.getPlayer(playerId)).thenReturn(player);
        when(player.isOnline()).thenReturn(true);
        when(player.isOp()).thenReturn(true);
        when(player.getName()).thenReturn("Steve");
        when(player.getWorld()).thenReturn(world);
        when(world.getName()).thenReturn("world");

        assertTrue(bridge.isOnline(playerId));
        assertEquals("Steve", bridge.onlineDisplayName(playerId));
        assertEquals("world", bridge.onlineRealm(playerId));
        assertTrue(bridge.isOperator(playerId));
    }
}
