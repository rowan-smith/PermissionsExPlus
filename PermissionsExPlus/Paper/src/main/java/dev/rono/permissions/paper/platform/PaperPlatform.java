package dev.rono.permissions.paper.platform;

import cloud.commandframework.CommandManager;
import cloud.commandframework.bukkit.CloudBukkitCapabilities;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.paper.PaperCommandManager;
import dev.rono.permissions.bukkit.platform.BukkitPlatform;
import java.util.function.Function;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

/** Paper registers commands through Cloud's Paper integration. */
public final class PaperPlatform extends BukkitPlatform {
    public PaperPlatform(JavaPlugin plugin) {
        super(plugin);
    }

    @Override
    public CommandManager<CommandSender> createCommandManager() throws Exception {
        var manager = new PaperCommandManager<>(plugin, CommandExecutionCoordinator.simpleCoordinator(), Function.identity(), Function.identity());

        if (manager.queryCapabilities().contains(CloudBukkitCapabilities.NATIVE_BRIGADIER)) {
            try {
                manager.registerBrigadier();
            } catch (RuntimeException unavailable) {
                logger().warn("Native Brigadier mappings are unavailable; legacy Bukkit command registration remains active");
            }
        }

        if (manager.queryCapabilities().contains(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
            manager.registerAsynchronousCompletions();
        }

        return manager;
    }
}
