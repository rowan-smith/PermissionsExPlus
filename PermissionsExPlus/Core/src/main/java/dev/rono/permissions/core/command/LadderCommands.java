package dev.rono.permissions.core.command;

import lombok.var;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import dev.rono.permissions.api.context.ContextSet;
import dev.rono.permissions.core.PexApiImpl;

import java.util.function.BiConsumer;

@CommandMethod("pex ladder")
public final class LadderCommands<C> extends CommandSupport<C> {
    LadderCommands(PexApiImpl<C> core, BiConsumer<C, String> messages) {
        super(core, messages);
    }

    @CommandMethod("<ladder>")
    @CommandPermission("pex.ladder.info")
    public void info(C sender, @Argument(value = "ladder", suggestions = "ladders") String name) {
        var ladder = core.ladders().cache().get(name).orElse(null);
        if (ladder == null) {
            reply(sender, "§cLadder not found: " + name);
            return;
        }

        heading(sender, "Ladder", ladder.name());
        orderedSection(sender, "Groups", ladder.groups());
    }

    @CommandMethod("<ladder> create")
    @CommandPermission("pex.ladder.create")
    public void create(C sender, @Argument("ladder") String name) {
        await(core.ladders().create(name));

        audit(sender, "CREATED ladder '" + name + "'");

        reply(sender, "§aCreated ladder §e" + name);
    }

    @CommandMethod("<ladder> delete")
    @CommandPermission("pex.ladder.delete")
    public void delete(C sender, @Argument(value = "ladder", suggestions = "ladders") String name) {
        await(core.ladders().delete(name));

        reply(sender, "§aDeleted ladder §e" + name);
    }

    @CommandMethod("<ladder> groups add <group>")
    @CommandPermission("pex.ladder.groups.add")
    public void groupAdd(C sender, @Argument(value = "ladder", suggestions = "ladders") String ladder, @Argument(value = "group", suggestions = "groups") String group) {
        await(core.ladders().modify(ladder, modifier -> modifier.add(group)));

        reply(sender, "§aAdded §f" + group);
    }

    @CommandMethod("<ladder> groups remove <group>")
    @CommandPermission("pex.ladder.groups.remove")
    public void groupRemove(C sender, @Argument(value = "ladder", suggestions = "ladders") String ladder, @Argument(value = "group", suggestions = "groups") String group) {
        await(core.ladders().modify(ladder, modifier -> modifier.remove(group)));

        reply(sender, "§aRemoved §f" + group);
    }

    @CommandMethod("<ladder> groups move <group> <position>")
    @CommandPermission("pex.ladder.groups.move")
    public void groupMove(C sender, @Argument(value = "ladder", suggestions = "ladders") String ladder, @Argument(value = "group", suggestions = "groups") String group, @Argument("position") int position) {
        await(core.ladders().modify(ladder, modifier -> modifier.move(group, position)));

        reply(sender, "§aMoved §f" + group + " §ato position §e" + position);
    }

    @CommandMethod("<ladder> promote <user>")
    @CommandPermission("pex.ladder.promote")
    public void promote(C sender, @Argument(value = "ladder", suggestions = "ladders") String ladder, @Argument(value = "user", suggestions = "users") String user, ContextSet contexts) {
        var subject = await(core.users().find(user)).get();
        var result = await(core.ladders().promote(subject, ladder, contexts));

        heading(sender, "Promotion result", result.status().toString());
    }

    @CommandMethod("<ladder> demote <user>")
    @CommandPermission("pex.ladder.demote")
    public void demote(C sender, @Argument(value = "ladder", suggestions = "ladders") String ladder, @Argument(value = "user", suggestions = "users") String user, ContextSet contexts) {
        var subject = await(core.users().find(user)).get();
        var result = await(core.ladders().demote(subject, ladder, contexts));

        heading(sender, "Demotion result", result.status().toString());
    }
}
