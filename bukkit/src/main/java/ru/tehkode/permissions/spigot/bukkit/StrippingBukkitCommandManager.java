package ru.tehkode.permissions.spigot.bukkit;

import cloud.commandframework.CommandTree;
import cloud.commandframework.bukkit.BukkitCommandManager;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.execution.CommandResult;
import dev.rono.permissions.core.commands.PexCommandInput;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Strips leading/trailing Unicode whitespace from the raw command line before Cloud tokenizes it
 * on execution. Tab completion ({@link #suggest}) is left unchanged: trailing spaces disambiguate
 * “complete this token” vs “suggest next argument” in Cloud’s parser.
 */
public final class StrippingBukkitCommandManager<C> extends BukkitCommandManager<C> {

    public StrippingBukkitCommandManager(
            Plugin owningPlugin,
            Function<CommandTree<C>, CommandExecutionCoordinator<C>> executionCoordinator,
            Function<CommandSender, C> commandSenderMapper,
            Function<C, CommandSender> backwardsCommandSenderMapper) throws Exception {
        super(owningPlugin, executionCoordinator, commandSenderMapper, backwardsCommandSenderMapper);
    }

    @Override
    public CompletableFuture<CommandResult<C>> executeCommand(C commandSender, String input) {
        return super.executeCommand(commandSender, PexCommandInput.strip(input));
    }
}
