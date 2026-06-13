package dev.rono.permissions.core.commands.cloud.modern;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import dev.rono.permissions.core.commands.CoreCloudCommandContext;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.exceptions.RankingException;

/**
 * Shortcut promotion commands for modern mode ({@code /pex promote} delegates to the default ladder).
 */
public final class ModernPromotionCommand<C> extends AbstractModernPexCloudCommand<C> {

    public ModernPromotionCommand(CoreCloudCommandContext<C> ctx) {
        super(ctx);
    }

    @CommandMethod("pex promote <user>")
    public void pexPromoteDefault(C sender, @Argument(value = "user", suggestions = "pex-user") String user) {
        applyRank(sender, true, user, "default");
    }

    @CommandMethod("pex promote <user> <ladder>")
    public void pexPromote(
            C sender,
            @Argument(value = "user", suggestions = "pex-user") String user,
            @Argument(value = "ladder", suggestions = "pex-ladder") String ladder) {
        applyRank(sender, true, user, ladder);
    }

    @CommandMethod("pex demote <user>")
    public void pexDemoteDefault(C sender, @Argument(value = "user", suggestions = "pex-user") String user) {
        applyRank(sender, false, user, "default");
    }

    @CommandMethod("pex demote <user> <ladder>")
    public void pexDemote(
            C sender,
            @Argument(value = "user", suggestions = "pex-user") String user,
            @Argument(value = "ladder", suggestions = "pex-ladder") String ladder) {
        applyRank(sender, false, user, ladder);
    }

    @CommandMethod("promote <user>")
    public void promoteAlias(C sender, @Argument(value = "user", suggestions = "pex-user") String user) {
        applyRank(sender, true, user, "default");
    }

    @CommandMethod("demote <user>")
    public void demoteAlias(C sender, @Argument(value = "user", suggestions = "pex-user") String user) {
        applyRank(sender, false, user, "default");
    }

    private void applyRank(C sender, boolean promote, String user, String ladder) {
        try {
            PermissionUser actor = ctx.senderAdapter().actor(sender);
            reply(sender, promote
                    ? ctx.commandService().ladderPromote(ladder, user, actor)
                    : ctx.commandService().ladderDemote(ladder, user, actor));
        } catch (RankingException ex) {
            reply(sender, (promote ? "Promotion error: " : "Demotion error: ") + ex.getMessage());
        }
    }
}
