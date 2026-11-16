package dev.rono.permissions.sponge7.command;

import cloud.commandframework.CloudCapability;
import cloud.commandframework.Command;
import cloud.commandframework.CommandManager;
import cloud.commandframework.arguments.StaticArgument;
import cloud.commandframework.exceptions.ArgumentParseException;
import cloud.commandframework.exceptions.InvalidCommandSenderException;
import cloud.commandframework.exceptions.InvalidSyntaxException;
import cloud.commandframework.exceptions.NoPermissionException;
import cloud.commandframework.exceptions.NoSuchCommandException;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.internal.CommandRegistrationHandler;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.meta.SimpleCommandMeta;
import dev.rono.permissions.core.platform.PlatformLogger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandMapping;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

/**
 * Cloud command manager for SpongeAPI 7. Each Cloud root command is registered
 * as
 * a native Sponge command whose input is handed back to Cloud for parsing,
 * permission checks, execution, and tab completion.
 */
public final class Sponge7CommandManager extends CommandManager<CommandSource> {
    private final Object plugin;
    private final PlatformLogger logger;
    private final Map<String, CommandMapping> mappings = new ConcurrentHashMap<>();

    public Sponge7CommandManager(Object plugin, PlatformLogger logger) {
        super(CommandExecutionCoordinator.simpleCoordinator(), CommandRegistrationHandler.nullCommandRegistrationHandler());
        this.plugin = plugin;
        this.logger = logger;

        registerCapability(CloudCapability.StandardCapabilities.ROOT_COMMAND_DELETION);
        commandRegistrationHandler(new Registration());
    }

    @Override
    public boolean hasPermission(CommandSource sender, String permission) {
        return permission.isEmpty() || sender.hasPermission(permission);
    }

    @Override
    public CommandMeta createDefaultCommandMeta() {
        return SimpleCommandMeta.empty();
    }

    private final class Registration implements CommandRegistrationHandler {
        @Override
        public boolean registerCommand(Command<?> command) {
            String root = command.getArguments().get(0).getName();

            if (mappings.containsKey(root)) {
                return true; // Further Cloud commands under an existing root share its mapping.
            }

            Optional<CommandMapping> mapping = Sponge.getCommandManager().register(plugin, new Callable(root), root);

            mapping.ifPresent(value -> mappings.put(root, value));

            return mapping.isPresent();
        }

        @Override
        public void unregisterRootCommand(StaticArgument<?> root) {
            CommandMapping mapping = mappings.remove(root.getName());

            if (mapping != null) {
                Sponge.getCommandManager().removeMapping(mapping);
            }
        }
    }

    private final class Callable implements CommandCallable {
        private final String root;

        Callable(String root) {
            this.root = root;
        }

        private String input(String arguments) {
            return arguments.isEmpty() ? root : root + " " + arguments;
        }

        @Override
        public CommandResult process(CommandSource source, String arguments) {
            try {
                executeCommand(source, input(arguments)).join();
            } catch (CompletionException wrapped) {
                report(source, wrapped.getCause() == null ? wrapped : wrapped.getCause());
            } catch (RuntimeException error) {
                report(source, error);
            }

            return CommandResult.success();
        }

        private void report(CommandSource source, Throwable error) {
            if (error instanceof InvalidSyntaxException) {
                source.sendMessage(Text.of(TextColors.RED, "Invalid syntax. Correct usage: /", ((InvalidSyntaxException) error).getCorrectSyntax()));
            } else if (error instanceof NoPermissionException) {
                source.sendMessage(Text.of(TextColors.RED, "You do not have permission to use this command."));
            } else if (error instanceof NoSuchCommandException) {
                source.sendMessage(Text.of(TextColors.RED, "Unknown command."));
            } else if (error instanceof InvalidCommandSenderException) {
                source.sendMessage(Text.of(TextColors.RED, "This command cannot be used by ", source.getName(), "."));
            } else if (error instanceof ArgumentParseException) {
                Throwable cause = error.getCause() == null ? error : error.getCause();

                source.sendMessage(Text.of(TextColors.RED, "Invalid argument: ", String.valueOf(cause.getMessage())));
            } else {
                source.sendMessage(Text.of(TextColors.RED, "An internal error occurred while running this command."));
                logger.error("PermissionsExPlus command failed: /" + root, error);
            }
        }

        @Override
        public List<String> getSuggestions(CommandSource source, String arguments, Location<World> targetPosition) {
            try {
                return new ArrayList<>(suggest(source, input(arguments)));
            } catch (RuntimeException error) {
                return Collections.emptyList();
            }
        }

        @Override
        public boolean testPermission(CommandSource source) {
            return true; // Cloud checks each subcommand's permission.
        }

        @Override
        public Optional<Text> getShortDescription(CommandSource source) {
            return Optional.of(Text.of("PermissionsExPlus commands"));
        }

        @Override
        public Optional<Text> getHelp(CommandSource source) {
            return Optional.empty();
        }

        @Override
        public Text getUsage(CommandSource source) {
            return Text.of();
        }
    }
}
