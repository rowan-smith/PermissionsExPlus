package dev.rono.permissions.core.commands.cloud.classic;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.specifier.Greedy;
import dev.rono.permissions.core.commands.CoreCloudCommandContext;

/**
 * {@code pex group &lt;group&gt; …} routes. Parsed before {@link ClassicGroupCommand} so the bare
 * {@code pex group} list command does not shadow the group-argument branch for tab completion.
 *
 * @param <C> command sender type
 */
public final class ClassicGroupScopedCommand<C> extends AbstractClassicPexCloudCommand<C> {

    public ClassicGroupScopedCommand(CoreCloudCommandContext<C> ctx) {
        super(ctx);
    }

    @CommandMethod("pex group <group>")
    public void groupDetail(C sender, @Argument(value = "group", suggestions = "pex-group") String groupIdentifier) {
        try {
            var view = ctx.commandService().groupView(groupIdentifier);
            reply(sender, "Group " + view.name() + " permissions: " + view.permissions());
        } catch (IllegalArgumentException ex) {
            reply(sender, ex.getMessage());
        }
    }

    @CommandMethod("pex group <group> list")
    public void groupPermissionsDefault(C sender, @Argument(value = "group", suggestions = "pex-group") String group) {
        replyLines(sender, ctx.commandService().groupPermissionsLines(group, defaultWorld(sender)));
    }

    @CommandMethod("pex group <group> list <world>")
    public void groupPermissions(C sender,
            @Argument(value = "group", suggestions = "pex-group") String group,
            @Argument(value = "world", suggestions = "pex-world") String world) {
        replyLines(sender, ctx.commandService().groupPermissionsLines(group, world));
    }

    @CommandMethod("pex group <group> add <permission>")
    public void groupAddDefault(C sender,
            @Argument(value = "group", suggestions = "pex-group") String group,
            @Argument(value = "permission", suggestions = "pex-permission") String permission) {
        reply(sender, ctx.commandService().groupAddPermission(group, permission, defaultWorld(sender)));
    }

    @CommandMethod("pex group <group> add <permission> <world>")
    public void groupAdd(C sender,
            @Argument(value = "group", suggestions = "pex-group") String group,
            @Argument(value = "permission", suggestions = "pex-permission") String permission,
            @Argument(value = "world", suggestions = "pex-world") String world) {
        reply(sender, ctx.commandService().groupAddPermission(group, permission, world));
    }

    @CommandMethod("pex group <group> remove <permission>")
    public void groupRemoveDefault(C sender,
            @Argument(value = "group", suggestions = "pex-group") String group,
            @Argument(value = "permission", suggestions = "pex-permission") String permission) {
        reply(sender, ctx.commandService().groupRemovePermission(group, permission, defaultWorld(sender)));
    }

    @CommandMethod("pex group <group> remove <permission> <world>")
    public void groupRemove(C sender,
            @Argument(value = "group", suggestions = "pex-group") String group,
            @Argument(value = "permission", suggestions = "pex-permission") String permission,
            @Argument(value = "world", suggestions = "pex-world") String world) {
        reply(sender, ctx.commandService().groupRemovePermission(group, permission, world));
    }

    @CommandMethod("pex group <group> swap <permission> <targetPermission>")
    public void groupSwapDefault(C sender,
            @Argument(value = "group", suggestions = "pex-group") String group,
            @Argument(value = "permission", suggestions = "pex-permission") String permission,
            @Argument(value = "targetPermission", suggestions = "pex-permission") String targetPermission) {
        reply(sender, ctx.commandService().groupSwapPermission(group, permission, targetPermission, defaultWorld(sender)));
    }

    @CommandMethod("pex group <group> swap <permission> <targetPermission> <world>")
    public void groupSwap(C sender,
            @Argument(value = "group", suggestions = "pex-group") String group,
            @Argument(value = "permission", suggestions = "pex-permission") String permission,
            @Argument(value = "targetPermission", suggestions = "pex-permission") String targetPermission,
            @Argument(value = "world", suggestions = "pex-world") String world) {
        reply(sender, ctx.commandService().groupSwapPermission(group, permission, targetPermission, world));
    }

    @CommandMethod("pex group <group> timed add <permission>")
    public void groupTimedAddNoLifetimeDefault(C sender,
            @Argument(value = "group", suggestions = "pex-group") String group,
            @Argument(value = "permission", suggestions = "pex-permission") String permission) {
        reply(sender, ctx.commandService().groupAddTimedPermission(group, permission, null, defaultWorld(sender)));
    }

    @CommandMethod("pex group <group> timed add <permission> <lifetime>")
    public void groupTimedAddLifetimeDefault(C sender,
            @Argument(value = "group", suggestions = "pex-group") String group,
            @Argument(value = "permission", suggestions = "pex-permission") String permission,
            @Argument("lifetime") String lifetime) {
        reply(sender, ctx.commandService().groupAddTimedPermission(group, permission, lifetime, defaultWorld(sender)));
    }

