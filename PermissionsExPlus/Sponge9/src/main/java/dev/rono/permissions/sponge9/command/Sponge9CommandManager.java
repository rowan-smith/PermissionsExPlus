package dev.rono.permissions.sponge9.command;

import cloud.commandframework.CloudCapability;
import cloud.commandframework.Command;
import cloud.commandframework.CommandManager;
import cloud.commandframework.arguments.StaticArgument;
import cloud.commandframework.exceptions.ArgumentParseException;
import cloud.commandframework.exceptions.CommandExecutionException;
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
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.CommandCompletion;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.ArgumentReader;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.plugin.PluginContainer;

/**
 * Cloud command manager for SpongeAPI 8. Sponge accepts registrations only
 * during
 * {@link RegisterCommandEvent}, so Cloud's roots are recorded and registered
 * from
 * that event as raw commands whose input is handed back to Cloud. Sponge 8
 * cannot
 * remove a registered command, so deleting a root disables it instead.
 */
public final class Sponge9CommandManager extends CommandManager<CommandCause> {
    private final PluginContainer plugin;
    private final PlatformLogger logger;
    private final Set<String> roots = ConcurrentHashMap.newKeySet();
    private final Set<String> registered = ConcurrentHashMap.newKeySet();

    public Sponge9CommandManager(PluginContainer plugin, PlatformLogger logger) {
        super(CommandExecutionCoordinator.simpleCoordinator(), CommandRegistrationHandler.nullCommandRegistrationHandler());
        this.plugin = plugin;
        this.logger = logger;

        registerCapability(CloudCapability.StandardCapabilities.ROOT_COMMAND_DELETION);
        commandRegistrationHandler(new Registration());
    }

    @Override
    public boolean hasPermission(CommandCause sender, String permission) {
        return permission.isEmpty() || sender.hasPermission(permission);
    }

    @Override
    public CommandMeta createDefaultCommandMeta() {
        return SimpleCommandMeta.empty();
    }

    /** Registers every Cloud root with Sponge; called from RegisterCommandEvent. */
    public void register(RegisterCommandEvent<org.spongepowered.api.command.Command.Raw> event) {
        for (String root : roots) {
            if (registered.add(root)) {
                event.register(plugin, new Raw(root), root);
            }
        }
    }

    private final class Registration implements CommandRegistrationHandler {
        @Override
        public boolean registerCommand(Command<?> command) {
            String root = command.getArguments().get(0).getName();

            roots.add(root);

            if (!registered.isEmpty() && !registered.contains(root)) {
                logger.warn("Command root /" + root + " was added after Sponge's command registration and will not be available until restart");
            }

            return true;
        }

        @Override
        public void unregisterRootCommand(StaticArgument<?> root) {
            roots.remove(root.getName());
        }
    }

    private final class Raw implements org.spongepowered.api.command.Command.Raw {
        private final String root;

        Raw(String root) {
            this.root = root;
        }

        private String input(String arguments) {
            return arguments.isEmpty() ? root : root + " " + arguments;
        }

        @Override
        public CommandResult process(CommandCause cause, ArgumentReader.Mutable arguments) {
            if (!roots.contains(root)) {
                cause.audience().sendMessage(Component.text("Unknown command.", NamedTextColor.RED));
                return CommandResult.success();
            }

            try {
                executeCommand(cause, input(arguments.remaining())).join();
            } catch (CompletionException wrapped) {
                report(cause, wrapped.getCause() == null ? wrapped : wrapped.getCause());
            } catch (RuntimeException error) {
                report(cause, error);
            }

            return CommandResult.success();
        }

        private void report(CommandCause cause, Throwable error) {
            if (error instanceof InvalidSyntaxException) {
                cause.audience().sendMessage(Component.text("Invalid syntax. Correct usage: /" + ((InvalidSyntaxException) error).getCorrectSyntax(), NamedTextColor.RED));
            } else if (error instanceof NoPermissionException) {
                cause.audience().sendMessage(Component.text("You do not have permission to use this command.", NamedTextColor.RED));
            } else if (error instanceof NoSuchCommandException) {
                cause.audience().sendMessage(Component.text("Unknown command.", NamedTextColor.RED));
            } else if (error instanceof InvalidCommandSenderException) {
                cause.audience().sendMessage(Component.text("This command cannot be used here.", NamedTextColor.RED));
            } else if (error instanceof ArgumentParseException) {
                Throwable inner = error.getCause() == null ? error : error.getCause();

                cause.audience().sendMessage(Component.text("Invalid argument: " + inner.getMessage(), NamedTextColor.RED));
            } else if (error instanceof CommandExecutionException) {
                // Registered handlers (such as the one for expected command failures) decide
                // first.
                handleException(cause, CommandExecutionException.class, (CommandExecutionException) error, (sender, failure) -> internalError(sender, failure));
            } else {
                internalError(cause, error);
            }
        }

        private void internalError(CommandCause cause, Throwable error) {
            cause.audience().sendMessage(Component.text("An internal error occurred while running this command.", NamedTextColor.RED));
            logger.error("PermissionsExPlus command failed: /" + root, error);
        }

        @Override
        public List<CommandCompletion> complete(CommandCause cause, ArgumentReader.Mutable arguments) {
            if (!roots.contains(root)) {
                return Collections.emptyList();
            }

            try {
                List<CommandCompletion> completions = new ArrayList<>();

                for (String suggestion : suggest(cause, input(arguments.remaining()))) {
                    completions.add(CommandCompletion.of(suggestion));
                }

                return completions;
            } catch (RuntimeException error) {
                return Collections.emptyList();
            }
        }

        @Override
        public boolean canExecute(CommandCause cause) {
            return true; // Cloud checks each subcommand's permission.
        }

        @Override
        public Optional<Component> shortDescription(CommandCause cause) {
            return Optional.of(Component.text("PermissionsExPlus commands"));
        }

        @Override
        public Optional<Component> extendedDescription(CommandCause cause) {
            return Optional.empty();
        }

        @Override
        public Component usage(CommandCause cause) {
            return Component.empty();
        }
    }
}
