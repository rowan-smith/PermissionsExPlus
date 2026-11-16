package dev.rono.permissions.compat.commands;

import lombok.var;

import dev.rono.permissions.api.PexProvider;
import dev.rono.permissions.core.PexImplProvider;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;
import dev.rono.permissions.compat.commands.handlers.GroupCommands;
import dev.rono.permissions.compat.commands.handlers.PromotionCommands;
import dev.rono.permissions.compat.commands.handlers.UserCommands;
import dev.rono.permissions.compat.commands.handlers.UtilityCommands;
import dev.rono.permissions.compat.commands.handlers.WorldCommands;
import dev.rono.permissions.compat.commands.framework.CommandsManager;

/**
 * Legacy PEX command surface that clears PermissionsExPlus Cloud commands and
 * registers the original {@code /pex}, {@code /promote}, and {@code /demote}
 * handlers. All lookups and mutations delegate to PermissionsExPlus.
 */
public class PexCommandAdapter extends JavaPlugin {
    private static PexCommandAdapter instance;
    private CommandsManager commandsManager;

    {
        instance = this;
    }

    public static PexCommandAdapter getPlugin() {
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
                // Hand command ownership to this adapter. Core remembers the mode, so
                // /pex reload does not re-register the modern command tree over ours.
                api.useLegacyCommands();
                getLogger().info("Cleared PermissionsExPlus command tree");
            }

            claimCommandLabels();

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

    /**
     * PermissionsExPlus enables first, so its Cloud commands already held /pex when
     * Bukkit registered this plugin's commands; ours only received the namespaced
     * fallback (permissionsexcommandadapter:pex). Clearing the Cloud tree frees the
     * bare labels, so register our commands again to claim them.
     */
    private void claimCommandLabels() {
        CommandMap commandMap = commandMap();

        if (commandMap == null) {
            getLogger().warning("Unable to access the server command map; use /" + getName().toLowerCase(java.util.Locale.ROOT) + ":pex if /pex is unavailable");
            return;
        }

        for (String name : getDescription().getCommands().keySet()) {
            PluginCommand command = getCommand(name);

            if (command != null) {
                commandMap.register(getName().toLowerCase(java.util.Locale.ROOT), command);
            }
        }
    }

    /**
     * CraftServer#getCommandMap exists on every supported version but is not Bukkit
     * API before Paper added it.
     */
    private CommandMap commandMap() {
        try {
            Object map = getServer().getClass().getMethod("getCommandMap").invoke(getServer());

            return map instanceof CommandMap ? (CommandMap) map : null;
        } catch (ReflectiveOperationException | RuntimeException unavailable) {
            return null;
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
