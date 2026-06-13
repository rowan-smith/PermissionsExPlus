package dev.rono.permissions.spigot.platform;

import dev.rono.permissions.api.runtime.BukkitContextResolver;
import dev.rono.permissions.api.runtime.ContextResolver;
import dev.rono.permissions.api.runtime.PlatformAdapter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.UUID;

/**
 * Bukkit-backed {@link PlatformAdapter} for game servers.
 */
public final class BukkitPlatformAdapter implements PlatformAdapter {
    private final SpigotPlatformBridge bridge;
    private final ContextResolver contextResolver = new BukkitContextResolver();

    public BukkitPlatformAdapter(JavaPlugin plugin) {
        this.bridge = new SpigotPlatformBridge(plugin);
    }

    public BukkitPlatformAdapter(SpigotPlatformBridge bridge) {
        this.bridge = bridge;
    }

    public SpigotPlatformBridge bridge() {
        return bridge;
    }

    @Override
    public String uuidToName(UUID uid) {
        return bridge.uuidToName(uid);
    }

    @Override
    public UUID nameToUuid(String name) {
        return bridge.nameToUuid(name);
    }

    @Override
    public boolean isOnline(UUID uuid) {
        return bridge.isOnline(uuid);
    }

    @Override
    public UUID serverId() {
        return bridge.serverId();
    }

    @Override
    public Collection<String> realmNames() {
        return bridge.realmNames();
    }

    @Override
    public String onlineRealm(UUID player) {
        return bridge.onlineRealm(player);
    }

    @Override
    public String onlineDisplayName(UUID player) {
        return bridge.onlineDisplayName(player);
    }

    @Override
    public boolean isOperator(UUID uuid) {
        return bridge.isOperator(uuid);
    }

    @Override
    public ContextResolver getContextResolver() {
        return contextResolver;
    }
}
