package dev.rono.permissions.core.commands.cloud.modern;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import dev.rono.permissions.core.commands.CoreCloudCommandContext;

public final class ModernUsersCommand<C> extends AbstractModernPexCloudCommand<C> {

    public ModernUsersCommand(CoreCloudCommandContext<C> ctx) {
        super(ctx);
    }

    @CommandMethod("pex users")
    public void usersList(C sender) {
        replyLines(sender, ctx.commandService().knownUsersLines());
    }

    @CommandMethod("pex users list")
    public void usersListExplicit(C sender) {
        replyLines(sender, ctx.commandService().knownUsersLines());
    }

    @CommandMethod("pex users cleanup <group>")
    public void usersCleanup(C sender, @Argument(value = "group", suggestions = "pex-group") String group) {
        reply(sender, ctx.commandService().usersCleanup(group, null));
    }

    @CommandMethod("pex users cleanup <group> <threshold>")
    public void usersCleanupThreshold(
            C sender,
            @Argument(value = "group", suggestions = "pex-group") String group,
            @Argument("threshold") String threshold) {
        reply(sender, ctx.commandService().usersCleanup(group, threshold));
    }
}
