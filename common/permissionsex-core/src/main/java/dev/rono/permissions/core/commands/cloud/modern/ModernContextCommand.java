package dev.rono.permissions.core.commands.cloud.modern;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import dev.rono.permissions.core.commands.CoreCloudCommandContext;

public final class ModernContextCommand<C> extends AbstractModernPexCloudCommand<C> {

    public ModernContextCommand(CoreCloudCommandContext<C> ctx) {
        super(ctx);
    }

    @CommandMethod("pex contexts")
    public void contexts(C sender) {
        replyLines(sender, ctx.commandService().contextsLines(ctx.cloudPlatform()));
    }

    @CommandMethod("pex hierarchy [flags]")
    public void hierarchy(C sender, @Argument(value = "flags", parserName = "pex-flags") PexCommandFlags flags) {
        replyLines(sender, ctx.commandService().hierarchyLines(realm(sender, flags)));
    }
}
