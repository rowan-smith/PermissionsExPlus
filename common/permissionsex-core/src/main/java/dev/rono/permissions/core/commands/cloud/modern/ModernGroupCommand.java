package dev.rono.permissions.core.commands.cloud.modern;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import dev.rono.permissions.core.commands.CoreCloudCommandContext;

public final class ModernGroupCommand<C> extends AbstractModernPexCloudCommand<C> {

    public ModernGroupCommand(CoreCloudCommandContext<C> ctx) {
        super(ctx);
    }

    @CommandMethod("pex group <group>")
    public void groupInfo(C sender, @Argument(value = "group", suggestions = "pex-group") String group) {
        showInfo(sender, group);
    }

    @CommandMethod("pex group <group> info")
    public void groupInfoExplicit(C sender, @Argument(value = "group", suggestions = "pex-group") String group) {
        showInfo(sender, group);
    }

    @CommandMethod("pex group <group> permissions list [flags]")
    public void permissionsList(
            C sender,
            @Argument(value = "group", suggestions = "pex-group") String group,
            @Argument(value = "flags", parserName = "pex-flags") PexCommandFlags flags) {
        replyLines(sender, ctx.commandService().groupPermissionsLines(group, realm(sender, flags)));
    }

    @CommandMethod("pex group <group> permissions add <permission> [flags]")
    public void permissionsAdd(
            C sender,
            @Argument(value = "group", suggestions = "pex-group") String group,
            @Argument(value = "permission", suggestions = "pex-permission") String permission,
            @Argument(value = "flags", parserName = "pex-flags") PexCommandFlags flags) {
        reply(sender, ctx.commandService().groupAddPermission(group, permission, realm(sender, flags)));
    }

    @CommandMethod("pex group <group> permissions remove <permission> [flags]")
    public void permissionsRemove(
            C sender,
            @Argument(value = "group", suggestions = "pex-group") String group,
            @Argument(value = "permission", suggestions = "pex-permission") String permission,
            @Argument(value = "flags", parserName = "pex-flags") PexCommandFlags flags) {
        reply(sender, ctx.commandService().groupRemovePermission(group, permission, realm(sender, flags)));
    }

    @CommandMethod("pex group <group> permissions check <permission> [flags]")
    public void permissionsCheck(
            C sender,
            @Argument(value = "group", suggestions = "pex-group") String group,
            @Argument(value = "permission", suggestions = "pex-permission") String permission,
            @Argument(value = "flags", parserName = "pex-flags") PexCommandFlags flags) {
        reply(sender, ctx.commandService().groupCheckPermission(group, permission, realm(sender, flags)));
    }

    @CommandMethod("pex group <group> permissions trace <permission> [flags]")
    public void permissionsTrace(
            C sender,
            @Argument(value = "group", suggestions = "pex-group") String group,
            @Argument(value = "permission", suggestions = "pex-permission") String permission,
            @Argument(value = "flags", parserName = "pex-flags") PexCommandFlags flags) {
        replyLines(sender, ctx.commandService().groupPermissionTraceLines(group, permission, realm(sender, flags)));
    }

    @CommandMethod("pex group <group> permissions timed list [flags]")
    public void timedList(
            C sender,
            @Argument(value = "group", suggestions = "pex-group") String group,
            @Argument(value = "flags", parserName = "pex-flags") PexCommandFlags flags) {
        replyLines(sender, ctx.commandService().groupTimedPermissionsLines(group, realm(sender, flags)));
    }

    @CommandMethod("pex group <group> permissions timed add <permission> <duration> [flags]")
    public void timedAdd(
            C sender,
            @Argument(value = "group", suggestions = "pex-group") String group,
            @Argument(value = "permission", suggestions = "pex-permission") String permission,
            @Argument("duration") String duration,
            @Argument(value = "flags", parserName = "pex-flags") PexCommandFlags flags) {
        try {
            reply(sender, ModernTimedSupport.addGroupTimedPermission(
                    ctx.commandService(), group, permission, duration, realm(sender, flags)));
        } catch (Exception ex) {
            replyError(sender, ex);
        }
    }

    @CommandMethod("pex group <group> permissions timed remove <permission> [flags]")
    public void timedRemove(
            C sender,
            @Argument(value = "group", suggestions = "pex-group") String group,
            @Argument(value = "permission", suggestions = "pex-permission") String permission,
            @Argument(value = "flags", parserName = "pex-flags") PexCommandFlags flags) {
        reply(sender, ctx.commandService().groupRemoveTimedPermission(group, permission, realm(sender, flags)));
    }

    @CommandMethod("pex group <group> parents list [flags]")
    public void parentsList(
            C sender,
            @Argument(value = "group", suggestions = "pex-group") String group,
            @Argument(value = "flags", parserName = "pex-flags") PexCommandFlags flags) {
        replyLines(sender, ctx.commandService().groupParentListLines(group, realm(sender, flags)));
    }

