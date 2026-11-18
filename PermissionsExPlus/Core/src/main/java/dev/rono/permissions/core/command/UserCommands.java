package dev.rono.permissions.core.command;

import lombok.var;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import dev.rono.permissions.api.context.ContextSet;
import dev.rono.permissions.api.parent.ParentNode;
import dev.rono.permissions.core.PexApiImpl;
import dev.rono.permissions.core.util.Durations;

import java.util.function.BiConsumer;
import dev.rono.permissions.core.util.Immutables;

@CommandMethod("pex user")
public final class UserCommands<C> extends CommandSupport<C> {
    UserCommands(PexApiImpl<C> core, BiConsumer<C, String> messages) {
        super(core, messages);
    }

    @CommandMethod("<user>")
    @CommandPermission("pex.user")
    public void info(C sender, @Argument(value = "user", suggestions = "users") String name) {
        var user = await(core.users().find(name)).orElse(null);
        if (user == null) {
            reply(sender, "§cUser not found: " + name);
            return;
        }

        heading(sender, "User", user.name() + " (" + user.uniqueId() + ")");
        section(sender, "Options", user.explicitOptions().stream().map(this::option).collect(Immutables.toList()));
        section(sender, "Groups", user.groups().stream().map(this::parent).collect(Immutables.toList()));
        section(sender, "Permissions", user.explicitPermissions().stream().map(this::permission).collect(Immutables.toList()));
    }

    @CommandMethod("<user> permissions")
    @CommandPermission("pex.user.permissions.list")
    public void permissions(C sender, @Argument(value = "user", suggestions = "users") String name) {
        var user = user(name);

        heading(sender, "User", user.name());
        section(sender, "Permissions", user.explicitPermissions().stream().map(this::permission).collect(Immutables.toList()));
    }

    @CommandMethod("<user> permissions add <permission>")
    @CommandPermission("pex.user.permissions.add")
    public void permissionAdd(C sender, @Argument(value = "user", suggestions = "users") String name, @Argument(value = "permission", suggestions = "user-permissions-add") String permission, ContextSet contexts) {
        var user = user(name);

        await(core.users().modify(user, modifier -> core.expandPermissionNode(permission).forEach(expanded -> modifier.allowPermission(expanded, contexts))));

        audit(sender, "ADDED permission '" + permission + "' to user '" + user.name() + "'");

        reply(sender, "§aAdded §f" + permission + " §ato §e" + user.name());
    }

    @CommandMethod("<user> permissions remove <permission>")
    @CommandPermission("pex.user.permissions.remove")
    public void permissionRemove(C sender, @Argument(value = "user", suggestions = "users") String name, @Argument(value = "permission", suggestions = "user-permissions-remove") String permission, ContextSet contexts) {
        var user = user(name);

        await(core.users().modify(user, modifier -> core.expandPermissionNode(permission).forEach(expanded -> modifier.removePermission(expanded, contexts))));

        audit(sender, "REMOVED permission '" + permission + "' from user '" + user.name() + "'");

        reply(sender, "§aRemoved §f" + permission + " §afrom §e" + user.name());
    }

    @CommandMethod("<user> permissions list")
    @CommandPermission("pex.user.permissions.list")
    public void permissionList(C sender, @Argument(value = "user", suggestions = "users") String name) {
        var user = user(name);

        heading(sender, "User", user.name());
        section(sender, "Permissions", user.explicitPermissions().stream().map(this::permission).collect(Immutables.toList()));
    }

    @CommandMethod("<user> permissions check <permission>")
    @CommandPermission("pex.user.permissions.check")
    public void permissionCheck(C sender, @Argument(value = "user", suggestions = "users") String name, @Argument(value = "permission", suggestions = "permissions") String permission, ContextSet contexts) {
        var user = user(name);

        check(sender, user, permission, queryOptions(contexts));
    }

    @CommandMethod("<user> permissions trace <permission>")
    @CommandPermission("pex.user.permissions.trace")
    public void permissionTrace(C sender, @Argument(value = "user", suggestions = "users") String name, @Argument(value = "permission", suggestions = "permissions") String permission, ContextSet contexts) {
        var user = user(name);

        trace(sender, user, permission, queryOptions(contexts));
    }

    @CommandMethod("<user> groups")
    @CommandPermission("pex.user.groups.list")
    public void groups(C sender, @Argument(value = "user", suggestions = "users") String name) {
        var user = user(name);

        heading(sender, "User", user.name());
        section(sender, "Groups", user.groups().stream().map(this::parent).collect(Immutables.toList()));
    }

