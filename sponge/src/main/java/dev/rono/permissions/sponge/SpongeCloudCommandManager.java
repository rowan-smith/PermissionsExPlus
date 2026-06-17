package dev.rono.permissions.sponge;

import cloud.commandframework.Command;
import cloud.commandframework.CommandManager;
import cloud.commandframework.CommandTree;
import cloud.commandframework.arguments.StaticArgument;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.execution.CommandResult;
import cloud.commandframework.execution.CommandSuggestionProcessor;
import cloud.commandframework.execution.FilteringCommandSuggestionProcessor;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.meta.SimpleCommandMeta;
import dev.rono.permissions.core.commands.PexCommandInput;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.plugin.PluginContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Cloud command manager for SpongeAPI 8 using {@link RegisterCommandEvent} and raw command forwarding.
 */
public class SpongeCloudCommandManager<C> extends CommandManager<C> {
    private final PluginContainer owningPlugin;
    private final Function<CommandCause, C> commandSenderMapper;
    private final Function<C, CommandCause> backwardsCommandSenderMapper;
    private final SpongeRegistrationHandler<C> registrationHandler;

    public SpongeCloudCommandManager(
            PluginContainer owningPlugin,
            Function<CommandTree<C>, CommandExecutionCoordinator<C>> executionCoordinator,
            Function<CommandCause, C> commandSenderMapper,
            Function<C, CommandCause> backwardsCommandSenderMapper) {
        super(executionCoordinator, new SpongeRegistrationHandler<>());
        this.registrationHandler = (SpongeRegistrationHandler<C>) commandRegistrationHandler();
        this.owningPlugin = owningPlugin;
        this.commandSenderMapper = commandSenderMapper;
        this.backwardsCommandSenderMapper = backwardsCommandSenderMapper;
        this.commandSuggestionProcessor(new FilteringCommandSuggestionProcessor(
                FilteringCommandSuggestionProcessor.Filter.startsWith(true).andTrimBeforeLastSpace()));
        this.registrationHandler.initialize(this);
    }

    @Override
    public CompletableFuture<CommandResult<C>> executeCommand(C commandSender, String input) {
        return super.executeCommand(commandSender, PexCommandInput.strip(input));
    }

    public void registerQueuedCommands(RegisterCommandEvent<org.spongepowered.api.command.Command.Raw> event) {
        registrationHandler.registerQueued(event, owningPlugin);
    }

    @Override
    public final boolean hasPermission(@NonNull C sender, @NonNull String permission) {
        if (permission.isEmpty()) {
            return true;
        }
        return backwardsCommandSenderMapper.apply(sender).subject().hasPermission(permission);
    }

    @Override
    public final @NonNull CommandMeta createDefaultCommandMeta() {
        return SimpleCommandMeta.empty();
    }

    Function<CommandCause, C> commandSenderMapper() {
        return commandSenderMapper;
    }

    PluginContainer owningPlugin() {
        return owningPlugin;
    }

    static final class SpongeRegistrationHandler<C> implements cloud.commandframework.internal.CommandRegistrationHandler {
        private SpongeCloudCommandManager<C> manager;
        private final List<QueuedCommand<C>> queued = new ArrayList<>();

        private void initialize(SpongeCloudCommandManager<C> manager) {
            this.manager = manager;
        }

        @Override
        public boolean registerCommand(@NonNull Command<?> command) {
            StaticArgument<C> root = (StaticArgument<C>) command.getArguments().get(0);
            queued.add(new QueuedCommand<>(root, (Command<C>) command));
            return true;
        }

        private void registerQueued(
                RegisterCommandEvent<org.spongepowered.api.command.Command.Raw> event,
                PluginContainer container) {
            for (QueuedCommand<C> queuedCommand : queued) {
                SpongeRawCloudCommand<C> raw = new SpongeRawCloudCommand<>(
                        queuedCommand.root(), queuedCommand.cloudCommand(), manager);
                String primary = queuedCommand.root().getName();
                String[] aliases = queuedCommand.root().getAlternativeAliases().toArray(String[]::new);
                event.register(container, raw, primary, aliases);
            }
        }
    }

    private record QueuedCommand<C>(StaticArgument<C> root, Command<C> cloudCommand) {}
}
