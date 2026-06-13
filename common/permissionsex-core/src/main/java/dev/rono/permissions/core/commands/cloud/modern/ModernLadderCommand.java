package dev.rono.permissions.core.commands.cloud.modern;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import dev.rono.permissions.core.commands.CoreCloudCommandContext;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.exceptions.RankingException;

public final class ModernLadderCommand<C> extends AbstractModernPexCloudCommand<C> {

    public ModernLadderCommand(CoreCloudCommandContext<C> ctx) {
        super(ctx);
    }

    @CommandMethod("pex ladder")
    public void ladderRoot(C sender) {
        replyLines(sender, ctx.commandService().laddersListLines());
    }

    @CommandMethod("pex ladders")
    public void laddersList(C sender) {
        replyLines(sender, ctx.commandService().laddersListLines());
    }

    @CommandMethod("pex ladder <ladder>")
    public void ladderInfo(C sender, @Argument(value = "ladder", suggestions = "pex-ladder") String ladder) {
        replyLines(sender, ctx.commandService().ladderInfoLines(ladder));
    }

    @CommandMethod("pex ladder <ladder> info")
    public void ladderInfoExplicit(C sender, @Argument(value = "ladder", suggestions = "pex-ladder") String ladder) {
        replyLines(sender, ctx.commandService().ladderInfoLines(ladder));
    }

    @CommandMethod("pex ladder <ladder> groups list")
    public void groupsList(C sender, @Argument(value = "ladder", suggestions = "pex-ladder") String ladder) {
        replyLines(sender, ctx.commandService().ladderGroupsLines(ladder));
    }

    @CommandMethod("pex ladder <ladder> groups add <group>")
    public void groupsAdd(
            C sender,
            @Argument(value = "ladder", suggestions = "pex-ladder") String ladder,
            @Argument(value = "group", suggestions = "pex-group") String group) {
        reply(sender, ctx.commandService().ladderGroupsAdd(ladder, group));
    }

    @CommandMethod("pex ladder <ladder> groups remove <group>")
    public void groupsRemove(
            C sender,
            @Argument(value = "ladder", suggestions = "pex-ladder") String ladder,
            @Argument(value = "group", suggestions = "pex-group") String group) {
        reply(sender, ctx.commandService().ladderGroupsRemove(ladder, group));
    }

    @CommandMethod("pex ladder <ladder> groups move <group> <rank>")
    public void groupsMove(
            C sender,
            @Argument(value = "ladder", suggestions = "pex-ladder") String ladder,
            @Argument(value = "group", suggestions = "pex-group") String group,
            @Argument("rank") String rank) {
        try {
            reply(sender, ctx.commandService().ladderGroupsMove(ladder, group, Integer.parseInt(rank)));
        } catch (NumberFormatException ex) {
            reply(sender, "Rank must be a number.");
        }
    }

    @CommandMethod("pex ladder <ladder> promote <user>")
    public void promote(
            C sender,
            @Argument(value = "ladder", suggestions = "pex-ladder") String ladder,
            @Argument(value = "user", suggestions = "pex-user") String user) {
        applyRank(sender, true, ladder, user);
    }

    @CommandMethod("pex ladder <ladder> demote <user>")
    public void demote(
            C sender,
            @Argument(value = "ladder", suggestions = "pex-ladder") String ladder,
            @Argument(value = "user", suggestions = "pex-user") String user) {
        applyRank(sender, false, ladder, user);
    }

    private void applyRank(C sender, boolean promote, String ladder, String user) {
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
