package dev.rono.permissions.sponge.platform;

import dev.rono.permissions.api.runtime.SpongeContextResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Server;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.api.world.server.WorldManager;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpongePlatformAdapterTest {

    @Mock
    private Server server;
    @Mock
    private ServerPlayer player;
    @Mock
    private ServerWorld world;
    @Mock
    private WorldManager worldManager;
    @Mock
    private ResourceKey worldKey;

    private SpongePlatformAdapter adapter;
    private final UUID playerId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        adapter = new SpongePlatformAdapter(server);
    }

    @Test
    void serverIdIsStable() {
        UUID expected = UUID.nameUUIDFromBytes("permissionsexplus-sponge".getBytes(StandardCharsets.UTF_8));
        assertEquals(expected, adapter.serverId());
    }

    @Test
    void resolvesOnlinePlayer() {
        when(server.player(playerId)).thenReturn(Optional.of(player));
        when(server.player("Notch")).thenReturn(Optional.of(player));
        when(player.name()).thenReturn("Notch");
        when(player.uniqueId()).thenReturn(playerId);
        when(player.world()).thenReturn(world);
        when(world.key()).thenReturn(worldKey);
        when(worldKey.asString()).thenReturn("minecraft:overworld");
        when(player.hasPermission("permissionsex.admin")).thenReturn(true);

        assertTrue(adapter.isOnline(playerId));
        assertEquals("Notch", adapter.onlineDisplayName(playerId));
        assertEquals("minecraft:overworld", adapter.onlineRealm(playerId));
        assertTrue(adapter.isOperator(playerId));
        assertEquals(playerId, adapter.nameToUuid("Notch"));
        assertEquals("Notch", adapter.uuidToName(playerId));
    }

    @Test
    void realmNamesFromWorldManager() {
        when(server.worldManager()).thenReturn(worldManager);
        when(worldManager.worlds()).thenReturn(List.of(world));
        when(world.key()).thenReturn(worldKey);
        when(worldKey.asString()).thenReturn("minecraft:nether");
        assertEquals(List.of("minecraft:nether"), adapter.realmNames());
    }

    @Test
    void contextResolverIsSponge() {
        assertInstanceOf(SpongeContextResolver.class, adapter.getContextResolver());
    }
}
