package dev.rono.permissions.core.command;

import lombok.var;

import cloud.commandframework.CloudCapability;
import cloud.commandframework.CommandManager;
import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.meta.SimpleCommandMeta;
import dev.rono.permissions.api.context.ContextRegistry;
import dev.rono.permissions.core.PexApiImpl;

import java.util.function.BiConsumer;

public class CommandManagerImpl<C> {
    private final CommandManager<C> manager;
    private final AnnotationParser<C> parser;

    public CommandManagerImpl(CommandManager<C> manager, Class<C> senderType, ContextRegistry contextRegistry) {
        this.manager = manager;
        this.parser = new AnnotationParser<>(manager, senderType, parameters -> SimpleCommandMeta.empty());
        new PosixContextFlagExtractor<C>(contextRegistry).register(manager);
    }

    public void registerDefaultCommands(PexApiImpl<C> core, BiConsumer<C, String> messages) {
        register(new CommandSuggestions<>(core, messages));
        register(new RootCommands<>(core, messages));
        register(new BackendCommands<>(core, messages));
        register(new GroupsCommands<>(core, messages));
        register(new GroupCommands<>(core, messages));
        register(new UsersCommands<>(core, messages));
        register(new UserCommands<>(core, messages));
        register(new LaddersCommands<>(core, messages));
        register(new LadderCommands<>(core, messages));
    }

    public CommandManager<C> manager() {
        return manager;
    }

    public <T> void register(T commandInstance) {
        parser.parse(commandInstance);
        // Registration rebuilds node permissions, so widen them again after each one.
        CommandTreePermissions.widenExecutableParents(manager);
    }

    public void unregister(String commandName) {
        if (manager.hasCapability(CloudCapability.StandardCapabilities.ROOT_COMMAND_DELETION)) {
            manager.deleteRootCommand(commandName);
        }
    }

    public void clear() {
        var roots = manager.rootCommands();

        for (var root : roots.toArray(new String[0])) {
            unregister(root);
        }
    }
}
