package dev.rono.permissions.core.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cloud.commandframework.CommandManager;
import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import cloud.commandframework.exceptions.NoPermissionException;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.internal.CommandRegistrationHandler;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.meta.SimpleCommandMeta;
import dev.rono.permissions.core.util.Immutables;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletionException;
import org.junit.jupiter.api.Test;

/**
 * A player granted one subcommand's permission must be able to run that
 * subcommand. Found on a real 1.8.8 server: with only pex.groups.list, a player
 * was refused subcommands of a command that also runs on its own unless it held
 * that parent command's permission too.
 */
class CommandPermissionTest {
    /** Sender = the set of permissions it holds. */
    private static final class Manager extends CommandManager<Set<String>> {
        Manager() {
            super(CommandExecutionCoordinator.simpleCoordinator(), CommandRegistrationHandler.nullCommandRegistrationHandler());
        }

        @Override
        public boolean hasPermission(Set<String> sender, String permission) {
            return permission.isEmpty() || sender.contains(permission);
        }

        @Override
        public CommandMeta createDefaultCommandMeta() {
            return SimpleCommandMeta.empty();
        }
    }

    @CommandMethod("pex groups")
    static final class Groups {
        final List<String> ran = new ArrayList<>();

        @CommandMethod("")
        @CommandPermission("pex.groups.list")
        public void list(Set<String> sender) {
            ran.add("list");
        }

        @CommandMethod("create <group>")
        @CommandPermission("pex.groups.create")
        public void create(Set<String> sender, @Argument("group") String group) {
            ran.add("create");
        }

        @CommandMethod("delete <group>")
        @CommandPermission("pex.groups.delete")
        public void delete(Set<String> sender, @Argument("group") String group) {
            ran.add("delete");
        }
    }

    private static Groups register(Manager manager) {
        Groups groups = new Groups();

        new AnnotationParser<>(manager, castSet(), parameters -> SimpleCommandMeta.empty()).parse(groups);
        CommandTreePermissions.widenExecutableParents(manager);

        return groups;
    }

    @SuppressWarnings("unchecked")
    private static Class<Set<String>> castSet() {
        return (Class<Set<String>>) (Class<?>) Set.class;
    }

    @Test
    void oneSubcommandPermissionIsEnoughForThatSubcommand() {
        Manager manager = new Manager();
        Groups groups = register(manager);

        manager.executeCommand(Immutables.setOf("pex.groups.list"), "pex groups").join();
        manager.executeCommand(Immutables.setOf("pex.groups.create"), "pex groups create staff").join();

        assertEquals(Immutables.listOf("list", "create"), groups.ran);
    }

    /**
     * Bukkit checks the tree's hidden top node before any root command, and Cloud
     * builds that node's permission from leaf commands only.
     */
    @Test
    void theTopNodeAdmitsHoldersOfAnExecutableParentPermission() {
        Manager manager = new Manager();
        register(manager);

        Object top = manager.commandTree().getNamedNode("pex").getParent().getNodeMeta().get("permission");

        assertTrue(manager.hasPermission(Immutables.setOf("pex.groups.list"), (cloud.commandframework.permission.CommandPermission) top));
    }

    @Test
    void aSubcommandPermissionDoesNotGrantTheParentCommand() {
        Manager manager = new Manager();
        Groups groups = register(manager);

        CompletionException refused = assertThrows(CompletionException.class,
                () -> manager.executeCommand(Immutables.setOf("pex.groups.create"), "pex groups").join());
        assertInstanceOf(NoPermissionException.class, refused.getCause());

        refused = assertThrows(CompletionException.class,
                () -> manager.executeCommand(Immutables.setOf("pex.groups.list"), "pex groups delete staff").join());
        assertInstanceOf(NoPermissionException.class, refused.getCause());

        assertEquals(Immutables.listOf(), groups.ran);
    }
}
