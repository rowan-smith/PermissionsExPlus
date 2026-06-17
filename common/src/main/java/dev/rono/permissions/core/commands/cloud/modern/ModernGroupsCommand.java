package dev.rono.permissions.core.commands.cloud.modern;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import dev.rono.permissions.core.commands.CoreCloudCommandContext;

public final class ModernGroupsCommand<C> extends AbstractModernPexCloudCommand<C> {

    public ModernGroupsCommand(CoreCloudCommandContext<C> ctx) {
        super(ctx);
    }

    @CommandMethod("pex groups")
    public void groupsList(C sender) {
        replyLines(sender, ctx.commandService().knownGroupsLines(null));
    }

    @CommandMethod("pex groups list")
    public void groupsListExplicit(C sender) {
        replyLines(sender, ctx.commandService().knownGroupsLines(null));
    }

    @CommandMethod("pex groups list [flags]")
    public void groupsListContext(
            C sender, @Argument(value = "flags", parserName = "pex-flags") PexCommandFlags flags) {
        replyLines(sender, ctx.commandService().knownGroupsLines(realm(sender, flags)));
    }
}
