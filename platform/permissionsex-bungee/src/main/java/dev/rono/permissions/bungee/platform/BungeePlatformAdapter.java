package dev.rono.permissions.bungee.platform;

import dev.rono.permissions.api.runtime.PlatformAdapter;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.UUID;

/**
 * BungeeCord-backed {@link PlatformAdapter} for proxy runtimes.
 */
public final class BungeePlatformAdapter implements PlatformAdapter {
    private static final UUID PROXY_UUID =
            UUID.nameUUIDFromBytes("permissionsexplus-bungee".getBytes(StandardCharsets.UTF_8));

    private final Plugin plugin;

    public BungeePlatformAdapter(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String uuidToName(UUID uid) {
        ProxiedPlayer player = plugin.getProxy().getPlayer(uid);
        return player != null ? player.getName() : null;
    }

    @Override
    public UUID nameToUuid(String name) {
        ProxiedPlayer player = plugin.getProxy().getPlayer(name);
        return player != null ? player.getUniqueId() : null;
    }

    @Override
    public boolean isOnline(UUID uuid) {
        return plugin.getProxy().getPlayer(uuid) != null;
    }

    @Override
    public UUID serverId() {
        return PROXY_UUID;
    }

    @Override
    public Collection<String> realmNames() {
        return plugin.getProxy().getServers().keySet();
    }

    @Override
    public String onlineRealm(UUID uuid) {
        ProxiedPlayer player = plugin.getProxy().getPlayer(uuid);
        return player != null && player.getServer() != null ? player.getServer().getInfo().getName() : null;
    }

    @Override
    public String onlineDisplayName(UUID uuid) {
        ProxiedPlayer player = plugin.getProxy().getPlayer(uuid);
        return player != null ? player.getName() : null;
    }

    @Override
    public boolean isOperator(UUID uuid) {
        ProxiedPlayer player = plugin.getProxy().getPlayer(uuid);
        return player != null && player.hasPermission("permissionsex.admin");
    }
}
