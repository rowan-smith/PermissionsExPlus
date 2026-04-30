package dev.rono.permissions.core.commands.cloud;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import ru.tehkode.permissions.PermissionUser;
import dev.rono.permissions.core.commands.CoreCloudCommandContext;
import ru.tehkode.permissions.exceptions.RankingException;

public final class PromotionCommand<C> extends AbstractPexCloudCommand<C> {

    public PromotionCommand(CoreCloudCommandContext<C> ctx) {
        super(ctx);
    }

    @CommandMethod("pex promote <user>")
    public void pexPromoteDefault(C sender, @Argument(value = "user", suggestions = "pex-user") String user) {
        applyRank(sender, true, user, "default");
    }

    @CommandMethod("pex promote <user> <ladder>")
    public void pexPromote(C sender,
            @Argument(value = "user", suggestions = "pex-user") String user,
            @Argument(value = "ladder", suggestions = "pex-ladder") String ladder) {
        applyRank(sender, true, user, ladder);
    }

    @CommandMethod("pex demote <user>")
    public void pexDemoteDefault(C sender, @Argument(value = "user", suggestions = "pex-user") String user) {
        applyRank(sender, false, user, "default");
    }

    @CommandMethod("pex demote <user> <ladder>")
    public void pexDemote(C sender,
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
            reply(sender, promote ? ctx.commandService().promote(user, actor, ladder) : ctx.commandService().demote(user, actor, ladder));
        } catch (RankingException ex) {
            reply(sender, (promote ? "Promotion error: " : "Demotion error: ") + ex.getMessage());
        }
    }
}
