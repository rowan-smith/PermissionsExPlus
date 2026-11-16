package dev.rono.permissions.spigot.legacy.platform;

import lombok.var;

import cloud.commandframework.CommandManager;
import cloud.commandframework.bukkit.BukkitCommandManager;
import cloud.commandframework.bukkit.CloudBukkitCapabilities;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import dev.rono.permissions.core.platform.Platform;
import dev.rono.permissions.core.platform.PlatformConfiguration;
import dev.rono.permissions.core.platform.PlatformLogger;
import dev.rono.permissions.core.platform.PlatformScheduler;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import dev.rono.permissions.core.util.Immutables;

public class SpigotPlatform implements Platform<CommandSender> {
    private final JavaPlugin plugin;

    private final SpigotLogger logger;
    private final SpigotScheduler scheduler;
    private final SpigotConfiguration configuration;

    public SpigotPlatform(JavaPlugin plugin) {
        this.plugin = plugin;

        this.logger = new SpigotLogger(plugin.getLogger());
        this.scheduler = new SpigotScheduler(plugin);
        this.configuration = new SpigotConfiguration(plugin);
    }

    @Override
    public PlatformLogger logger() {
        return logger;
    }

    @Override
    public PlatformScheduler scheduler() {
        return scheduler;
    }

    @Override
    public PlatformConfiguration configuration() {
        return configuration;
    }

    @Override
    public Class<CommandSender> senderType() {
        return CommandSender.class;
    }

    @Override
    public void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(message);
    }

    @Override
    public void broadcastToOperators(String message) {
        plugin.getServer().getOnlinePlayers().stream().filter(Player::isOp)
                .forEach(player -> player.sendMessage(message));
    }

    @Override
    public CommandManager<CommandSender> createCommandManager() throws Exception {
        var manager = new BukkitCommandManager<>(plugin, CommandExecutionCoordinator.simpleCoordinator(), Function.identity(), Function.identity());

        if (manager.queryCapabilities().contains(CloudBukkitCapabilities.NATIVE_BRIGADIER)) {
            try {
                manager.registerBrigadier();
            } catch (BukkitCommandManager.BrigadierFailureException unavailable) {
                logger.warn("Native Brigadier mappings are unavailable; legacy Bukkit command registration remains active");
            }
        }

        return manager;
    }

    @Override
    public Set<String> integrations() {
        var found = new LinkedHashSet<String>();

        if (plugin.getServer().getPluginManager().getPlugin("Vault") != null) {
            found.add("Vault");
        }

        if (plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            found.add("PlaceholderAPI");
        }

        return Immutables.copyOfSet(found);
    }

    @Override
    public Set<UUID> onlineUserIds() {
        return plugin.getServer().getOnlinePlayers().stream()
                .map(Player::getUniqueId)
                .collect(Collectors.toSet());
    }
}
