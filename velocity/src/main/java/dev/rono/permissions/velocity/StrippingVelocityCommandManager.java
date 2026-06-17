package dev.rono.permissions.velocity;

import cloud.commandframework.CommandTree;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.execution.CommandResult;
import cloud.commandframework.velocity.VelocityCommandManager;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.rono.permissions.core.commands.PexCommandInput;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Strips leading/trailing Unicode whitespace from the raw command line before Cloud tokenizes it on execution.
 */
public final class StrippingVelocityCommandManager<C> extends VelocityCommandManager<C> {

    public StrippingVelocityCommandManager(
            PluginContainer plugin,
            ProxyServer server,
            Function<CommandTree<C>, CommandExecutionCoordinator<C>> executionCoordinator,
            Function<CommandSource, C> commandSenderMapper,
            Function<C, CommandSource> backwardsCommandSenderMapper) {
        super(plugin, server, executionCoordinator, commandSenderMapper, backwardsCommandSenderMapper);
    }

    @Override
    public CompletableFuture<CommandResult<C>> executeCommand(C commandSender, String input) {
        return super.executeCommand(commandSender, PexCommandInput.strip(input));
    }
}
