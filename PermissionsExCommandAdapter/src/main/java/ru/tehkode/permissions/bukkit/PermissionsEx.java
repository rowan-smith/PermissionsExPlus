package ru.tehkode.permissions.bukkit;

import dev.rono.permissions.api.PexProvider;
import dev.rono.permissions.core.PexImplProvider;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;
import ru.tehkode.permissions.bukkit.commands.GroupCommands;
import ru.tehkode.permissions.bukkit.commands.PromotionCommands;
import ru.tehkode.permissions.bukkit.commands.UserCommands;
import ru.tehkode.permissions.bukkit.commands.UtilityCommands;
import ru.tehkode.permissions.bukkit.commands.WorldCommands;
import ru.tehkode.permissions.commands.CommandsManager;

/**
 * Legacy PEX command surface that clears PermissionsExPlus Cloud commands and
 * registers the original {@code /pex}, {@code /promote}, and {@code /demote}
 * handlers. All lookups and mutations delegate to PermissionsExPlus.
 */
public class PermissionsEx extends JavaPlugin {
    private static PermissionsEx instance;
    private CommandsManager commandsManager;

    {
        instance = this;
    }

    public static PermissionsEx getPlugin() {
        return instance;
    }

    @Override
    public void onLoad() {
        this.commandsManager = new CommandsManager(this);
    }

    @Override
    public void onEnable() {
        if (!PexProvider.available()) {
            getLogger().severe("PermissionsExPlus is unavailable.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        try {
            var api = PexImplProvider.get();

            if (api.commands() != null) {
                api.commands().clear();
                getLogger().info("Cleared PermissionsExPlus command tree");
            }

            this.commandsManager.register(new UserCommands());
            this.commandsManager.register(new GroupCommands());
            this.commandsManager.register(new PromotionCommands());
            this.commandsManager.register(new WorldCommands());
            this.commandsManager.register(new UtilityCommands());

            getLogger().info("Registered legacy PermissionsEx command surface");
        } catch (Throwable error) {
            getLogger().log(Level.SEVERE, "Failed to enable PermissionsExCommandAdapter", error);
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        instance = null;
    }

    @Override
    public boolean onCommand(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, String[] args) {
        if (commandsManager == null) {
            sender.sendMessage(ChatColor.RED + "PermissionsExCommandAdapter is not ready.");
            return true;
        }

        return commandsManager.execute(sender, command, args);
    }

    public CommandsManager getCommandsManager() {
        return commandsManager;
    }
}
