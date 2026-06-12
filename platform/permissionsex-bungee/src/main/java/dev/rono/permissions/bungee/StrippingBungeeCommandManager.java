package dev.rono.permissions.bungee;

import cloud.commandframework.CommandTree;
import cloud.commandframework.bungee.BungeeCommandManager;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.execution.CommandResult;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Plugin;
import dev.rono.permissions.core.commands.PexCommandInput;

/**
 * Strips leading/trailing Unicode whitespace from the raw command line before Cloud tokenizes it
 * on execution. Tab completion ({@link #suggest}) is left unchanged: trailing spaces disambiguate
 * “complete this token” vs “suggest next argument” in Cloud’s parser.
 */
public final class StrippingBungeeCommandManager<C> extends BungeeCommandManager<C> {

    public StrippingBungeeCommandManager(
            Plugin plugin,
            Function<CommandTree<C>, CommandExecutionCoordinator<C>> executionCoordinator,
            Function<CommandSender, C> commandSenderMapper,
            Function<C, CommandSender> backwardsCommandSenderMapper) {
        super(plugin, executionCoordinator, commandSenderMapper, backwardsCommandSenderMapper);
    }

    @Override
    public CompletableFuture<CommandResult<C>> executeCommand(C commandSender, String input) {
        return super.executeCommand(commandSender, PexCommandInput.strip(input));
    }
}
