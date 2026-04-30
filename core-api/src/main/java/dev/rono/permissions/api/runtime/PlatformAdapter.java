package dev.rono.permissions.api.runtime;

import dev.rono.permissions.api.bus.PermissionDispatch;
import java.util.Collection;
import java.util.UUID;

/**
 * Host integration (Spigot, Bungee, tests). No Bukkit types.
 */
public interface PlatformAdapter {
    String uuidToName(UUID uid);

    UUID nameToUuid(String name);

    boolean isOnline(UUID uuid);

    UUID serverId();

    /**
     * Worlds on game servers; backend server ids on proxies.
     */
    Collection<String> realmNames();

    void publish(PermissionDispatch dispatch);

    /** @return current realm when online, else null */
    String onlineRealm(UUID player);

    /** @return display name when online, else null */
    String onlineDisplayName(UUID player);

    boolean isOperator(UUID player);
}
