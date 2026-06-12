package dev.rono.permissions.core.commands.cloud;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.specifier.Greedy;
import dev.rono.permissions.core.commands.CoreCloudCommandContext;

/**
 * {@code pex user &lt;user&gt; …} routes. Parsed before {@link UserCommand} so the bare
 * {@code pex user} list command does not shadow the user-argument branch for tab completion.
 *
 * @param <C> command sender type
 */
public final class UserScopedCommand<C> extends AbstractPexCloudCommand<C> {

    public UserScopedCommand(CoreCloudCommandContext<C> ctx) {
        super(ctx);
    }

    @CommandMethod("pex user <user>")
    public void userInfo(C sender, @Argument(value = "user", suggestions = "pex-user") String user) {
        var view = ctx.commandService().userView(user);
        reply(sender, "User " + view.identifier() + "/" + view.name());
        reply(sender, "Groups: " + view.groups());
        reply(sender, "Permissions: " + view.permissions());
    }

    @CommandMethod("pex user <user> list")
    public void userPermissionsDefault(C sender, @Argument(value = "user", suggestions = "pex-user") String user) {
        replyLines(sender, ctx.commandService().userPermissionsLines(user, defaultWorld(sender)));
    }

    @CommandMethod("pex user <user> list <world>")
    public void userPermissions(
            C sender,
            @Argument(value = "user", suggestions = "pex-user") String user,
            @Argument(value = "world", suggestions = "pex-world") String world) {
        replyLines(sender, ctx.commandService().userPermissionsLines(user, world));
    }

    @CommandMethod("pex user <user> add <permission>")
    public void userAddPermissionDefault(C sender,
            @Argument(value = "user", suggestions = "pex-user") String user,
            @Argument(value = "permission", suggestions = "pex-permission") String permission) {
        reply(sender, ctx.commandService().userAddPermission(user, permission, defaultWorld(sender)));
    }

    @CommandMethod("pex user <user> add <permission> <world>")
    public void userAddPermission(C sender,
            @Argument(value = "user", suggestions = "pex-user") String user,
            @Argument(value = "permission", suggestions = "pex-permission") String permission,
            @Argument(value = "world", suggestions = "pex-world") String world) {
        reply(sender, ctx.commandService().userAddPermission(user, permission, world));
    }

    @CommandMethod("pex user <user> remove <permission>")
    public void userRemovePermissionDefault(C sender,
            @Argument(value = "user", suggestions = "pex-user") String user,
            @Argument(value = "permission", suggestions = "pex-permission") String permission) {
        reply(sender, ctx.commandService().userRemovePermission(user, permission, defaultWorld(sender)));
    }

    @CommandMethod("pex user <user> remove <permission> <world>")
    public void userRemovePermission(C sender,
            @Argument(value = "user", suggestions = "pex-user") String user,
            @Argument(value = "permission", suggestions = "pex-permission") String permission,
            @Argument(value = "world", suggestions = "pex-world") String world) {
        reply(sender, ctx.commandService().userRemovePermission(user, permission, world));
    }

    @CommandMethod("pex user <user> swap <permission> <targetPermission>")
    public void userSwapDefault(C sender,
            @Argument(value = "user", suggestions = "pex-user") String user,
            @Argument(value = "permission", suggestions = "pex-permission") String permission,
            @Argument(value = "targetPermission", suggestions = "pex-permission") String targetPermission) {
        reply(sender, ctx.commandService().userSwapPermission(user, permission, targetPermission, defaultWorld(sender)));
    }

    @CommandMethod("pex user <user> swap <permission> <targetPermission> <world>")
    public void userSwap(C sender,
            @Argument(value = "user", suggestions = "pex-user") String user,
            @Argument(value = "permission", suggestions = "pex-permission") String permission,
            @Argument(value = "targetPermission", suggestions = "pex-permission") String targetPermission,
            @Argument(value = "world", suggestions = "pex-world") String world) {
        reply(sender, ctx.commandService().userSwapPermission(user, permission, targetPermission, world));
    }

    @CommandMethod("pex user <user> timed add <permission>")
    public void userTimedAddNoLifetimeDefault(C sender,
            @Argument(value = "user", suggestions = "pex-user") String user,
            @Argument(value = "permission", suggestions = "pex-permission") String permission) {
        reply(sender, ctx.commandService().userAddTimedPermission(user, permission, null, defaultWorld(sender)));
    }

    @CommandMethod("pex user <user> timed add <permission> <lifetime>")
    public void userTimedAddLifetimeDefault(C sender,
            @Argument(value = "user", suggestions = "pex-user") String user,
            @Argument(value = "permission", suggestions = "pex-permission") String permission,
            @Argument("lifetime") String lifetime) {
        reply(sender, ctx.commandService().userAddTimedPermission(user, permission, lifetime, defaultWorld(sender)));
    }

