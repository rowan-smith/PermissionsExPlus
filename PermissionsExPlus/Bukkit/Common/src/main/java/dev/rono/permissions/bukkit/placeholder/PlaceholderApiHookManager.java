package dev.rono.permissions.bukkit.placeholder;

import lombok.var;

import dev.rono.permissions.core.PexApiImpl;
import dev.rono.permissions.bukkit.PermissionsExPlusBukkitPlugin;

import java.util.logging.Level;

public class PlaceholderApiHookManager {
    private final PermissionsExPlusBukkitPlugin plugin;
    private final PexApiImpl<?> api;

    private PermissionsExPlusPlaceholderApiExpansion placeholder = null;

    public PlaceholderApiHookManager(PermissionsExPlusBukkitPlugin plugin, PexApiImpl<?> api) {
        this.plugin = plugin;
        this.api = api;
    }

    public void hook() {
        if (placeholder != null) {
            return;
        }

        placeholder = new PermissionsExPlusPlaceholderApiExpansion(plugin, api);

        var success = placeholder.register();
        if (!success) {
            plugin.getLogger().log(Level.SEVERE, "An error occurred attempting to hook PlaceholderAPI.");
        }
    }

    public void unhook() {
        if (placeholder == null) {
            return;
        }

        var success = placeholder.unregister();
        if (!success) {
            plugin.getLogger().log(Level.SEVERE, "An error occurred attempting to unhook PlaceholderAPI.");
        }

        placeholder = null;
    }
}
