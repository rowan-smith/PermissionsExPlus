package dev.rono.permissions.bungee.platform;

import dev.rono.permissions.api.runtime.BungeeContextResolver;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.plugin.Plugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BungeePlatformAdapterTest {

    @Mock
    private Plugin plugin;
    @Mock
    private ProxyServer proxy;
    @Mock
    private ProxiedPlayer player;
    @Mock
    private Server serverConnection;
    @Mock
    private ServerInfo serverInfo;

    private BungeePlatformAdapter adapter;
    private final UUID playerId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        when(plugin.getProxy()).thenReturn(proxy);
        adapter = new BungeePlatformAdapter(plugin);
    }

    @Test
    void serverIdIsStable() {
        UUID expected = UUID.nameUUIDFromBytes("permissionsexplus-bungee".getBytes(StandardCharsets.UTF_8));
        assertEquals(expected, adapter.serverId());
    }

    @Test
    void resolvesOnlinePlayer() {
        when(proxy.getPlayer(playerId)).thenReturn(player);
        when(proxy.getPlayer("Steve")).thenReturn(player);
        when(player.getUniqueId()).thenReturn(playerId);
        when(player.getName()).thenReturn("Steve");
        when(player.getServer()).thenReturn(serverConnection);
        when(serverConnection.getInfo()).thenReturn(serverInfo);
        when(serverInfo.getName()).thenReturn("lobby");
        when(player.hasPermission("permissionsex.admin")).thenReturn(true);

        assertTrue(adapter.isOnline(playerId));
        assertEquals("Steve", adapter.onlineDisplayName(playerId));
        assertEquals("lobby", adapter.onlineRealm(playerId));
        assertTrue(adapter.isOperator(playerId));
        assertEquals(playerId, adapter.nameToUuid("Steve"));
        assertEquals("Steve", adapter.uuidToName(playerId));
    }

    @Test
    void realmNamesFromProxyServers() {
        when(proxy.getServers()).thenReturn(Map.of("lobby", serverInfo, "survival", serverInfo));
        assertEquals(2, adapter.realmNames().size());
        assertTrue(adapter.realmNames().contains("lobby"));
    }

    @Test
    void contextResolverIsBungee() {
        assertInstanceOf(BungeeContextResolver.class, adapter.getContextResolver());
    }
}
