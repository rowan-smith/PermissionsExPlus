package dev.rono.permissions.bukkit.platform;

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
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import dev.rono.permissions.core.util.Immutables;

/**
 * Bukkit implementation of the Core platform. Servers with their own command
 * integration (Paper) override {@link #createCommandManager()}.
 */
public class BukkitPlatform implements Platform<CommandSender> {
    protected final JavaPlugin plugin;

    private final BukkitLogger logger;
    private final BukkitTaskScheduler scheduler;
    private final BukkitConfiguration configuration;

    private volatile BukkitPermissionInjector injector;

    public BukkitPlatform(JavaPlugin plugin) {
        this.plugin = plugin;

        this.logger = new BukkitLogger(plugin.getLogger());
        this.scheduler = new BukkitTaskScheduler(plugin);
        this.configuration = new BukkitConfiguration(plugin);
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

    /** Reports this injector's state in {@link #healthReport()}. */
    public void attach(BukkitPermissionInjector injector) {
        this.injector = injector;
    }

    @Override
    public List<String> healthReport() {
        var current = injector;

        return current == null ? Immutables.listOf() : Immutables.listOf(current.healthLine());
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
