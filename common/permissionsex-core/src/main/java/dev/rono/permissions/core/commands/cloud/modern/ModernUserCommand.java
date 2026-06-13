package dev.rono.permissions.core.commands.cloud.modern;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import dev.rono.permissions.core.commands.CoreCloudCommandContext;

public final class ModernUserCommand<C> extends AbstractModernPexCloudCommand<C> {

    public ModernUserCommand(CoreCloudCommandContext<C> ctx) {
        super(ctx);
    }

    @CommandMethod("pex user <user>")
    public void userInfo(C sender, @Argument(value = "user", suggestions = "pex-user") String user) {
        showInfo(sender, user);
    }

    @CommandMethod("pex user <user> info")
    public void userInfoExplicit(C sender, @Argument(value = "user", suggestions = "pex-user") String user) {
        showInfo(sender, user);
    }

    @CommandMethod("pex user <user> permissions list [flags]")
    public void permissionsList(
            C sender,
            @Argument(value = "user", suggestions = "pex-user") String user,
            @Argument(value = "flags", parserName = "pex-flags") PexCommandFlags flags) {
        replyLines(sender, ctx.commandService().userPermissionsLines(user, realm(sender, flags)));
    }

    @CommandMethod("pex user <user> permissions add <permission> [flags]")
    public void permissionsAdd(
            C sender,
            @Argument(value = "user", suggestions = "pex-user") String user,
            @Argument(value = "permission", suggestions = "pex-permission") String permission,
            @Argument(value = "flags", parserName = "pex-flags") PexCommandFlags flags) {
        reply(sender, ctx.commandService().userAddPermission(user, permission, realm(sender, flags)));
    }

    @CommandMethod("pex user <user> permissions remove <permission> [flags]")
    public void permissionsRemove(
            C sender,
            @Argument(value = "user", suggestions = "pex-user") String user,
            @Argument(value = "permission", suggestions = "pex-permission") String permission,
            @Argument(value = "flags", parserName = "pex-flags") PexCommandFlags flags) {
        reply(sender, ctx.commandService().userRemovePermission(user, permission, realm(sender, flags)));
    }

    @CommandMethod("pex user <user> permissions check <permission> [flags]")
    public void permissionsCheck(
            C sender,
            @Argument(value = "user", suggestions = "pex-user") String user,
            @Argument(value = "permission", suggestions = "pex-permission") String permission,
            @Argument(value = "flags", parserName = "pex-flags") PexCommandFlags flags) {
        reply(sender, ctx.commandService().userHas(user, permission, realm(sender, flags)));
    }

    @CommandMethod("pex user <user> permissions trace <permission> [flags]")
    public void permissionsTrace(
            C sender,
            @Argument(value = "user", suggestions = "pex-user") String user,
            @Argument(value = "permission", suggestions = "pex-permission") String permission,
            @Argument(value = "flags", parserName = "pex-flags") PexCommandFlags flags) {
        replyLines(sender, ctx.commandService().userPermissionTraceLines(user, permission, realm(sender, flags)));
    }

    @CommandMethod("pex user <user> permissions timed list [flags]")
    public void timedList(
            C sender,
            @Argument(value = "user", suggestions = "pex-user") String user,
            @Argument(value = "flags", parserName = "pex-flags") PexCommandFlags flags) {
        replyLines(sender, ctx.commandService().userTimedPermissionsLines(user, realm(sender, flags)));
    }

    @CommandMethod("pex user <user> permissions timed add <permission> <duration> [flags]")
    public void timedAdd(
            C sender,
            @Argument(value = "user", suggestions = "pex-user") String user,
            @Argument(value = "permission", suggestions = "pex-permission") String permission,
            @Argument("duration") String duration,
            @Argument(value = "flags", parserName = "pex-flags") PexCommandFlags flags) {
        try {
            reply(sender, ModernTimedSupport.addUserTimedPermission(
                    ctx.commandService(), user, permission, duration, realm(sender, flags)));
        } catch (Exception ex) {
            replyError(sender, ex);
        }
    }

    @CommandMethod("pex user <user> permissions timed remove <permission> [flags]")
    public void timedRemove(
            C sender,
            @Argument(value = "user", suggestions = "pex-user") String user,
            @Argument(value = "permission", suggestions = "pex-permission") String permission,
            @Argument(value = "flags", parserName = "pex-flags") PexCommandFlags flags) {
        reply(sender, ctx.commandService().userRemoveTimedPermission(user, permission, realm(sender, flags)));
    }

    @CommandMethod("pex user <user> groups list [flags]")
    public void groupsList(
            C sender,
            @Argument(value = "user", suggestions = "pex-user") String user,
            @Argument(value = "flags", parserName = "pex-flags") PexCommandFlags flags) {
        replyLines(sender, ctx.commandService().userGroupListLines(user, realm(sender, flags)));
    }

