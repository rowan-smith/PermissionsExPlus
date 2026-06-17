package dev.rono.permissions.velocity.platform;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import dev.rono.permissions.api.runtime.VelocityContextResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VelocityPlatformAdapterTest {

    @Mock
    private ProxyServer server;
    @Mock
    private Player player;
    @Mock
    private ServerConnection connection;
    @Mock
    private RegisteredServer registeredServer;

    private VelocityPlatformAdapter adapter;
    private final UUID playerId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        adapter = new VelocityPlatformAdapter(server);
    }

    @Test
    void serverIdIsStable() {
        UUID expected = UUID.nameUUIDFromBytes("permissionsexplus-velocity".getBytes(StandardCharsets.UTF_8));
        assertEquals(expected, adapter.serverId());
    }

    @Test
    void resolvesOnlinePlayer() {
        when(server.getPlayer(playerId)).thenReturn(Optional.of(player));
        when(player.getUsername()).thenReturn("Alex");
        when(player.getUniqueId()).thenReturn(playerId);
        when(player.getCurrentServer()).thenReturn(Optional.of(connection));
        when(connection.getServerInfo()).thenReturn(new ServerInfo("lobby", InetSocketAddress.createUnresolved("localhost", 25565)));
        when(player.hasPermission("permissionsex.admin")).thenReturn(true);
        when(server.getPlayer("Alex")).thenReturn(Optional.of(player));

        assertTrue(adapter.isOnline(playerId));
        assertEquals("Alex", adapter.onlineDisplayName(playerId));
        assertEquals("lobby", adapter.onlineRealm(playerId));
        assertTrue(adapter.isOperator(playerId));
        assertEquals(playerId, adapter.nameToUuid("Alex"));
        assertEquals("Alex", adapter.uuidToName(playerId));
    }

    @Test
    void realmNamesFromRegisteredServers() {
        when(server.getAllServers()).thenReturn(List.of(registeredServer));
        when(registeredServer.getServerInfo()).thenReturn(new ServerInfo("survival", InetSocketAddress.createUnresolved("localhost", 25566)));
        assertEquals(List.of("survival"), adapter.realmNames());
    }

    @Test
    void contextResolverIsVelocity() {
        assertInstanceOf(VelocityContextResolver.class, adapter.getContextResolver());
    }
}
