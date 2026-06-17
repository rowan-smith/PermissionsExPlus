package dev.rono.permissions.sponge.platform;

import dev.rono.permissions.api.runtime.ContextResolver;
import dev.rono.permissions.api.runtime.PlatformAdapter;
import dev.rono.permissions.api.runtime.SpongeContextResolver;
import org.spongepowered.api.Server;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

public final class SpongePlatformAdapter implements PlatformAdapter {
    private static final UUID SERVER_UUID =
            UUID.nameUUIDFromBytes("permissionsexplus-sponge".getBytes(StandardCharsets.UTF_8));
    private static final ContextResolver CONTEXT_RESOLVER = new SpongeContextResolver();

    private final Server server;

    public SpongePlatformAdapter(Server server) {
        this.server = server;
    }

    @Override
    public String uuidToName(UUID uid) {
        return server.player(uid).map(ServerPlayer::name).orElse(null);
    }

    @Override
    public UUID nameToUuid(String name) {
        return server.player(name).map(ServerPlayer::uniqueId).orElse(null);
    }

    @Override
    public boolean isOnline(UUID uuid) {
        return server.player(uuid).isPresent();
    }

    @Override
    public UUID serverId() {
        return SERVER_UUID;
    }

    @Override
    public Collection<String> realmNames() {
        return server.worldManager().worlds().stream()
                .map(w -> w.key().asString())
                .collect(Collectors.toList());
    }

    @Override
    public String onlineRealm(UUID player) {
        return server.player(player).map(p -> {
            var world = p.world();
            return world != null ? world.key().asString() : null;
        }).orElse(null);
    }

    @Override
    public String onlineDisplayName(UUID player) {
        return server.player(player).map(ServerPlayer::name).orElse(null);
    }

    @Override
    public boolean isOperator(UUID uuid) {
        return server.player(uuid).map(p -> p.hasPermission("permissionsex.admin")).orElse(false);
    }

    @Override
    public ContextResolver getContextResolver() {
        return CONTEXT_RESOLVER;
    }
}