    @CommandMethod("pex user <user> groups add <group> [flags]")
    public void groupsAdd(
            C sender,
            @Argument(value = "user", suggestions = "pex-user") String user,
            @Argument(value = "group", suggestions = "pex-group") String group,
            @Argument(value = "flags", parserName = "pex-flags") PexCommandFlags flags) {
        reply(sender, ctx.commandService().userAddGroup(user, group, realm(sender, flags)));
    }

    @CommandMethod("pex user <user> groups remove <group> [flags]")
    public void groupsRemove(
            C sender,
            @Argument(value = "user", suggestions = "pex-user") String user,
            @Argument(value = "group", suggestions = "pex-group") String group,
            @Argument(value = "flags", parserName = "pex-flags") PexCommandFlags flags) {
        reply(sender, ctx.commandService().userRemoveGroup(user, group, realm(sender, flags)));
    }

    @CommandMethod("pex user <user> groups set <groups> [flags]")
    public void groupsSet(
            C sender,
            @Argument(value = "user", suggestions = "pex-user") String user,
            @Argument(value = "groups", suggestions = "pex-groups-csv") String groups,
            @Argument(value = "flags", parserName = "pex-flags") PexCommandFlags flags) {
        reply(sender, ctx.commandService().userSetGroups(user, groups, realm(sender, flags)));
    }

    @CommandMethod("pex user <user> groups timed list [flags]")
    public void groupsTimedList(
            C sender,
            @Argument(value = "user", suggestions = "pex-user") String user,
            @Argument(value = "flags", parserName = "pex-flags") PexCommandFlags flags) {
        replyLines(sender, ctx.commandService().userTimedGroupsLines(user, realm(sender, flags)));
    }

    @CommandMethod("pex user <user> groups timed add <group> <duration> [flags]")
    public void groupsTimedAdd(
            C sender,
            @Argument(value = "user", suggestions = "pex-user") String user,
            @Argument(value = "group", suggestions = "pex-group") String group,
            @Argument("duration") String duration,
            @Argument(value = "flags", parserName = "pex-flags") PexCommandFlags flags) {
        try {
            reply(sender, ModernTimedSupport.addUserTimedGroup(
                    ctx.commandService(), user, group, duration, realm(sender, flags)));
        } catch (Exception ex) {
            replyError(sender, ex);
        }
    }

    @CommandMethod("pex user <user> groups timed remove <group> [flags]")
    public void groupsTimedRemove(
            C sender,
            @Argument(value = "user", suggestions = "pex-user") String user,
            @Argument(value = "group", suggestions = "pex-group") String group,
            @Argument(value = "flags", parserName = "pex-flags") PexCommandFlags flags) {
        reply(sender, ctx.commandService().userRemoveGroup(user, group, realm(sender, flags)));
    }

    @CommandMethod("pex user <user> options list [flags]")
    public void optionsList(
            C sender,
            @Argument(value = "user", suggestions = "pex-user") String user,
            @Argument(value = "flags", parserName = "pex-flags") PexCommandFlags flags) {
        replyLines(sender, ctx.commandService().userOptionsListLines(user, realm(sender, flags)));
    }

    @CommandMethod("pex user <user> options get <option> [flags]")
    public void optionsGet(
            C sender,
            @Argument(value = "user", suggestions = "pex-user") String user,
            @Argument("option") String option,
            @Argument(value = "flags", parserName = "pex-flags") PexCommandFlags flags) {
        reply(sender, ctx.commandService().userGetOption(user, option, realm(sender, flags)));
    }

    @CommandMethod("pex user <user> options set <option> <value> [flags]")
    public void optionsSet(
            C sender,
            @Argument(value = "user", suggestions = "pex-user") String user,
            @Argument("option") String option,
            @Argument("value") String value,
            @Argument(value = "flags", parserName = "pex-flags") PexCommandFlags flags) {
        reply(sender, ctx.commandService().userSetOption(user, option, value, realm(sender, flags)));
    }

    @CommandMethod("pex user <user> options unset <option> [flags]")
    public void optionsUnset(
            C sender,
            @Argument(value = "user", suggestions = "pex-user") String user,
            @Argument("option") String option,
            @Argument(value = "flags", parserName = "pex-flags") PexCommandFlags flags) {
        reply(sender, ctx.commandService().userOptionUnset(user, option, realm(sender, flags)));
    }

    @CommandMethod("pex user <user> delete")
    public void delete(C sender, @Argument(value = "user", suggestions = "pex-user") String user) {
        reply(sender, ctx.commandService().userDelete(user));
    }

    private void showInfo(C sender, String user) {
        var view = ctx.commandService().userView(user);
        reply(sender, "User " + view.identifier() + "/" + view.name());
        reply(sender, "Groups: " + view.groups());
        reply(sender, "Permissions: " + view.permissions());
    }
}