    @CommandMethod("pex group <group> parents add <parents> [flags]")
    public void parentsAdd(
            C sender,
            @Argument(value = "group", suggestions = "pex-group") String group,
            @Argument(value = "parents", suggestions = "pex-groups-csv") String parents,
            @Argument(value = "flags", parserName = "pex-flags") PexCommandFlags flags) {
        reply(sender, ctx.commandService().groupAddParents(group, parents, realm(sender, flags)));
    }

    @CommandMethod("pex group <group> parents remove <parents> [flags]")
    public void parentsRemove(
            C sender,
            @Argument(value = "group", suggestions = "pex-group") String group,
            @Argument(value = "parents", suggestions = "pex-groups-csv") String parents,
            @Argument(value = "flags", parserName = "pex-flags") PexCommandFlags flags) {
        reply(sender, ctx.commandService().groupRemoveParents(group, parents, realm(sender, flags)));
    }

    @CommandMethod("pex group <group> parents set <parents> [flags]")
    public void parentsSet(
            C sender,
            @Argument(value = "group", suggestions = "pex-group") String group,
            @Argument(value = "parents", suggestions = "pex-groups-csv") String parents,
            @Argument(value = "flags", parserName = "pex-flags") PexCommandFlags flags) {
        reply(sender, ctx.commandService().groupSetParents(group, parents, realm(sender, flags)));
    }

    @CommandMethod("pex group <group> members list")
    public void membersList(C sender, @Argument(value = "group", suggestions = "pex-group") String group) {
        replyLines(sender, ctx.commandService().groupUsersLines(group));
    }

    @CommandMethod("pex group <group> members add <user> [flags]")
    public void membersAdd(
            C sender,
            @Argument(value = "group", suggestions = "pex-group") String group,
            @Argument(value = "user", suggestions = "pex-user") String user,
            @Argument(value = "flags", parserName = "pex-flags") PexCommandFlags flags) {
        reply(sender, ctx.commandService().userAddGroup(user, group, realm(sender, flags)));
    }

    @CommandMethod("pex group <group> members remove <user> [flags]")
    public void membersRemove(
            C sender,
            @Argument(value = "group", suggestions = "pex-group") String group,
            @Argument(value = "user", suggestions = "pex-user") String user,
            @Argument(value = "flags", parserName = "pex-flags") PexCommandFlags flags) {
        reply(sender, ctx.commandService().userRemoveGroup(user, group, realm(sender, flags)));
    }

    @CommandMethod("pex group <group> options list [flags]")
    public void optionsList(
            C sender,
            @Argument(value = "group", suggestions = "pex-group") String group,
            @Argument(value = "flags", parserName = "pex-flags") PexCommandFlags flags) {
        replyLines(sender, ctx.commandService().groupOptionsListLines(group, realm(sender, flags)));
    }

    @CommandMethod("pex group <group> options get <option> [flags]")
    public void optionsGet(
            C sender,
            @Argument(value = "group", suggestions = "pex-group") String group,
            @Argument("option") String option,
            @Argument(value = "flags", parserName = "pex-flags") PexCommandFlags flags) {
        reply(sender, ctx.commandService().groupGetOption(group, option, realm(sender, flags)));
    }

    @CommandMethod("pex group <group> options set <option> <value> [flags]")
    public void optionsSet(
            C sender,
            @Argument(value = "group", suggestions = "pex-group") String group,
            @Argument("option") String option,
            @Argument("value") String value,
            @Argument(value = "flags", parserName = "pex-flags") PexCommandFlags flags) {
        reply(sender, ctx.commandService().groupSetOption(group, option, value, realm(sender, flags)));
    }

    @CommandMethod("pex group <group> options unset <option> [flags]")
    public void optionsUnset(
            C sender,
            @Argument(value = "group", suggestions = "pex-group") String group,
            @Argument("option") String option,
            @Argument(value = "flags", parserName = "pex-flags") PexCommandFlags flags) {
        reply(sender, ctx.commandService().groupOptionUnset(group, option, realm(sender, flags)));
    }

    @CommandMethod("pex group <group> create")
    public void create(C sender, @Argument(value = "group", suggestions = "pex-group") String group) {
        reply(sender, ctx.commandService().groupCreate(group, null));
    }

    @CommandMethod("pex group <group> create <parents>")
    public void createWithParents(
            C sender,
            @Argument(value = "group", suggestions = "pex-group") String group,
            @Argument(value = "parents", suggestions = "pex-groups-csv") String parents) {
        reply(sender, ctx.commandService().groupCreate(group, parents));
    }

    @CommandMethod("pex group <group> delete")
    public void delete(C sender, @Argument(value = "group", suggestions = "pex-group") String group) {
        reply(sender, ctx.commandService().groupDelete(group));
    }

    private void showInfo(C sender, String group) {
        var view = ctx.commandService().groupView(group);
        reply(sender, "Group " + view.name());
        reply(sender, "Permissions: " + view.permissions());
    }
}
