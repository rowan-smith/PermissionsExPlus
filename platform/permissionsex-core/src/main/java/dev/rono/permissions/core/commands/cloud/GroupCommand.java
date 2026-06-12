package dev.rono.permissions.core.commands.cloud;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import dev.rono.permissions.core.commands.CoreCloudCommandContext;

/**
 * Bare {@code pex group} / {@code pex groups} / default-group commands. Registered after
 * {@link GroupScopedCommand} so {@code pex group &lt;group&gt;} tab completion is not shadowed
 * by the list command.
 *
 * @param <C> command sender type
 */
public final class GroupCommand<C> extends AbstractPexCloudCommand<C> {

    public GroupCommand(CoreCloudCommandContext<C> ctx) {
        super(ctx);
    }

    @CommandMethod("pex group")
    public void groupsListBare(C sender) {
        replyLines(sender, ctx.commandService().knownGroupsLines(defaultWorld(sender)));
    }

    @CommandMethod("pex groups")
    public void groupsList(C sender) {
        replyLines(sender, ctx.commandService().knownGroupsLines(defaultWorld(sender)));
    }

    @CommandMethod("pex groups list")
    public void groupsListExplicit(C sender) {
        replyLines(sender, ctx.commandService().knownGroupsLines(defaultWorld(sender)));
    }

    @CommandMethod("pex groups list <world>")
    public void groupsListWorld(C sender, @Argument(value = "world", suggestions = "pex-world") String world) {
        replyLines(sender, ctx.commandService().knownGroupsLines(world));
    }

    @CommandMethod("pex default group")
    public void defaultGroupsDefaultWorld(C sender) {
        replyLines(sender, ctx.commandService().defaultGroupsLines(defaultWorld(sender)));
    }

    @CommandMethod("pex default group <world>")
    public void defaultGroups(C sender, @Argument(value = "world", suggestions = "pex-world") String world) {
        replyLines(sender, ctx.commandService().defaultGroupsLines(world));
    }

    @CommandMethod("pex set default group <group> <value>")
    public void setDefaultGroupDefaultWorld(C sender,
            @Argument(value = "group", suggestions = "pex-group") String group,
            @Argument("value") String value) {
        reply(sender, ctx.commandService().setDefaultGroup(group, Boolean.parseBoolean(value), defaultWorld(sender)));
    }

    @CommandMethod("pex set default group <group> <value> <world>")
    public void setDefaultGroup(C sender,
            @Argument(value = "group", suggestions = "pex-group") String group,
            @Argument("value") String value,
            @Argument(value = "world", suggestions = "pex-world") String world) {
        reply(sender, ctx.commandService().setDefaultGroup(group, Boolean.parseBoolean(value), world));
    }
}