    @CommandMethod("pex group <group> timed add <permission> <lifetime> <world>")
    public void groupTimedAdd(C sender,
            @Argument(value = "group", suggestions = "pex-group") String group,
            @Argument(value = "permission", suggestions = "pex-permission") String permission,
            @Argument("lifetime") String lifetime,
            @Argument(value = "world", suggestions = "pex-world") String world) {
        reply(sender, ctx.commandService().groupAddTimedPermission(group, permission, lifetime, world));
    }

    @CommandMethod("pex group <group> timed remove <permission>")
    public void groupTimedRemoveDefault(C sender,
            @Argument(value = "group", suggestions = "pex-group") String group,
            @Argument(value = "permission", suggestions = "pex-permission") String permission) {
        reply(sender, ctx.commandService().groupRemoveTimedPermission(group, permission, defaultWorld(sender)));
    }

    @CommandMethod("pex group <group> timed remove <permission> <world>")
    public void groupTimedRemove(C sender,
            @Argument(value = "group", suggestions = "pex-group") String group,
            @Argument(value = "permission", suggestions = "pex-permission") String permission,
            @Argument(value = "world", suggestions = "pex-world") String world) {
        reply(sender, ctx.commandService().groupRemoveTimedPermission(group, permission, world));
    }

    @CommandMethod("pex group <group> parents list")
    public void parentsListDefault(C sender, @Argument(value = "group", suggestions = "pex-group") String group) {
        replyLines(sender, ctx.commandService().groupParentListLines(group, defaultWorld(sender)));
    }

    @CommandMethod("pex group <group> parents list <world>")
    public void parentsList(C sender,
            @Argument(value = "group", suggestions = "pex-group") String group,
            @Argument(value = "world", suggestions = "pex-world") String world) {
        replyLines(sender, ctx.commandService().groupParentListLines(group, world));
    }

    @CommandMethod("pex group <group> parents set <parents>")
    public void parentsSetDefault(C sender,
            @Argument(value = "group", suggestions = "pex-group") String group,
            @Argument(value = "parents", suggestions = "pex-groups-csv") String parents) {
        reply(sender, ctx.commandService().groupSetParents(group, parents, defaultWorld(sender)));
    }

    @CommandMethod("pex group <group> parents set <parents> <world>")
    public void parentsSet(C sender,
            @Argument(value = "group", suggestions = "pex-group") String group,
            @Argument(value = "parents", suggestions = "pex-groups-csv") String parents,
            @Argument(value = "world", suggestions = "pex-world") String world) {
        reply(sender, ctx.commandService().groupSetParents(group, parents, world));
    }

    @CommandMethod("pex group <group> parents add <parents>")
    public void parentsAddDefault(C sender,
            @Argument(value = "group", suggestions = "pex-group") String group,
            @Argument(value = "parents", suggestions = "pex-groups-csv") String parents) {
        reply(sender, ctx.commandService().groupAddParents(group, parents, defaultWorld(sender)));
    }

    @CommandMethod("pex group <group> parents add <parents> <world>")
    public void parentsAdd(C sender,
            @Argument(value = "group", suggestions = "pex-group") String group,
            @Argument(value = "parents", suggestions = "pex-groups-csv") String parents,
            @Argument(value = "world", suggestions = "pex-world") String world) {
        reply(sender, ctx.commandService().groupAddParents(group, parents, world));
    }

    @CommandMethod("pex group <group> parents remove <parents>")
    public void parentsRemoveDefault(C sender,
            @Argument(value = "group", suggestions = "pex-group") String group,
            @Argument(value = "parents", suggestions = "pex-groups-csv") String parents) {
        reply(sender, ctx.commandService().groupRemoveParents(group, parents, defaultWorld(sender)));
    }

    @CommandMethod("pex group <group> parents remove <parents> <world>")
    public void parentsRemove(C sender,
            @Argument(value = "group", suggestions = "pex-group") String group,
            @Argument(value = "parents", suggestions = "pex-groups-csv") String parents,
            @Argument(value = "world", suggestions = "pex-world") String world) {
        reply(sender, ctx.commandService().groupRemoveParents(group, parents, world));
    }

    @CommandMethod("pex group <group> rank")
    public void rank(C sender, @Argument(value = "group", suggestions = "pex-group") String group) {
        reply(sender, ctx.commandService().groupRank(group, null, null));
    }

    @CommandMethod("pex group <group> rank <rank>")
    public void rankValue(C sender,
            @Argument(value = "group", suggestions = "pex-group") String group,
            @Argument("rank") String rank) {
        reply(sender, ctx.commandService().groupRank(group, rank, null));
    }

    @CommandMethod("pex group <group> rank <rank> <ladder>")
    public void rankLadder(C sender,
            @Argument(value = "group", suggestions = "pex-group") String group,
            @Argument("rank") String rank,
            @Argument(value = "ladder", suggestions = "pex-ladder") String ladder) {
        reply(sender, ctx.commandService().groupRank(group, rank, ladder));
    }

    @CommandMethod("pex group <group> weight")
    public void weight(C sender, @Argument(value = "group", suggestions = "pex-group") String group) {
        reply(sender, ctx.commandService().groupWeight(group, null));
    }

