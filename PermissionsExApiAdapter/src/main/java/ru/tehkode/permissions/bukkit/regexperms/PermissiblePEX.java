package ru.tehkode.permissions.bukkit.regexperms;

import java.util.Set;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.PermissibleBase;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachmentInfo;
import ru.tehkode.permissions.bukkit.PermissionsEx;

/**
 * ABI shell. Plus owns Bukkit permissible injection.
 */
public class PermissiblePEX extends PermissibleBase {
    private Permissible previousPermissible;
    private final Player player;
    private final PermissionsEx plugin;

    public PermissiblePEX(Player player, PermissionsEx plugin) {
        super(player);
        this.player = player;
        this.plugin = plugin;
    }

    public Permissible getPreviousPermissible() {
        return previousPermissible;
    }

    public void setPreviousPermissible(Permissible previousPermissible) {
        this.previousPermissible = previousPermissible;
    }

    public boolean isDebug() {
        return plugin != null && plugin.isDebug();
    }

    @Override
    public boolean hasPermission(String permission) {
        return previousPermissible != null
                ? previousPermissible.hasPermission(permission)
                : super.hasPermission(permission);
    }

    @Override
    public boolean hasPermission(Permission permission) {
        return previousPermissible != null
                ? previousPermissible.hasPermission(permission)
                : super.hasPermission(permission);
    }

    @Override
    public void recalculatePermissions() {
        if (previousPermissible != null) {
            previousPermissible.recalculatePermissions();
        } else {
            super.recalculatePermissions();
        }
    }

    @Override
    public boolean isPermissionSet(String permission) {
        return previousPermissible != null
                ? previousPermissible.isPermissionSet(permission)
                : super.isPermissionSet(permission);
    }

    @Override
    public Set<PermissionAttachmentInfo> getEffectivePermissions() {
        return previousPermissible != null
                ? previousPermissible.getEffectivePermissions()
                : super.getEffectivePermissions();
    }
}
