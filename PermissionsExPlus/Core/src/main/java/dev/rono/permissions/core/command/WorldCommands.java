package dev.rono.permissions.core.command;

import lombok.var;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import cloud.commandframework.annotations.specifier.Greedy;
import dev.rono.permissions.core.PexApiImpl;
import dev.rono.permissions.core.util.Immutables;

import java.util.Arrays;
import java.util.function.BiConsumer;

/** World inheritance, as configured in worlds.yml. */
@CommandMethod("pex")
public final class WorldCommands<C> extends CommandSupport<C> {
    WorldCommands(PexApiImpl<C> core, BiConsumer<C, String> messages) {
        super(core, messages);
    }

    @CommandMethod("worlds")
    @CommandPermission("pex.worlds.list")
    public void worlds(C sender) {
        var all = core.worlds().all();

        section(sender, "World inheritance", all.entrySet().stream().map(entry -> entry.getKey() + " §7inherits §f" + String.join(", ", entry.getValue()))
                .collect(Immutables.toList()));
    }

    @CommandMethod("world <world>")
    @CommandPermission("pex.world.info")
    public void world(C sender, @Argument("world") String world) {
        heading(sender, "World", world);
        orderedSection(sender, "Inherits from", core.worlds().parents(world));
    }

    @CommandMethod("world <world> inherit <parents>")
    @CommandPermission("pex.world.inherit")
    public void inherit(C sender, @Argument("world") String world, @Argument("parents") @Greedy String parents) {
        var list = Arrays.stream(parents.split("[,\\s]+")).filter(value -> !value.isEmpty()).collect(Immutables.toList());

        core.worlds().set(world, list);

        audit(sender, "SET world '" + world + "' inheritance to " + list);

        reply(sender, "§aWorld §e" + world + " §anow inherits from §f" + String.join(", ", core.worlds().parents(world)));
    }

    @CommandMethod("world <world> clear")
    @CommandPermission("pex.world.inherit")
    public void clear(C sender, @Argument("world") String world) {
        core.worlds().set(world, Immutables.listOf());

        audit(sender, "CLEARED world '" + world + "' inheritance");

        reply(sender, "§aWorld §e" + world + " §ano longer inherits from other worlds");
    }
}
