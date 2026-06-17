package dev.rono.permissions.bungee;

import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PermissionCheckEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.PermissionUser;

import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BungeePexPermissionBridgeTest {

    @Mock
    private PermissionManager manager;
    @Mock
    private PermissionUser user;
    @Mock
    private ProxiedPlayer player;
    @Mock
    private Server serverConnection;
    @Mock
    private ServerInfo serverInfo;
    @Mock
    private PermissionCheckEvent event;

    private final UUID playerId = UUID.randomUUID();
    private BungeePexPermissionBridge bridge;

    @BeforeEach
    void setUp() {
        bridge = new BungeePexPermissionBridge(manager);
    }

    @Test
    void ignoresNonPlayerSenders() {
        when(event.getSender()).thenReturn(mock(net.md_5.bungee.api.CommandSender.class));
        bridge.onPermissionCheck(event);
        verifyNoInteractions(manager);
    }

    @Test
    void leavesEventUnchangedWhenNoExpression() {
        when(event.getSender()).thenReturn(player);
        when(player.getUniqueId()).thenReturn(playerId);
        when(manager.getUser(playerId)).thenReturn(user);
        when(event.getPermission()).thenReturn("missing.node");
        when(player.getServer()).thenReturn(serverConnection);
        when(serverConnection.getInfo()).thenReturn(serverInfo);
        when(serverInfo.getName()).thenReturn("lobby");
        when(user.getMatchingExpression("missing.node", "lobby")).thenReturn(null);

        bridge.onPermissionCheck(event);
        verify(event, never()).setHasPermission(anyBoolean());
    }

    @Test
    void overridesOutcomeWhenExpressionMatches() {
        when(event.getSender()).thenReturn(player);
        when(player.getUniqueId()).thenReturn(playerId);
        when(manager.getUser(playerId)).thenReturn(user);
        when(event.getPermission()).thenReturn("my.node");
        when(player.getServer()).thenReturn(serverConnection);
        when(serverConnection.getInfo()).thenReturn(serverInfo);
        when(serverInfo.getName()).thenReturn("lobby");
        when(user.getMatchingExpression("my.node", "lobby")).thenReturn("true");
        when(user.explainExpression("true")).thenReturn(true);

        bridge.onPermissionCheck(event);
        verify(event).setHasPermission(true);
    }
}
