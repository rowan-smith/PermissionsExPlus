package dev.rono.permissions.core.command;

import lombok.var;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import dev.rono.permissions.api.context.ContextSet;
import dev.rono.permissions.api.user.User;
import dev.rono.permissions.core.PexApiImpl;

import java.util.ArrayList;
import java.util.function.BiConsumer;
import dev.rono.permissions.core.util.Immutables;

@CommandMethod("pex group")
public final class GroupCommands<C> extends CommandSupport<C> {
    GroupCommands(PexApiImpl<C> core, BiConsumer<C, String> messages) {
        super(core, messages);
    }

    @CommandMethod("<group>")
    @CommandPermission("pex.group.info")
    public void info(C sender, @Argument(value = "group", suggestions = "groups") String name) {
        var group = core.groups().cache().get(name).orElse(null);
        if (group == null) {
            reply(sender, "§cGroup not found: " + name);

            return;
        }

        heading(sender, "Group", group.name());

        var options = new ArrayList<>(group.explicitOptions().stream().map(this::option).collect(Immutables.toList()));

        group.weight().ifPresent(weight -> options.add("weight:" + weight));

        section(sender, "Options", options);
        section(sender, "Parents", group.parents().stream().map(this::parent).collect(Immutables.toList()));
        section(sender, "Permissions", group.explicitPermissions().stream().map(this::permission).collect(Immutables.toList()));
    }

    @CommandMethod("<group> create")
    @CommandPermission("pex.group.create")
    public void create(C sender, @Argument("group") String name) {
        await(core.groups().create(name));

        audit(sender, "CREATED group '" + name + "'");

        reply(sender, "§aCreated group §e" + name);
    }

    @CommandMethod("<group> delete")
    @CommandPermission("pex.group.delete")
    public void delete(C sender, @Argument(value = "group", suggestions = "groups") String name) {
        var deleted = await(core.groups().delete(name));

        if (deleted) {
            audit(sender, "DELETED group '" + name + "'");
        }

        reply(sender, deleted ? "§aDeleted group §e" + name : "§cGroup not found: " + name);
    }

    @CommandMethod("<group> clone <new_name>")
    @CommandPermission("pex.group.clone")
    public void clone(C sender, @Argument(value = "group", suggestions = "groups") String name, @Argument("new_name") String newName) {
        var source = group(name);

        await(core.groups().create(newName));
        await(core.groups().modify(newName, modifier -> {
            if (source.weight().isPresent()) {
                modifier.setWeight(source.weight().getAsInt());
            }

            modifier.setParents(source.parents());
            source.explicitPermissions().forEach(modifier::setPermission);
            source.explicitOptions().forEach(modifier::setOption);
        }));

        reply(sender, "§aCloned group §e" + name + " §aas §e" + newName);
    }

    @CommandMethod("<group> permissions add <permission>")
    @CommandPermission("pex.group.permissions.add")
    public void permissionAdd(C sender, @Argument(value = "group", suggestions = "groups") String group, @Argument(value = "permission", suggestions = "permissions") String permission, ContextSet contexts) {
        await(core.groups().modify(group, modifier -> core.expandPermissionNode(permission).forEach(expanded -> modifier.allowPermission(expanded, contexts))));

        reply(sender, "§aAdded permission §f" + permission);
    }

    @CommandMethod("<group> permissions remove <permission>")
    @CommandPermission("pex.group.permissions.remove")
    public void permissionRemove(C sender, @Argument(value = "group", suggestions = "groups") String group, @Argument(value = "permission", suggestions = "permissions") String permission, ContextSet contexts) {
        await(core.groups().modify(group, modifier -> core.expandPermissionNode(permission).forEach(expanded -> modifier.removePermission(expanded, contexts))));

        reply(sender, "§aRemoved permission §f" + permission);
    }

    @CommandMethod("<group> permissions list")
    @CommandPermission("pex.group.permissions.list")
    public void permissionList(C sender, @Argument(value = "group", suggestions = "groups") String name) {
        var group = group(name);

        heading(sender, "Group", group.name());
        section(sender, "Permissions", group.explicitPermissions().stream().map(this::permission).collect(Immutables.toList()));
    }

    @CommandMethod("<group> permissions check <permission>")
    @CommandPermission("pex.group.permissions.check")
    public void permissionCheck(C sender, @Argument(value = "group", suggestions = "groups") String name, @Argument(value = "permission", suggestions = "permissions") String permission, ContextSet contexts) {
        var group = group(name);

        check(sender, group, permission, queryOptions(contexts));
    }