    @CommandMethod("pex user <user> timed add <permission> <lifetime> <world>")
    public void userTimedAdd(C sender,
            @Argument(value = "user", suggestions = "pex-user") String user,
            @Argument(value = "permission", suggestions = "pex-permission") String permission,
            @Argument("lifetime") String lifetime,
            @Argument(value = "world", suggestions = "pex-world") String world) {
        reply(sender, ctx.commandService().userAddTimedPermission(user, permission, lifetime, world));
    }

    @CommandMethod("pex user <user> timed remove <permission>")
    public void userTimedRemoveDefault(C sender,
            @Argument(value = "user", suggestions = "pex-user") String user,
            @Argument(value = "permission", suggestions = "pex-permission") String permission) {
        reply(sender, ctx.commandService().userRemoveTimedPermission(user, permission, defaultWorld(sender)));
    }

    @CommandMethod("pex user <user> timed remove <permission> <world>")
    public void userTimedRemove(C sender,
            @Argument(value = "user", suggestions = "pex-user") String user,
            @Argument(value = "permission", suggestions = "pex-permission") String permission,
            @Argument(value = "world", suggestions = "pex-world") String world) {
        reply(sender, ctx.commandService().userRemoveTimedPermission(user, permission, world));
    }

    @CommandMethod("pex user <user> set <option> <value>")
    public void userSetOptionDefault(C sender,
            @Argument(value = "user", suggestions = "pex-user") String user,
            @Argument("option") String option,
            @Argument("value") String value) {
        reply(sender, ctx.commandService().userSetOption(user, option, value, defaultWorld(sender)));
    }

    @CommandMethod("pex user <user> set <option> <value> <world>")
    public void userSetOption(C sender,
            @Argument(value = "user", suggestions = "pex-user") String user,
            @Argument("option") String option,
            @Argument("value") String value,
            @Argument(value = "world", suggestions = "pex-world") String world) {
        reply(sender, ctx.commandService().userSetOption(user, option, value, world));
    }

    @CommandMethod("pex user <user> group list")
    public void userGroupListDefault(C sender, @Argument(value = "user", suggestions = "pex-user") String user) {
        replyLines(sender, ctx.commandService().userGroupListLines(user, defaultWorld(sender)));
    }

    @CommandMethod("pex user <user> group list <world>")
    public void userGroupList(C sender,
            @Argument(value = "user", suggestions = "pex-user") String user,
            @Argument(value = "world", suggestions = "pex-world") String world) {
        replyLines(sender, ctx.commandService().userGroupListLines(user, world));
    }

    @CommandMethod("pex user <user> group set <group>")
    public void userGroupSetDefault(C sender,
            @Argument(value = "user", suggestions = "pex-user") String user,
            @Argument(value = "group", suggestions = "pex-groups-csv") String groupsCsv) {
        reply(sender, ctx.commandService().userSetGroups(user, groupsCsv, defaultWorld(sender)));
    }

    @CommandMethod("pex user <user> group set <group> <world>")
    public void userGroupSet(C sender,
            @Argument(value = "user", suggestions = "pex-user") String user,
            @Argument(value = "group", suggestions = "pex-groups-csv") String groupsCsv,
            @Argument(value = "world", suggestions = "pex-world") String world) {
        reply(sender, ctx.commandService().userSetGroups(user, groupsCsv, world));
    }

    @CommandMethod("pex user <user> delete")
    public void userDelete(C sender, @Argument(value = "user", suggestions = "pex-user") String user) {
        reply(sender, ctx.commandService().userDelete(user));
    }

    @CommandMethod("pex user <user> check <permission>")
    public void userCheckDefault(C sender,
            @Argument(value = "user", suggestions = "pex-user") String user,
            @Argument(value = "permission", suggestions = "pex-permission") String permission) {
        reply(sender, ctx.commandService().userCheck(user, permission, defaultWorld(sender)));
    }

    @CommandMethod("pex user <user> check <permission> <world>")
    public void userCheck(C sender,
            @Argument(value = "user", suggestions = "pex-user") String user,
            @Argument(value = "permission", suggestions = "pex-permission") String permission,
            @Argument(value = "world", suggestions = "pex-world") String world) {
        reply(sender, ctx.commandService().userCheck(user, permission, world));
    }

    @CommandMethod("pex user <user> get <option>")
    public void userGetOptionDefault(C sender,
            @Argument(value = "user", suggestions = "pex-user") String user,
            @Argument("option") String option) {
        reply(sender, ctx.commandService().userGetOption(user, option, defaultWorld(sender)));
    }

