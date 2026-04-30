package dev.rono.permissions.core.commands.cloud;

import cloud.commandframework.annotations.CommandMethod;
import dev.rono.permissions.core.commands.CoreCloudCommandContext;

/** Shared default hierarchy route — registered on game servers and proxies. */
public final class WorldCommand<C> extends AbstractPexCloudCommand<C> {

    public WorldCommand(CoreCloudCommandContext<C> ctx) {
        super(ctx);
    }

    @CommandMethod("pex hierarchy")
    public void hierarchyDefault(C sender) {
        replyLines(sender, ctx.commandService().hierarchyLines(defaultWorld(sender)));
    }
}
