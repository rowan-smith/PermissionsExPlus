package dev.rono.permissions.core.commands.cloud.classic;

import cloud.commandframework.annotations.CommandMethod;
import dev.rono.permissions.core.commands.CoreCloudCommandContext;

/** Shared default hierarchy route — registered on game servers and proxies. */
public final class ClassicWorldCommand<C> extends AbstractClassicPexCloudCommand<C> {

    public ClassicWorldCommand(CoreCloudCommandContext<C> ctx) {
        super(ctx);
    }

    @CommandMethod("pex hierarchy")
    public void hierarchyDefault(C sender) {
        replyLines(sender, ctx.commandService().hierarchyLines(defaultWorld(sender)));
    }
}
