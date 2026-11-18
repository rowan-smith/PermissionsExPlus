package dev.rono.permissions.core.command;

import lombok.var;

import cloud.commandframework.Command;
import cloud.commandframework.CommandManager;
import cloud.commandframework.CommandTree;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.permission.CommandPermission;
import cloud.commandframework.permission.OrPermission;

import java.util.ArrayList;
import java.util.List;

/**
 * Works around Cloud 1.8 gating subcommands behind their parent's permission.
 *
 * <p>
 * When a node both runs a command and has subcommands (for example
 * {@code /pex groups} lists groups and {@code /pex groups create} creates
 * one), Cloud stores only the parent command's own permission on that node
 * and checks it while walking to any child. A player holding only
 * {@code pex.groups.create} was therefore refused {@code /pex groups create},
 * and anything below {@code /pex} needed the root command's permission.
 * Cloud also checks the tree's hidden top node, whose permission it builds from
 * leaf commands only. Seen on Paper 1.8.8; Paper 1.21, where Brigadier checks
 * each node itself, was unaffected.
 *
 * <p>
 * This widens each such node's permission to "its own command's permission,
 * or any child's". The command itself still requires its own permission: Cloud
 * checks the owning command's permission again before executing it.
 */
final class CommandTreePermissions {
    private static final String PERMISSION = "permission";

    private CommandTreePermissions() {}

    static <C> void widenExecutableParents(CommandManager<C> manager) {
        var roots = manager.commandTree().getRootNodes();

        if (roots.isEmpty()) {
            return;
        }

        // Start from the tree's hidden top node, above the root commands: Cloud checks
        // its permission first, and builds it from leaf commands only.
        widen(roots.iterator().next().getParent());
    }

    private static <C> CommandPermission widen(CommandTree.Node<CommandArgument<C, ?>> node) {
        CommandArgument<C, ?> value = node.getValue();
        Command<C> owning = value == null ? null : value.getOwningCommand();

        if (node.isLeaf()) {
            Object stored = node.getNodeMeta().get(PERMISSION);

            if (stored instanceof CommandPermission) {
                return (CommandPermission) stored;
            }

            return owning == null ? null : owning.getCommandPermission();
        }

        List<CommandPermission> permissions = new ArrayList<>();

        if (owning != null) {
            permissions.add(owning.getCommandPermission());
        }

        for (CommandTree.Node<CommandArgument<C, ?>> child : node.getChildren()) {
            CommandPermission permission = widen(child);

            if (permission != null) {
                permissions.add(permission);
            }
        }

        if (permissions.isEmpty()) {
            return null;
        }

        CommandPermission widened = permissions.size() == 1 ? permissions.get(0) : OrPermission.of(permissions);
        node.getNodeMeta().put(PERMISSION, widened);

        return widened;
    }
}
