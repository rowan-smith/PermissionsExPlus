package dev.rono.permissions.paper;

import dev.rono.permissions.bukkit.PermissionsExPlusBukkitPlugin;
import dev.rono.permissions.bukkit.platform.BukkitPlatform;
import dev.rono.permissions.paper.platform.PaperPlatform;

/**
 * Entry point for Paper 1.21+, loaded as a Paper plugin. Everything else is
 * shared with the other
 * Bukkit-based servers in BukkitCommon.
 */
public class PaperPlugin extends PermissionsExPlusBukkitPlugin {
    @Override
    protected BukkitPlatform createPlatform() {
        return new PaperPlatform(this);
    }
}
