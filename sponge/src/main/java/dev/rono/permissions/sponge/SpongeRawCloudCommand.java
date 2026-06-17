package dev.rono.permissions.sponge;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.StaticArgument;
import cloud.commandframework.exceptions.ArgumentParseException;
import cloud.commandframework.exceptions.CommandExecutionException;
import cloud.commandframework.exceptions.InvalidCommandSenderException;
import cloud.commandframework.exceptions.InvalidSyntaxException;
import cloud.commandframework.exceptions.NoPermissionException;
import cloud.commandframework.exceptions.NoSuchCommandException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.CommandCompletion;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.ArgumentReader;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionException;

final class SpongeRawCloudCommand<C> implements org.spongepowered.api.command.Command.Raw {
    private static final Component MESSAGE_INTERNAL_ERROR =
            Component.text("An internal error occurred while attempting to perform this command.", NamedTextColor.RED);
    private static final Component MESSAGE_NO_PERMS = Component.text(
            "I'm sorry, but you do not have permission to perform this command. Please contact the server administrators if you believe that this is in error.",
            NamedTextColor.RED);
    private static final Component MESSAGE_UNKNOWN_COMMAND =
            Component.text("Unknown command. Type \"/help\" for help.", NamedTextColor.RED);

    private final StaticArgument<C> root;
    private final Command<C> cloudCommand;
    private final SpongeCloudCommandManager<C> manager;

    SpongeRawCloudCommand(
            StaticArgument<C> root, Command<C> cloudCommand, SpongeCloudCommandManager<C> manager) {
        this.root = root;
        this.cloudCommand = cloudCommand;
        this.manager = manager;
    }

    @Override
    public CommandResult process(CommandCause cause, ArgumentReader.Mutable arguments) throws CommandException {
        C cloudSender = manager.commandSenderMapper().apply(cause);
        String input = formatCommand(arguments);
        manager.executeCommand(cloudSender, input).whenComplete((result, throwable) -> {
            if (throwable == null) {
                return;
            }
            if (throwable instanceof CompletionException completionException && completionException.getCause() != null) {
                throwable = completionException.getCause();
            }
            Throwable failure = throwable;
            if (failure instanceof InvalidSyntaxException invalidSyntax) {
                manager.handleException(
                        cloudSender,
                        InvalidSyntaxException.class,
                        invalidSyntax,
                        (c, e) -> cause.audience().sendMessage(Component.text(
                                "Invalid Command Syntax. Correct command syntax is: "
                                        + invalidSyntax.getCorrectSyntax(),
                                NamedTextColor.RED)));
            } else if (failure instanceof InvalidCommandSenderException invalidSender) {
                manager.handleException(
                        cloudSender,
                        InvalidCommandSenderException.class,
                        invalidSender,
                        (c, e) -> cause.audience().sendMessage(Component.text(failure.getMessage(), NamedTextColor.RED)));
            } else if (failure instanceof NoPermissionException noPermission) {
                manager.handleException(
                        cloudSender,
                        NoPermissionException.class,
                        noPermission,
                        (c, e) -> cause.audience().sendMessage(MESSAGE_NO_PERMS));
            } else if (failure instanceof NoSuchCommandException noSuchCommand) {
                manager.handleException(
                        cloudSender,
                        NoSuchCommandException.class,
                        noSuchCommand,
                        (c, e) -> cause.audience().sendMessage(MESSAGE_UNKNOWN_COMMAND));
            } else if (failure instanceof ArgumentParseException argumentParse) {
                manager.handleException(
                        cloudSender,
                        ArgumentParseException.class,
                        argumentParse,
                        (c, e) -> cause.audience()
                                .sendMessage(Component.text(
                                        "Invalid Command Argument: "
                                                + (argumentParse.getCause() != null
                                                        ? argumentParse.getCause().getMessage()
                                                        : argumentParse.getMessage()),
                                        NamedTextColor.GRAY)));
            } else if (failure instanceof CommandExecutionException commandExecution) {
                manager.handleException(
                        cloudSender,
                        CommandExecutionException.class,
                        commandExecution,
                        (c, e) -> {
                            cause.audience().sendMessage(MESSAGE_INTERNAL_ERROR);
                            manager.owningPlugin()
                                    .logger()
                                    .error("Exception executing command handler", commandExecution.getCause());
                        });
            } else {
                cause.audience().sendMessage(MESSAGE_INTERNAL_ERROR);
                manager.owningPlugin().logger().error("An unhandled exception was thrown during command execution", failure);
            }
        });
        return CommandResult.success();
    }

    @Override
    public List<CommandCompletion> complete(CommandCause cause, ArgumentReader.Mutable arguments)
            throws CommandException {
        C cloudSender = manager.commandSenderMapper().apply(cause);
        return manager.suggest(cloudSender, formatCommand(arguments)).stream()
                .map(CommandCompletion::of)
                .toList();
    }

    @Override
    public boolean canExecute(CommandCause cause) {
        return manager.hasPermission(
                manager.commandSenderMapper().apply(cause), cloudCommand.getCommandPermission());
    }

    @Override
    public Optional<Component> shortDescription(CommandCause cause) {
        return Optional.empty();
    }

    @Override
    public Optional<Component> extendedDescription(CommandCause cause) {
        return Optional.empty();
    }

    @Override
    public Component usage(CommandCause cause) {
        return Component.text("/" + root.getName());
    }

    private String formatCommand(ArgumentReader.Mutable arguments) {
        String rest = arguments.remaining().trim();
        if (rest.isEmpty()) {
            return root.getName();
        }
        return root.getName() + " " + rest;
    }
}
