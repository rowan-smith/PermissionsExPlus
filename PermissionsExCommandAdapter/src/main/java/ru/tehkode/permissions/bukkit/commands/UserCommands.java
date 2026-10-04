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

import dev.rono.permissions.api.context.ContextSet;
import dev.rono.permissions.api.parent.ParentNode;
import dev.rono.permissions.api.permission.PermissionResult;
import dev.rono.permissions.api.user.User;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;
import ru.tehkode.permissions.bukkit.ApiWrapper;
import ru.tehkode.permissions.bukkit.PermissionsEx;
import ru.tehkode.permissions.commands.Command;

public class UserCommands extends PermissionsCommand {

    @Command(name = "pex", syntax = "users list", permission = "permissions.manage.users", description = "List all registered users")
    public void usersList(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
        var users = ApiWrapper.users().cache().all();
        sender.sendMessage(ChatColor.WHITE + "Currently registered users: ");

        for (User user : users) {
            sender.sendMessage(" " + user.name() + " (" + user.uniqueId() + ")");
        }
    }

    @Command(name = "pex", syntax = "users", permission = "permissions.manage.users", description = "List all registered users (alias)", isPrimary = true)
    public void usersAlias(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
        usersList(plugin, sender, args);
    }

    @Command(name = "pex", syntax = "user", permission = "permissions.manage.users", description = "List all registered users (alias)")
    public void userAlias(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
        usersList(plugin, sender, args);
    }

    @Command(name = "pex", syntax = "user <user>", permission = "permissions.manage.users.permissions.<user>", description = "List user permissions (list alias)")
    public void userInfo(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
        userListPermissions(plugin, sender, args);
    }

    @Command(name = "pex", syntax = "user <user> list [world]", permission = "permissions.manage.users.permissions.<user>", description = "List user permissions")
    public void userListPermissions(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
        try {
            String userName = autoCompletePlayerName(args.get("user"));
            User user = ApiWrapper.requireUser(userName);
            String world = args.get("world");
            ContextSet contexts = ApiWrapper.world(world);
            String name = describeUser(user);

            sender.sendMessage("'" + name + "' is a member of:");
            user.groups().stream()
                    .filter(node -> world == null || node.contexts().equals(contexts) || node.contexts().isEmpty())
                    .forEach(node -> sender.sendMessage("  " + node.group() + (node.contexts().isEmpty() ? "" : " @" + node.contexts())));

            sender.sendMessage(name + "'s permissions:");
            user.explicitPermissions().stream()
                    .filter(node -> world == null || node.contexts().equals(contexts) || node.contexts().isEmpty())
                    .forEach(node -> sender.sendMessage("  " + describePermission(node)));

            sender.sendMessage(name + "'s options:");
            user.explicitOptions().stream()
                    .filter(node -> world == null || node.contexts().equals(contexts) || node.contexts().isEmpty())
                    .forEach(node -> sender.sendMessage("  " + node.key() + " = \"" + node.value() + "\""));
        } catch (Exception error) {
            sendError(sender, error);
        }
    }

    @Command(name = "pex", syntax = "user <user> superperms", permission = "permissions.manage.users.permissions.<user>", description = "List user actual superperms")
    public void userSuperperms(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
        try {
            String userName = autoCompletePlayerName(args.get("user"));
            User user = ApiWrapper.requireUser(userName);
            Player player = Bukkit.getPlayer(user.uniqueId());

            if (player == null) {
                sender.sendMessage(ChatColor.RED + "Player not found (offline?)");
                return;
            }

            sender.sendMessage(user.name() + "'s superperms:");

            for (PermissionAttachmentInfo info : player.getEffectivePermissions()) {
                String value = info.getValue() ? "true" : "false";
                String source = info.getAttachment() != null ? info.getAttachment().getPlugin().getName() : "default";
                sender.sendMessage(" " + ChatColor.GREEN + info.getPermission() + " = " + value + ChatColor.DARK_GRAY + " (" + source + ")");
            }
        } catch (Exception error) {
            sendError(sender, error);
        }
    }

    @Command(name = "pex", syntax = "user <user> prefix [newprefix] [world]", permission = "permissions.manage.users.prefix.<user>", description = "Get or set <user> prefix")
    public void userPrefix(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
        try {
            String userName = autoCompletePlayerName(args.get("user"));
            User user = ApiWrapper.requireUser(userName);
            String world = args.get("world");
            String name = describeUser(user);

            if (args.containsKey("newprefix")) {
                String prefix = args.get("newprefix");
                ApiWrapper.await(ApiWrapper.users().modify(user.uniqueId(), modifier -> {
                    if (world != null) {
                        modifier.setPrefix(prefix, ApiWrapper.world(world));
                    } else {
                        modifier.setPrefix(prefix);
                    }
                }));
                sender.sendMessage(name + "'s prefix" + worldClause(world) + (world == null ? " " : "") + "has been set to \"" + prefix + "\"");
            } else {
                var value = ApiWrapper.api().resolvers().options().prefix(user, ApiWrapper.query(world));
                sender.sendMessage(name + "'s prefix" + worldClause(world) + (world == null ? " " : "") + "is \"" + value.orElse("") + "\"");
            }
        } catch (Exception error) {
            sendError(sender, error);
        }
    }

