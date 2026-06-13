package dev.rono.permissions.velocity.platform;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.rono.permissions.api.runtime.PlatformAdapter;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.UUID;

public final class VelocityPlatformAdapter implements PlatformAdapter {
    private static final UUID PROXY_UUID =
            UUID.nameUUIDFromBytes("permissionsexplus-velocity".getBytes(StandardCharsets.UTF_8));

    private final ProxyServer server;

    public VelocityPlatformAdapter(ProxyServer server) {
        this.server = server;
    }

    @Override
    public String uuidToName(UUID uid) {
        return server.getPlayer(uid).map(Player::getUsername).orElse(null);
    }

    @Override
    public UUID nameToUuid(String name) {
        return server.getPlayer(name).map(Player::getUniqueId).orElse(null);
    }

    @Override
    public boolean isOnline(UUID uuid) {
        return server.getPlayer(uuid).isPresent();
    }

    @Override
    public UUID serverId() {
        return PROXY_UUID;
    }

    @Override
    public Collection<String> realmNames() {
        return server.getAllServers().stream().map(s -> s.getServerInfo().getName()).toList();
    }

    @Override
    public String onlineRealm(UUID player) {
        return server.getPlayer(player)
                .flatMap(p -> p.getCurrentServer())
                .map(c -> c.getServerInfo().getName())
                .orElse(null);
    }

    @Override
    public String onlineDisplayName(UUID player) {
        return server.getPlayer(player).map(Player::getUsername).orElse(null);
    }

    @Override
    public boolean isOperator(UUID uuid) {
        return server.getPlayer(uuid).map(p -> p.hasPermission("permissionsex.admin")).orElse(false);
    }
}
