package dev.rono.permissions.spigot.platform;

import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Bukkit-backed {@link dev.rono.permissions.api.runtime.PlatformAdapter} and
 * {@link ru.tehkode.permissions.NativeInterface} helpers for game servers.
 */
public final class SpigotPlatformBridge {
    private final JavaPlugin plugin;

    public SpigotPlatformBridge(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public String uuidToName(UUID uid) {
        Player online = plugin.getServer().getPlayer(uid);
        return online != null ? online.getName() : null;
    }

    public UUID nameToUuid(String name) {
        OfflinePlayer player = plugin.getServer().getOfflinePlayer(name);
        try {
            return player.getUniqueId();
        } catch (Throwable t) {
            return null;
        }
    }

    public boolean isOnline(UUID uuid) {
        Player player = plugin.getServer().getPlayer(uuid);
        return player != null && player.isOnline();
    }

    public UUID serverId() {
        List<World> worlds = plugin.getServer().getWorlds();
        return worlds.isEmpty() ? null : worlds.get(0).getUID();
    }

    public Collection<String> realmNames() {
        List<World> worlds = plugin.getServer().getWorlds();
        List<String> names = new ArrayList<>(worlds.size());
        for (World world : worlds) {
            names.add(world.getName());
        }
        return names;
    }

    public String onlineRealm(UUID playerId) {
        Player player = plugin.getServer().getPlayer(playerId);
        return player != null ? player.getWorld().getName() : null;
    }

    public String onlineDisplayName(UUID playerId) {
        Player player = plugin.getServer().getPlayer(playerId);
        return player != null ? player.getName() : null;
    }

    public boolean isOperator(UUID uuid) {
        Player player = plugin.getServer().getPlayer(uuid);
        return player != null && player.isOp();
    }
}