    @Command(name = "pex", syntax = "user <user> suffix [newsuffix] [world]", permission = "permissions.manage.users.suffix.<user>", description = "Get or set <user> suffix")
    public void userSuffix(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
        try {
            String userName = autoCompletePlayerName(args.get("user"));
            User user = ApiWrapper.requireUser(userName);
            String world = args.get("world");
            String name = describeUser(user);

            if (args.containsKey("newsuffix")) {
                String suffix = args.get("newsuffix");
                ApiWrapper.await(ApiWrapper.users().modify(user.uniqueId(), modifier -> {
                    if (world != null) {
                        modifier.setSuffix(suffix, ApiWrapper.world(world));
                    } else {
                        modifier.setSuffix(suffix);
                    }
                }));
                sender.sendMessage(name + "'s suffix" + worldClause(world) + (world == null ? " " : "") + "has been set to \"" + suffix + "\"");
            } else {
                var value = ApiWrapper.api().resolvers().options().suffix(user, ApiWrapper.query(world));
                sender.sendMessage(name + "'s suffix" + worldClause(world) + (world == null ? " " : "") + "is \"" + value.orElse("") + "\"");
            }
        } catch (Exception error) {
            sendError(sender, error);
        }
    }

    @Command(name = "pex", syntax = "user <user> toggle debug", permission = "permissions.manage.<user>", description = "Toggle debug only for <user>")
    public void userToggleDebug(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
        sender.sendMessage(ChatColor.YELLOW + "Per-user debug is managed by PermissionsExPlus logging; use /pex toggle debug.");
    }

    @Command(name = "pex", syntax = "user <user> check <permission> [world]", permission = "permissions.manage.<user>", description = "Checks player for <permission>")
    public void userCheckPermission(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
        try {
            String userName = autoCompletePlayerName(args.get("user"));
            User user = ApiWrapper.requireUser(userName);
            String permission = args.get("permission");
            PermissionResult result = ApiWrapper.api().resolvers().permissions()
                    .check(user, permission, ApiWrapper.query(args.get("world")));

            if (result == PermissionResult.ALLOW) {
                sender.sendMessage("Player \"" + describeUser(user) + "\" has \"" + permission + "\"");
            } else {
                sender.sendMessage("Player \"" + describeUser(user) + "\" doesn't have \"" + permission + "\"");
            }
        } catch (Exception error) {
            sendError(sender, error);
        }
    }

    @Command(name = "pex", syntax = "user <user> get <option> [world]", permission = "permissions.manage.<user>", description = "Get option for <user>")
    public void userGetOption(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
        try {
            String userName = autoCompletePlayerName(args.get("user"));
            User user = ApiWrapper.requireUser(userName);
            String world = args.get("world");
            var value = ApiWrapper.api().resolvers().options().resolve(user, args.get("option"), ApiWrapper.query(world));
            sender.sendMessage("Player \"" + describeUser(user) + "\" @ " + world + " option \"" + args.get("option") + "\" = \"" + value.orElse("") + "\"");
        } catch (Exception error) {
            sendError(sender, error);
        }
    }

    @Command(name = "pex", syntax = "user <user> delete", permission = "permissions.manage.users.<user>", description = "Remove <user>")
    public void userDelete(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
        try {
            String userName = autoCompletePlayerName(args.get("user"));
            User user = ApiWrapper.requireUser(userName);
            boolean removed = ApiWrapper.await(ApiWrapper.users().delete(user.uniqueId()));

            if (removed) {
                sender.sendMessage(ChatColor.WHITE + "User \"" + describeUser(user) + "\" removed!");
            } else {
                sender.sendMessage(ChatColor.RED + "User \"" + describeUser(user) + "\" is virtual.");
            }
        } catch (Exception error) {
            sendError(sender, error);
        }
    }

