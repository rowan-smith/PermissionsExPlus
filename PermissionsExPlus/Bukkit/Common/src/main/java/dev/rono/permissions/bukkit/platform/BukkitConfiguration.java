package dev.rono.permissions.bukkit.platform;

import dev.rono.permissions.core.platform.PlatformConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;

public class BukkitConfiguration implements PlatformConfiguration {
    private final JavaPlugin plugin;

    public BukkitConfiguration(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Path dataDirectory() {
        return plugin.getDataFolder().toPath();
    }

    @Override
    public void saveResource(String resource, boolean replace) {
        plugin.saveResource(resource, replace);
    }
}