    @CommandMethod("<group> permissions trace <permission>")
    @CommandPermission("pex.group.permissions.trace")
    public void permissionTrace(C sender, @Argument(value = "group", suggestions = "groups") String name, @Argument(value = "permission", suggestions = "permissions") String permission, ContextSet contexts) {
        var group = group(name);

        trace(sender, group, permission, queryOptions(contexts));
    }

    @CommandMethod("<group> parents add <parent>")
    @CommandPermission("pex.group.parents.add")
    public void parentAdd(C sender, @Argument(value = "group", suggestions = "groups") String group, @Argument(value = "parent", suggestions = "groups") String parent, ContextSet contexts) {
        await(core.groups().modify(group, modifier -> modifier.addParent(parent, contexts)));

        reply(sender, "§aAdded parent §f" + parent);
    }

    @CommandMethod("<group> parents remove <parent>")
    @CommandPermission("pex.group.parents.remove")
    public void parentRemove(C sender, @Argument(value = "group", suggestions = "groups") String group, @Argument(value = "parent", suggestions = "groups") String parent, ContextSet contexts) {
        await(core.groups().modify(group, modifier -> modifier.removeParent(parent, contexts)));

        reply(sender, "§aRemoved parent §f" + parent);
    }

    @CommandMethod("<group> members list")
    @CommandPermission("pex.group.members.list")
    public void memberList(C sender, @Argument(value = "group", suggestions = "groups") String name) {
        heading(sender, "Group", name);
        section(sender, "Members", core.users().cache().all().stream()
                .filter(user -> user.groups().stream().anyMatch(node -> node.group().equalsIgnoreCase(name)))
                .map(User::name).collect(Immutables.toList()));
    }

    @CommandMethod("<group> members add <user>")
    @CommandPermission("pex.group.members.add")
    public void memberAdd(C sender, @Argument(value = "group", suggestions = "groups") String group, @Argument(value = "user", suggestions = "users") String name, ContextSet contexts) {
        var user = user(name);

        await(core.users().modify(user, modifier -> modifier.addGroup(group, contexts)));

        reply(sender, "§aAdded §e" + user.name() + " §ato §f" + group);
    }

    @CommandMethod("<group> members remove <user>")
    @CommandPermission("pex.group.members.remove")
    public void memberRemove(C sender, @Argument(value = "group", suggestions = "groups") String group, @Argument(value = "user", suggestions = "users") String name, ContextSet contexts) {
        var user = user(name);

        await(core.users().modify(user, modifier -> modifier.removeGroup(group, contexts)));

        reply(sender, "§aRemoved §e" + user.name() + " §afrom §f" + group);
    }

    @CommandMethod("<group> options set <key> <value>")
    @CommandPermission("pex.group.options.set")
    public void optionSet(C sender, @Argument(value = "group", suggestions = "groups") String group, @Argument(value = "key", suggestions = "options") String key, @Argument("value") String value, ContextSet contexts) {
        await(core.groups().modify(group, modifier -> modifier.setOption(key, value, contexts)));

        reply(sender, "§aSet option §e" + key);
    }

    @CommandMethod("<group> options list")
    @CommandPermission("pex.group.options.list")
    public void optionList(C sender, @Argument(value = "group", suggestions = "groups") String name) {
        var group = group(name);

        heading(sender, "Group", group.name());
        section(sender, "Options", group.explicitOptions().stream().map(this::option).collect(Immutables.toList()));
    }

    @CommandMethod("<group> options get <key>")
    @CommandPermission("pex.group.options.get")
    public void optionGet(C sender, @Argument(value = "group", suggestions = "groups") String name, @Argument(value = "key", suggestions = "options") String key, ContextSet contexts) {
        var group = group(name);
        var value = core.resolvers().options().resolve(group, key, queryOptions(contexts));

        reply(sender, value.map(result -> "§6" + key + ": §f" + result).orElse("§cOption is not set: " + key));
    }

    @CommandMethod("<group> options unset <key>")
    @CommandPermission("pex.group.options.unset")
    public void optionUnset(C sender, @Argument(value = "group", suggestions = "groups") String name, @Argument(value = "key", suggestions = "options") String key, ContextSet contexts) {
        await(core.groups().modify(name, modifier -> {
            if (contexts.isEmpty()) {
                modifier.removeOptions(key);
            } else {
                modifier.removeOption(key, contexts);
            }
        }));

        reply(sender, "§aRemoved option §e" + key);
    }
}
