package dev.rono.permissions.core.commands.cloud.classic;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import dev.rono.permissions.core.commands.CoreCloudCommandContext;

/**
 * Bare {@code pex user} / {@code pex users} Cloud commands. Registered after {@link ClassicUserScopedCommand}
 * so {@code pex user &lt;user&gt;} tab completion is not shadowed by the list command.
 *
 * @param <C> command sender type
 */
public final class ClassicUserCommand<C> extends AbstractClassicPexCloudCommand<C> {

    public ClassicUserCommand(CoreCloudCommandContext<C> ctx) {
        super(ctx);
    }

    @CommandMethod("pex user")
    public void userAliasList(C sender) {
        replyLines(sender, ctx.commandService().knownUsersLines());
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
