package dev.rono.permissions.core.command;

import lombok.var;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import dev.rono.permissions.core.PexApiImpl;

import java.util.function.BiConsumer;

@CommandMethod("pex groups")
public final class GroupsCommands<C> extends CommandSupport<C> {
    GroupsCommands(PexApiImpl<C> core, BiConsumer<C, String> messages) {
        super(core, messages);
    }

    @CommandMethod("")
    @CommandPermission("pex.groups.list")
    public void list(C sender) {
        section(sender, "Groups", await(core.groups().storage().identifiers()));
    }

    @CommandMethod("create <group>")
    @CommandPermission("pex.groups.create")
    public void create(C sender, @Argument("group") String name) {
        await(core.groups().create(name));

        audit(sender, "CREATED group '" + name + "'");

        reply(sender, "§aCreated group §e" + name);
    }

    @CommandMethod("delete <group>")
    @CommandPermission("pex.groups.delete")
    public void delete(C sender, @Argument(value = "group", suggestions = "groups") String name) {
        var deleted = await(core.groups().delete(name));

        if (deleted) {
            audit(sender, "DELETED group '" + name + "'");
        }

        reply(sender, deleted ? "§aDeleted group §e" + name : "§cGroup not found: " + name);
    }
}