    @CommandMethod("pex user <user> get <option> <world>")
    public void userGetOption(C sender,
            @Argument(value = "user", suggestions = "pex-user") String user,
            @Argument("option") String option,
            @Argument(value = "world", suggestions = "pex-world") String world) {
        reply(sender, ctx.commandService().userGetOption(user, option, world));
    }

    @CommandMethod("pex user <user> prefix")
    public void userPrefixShow(C sender, @Argument(value = "user", suggestions = "pex-user") String user) {
        reply(sender, ctx.commandService().userPrefix(user, null, defaultWorld(sender)));
    }

    @CommandMethod("pex user <user> prefix <newprefix>")
    public void userPrefixSetGreedy(C sender,
            @Argument(value = "user", suggestions = "pex-user") String user,
            @Argument(value = "newprefix") @Greedy String newPrefix) {
        reply(sender, ctx.commandService().userPrefix(user, newPrefix, defaultWorld(sender)));
    }

    @CommandMethod("pex user <user> prefix <newprefix> <world>")
    public void userPrefixSetWorld(C sender,
            @Argument(value = "user", suggestions = "pex-user") String user,
            @Argument("newprefix") String newPrefix,
            @Argument(value = "world", suggestions = "pex-world") String world) {
        reply(sender, ctx.commandService().userPrefix(user, newPrefix, world));
    }

    @CommandMethod("pex user <user> suffix")
    public void userSuffixShow(C sender, @Argument(value = "user", suggestions = "pex-user") String user) {
        reply(sender, ctx.commandService().userSuffix(user, null, defaultWorld(sender)));
    }

    @CommandMethod("pex user <user> suffix <newsuffix>")
    public void userSuffixSetGreedy(C sender,
            @Argument(value = "user", suggestions = "pex-user") String user,
            @Argument(value = "newsuffix") @Greedy String newSuffix) {
        reply(sender, ctx.commandService().userSuffix(user, newSuffix, defaultWorld(sender)));
    }

    @CommandMethod("pex user <user> suffix <newsuffix> <world>")
    public void userSuffixSetWorld(C sender,
            @Argument(value = "user", suggestions = "pex-user") String user,
            @Argument("newsuffix") String newSuffix,
            @Argument(value = "world", suggestions = "pex-world") String world) {
        reply(sender, ctx.commandService().userSuffix(user, newSuffix, world));
    }

    @CommandMethod("pex user <user> toggle debug")
    public void userToggleDebug(C sender, @Argument(value = "user", suggestions = "pex-user") String user) {
        reply(sender, ctx.commandService().userToggleDebug(user));
    }

    @CommandMethod("pex user <user> superperms")
    public void userSuperperms(C sender, @Argument(value = "user", suggestions = "pex-user") String user) {
        reply(sender, ctx.senderAdapter().superPermsText(sender, user));
    }

    @CommandMethod("pex user <user> group add <group>")
    public void userGroupAddDefault(C sender,
            @Argument(value = "user", suggestions = "pex-user") String user,
            @Argument(value = "group", suggestions = "pex-group") String group) {
        reply(sender, ctx.commandService().userAddGroup(user, group, defaultWorld(sender)));
    }

    @CommandMethod("pex user <user> group add <group> <world>")
    public void userGroupAddWorld(C sender,
            @Argument(value = "user", suggestions = "pex-user") String user,
            @Argument(value = "group", suggestions = "pex-group") String group,
            @Argument(value = "world", suggestions = "pex-world") String world) {
        reply(sender, ctx.commandService().userAddGroup(user, group, world));
    }

    @CommandMethod("pex user <user> group add <group> <world> <lifetime>")
    public void userGroupAddTimed(C sender,
            @Argument(value = "user", suggestions = "pex-user") String user,
            @Argument(value = "group", suggestions = "pex-group") String group,
            @Argument(value = "world", suggestions = "pex-world") String world,
            @Argument("lifetime") String lifetime) {
        reply(sender, ctx.commandService().userAddGroup(user, group, world, lifetime));
    }

    @CommandMethod("pex user <user> group remove <group>")
    public void userGroupRemoveDefault(C sender,
            @Argument(value = "user", suggestions = "pex-user") String user,
            @Argument(value = "group", suggestions = "pex-group") String group) {
        reply(sender, ctx.commandService().userRemoveGroup(user, group, defaultWorld(sender)));
    }

    @CommandMethod("pex user <user> group remove <group> <world>")
    public void userGroupRemoveWorld(C sender,
            @Argument(value = "user", suggestions = "pex-user") String user,
            @Argument(value = "group", suggestions = "pex-group") String group,
            @Argument(value = "world", suggestions = "pex-world") String world) {
        reply(sender, ctx.commandService().userRemoveGroup(user, group, world));
    }
}
