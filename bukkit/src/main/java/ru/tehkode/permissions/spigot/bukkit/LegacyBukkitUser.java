package ru.tehkode.permissions.spigot.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.tehkode.permissions.PermissionUser;

/**
 * Bridges {@link PermissionUser} to an online {@link Player} where Bukkit APIs exist only on game servers.
 * {@link PermissionUser#getPlayer()} was removed from the portable core module; use this instead when you need CraftBukkit entities.
 *
 * @deprecated Prefer {@link org.bukkit.Server#getPlayer(java.util.UUID)} with the user's UUID.
 */
@Deprecated(since = "3.0.0", forRemoval = false)
public final class LegacyBukkitUser {
    private LegacyBukkitUser() {}

    /** @return Live {@link Player} if UUID-based ID matches someone online on this server, else tries name fallback. */
    public static Player getPlayer(PermissionUser user) {
        if (user == null || Bukkit.getServer() == null) {
            return null;
        }
        try {
            return Bukkit.getServer().getPlayer(java.util.UUID.fromString(user.getIdentifier()));
        } catch (Throwable ex) {
            return Bukkit.getServer().getPlayerExact(user.getIdentifier());
        }
    }
}
