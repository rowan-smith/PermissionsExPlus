package dev.rono.permissions.core.commands.cloud;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import java.util.Arrays;
import java.util.List;
import dev.rono.permissions.core.commands.CoreCloudCommandContext;

/**
 * {@code pex worlds}, {@code pex hierarchy <world>}, and {@code pex world <world>} — game servers only (not proxies).
 */
public final class WorldGameSubtreeCommand<C> extends AbstractPexCloudCommand<C> {

    public WorldGameSubtreeCommand(CoreCloudCommandContext<C> ctx) {
        super(ctx);
    }

    @CommandMethod("pex worlds")
    public void worlds(C sender) {
        replyLines(sender, ctx.commandService().worldsTreeLines());
    }

    @CommandMethod("pex hierarchy <world>")
    public void hierarchy(C sender, @Argument(value = "world", suggestions = "pex-world") String world) {
        replyLines(sender, ctx.commandService().hierarchyLines(world));
    }

    @CommandMethod("pex world <world>")
    public void worldInherit(C sender, @Argument(value = "world", suggestions = "pex-world") String world) {
        replyLines(sender, ctx.commandService().worldInheritanceLines(world));
    }

    @CommandMethod("pex world <world> inherit <parents>")
    public void worldInheritSet(
            C sender,
            @Argument(value = "world", suggestions = "pex-world") String world,
            @Argument(value = "parents", suggestions = "pex-world") String parents) {
        List<String> list = Arrays.stream(parents.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
        reply(sender, ctx.commandService().setWorldInheritance(world, list));
    }
}
