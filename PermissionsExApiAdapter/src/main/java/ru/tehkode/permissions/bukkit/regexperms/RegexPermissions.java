package ru.tehkode.permissions.bukkit.regexperms;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import ru.tehkode.permissions.bukkit.PermissionsEx;

/**
 * ABI shell. Bukkit injection is owned by PermissionsExPlus; this type remains
 * for {@link PermissionsEx#getRegexPerms()} binary compatibility only.
 */
public class RegexPermissions {
    private final PermissionsEx plugin;

    public RegexPermissions(PermissionsEx plugin) {
        this.plugin = plugin;
    }

    public void onDisable() {}

    public boolean hasDebugMode() {
        return plugin != null && plugin.isDebug();
    }

    public PermissionList getPermissionList() {
        return null;
    }

    public void injectPermissible(Player player) {}

    @SuppressWarnings("unused")
    private static final class EventListener implements Listener {}
}