    @Command(name = "pex", syntax = "user <user> add <permission> [world]", permission = "permissions.manage.users.permissions.<user>", description = "Add <permission> to <user> in [world]")
    public void userAddPermission(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
        try {
            String userName = autoCompletePlayerName(args.get("user"));
            User user = ApiWrapper.requireUser(userName);
            String permission = args.get("permission");
            String world = args.get("world");
            boolean deny = permission.startsWith("-");
            String node = deny ? permission.substring(1) : permission;

            ApiWrapper.await(ApiWrapper.users().modify(user.uniqueId(), modifier -> {
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

            sender.sendMessage(ChatColor.WHITE + "Permission \"" + permission + "\" added!");
            informPlayer(plugin, user.name(), "Your permissions have been changed!");
        } catch (Exception error) {
            sendError(sender, error);
        }
    }

    @Command(name = "pex", syntax = "user <user> remove <permission> [world]", permission = "permissions.manage.users.permissions.<user>", description = "Remove permission from <user> in [world]")
    public void userRemovePermission(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
        try {
            String userName = autoCompletePlayerName(args.get("user"));
            User user = ApiWrapper.requireUser(userName);
            String permission = args.get("permission");
            String node = permission.startsWith("-") ? permission.substring(1) : permission;
            String world = args.get("world");

            ApiWrapper.await(ApiWrapper.users().modify(user.uniqueId(), modifier -> {
                if (world != null) {
                    modifier.removePermission(node, ApiWrapper.world(world));
                } else {
                    modifier.removePermission(node);
                }
            }));

            sender.sendMessage(ChatColor.WHITE + "Permission \"" + permission + "\" removed!");
            informPlayer(plugin, user.name(), "Your permissions have been changed!");
        } catch (Exception error) {
            sendError(sender, error);
        }
    }

    @Command(name = "pex", syntax = "user <user> swap <permission> <targetPermission> [world]", permission = "permissions.manage.users.permissions.<user>", description = "Swap permissions")
    public void userSwapPermission(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
        sender.sendMessage(ChatColor.YELLOW + "Permission swap is not supported by the PermissionsExPlus adapter. Remove and re-add permissions instead.");
    }

    @Command(name = "pex", syntax = "user <user> timed add <permission> [lifetime] [world]", permission = "permissions.manage.users.permissions.timed.<user>", description = "Add timed permission")
    public void userAddTimedPermission(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
        try {
            String userName = autoCompletePlayerName(args.get("user"));
            User user = ApiWrapper.requireUser(userName);
            String permission = args.get("permission");
            boolean deny = permission.startsWith("-");
            String node = deny ? permission.substring(1) : permission;
            int lifetime = parseInteger(args.get("lifetime"));
            Duration duration = lifetime > 0 ? Duration.ofSeconds(lifetime) : Duration.ofDays(1);
            String world = args.get("world");

            ApiWrapper.await(ApiWrapper.users().modify(user.uniqueId(), modifier -> {
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

            sender.sendMessage(ChatColor.WHITE + "Timed permission \"" + permission + "\" added!");
            informPlayer(plugin, user.name(), "Your permissions have been changed!");
        } catch (Exception error) {
            sendError(sender, error);
        }
    }

    @Command(name = "pex", syntax = "user <user> timed remove <permission> [world]", permission = "permissions.manage.users.permissions.timed.<user>", description = "Remove timed permission")
    public void userRemoveTimedPermission(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
        try {
            String userName = autoCompletePlayerName(args.get("user"));
            User user = ApiWrapper.requireUser(userName);
            String permission = args.get("permission");
            String node = permission.startsWith("-") ? permission.substring(1) : permission;
            String world = args.get("world");

            ApiWrapper.await(ApiWrapper.users().modify(user.uniqueId(), modifier -> {
                if (world != null) {
                    modifier.removePermission(node, ApiWrapper.world(world));
                } else {
                    modifier.removePermission(node);
                }
            }));

            sender.sendMessage(ChatColor.WHITE + "Timed permission \"" + permission + "\" removed!");
            informPlayer(plugin, user.name(), "Your permissions have been changed!");
        } catch (Exception error) {
            sendError(sender, error);
        }
    }

    @Command(name = "pex", syntax = "user <user> set <option> <value> [world]", permission = "permissions.manage.users.permissions.<user>", description = "Set option")
    public void userSetOption(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
        try {
            String userName = autoCompletePlayerName(args.get("user"));
            User user = ApiWrapper.requireUser(userName);
            String world = args.get("world");

            ApiWrapper.await(ApiWrapper.users().modify(user.uniqueId(), modifier -> {
                if (world != null) {
                    modifier.setOption(args.get("option"), args.get("value"), ApiWrapper.world(world));
                } else {
                    modifier.setOption(args.get("option"), args.get("value"));
                }
            }));

            sender.sendMessage(ChatColor.WHITE + "Option \"" + args.get("option") + "\" set!");
            informPlayer(plugin, user.name(), "Your permissions have been changed!");
        } catch (Exception error) {
            sendError(sender, error);
        }
    }

    @Command(name = "pex", syntax = "user <user> group list [world]", permission = "permissions.manage.membership.<user>", description = "List groups")
    public void userListGroup(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
        try {
            String userName = autoCompletePlayerName(args.get("user"));
            User user = ApiWrapper.requireUser(userName);
            String world = args.get("world");
            ContextSet contexts = ApiWrapper.world(world);

            sender.sendMessage("User \"" + describeUser(user) + "\" @" + world + " currently in:");

            for (ParentNode node : user.groups()) {
                if (world == null || node.contexts().equals(contexts) || node.contexts().isEmpty()) {
                    sender.sendMessage("  " + node.group());
                }
            }
        } catch (Exception error) {
            sendError(sender, error);
        }
    }

    @Command(name = "pex", syntax = "user <user> group add <group> [world] [lifetime]", permission = "permissions.manage.membership.<group>", description = "Add user to group")
    public void userAddGroup(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
        try {
            String userName = autoCompletePlayerName(args.get("user"));
            String groupName = autoCompleteGroupName(args.get("group"));
            User user = ApiWrapper.requireUser(userName);
            ApiWrapper.requireOrCreateGroup(groupName);
            String world = args.get("world");
            int lifetime = parseInteger(args.get("lifetime"));

            ApiWrapper.await(ApiWrapper.users().modify(user.uniqueId(), modifier -> {
                if (lifetime > 0) {
                    Duration duration = Duration.ofSeconds(lifetime);

                    if (world != null) {
                        modifier.addTemporaryGroup(groupName, ApiWrapper.world(world), duration);
                    } else {
                        modifier.addTemporaryGroup(groupName, duration);
                    }
                } else if (world != null) {
                    modifier.addGroup(groupName, ApiWrapper.world(world));
                } else {
                    modifier.addGroup(groupName);
                }
            }));

            sender.sendMessage(ChatColor.WHITE + "User \"" + describeUser(user) + "\" added to group \"" + groupName + "\"!");
            informPlayer(plugin, user.name(), "You are assigned to group \"" + groupName + "\"");
        } catch (Exception error) {
            sendError(sender, error);
        }
    }

    @Command(name = "pex", syntax = "user <user> group set <group> [world]", permission = "", description = "Set group for user")
    public void userSetGroup(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
        try {
            String userName = autoCompletePlayerName(args.get("user"));
            String groupName = autoCompleteGroupName(args.get("group"));
            User user = ApiWrapper.requireUser(userName);
            ApiWrapper.requireOrCreateGroup(groupName);
            String world = args.get("world");

            ApiWrapper.await(ApiWrapper.users().modify(user.uniqueId(), modifier -> {
                if (world != null) {
                    List<ParentNode> kept = user.groups().stream()
                            .filter(node -> !node.contexts().equals(ApiWrapper.world(world)))
                            .collect(Collectors.toList());
                    kept.add(ParentNode.builder().group(groupName).contexts(ApiWrapper.world(world)).build());
                    modifier.setGroups(kept);
                } else {
                    modifier.setGroups(List.of(ParentNode.builder().group(groupName).build()));
                }
            }));

            sender.sendMessage(ChatColor.WHITE + "User groups set!");
            informPlayer(plugin, user.name(), "You are now only in \"" + groupName + "\" group");
        } catch (Exception error) {
            sendError(sender, error);
        }
    }

    @Command(name = "pex", syntax = "user <user> group remove <group> [world]", permission = "permissions.manage.membership.<group>", description = "Remove user from group")
    public void userRemoveGroup(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
        try {
            String userName = autoCompletePlayerName(args.get("user"));
            String groupName = autoCompleteGroupName(args.get("group"));
            User user = ApiWrapper.requireUser(userName);
            String world = args.get("world");

            ApiWrapper.await(ApiWrapper.users().modify(user.uniqueId(), modifier -> {
                if (world != null) {
                    modifier.removeGroup(groupName, ApiWrapper.world(world));
                } else {
                    modifier.removeGroup(groupName);
                }
            }));

            sender.sendMessage(ChatColor.WHITE + "User \"" + describeUser(user) + "\" removed from group \"" + groupName + "\"!");
            informPlayer(plugin, user.name(), "You were removed from \"" + groupName + "\" group");
        } catch (Exception error) {
            sendError(sender, error);
        }
    }

    @Command(name = "pex", syntax = "users cleanup <group> [threshold]", permission = "permissions.manage.users.cleanup", description = "Clean inactive users")
    public void usersCleanup(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
        sender.sendMessage(ChatColor.YELLOW + "User cleanup is not available through the command adapter. Use storage tooling instead.");
    }
}