    @CommandMethod("<user> groups add <group> [duration]")
    @CommandPermission("pex.user.groups.add")
    public void groupAdd(C sender, @Argument(value = "user", suggestions = "users") String name, @Argument(value = "group", suggestions = "user-groups-add") String group,
            @Argument("duration") String duration, ContextSet contexts) {
        var user = user(name);
        var length = duration == null ? null : Durations.parse(duration);
        var membership = ParentNode.builder().group(group).contexts(contexts);

        if (length != null) {
            membership.duration(length);
        }

        var node = membership.build();

        // Replaces any membership in these contexts, so the new duration (or none)
        // wins.
        await(core.users().modify(user, modifier -> modifier.addGroup(node)));

        reply(sender, "§aAdded §e" + user.name() + " §ato §f" + group + (length == null ? "" : " §afor §f" + Durations.format(length)));
    }

    @CommandMethod("<user> groups list")
    @CommandPermission("pex.user.groups.list")
    public void groupList(C sender, @Argument(value = "user", suggestions = "users") String name) {
        var user = user(name);

        heading(sender, "User", user.name());
        section(sender, "Groups", user.groups().stream().map(this::parent).collect(Immutables.toList()));
    }

    @CommandMethod("<user> groups remove <group>")
    @CommandPermission("pex.user.groups.remove")
    public void groupRemove(C sender, @Argument(value = "user", suggestions = "users") String name, @Argument(value = "group", suggestions = "user-groups-remove") String group, ContextSet contexts) {
        var user = user(name);

        await(core.users().modify(user, modifier -> modifier.removeGroup(group, contexts)));

        reply(sender, "§aRemoved §e" + user.name() + " §afrom §f" + group);
    }

    @CommandMethod("<user> groups set <group>")
    @CommandPermission("pex.user.groups.set")
    public void groupSet(C sender, @Argument(value = "user", suggestions = "users") String name, @Argument(value = "group", suggestions = "groups") String group, ContextSet contexts) {
        var user = user(name);
        var membership = ParentNode.builder().group(group).contexts(contexts).build();

        await(core.users().modify(user, modifier -> modifier.setGroups(Immutables.listOf(membership))));

        reply(sender, "§aSet §e" + user.name() + "§a's group to §f" + group);
    }

    @CommandMethod("<user> options")
    @CommandPermission("pex.user.options.list")
    public void options(C sender, @Argument(value = "user", suggestions = "users") String name) {
        var user = user(name);

        heading(sender, "User", user.name());
        section(sender, "Options", user.explicitOptions().stream().map(this::option).collect(Immutables.toList()));
    }

    @CommandMethod("<user> options set <key> <value>")
    @CommandPermission("pex.user.options.set")
    public void optionSet(C sender, @Argument(value = "user", suggestions = "users") String name, @Argument(value = "key", suggestions = "options") String key, @Argument("value") String value, ContextSet contexts) {
        var user = user(name);

        await(core.users().modify(user, modifier -> modifier.setOption(key, value, contexts)));

        reply(sender, "§aSet option §e" + key);
    }

    @CommandMethod("<user> options list")
    @CommandPermission("pex.user.options.list")
    public void optionList(C sender, @Argument(value = "user", suggestions = "users") String name) {
        var user = user(name);

        heading(sender, "User", user.name());
        section(sender, "Options", user.explicitOptions().stream().map(this::option).collect(Immutables.toList()));
    }

    @CommandMethod("<user> options get <key>")
    @CommandPermission("pex.user.options.get")
    public void optionGet(C sender, @Argument(value = "user", suggestions = "users") String name, @Argument(value = "key", suggestions = "options") String key, ContextSet contexts) {
        var user = user(name);
        var value = core.resolvers().options().resolve(user, key, queryOptions(contexts));

        reply(sender, value.map(result -> "§6" + key + ": §f" + result).orElse("§cOption is not set: " + key));
    }

    @CommandMethod("<user> options unset <key>")
    @CommandPermission("pex.user.options.unset")
    public void optionUnset(C sender, @Argument(value = "user", suggestions = "users") String name, @Argument(value = "key", suggestions = "options") String key, ContextSet contexts) {
        var user = user(name);

        await(core.users().modify(user, modifier -> {
            if (contexts.isEmpty()) {
                modifier.removeOptions(key);
            } else {
                modifier.removeOption(key, contexts);
            }
        }));

        reply(sender, "§aRemoved option §e" + key);
    }
}