    @CommandMethod("pex group <group> weight <weight>")
    public void weightSet(C sender,
            @Argument(value = "group", suggestions = "pex-group") String group,
            @Argument("weight") String weight) {
        reply(sender, ctx.commandService().groupWeight(group, weight));
    }

    @CommandMethod("pex group <group> toggle debug")
    public void toggleDebug(C sender, @Argument(value = "group", suggestions = "pex-group") String group) {
        reply(sender, ctx.commandService().groupToggleDebug(group));
    }

    @CommandMethod("pex group <group> prefix")
    public void prefixShow(C sender, @Argument(value = "group", suggestions = "pex-group") String group) {
        reply(sender, ctx.commandService().groupPrefix(group, null, defaultWorld(sender)));
    }

    @CommandMethod("pex group <group> prefix <newprefix>")
    public void prefixSetGreedy(C sender,
            @Argument(value = "group", suggestions = "pex-group") String group,
            @Argument(value = "newprefix") @Greedy String newPrefix) {
        reply(sender, ctx.commandService().groupPrefix(group, newPrefix, defaultWorld(sender)));
    }

    @CommandMethod("pex group <group> prefix <newprefix> <world>")
    public void prefixSetWorld(C sender,
            @Argument(value = "group", suggestions = "pex-group") String group,
            @Argument("newprefix") String newPrefix,
            @Argument(value = "world", suggestions = "pex-world") String world) {
        reply(sender, ctx.commandService().groupPrefix(group, newPrefix, world));
    }

    @CommandMethod("pex group <group> suffix")
    public void suffixShow(C sender, @Argument(value = "group", suggestions = "pex-group") String group) {
        reply(sender, ctx.commandService().groupSuffix(group, null, defaultWorld(sender)));
    }

    @CommandMethod("pex group <group> suffix <newsuffix>")
    public void suffixSetGreedy(C sender,
            @Argument(value = "group", suggestions = "pex-group") String group,
            @Argument(value = "newsuffix") @Greedy String newSuffix) {
        reply(sender, ctx.commandService().groupSuffix(group, newSuffix, defaultWorld(sender)));
    }

    @CommandMethod("pex group <group> suffix <newsuffix> <world>")
    public void suffixSetWorld(C sender,
            @Argument(value = "group", suggestions = "pex-group") String group,
            @Argument("newsuffix") String newSuffix,
            @Argument(value = "world", suggestions = "pex-world") String world) {
        reply(sender, ctx.commandService().groupSuffix(group, newSuffix, world));
    }

    @CommandMethod("pex group <group> create")
    public void groupCreateBare(C sender, @Argument(value = "group", suggestions = "pex-group") String group) {
        reply(sender, ctx.commandService().groupCreate(group, null));
    }

    @CommandMethod("pex group <group> create <parents>")
    public void groupCreate(C sender,
            @Argument(value = "group", suggestions = "pex-group") String group,
            @Argument(value = "parents", suggestions = "pex-groups-csv") String parents) {
        reply(sender, ctx.commandService().groupCreate(group, parents));
    }

    @CommandMethod("pex group <group> delete")
    public void groupDelete(C sender, @Argument(value = "group", suggestions = "pex-group") String group) {
        reply(sender, ctx.commandService().groupDelete(group));
    }

    @CommandMethod("pex group <group> users")
    public void groupUsers(C sender, @Argument(value = "group", suggestions = "pex-group") String group) {
        replyLines(sender, ctx.commandService().groupUsersLines(group));
    }

    @CommandMethod("pex group <group> user add <user>")
    public void groupUserAddDefault(C sender,
            @Argument(value = "group", suggestions = "pex-group") String group,
            @Argument(value = "user", suggestions = "pex-user") String user) {
        reply(sender, ctx.commandService().userAddGroup(user, group, defaultWorld(sender)));
    }

    @CommandMethod("pex group <group> user add <user> <world>")
    public void groupUserAdd(C sender,
            @Argument(value = "group", suggestions = "pex-group") String group,
            @Argument(value = "user", suggestions = "pex-user") String user,
            @Argument(value = "world", suggestions = "pex-world") String world) {
        reply(sender, ctx.commandService().userAddGroup(user, group, world));
    }

    @CommandMethod("pex group <group> user remove <user>")
    public void groupUserRemoveDefault(C sender,
            @Argument(value = "group", suggestions = "pex-group") String group,
            @Argument(value = "user", suggestions = "pex-user") String user) {
        reply(sender, ctx.commandService().userRemoveGroup(user, group, defaultWorld(sender)));
    }

    @CommandMethod("pex group <group> user remove <user> <world>")
    public void groupUserRemove(C sender,
            @Argument(value = "group", suggestions = "pex-group") String group,
            @Argument(value = "user", suggestions = "pex-user") String user,
            @Argument(value = "world", suggestions = "pex-world") String world) {
        reply(sender, ctx.commandService().userRemoveGroup(user, group, world));
    }
}
