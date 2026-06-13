package dev.rono.permissions.paper;

import cloud.commandframework.bukkit.BukkitCommandManager;
import ru.tehkode.permissions.spigot.bukkit.SpigotPermissionsExPlugin;

/**
 * Paper runtime entry point. Generic Bukkit wiring lives in {@link SpigotPermissionsExPlugin}.
 */
public final class PaperPermissionsExPlugin extends SpigotPermissionsExPlugin {

    @Override
    public void onEnable() {
        super.onEnable();
        registerPaperBrigadier();
    }

    private void registerPaperBrigadier() {
        var manager = getCloudManager();
        if (manager == null) {
            return;
        }
        try {
            manager.registerBrigadier();
        } catch (BukkitCommandManager.BrigadierFailureException ex) {
            getLogger().fine("Brigadier tab-completion hook not available: " + ex.getMessage());
        }
    }
}
