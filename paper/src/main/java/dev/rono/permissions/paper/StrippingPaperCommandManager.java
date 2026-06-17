package dev.rono.permissions.paper;

import cloud.commandframework.CommandTree;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.execution.CommandResult;
import cloud.commandframework.paper.PaperCommandManager;
import dev.rono.permissions.core.commands.PexCommandInput;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Paper-specific Cloud manager with the same input stripping as the Spigot Bukkit manager.
 */
public final class StrippingPaperCommandManager<C> extends PaperCommandManager<C> {

    public StrippingPaperCommandManager(
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
