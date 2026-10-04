/*
 * PermissionsEx - Permissions plugin for Bukkit
 * Copyright (C) 2011 t3hk0d3 http://www.tehkode.ru
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package ru.tehkode.permissions.bukkit.commands;

import dev.rono.permissions.api.group.Group;
import dev.rono.permissions.api.parent.ParentNode;
import dev.rono.permissions.api.permission.PermissionResult;
import dev.rono.permissions.api.user.User;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import ru.tehkode.permissions.bukkit.ApiWrapper;
import ru.tehkode.permissions.bukkit.PermissionsEx;
import ru.tehkode.permissions.commands.Command;

public class GroupCommands extends PermissionsCommand {

    @Command(name = "pex", syntax = "groups list [world]", permission = "permissions.manage.groups.list", description = "List all registered groups")
    public void groupsList(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
        sender.sendMessage(ChatColor.WHITE + "Registered groups: ");

        for (Group group : ApiWrapper.groups().cache().all()) {
            String weight = group.weight().isPresent() ? " @" + group.weight().getAsInt() : "";
            sender.sendMessage(" " + group.name() + weight);
        }
    }

    @Command(name = "pex", syntax = "groups", permission = "permissions.manage.groups.list", description = "List all registered groups (alias)")
    public void groupsListAlias(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
        groupsList(plugin, sender, args);
    }

    @Command(name = "pex", syntax = "group", permission = "permissions.manage.groups.list", description = "List all registered groups (alias)")
    public void groupAlias(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
        groupsList(plugin, sender, args);
    }

    @Command(name = "pex", syntax = "group <group> weight [weight]", permission = "permissions.manage.groups.weight.<group>", description = "Display or set group weight")
    public void groupWeight(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
        try {
            String groupName = autoCompleteGroupName(args.get("group"));
            Group group = ApiWrapper.requireOrCreateGroup(groupName);
            int weight = group.weight().orElse(0);

            if (args.containsKey("weight")) {
                weight = parseInteger(args.get("weight"));
                int setWeight = weight;
                ApiWrapper.await(ApiWrapper.groups().modify(group.name(), modifier -> modifier.setWeight(setWeight)));
            }

            sender.sendMessage("Group \"" + group.name() + "\" has " + weight + " calories.");
        } catch (Exception error) {
            sendError(sender, error);
        }
    }

    @Command(name = "pex", syntax = "group <group> toggle debug", permission = "permissions.manage.groups.debug.<group>", description = "Toggle debug mode for group")
    public void groupToggleDebug(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
        sender.sendMessage(ChatColor.YELLOW + "Per-group debug is managed by PermissionsExPlus logging.");
    }

    @Command(name = "pex", syntax = "group <group> prefix [newprefix] [world]", permission = "permissions.manage.groups.prefix.<group>", description = "Get or set group prefix")
    public void groupPrefix(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
        try {
            String groupName = autoCompleteGroupName(args.get("group"));
            Group group = ApiWrapper.requireOrCreateGroup(groupName);
            String world = args.get("world");
            String name = group.name();

            if (args.containsKey("newprefix")) {
                String prefix = args.get("newprefix");
                ApiWrapper.await(ApiWrapper.groups().modify(group.name(), modifier -> {
                    if (world != null) {
                        modifier.setPrefix(prefix, ApiWrapper.world(world));
                    } else {
                        modifier.setPrefix(prefix);
                    }
                }));
                sender.sendMessage(name + "'s prefix" + worldClause(world) + (world == null ? " " : "") + "has been set to \"" + prefix + "\"");
            } else {
                var value = ApiWrapper.api().resolvers().options().prefix(group, ApiWrapper.query(world));
                sender.sendMessage(name + "'s prefix" + worldClause(world) + (world == null ? " " : "") + "is \"" + value.orElse("") + "\"");
            }
        } catch (Exception error) {
            sendError(sender, error);
        }
    }

    @Command(name = "pex", syntax = "group <group> suffix [newsuffix] [world]", permission = "permissions.manage.groups.suffix.<group>", description = "Get or set group suffix")
    public void groupSuffix(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
        try {
            String groupName = autoCompleteGroupName(args.get("group"));
            Group group = ApiWrapper.requireOrCreateGroup(groupName);
            String world = args.get("world");
            String name = group.name();

            if (args.containsKey("newsuffix")) {
                String suffix = args.get("newsuffix");
                ApiWrapper.await(ApiWrapper.groups().modify(group.name(), modifier -> {
                    if (world != null) {
                        modifier.setSuffix(suffix, ApiWrapper.world(world));
                    } else {
                        modifier.setSuffix(suffix);
                    }
                }));
                sender.sendMessage(name + "'s suffix" + worldClause(world) + (world == null ? " " : "") + "has been set to \"" + suffix + "\"");
            } else {
                var value = ApiWrapper.api().resolvers().options().suffix(group, ApiWrapper.query(world));
                sender.sendMessage(name + "'s suffix" + worldClause(world) + (world == null ? " " : "") + "is \"" + value.orElse("") + "\"");
            }
        } catch (Exception error) {
            sendError(sender, error);
        }
    }

    @Command(name = "pex", syntax = "group <group> create [parents]", permission = "permissions.manage.groups.create.<group>", description = "Create group")
    public void groupCreate(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
        try {
            String groupName = args.get("group");

            if (ApiWrapper.findGroup(groupName).isPresent()) {
                sender.sendMessage(ChatColor.RED + "Group \"" + groupName + "\" already exists.");
                return;
            }

            Group group = ApiWrapper.await(ApiWrapper.groups().create(groupName));

            if (args.containsKey("parents")) {
                List<ParentNode> parents = Arrays.stream(args.get("parents").split(","))
                        .map(String::trim)
                        .filter(name -> !name.isEmpty())
                        .map(name -> ParentNode.builder().group(autoCompleteGroupName(name)).build())
                        .collect(Collectors.toList());
                ApiWrapper.await(ApiWrapper.groups().modify(group.name(), modifier -> modifier.setParents(parents)));
            }

            sender.sendMessage(ChatColor.WHITE + "Group \"" + group.name() + "\" created!");
        } catch (Exception error) {
            sendError(sender, error);
        }
    }

    @Command(name = "pex", syntax = "group <group> delete", permission = "permissions.manage.groups.remove.<group>", description = "Remove group")
    public void groupDelete(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
        try {
            String groupName = autoCompleteGroupName(args.get("group"));
            boolean removed = ApiWrapper.await(ApiWrapper.groups().delete(groupName));

            if (removed) {
                sender.sendMessage(ChatColor.WHITE + "Group \"" + groupName + "\" removed!");
            } else {
                sender.sendMessage(ChatColor.RED + "Group \"" + groupName + "\" doesn't exist.");
            }
        } catch (Exception error) {
            sendError(sender, error);
        }
    }

    @Command(name = "pex", syntax = "group <group> parents [world]", permission = "permissions.manage.groups.inheritance.<group>", description = "List parents alias")
    public void groupListParentsAlias(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
        groupListParents(plugin, sender, args);
    }

    @Command(name = "pex", syntax = "group <group> parents list [world]", permission = "permissions.manage.groups.inheritance.<group>", description = "List parents")
    public void groupListParents(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
        try {
            Group group = ApiWrapper.requireGroup(autoCompleteGroupName(args.get("group")));
            String world = args.get("world");
            List<String> parents = group.parents().stream()
                    .filter(node -> world == null || node.contexts().equals(ApiWrapper.world(world)) || node.contexts().isEmpty())
                    .map(ParentNode::group)
                    .collect(Collectors.toList());

            if (parents.isEmpty()) {
                sender.sendMessage(ChatColor.RED + "Group \"" + group.name() + "\" has no parents.");
                return;
            }

            sender.sendMessage("Group " + group.name() + " parents:");
            parents.forEach(parent -> sender.sendMessage("  " + parent));
        } catch (Exception error) {
            sendError(sender, error);
        }
    }

    @Command(name = "pex", syntax = "group <group> parents set <parents> [world]", permission = "permissions.manage.groups.inheritance.<group>", description = "Set parents")
    public void groupSetParents(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
        try {
            Group group = ApiWrapper.requireOrCreateGroup(autoCompleteGroupName(args.get("group")));
            String world = args.get("world");
            List<ParentNode> parents = Arrays.stream(args.get("parents").split(","))
                    .map(String::trim)
                    .filter(name -> !name.isEmpty())
                    .map(name -> {
                        String parent = autoCompleteGroupName(name);
                        return world != null
                                ? ParentNode.builder().group(parent).contexts(ApiWrapper.world(world)).build()
                                : ParentNode.builder().group(parent).build();
                    })
                    .collect(Collectors.toList());

            ApiWrapper.await(ApiWrapper.groups().modify(group.name(), modifier -> modifier.setParents(parents)));
            sender.sendMessage(ChatColor.WHITE + "Group " + group.name() + " inheritance updated!");
        } catch (Exception error) {
            sendError(sender, error);
        }
    }

    @Command(name = "pex", syntax = "group <group> parents add <parents> [world]", permission = "permissions.manage.groups.inheritance.<group>", description = "Add parents")
    public void groupAddParents(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
        try {
            Group group = ApiWrapper.requireOrCreateGroup(autoCompleteGroupName(args.get("group")));
            String world = args.get("world");

            ApiWrapper.await(ApiWrapper.groups().modify(group.name(), modifier -> {
                for (String raw : args.get("parents").split(",")) {
                    String parent = autoCompleteGroupName(raw.trim());

                    if (parent.isEmpty()) {
                        continue;
                    }

                    if (world != null) {
                        modifier.addParent(parent, ApiWrapper.world(world));
                    } else {
                        modifier.addParent(parent);
                    }
                }
            }));

            sender.sendMessage(ChatColor.WHITE + "Group " + group.name() + " inheritance updated!");
        } catch (Exception error) {
            sendError(sender, error);
        }
    }

    @Command(name = "pex", syntax = "group <group> parents remove <parents> [world]", permission = "permissions.manage.groups.inheritance.<group>", description = "Remove parents")
    public void groupRemoveParents(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
        try {
            Group group = ApiWrapper.requireGroup(autoCompleteGroupName(args.get("group")));
            String world = args.get("world");

            ApiWrapper.await(ApiWrapper.groups().modify(group.name(), modifier -> {
                for (String raw : args.get("parents").split(",")) {
                    String parent = autoCompleteGroupName(raw.trim());

                    if (parent.isEmpty()) {
                        continue;
                    }

                    if (world != null) {
                        modifier.removeParent(parent, ApiWrapper.world(world));
                    } else {
                        modifier.removeParent(parent);
                    }
                }
            }));

            sender.sendMessage(ChatColor.WHITE + "Group \"" + group.name() + "\" inheritance updated!");
        } catch (Exception error) {
            sendError(sender, error);
        }
    }

    @Command(name = "pex", syntax = "group <group>", permission = "permissions.manage.groups.permissions.<group>", description = "List group permissions alias")
    public void groupInfoAlias(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
        groupListPermissions(plugin, sender, args);
    }

    @Command(name = "pex", syntax = "group <group> list [world]", permission = "permissions.manage.groups.permissions.<group>", description = "List group permissions")
    public void groupListPermissions(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
        try {
            Group group = ApiWrapper.requireGroup(autoCompleteGroupName(args.get("group")));
            String world = args.get("world");

            sender.sendMessage("Group \"" + group.name() + "\"'s permissions:");
            group.explicitPermissions().stream()
                    .filter(node -> world == null || node.contexts().equals(ApiWrapper.world(world)) || node.contexts().isEmpty())
                    .forEach(node -> sender.sendMessage("  " + describePermission(node)));

            sender.sendMessage("Group \"" + group.name() + "\"'s Options: ");
            group.explicitOptions().stream()
                    .filter(node -> world == null || node.contexts().equals(ApiWrapper.world(world)) || node.contexts().isEmpty())
                    .forEach(node -> sender.sendMessage("  " + node.key() + " = \"" + node.value() + "\""));
        } catch (Exception error) {
            sendError(sender, error);
        }
    }

    @Command(name = "pex", syntax = "group <group> add <permission> [world]", permission = "permissions.manage.groups.permissions.<group>", description = "Add permission")
    public void groupAddPermission(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
        try {
            Group group = ApiWrapper.requireOrCreateGroup(autoCompleteGroupName(args.get("group")));
            String permission = args.get("permission");
            boolean deny = permission.startsWith("-");
            String node = deny ? permission.substring(1) : permission;
            String world = args.get("world");

            ApiWrapper.await(ApiWrapper.groups().modify(group.name(), modifier -> {
                if (deny) {
                    if (world != null) {
                        modifier.denyPermission(node, ApiWrapper.world(world));
                    } else {
                        modifier.denyPermission(node);
                    }
                } else if (world != null) {
                    modifier.allowPermission(node, ApiWrapper.world(world));
                } else {
                    modifier.allowPermission(node);
                }
            }));

            sender.sendMessage(ChatColor.WHITE + "Permission \"" + permission + "\" added to group \"" + group.name() + "\"!");
        } catch (Exception error) {
            sendError(sender, error);
        }
    }

    @Command(name = "pex", syntax = "group <group> set <option> <value> [world]", permission = "permissions.manage.groups.permissions.<group>", description = "Set option")
    public void groupSetOption(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
        try {
            Group group = ApiWrapper.requireOrCreateGroup(autoCompleteGroupName(args.get("group")));
            String world = args.get("world");

            ApiWrapper.await(ApiWrapper.groups().modify(group.name(), modifier -> {
                if (world != null) {
                    modifier.setOption(args.get("option"), args.get("value"), ApiWrapper.world(world));
                } else {
                    modifier.setOption(args.get("option"), args.get("value"));
                }
            }));

            sender.sendMessage(ChatColor.WHITE + "Option \"" + args.get("option") + "\" set!");
        } catch (Exception error) {
            sendError(sender, error);
        }
    }

    @Command(name = "pex", syntax = "group <group> remove <permission> [world]", permission = "permissions.manage.groups.permissions.<group>", description = "Remove permission")
    public void groupRemovePermission(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
        try {
            Group group = ApiWrapper.requireGroup(autoCompleteGroupName(args.get("group")));
            String permission = args.get("permission");
            String node = permission.startsWith("-") ? permission.substring(1) : permission;
            String world = args.get("world");

            ApiWrapper.await(ApiWrapper.groups().modify(group.name(), modifier -> {
                if (world != null) {
                    modifier.removePermission(node, ApiWrapper.world(world));
                } else {
                    modifier.removePermission(node);
                }
            }));

            sender.sendMessage(ChatColor.WHITE + "Permission \"" + permission + "\" removed from group \"" + group.name() + "\"!");
        } catch (Exception error) {
            sendError(sender, error);
        }
    }

    @Command(name = "pex", syntax = "group <group> swap <permission> <targetPermission> [world]", permission = "permissions.manage.groups.permissions.<group>", description = "Swap permissions")
    public void groupSwapPermission(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
        sender.sendMessage(ChatColor.YELLOW + "Permission swap is not supported by the PermissionsExPlus adapter.");
    }

    @Command(name = "pex", syntax = "group <group> timed add <permission> [lifetime] [world]", permission = "permissions.manage.groups.permissions.timed.<group>", description = "Add timed permission")
    public void groupAddTimedPermission(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
        try {
            Group group = ApiWrapper.requireOrCreateGroup(autoCompleteGroupName(args.get("group")));
            String permission = args.get("permission");
            boolean deny = permission.startsWith("-");
            String node = deny ? permission.substring(1) : permission;
            int lifetime = parseInteger(args.get("lifetime"));
            Duration duration = lifetime > 0 ? Duration.ofSeconds(lifetime) : Duration.ofDays(1);
            String world = args.get("world");

            ApiWrapper.await(ApiWrapper.groups().modify(group.name(), modifier -> {
                if (deny) {
                    if (world != null) {
                        modifier.denyTimedPermission(node, ApiWrapper.world(world), duration);
                    } else {
                        modifier.denyTimedPermission(node, duration);
                    }
                } else if (world != null) {
                    modifier.allowTimedPermission(node, ApiWrapper.world(world), duration);
                } else {
                    modifier.allowTimedPermission(node, duration);
                }
            }));

            sender.sendMessage(ChatColor.WHITE + "Timed permission added!");
        } catch (Exception error) {
            sendError(sender, error);
        }
    }

    @Command(name = "pex", syntax = "group <group> timed remove <permission> [world]", permission = "permissions.manage.groups.permissions.timed.<group>", description = "Remove timed permission")
    public void groupRemoveTimedPermission(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
        try {
            Group group = ApiWrapper.requireGroup(autoCompleteGroupName(args.get("group")));
            String permission = args.get("permission");
            String node = permission.startsWith("-") ? permission.substring(1) : permission;
            String world = args.get("world");

            ApiWrapper.await(ApiWrapper.groups().modify(group.name(), modifier -> {
                if (world != null) {
                    modifier.removePermission(node, ApiWrapper.world(world));
                } else {
                    modifier.removePermission(node);
                }
            }));

            sender.sendMessage(ChatColor.WHITE + "Timed permission \"" + permission + "\" removed!");
        } catch (Exception error) {
            sendError(sender, error);
        }
    }

    @Command(name = "pex", syntax = "group <group> users", permission = "permissions.manage.membership.<group>", description = "List users in group")
    public void groupUsersList(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
        try {
            String groupName = autoCompleteGroupName(args.get("group"));
            List<User> users = new ArrayList<>();

            for (User user : ApiWrapper.users().cache().all()) {
                boolean member = user.groups().stream().anyMatch(node -> node.group().equalsIgnoreCase(groupName));

                if (member) {
                    users.add(user);
                }
            }

            if (users.isEmpty()) {
                sender.sendMessage(ChatColor.RED + "Group \"" + groupName + "\" has no users.");
                return;
            }

            sender.sendMessage("Group \"" + groupName + "\"'s users (" + users.size() + "):");

            for (User user : users) {
                sender.sendMessage("   " + describeUser(user));
            }
        } catch (Exception error) {
            sendError(sender, error);
        }
    }

    @Command(name = "pex", syntax = "group <group> user add <user> [world]", permission = "permissions.manage.membership.<group>", description = "Add users to group")
    public void groupAddUser(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
        try {
            String groupName = autoCompleteGroupName(args.get("group"));
            ApiWrapper.requireOrCreateGroup(groupName);
            String world = args.get("world");

            for (String raw : args.get("user").split(",")) {
                User user = ApiWrapper.requireUser(autoCompletePlayerName(raw.trim()));
                ApiWrapper.await(ApiWrapper.users().modify(user.uniqueId(), modifier -> {
                    if (world != null) {
                        modifier.addGroup(groupName, ApiWrapper.world(world));
                    } else {
                        modifier.addGroup(groupName);
                    }
                }));
                sender.sendMessage(ChatColor.WHITE + "User " + user.name() + " added to " + groupName + " !");
                informPlayer(plugin, user.name(), "You are assigned to \"" + groupName + "\" group");
            }
        } catch (Exception error) {
            sendError(sender, error);
        }
    }

    @Command(name = "pex", syntax = "group <group> user remove <user> [world]", permission = "permissions.manage.membership.<group>", description = "Remove users from group")
    public void groupRemoveUser(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
        try {
            String groupName = autoCompleteGroupName(args.get("group"));
            String world = args.get("world");

            for (String raw : args.get("user").split(",")) {
                User user = ApiWrapper.requireUser(autoCompletePlayerName(raw.trim()));
                ApiWrapper.await(ApiWrapper.users().modify(user.uniqueId(), modifier -> {
                    if (world != null) {
                        modifier.removeGroup(groupName, ApiWrapper.world(world));
                    } else {
                        modifier.removeGroup(groupName);
                    }
                }));
                sender.sendMessage(ChatColor.WHITE + "User " + user.name() + " removed from " + groupName + " !");
                informPlayer(plugin, user.name(), "You were removed from \"" + groupName + "\" group");
            }
        } catch (Exception error) {
            sendError(sender, error);
        }
    }

    @Command(name = "pex", syntax = "default group [world]", permission = "permissions.manage.groups.inheritance", description = "Print default group")
    public void defaultGroup(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
        var defaultGroup = ApiWrapper.api().resolvers().defaultGroups().resolve();

        if (defaultGroup.isPresent()) {
            sender.sendMessage("Default group: " + defaultGroup.get().name());
        } else {
            sender.sendMessage("No default group configured");
        }
    }

    @Command(name = "pex", syntax = "set default group <group> <value> [world]", permission = "permissions.manage.groups.inheritance", description = "Set default group")
    public void setDefaultGroup(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
        sender.sendMessage(ChatColor.YELLOW + "Default group is configured in PermissionsExPlus config.yml (default-group).");
    }

    @Command(name = "pex", syntax = "group <group> check <permission> [world]", permission = "permissions.manage.groups.permissions.<group>", description = "Check group permission")
    public void groupCheckPermission(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
        try {
            Group group = ApiWrapper.requireGroup(autoCompleteGroupName(args.get("group")));
            PermissionResult result = ApiWrapper.api().resolvers().permissions()
                    .check(group, args.get("permission"), ApiWrapper.query(args.get("world")));
            sender.sendMessage("\"" + args.get("permission") + "\" = " + result.name().toLowerCase());
        } catch (Exception error) {
            sendError(sender, error);
        }
    }
}
