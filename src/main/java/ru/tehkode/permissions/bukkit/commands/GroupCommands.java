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

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PEXAdventure;
import ru.tehkode.permissions.bukkit.PermissionsEx;
import ru.tehkode.permissions.commands.Command;
import ru.tehkode.utils.DateUtils;
import ru.tehkode.utils.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GroupCommands extends PermissionsCommand {

	@Command(name = "pex",
			syntax = "groups list [world]",
			permission = "permissions.manage.groups.list",
			description = "List all registered groups")
	public void groupsList(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
		List<PermissionGroup> groups = plugin.getPermissionsManager().getGroupList();
		String worldName = this.autoCompleteWorldName(args.get("world"));

		PEXAdventure.sendMessage(sender,ChatColor.WHITE + "Registered groups: ");
		for (PermissionGroup group : groups) {
			String rank = "";
			if (group.isRanked()) {
				rank = " (rank: " + group.getRank() + "@" + group.getRankLadder() + ") ";
			}

			PEXAdventure.sendMessage(sender,String.format("  %s %s %s %s[%s]", group.getIdentifier(), " #" + group.getWeight(), rank, ChatColor.DARK_GREEN, StringUtils.implode(group.getParentIdentifiers(worldName), ", ")));
		}
	}

	@Command(name = "pex",
			syntax = "groups",
			permission = "permissions.manage.groups.list",
			description = "List all registered groups (alias)")
	public void groupsListAlias(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
		this.groupsList(plugin, sender, args);
	}

	@Command(name = "pex",
			syntax = "group",
			permission = "permissions.manage.groups.list",
			description = "List all registered groups (alias)")
	public void groupsListAnotherAlias(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
		this.groupsList(plugin, sender, args);
	}

	@Command(name = "pex",
			syntax = "group <group> weight [weight]",
			permission = "permissions.manage.groups.weight.<group>",
			description = "Display or set group weight")
	public void groupDisplaySetWeight(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
		String groupName = this.autoCompleteGroupName(args.get("group"));
		PermissionGroup group = plugin.getPermissionsManager().getGroup(groupName);

		if (group == null) {
			PEXAdventure.sendMessage(sender,ChatColor.RED + "Group \"" + groupName + "\" doesn't exist.");
			return;
		}

		if (args.containsKey("weight")) {
			try {
				group.setWeight(Integer.parseInt(args.get("weight")));
			} catch (NumberFormatException e) {
				PEXAdventure.sendMessage(sender,"Error! Weight should be integer value.");
				return;
			}
		}

		PEXAdventure.sendMessage(sender,"Group \"" + group.getIdentifier() + "\" has " + group.getWeight() + " calories.");
	}

	@Command(name = "pex",
			syntax = "group <group> toggle debug",
			permission = "permissions.manage.groups.debug.<group>",
			description = "Toggle debug mode for group")
	public void groupToggleDebug(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
		String groupName = this.autoCompleteGroupName(args.get("group"));
		PermissionGroup group = plugin.getPermissionsManager().getGroup(groupName);

		if (group == null) {
			PEXAdventure.sendMessage(sender,ChatColor.RED + "Group \"" + groupName + "\" doesn't exist.");
			return;
		}

		group.setDebug(!group.isDebug());

		PEXAdventure.sendMessage(sender,"Debug mode for group " + group.getIdentifier() + " have been " + (group.isDebug() ? "enabled" : "disabled") + "!");
	}

	@Command(name = "pex",
			syntax = "group <group> prefix [newprefix] [world]",
			permission = "permissions.manage.groups.prefix.<group>",
			description = "Get or set <group> prefix.")
	public void groupPrefix(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
		String groupName = this.autoCompleteGroupName(args.get("group"));
		String worldName = this.autoCompleteWorldName(args.get("world"));

		PermissionGroup group = plugin.getPermissionsManager().getGroup(groupName);

		if (group == null) {
			PEXAdventure.sendMessage(sender,ChatColor.RED + "Group \"" + groupName + "\" doesn't exist.");
			return;
		}

		if (args.containsKey("newprefix")) {
			group.setPrefix(args.get("newprefix"), worldName);
			PEXAdventure.sendMessage(sender,group.getIdentifier() + "'s prefix" + (worldName != null ? " (in world \"" + worldName + "\") " : "") + " has been set to \"" + group.getPrefix() + "\"");
		} else {
			PEXAdventure.sendMessage(sender,group.getIdentifier() + "'s prefix" + (worldName != null ? " (in world \"" + worldName + "\") " : "") + " is \"" + group.getPrefix() + "\"");
		}
	}

	@Command(name = "pex",
			syntax = "group <group> suffix [newsuffix] [world]",
			permission = "permissions.manage.groups.suffix.<group>",
			description = "Get or set <group> suffix")
	public void groupSuffix(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
		String groupName = this.autoCompleteGroupName(args.get("group"));
		String worldName = this.autoCompleteWorldName(args.get("world"));

		PermissionGroup group = plugin.getPermissionsManager().getGroup(groupName);

		if (group == null) {
			PEXAdventure.sendMessage(sender,ChatColor.RED + "Group \"" + groupName + "\" doesn't exist.");
			return;
		}

		if (args.containsKey("newsuffix")) {
			group.setSuffix(args.get("newsuffix"), worldName);
			PEXAdventure.sendMessage(sender,group.getIdentifier() + "'s suffix" + (worldName != null ? " (in world \"" + worldName + "\") " : "") + " has been set to \"" + group.getSuffix() + "\"");
		} else {
			PEXAdventure.sendMessage(sender,group.getIdentifier() + "'s suffix" + (worldName != null ? " (in world \"" + worldName + "\") " : "") + " is \"" + group.getSuffix() + "\"");
		}
	}

	@Command(name = "pex",
			syntax = "group <group> create [parents]",
			permission = "permissions.manage.groups.create.<group>",
			description = "Create <group> and/or set [parents]")
	public void groupCreate(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
		String groupName = this.autoCompleteGroupName(args.get("group"));
		PermissionGroup group = plugin.getPermissionsManager().getGroup(groupName);

		if (group == null) {
			PEXAdventure.sendMessage(sender,ChatColor.RED + "Group \"" + groupName + "\" doesn't exist.");
			return;
		}

		if (!group.isVirtual()) {
			PEXAdventure.sendMessage(sender,ChatColor.RED + "Group \"" + args.get("group") + "\" already exists.");
			return;
		}

		if (args.get("parents") != null) {
			String[] parents = args.get("parents").split(",");
			List<PermissionGroup> groups = new LinkedList<>();

			for (String parent : parents) {
				groups.add(plugin.getPermissionsManager().getGroup(parent));
			}

			group.setParents(groups, null);
		}

		PEXAdventure.sendMessage(sender,ChatColor.WHITE + "Group \"" + group.getIdentifier() + "\" created!");

		group.save();
	}

	@Command(name = "pex",
			syntax = "group <group> delete",
			permission = "permissions.manage.groups.remove.<group>",
			description = "Remove <group>")
	public void groupDelete(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
		String groupName = this.autoCompleteGroupName(args.get("group"));

		PermissionGroup group = plugin.getPermissionsManager().getGroup(groupName);

		if (group == null) {
			PEXAdventure.sendMessage(sender,ChatColor.RED + "Group \"" + groupName + "\" doesn't exist.");
			return;
		}

		PEXAdventure.sendMessage(sender,ChatColor.WHITE + "Group \"" + group.getIdentifier() + "\" removed!");

		group.remove();
		plugin.getPermissionsManager().resetGroup(group.getIdentifier());
	}

	/**
	 * Group inheritance
	 */
	@Command(name = "pex",
			syntax = "group <group> parents [world]",
			permission = "permissions.manage.groups.inheritance.<group>",
			description = "List parents for <group> (alias)")
	public void groupListParentsAlias(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
		this.groupListParents(plugin, sender, args);
	}

	@Command(name = "pex",
			syntax = "group <group> parents list [world]",
			permission = "permissions.manage.groups.inheritance.<group>",
			description = "List parents for <group>")
	public void groupListParents(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
		String groupName = this.autoCompleteGroupName(args.get("group"));
		String worldName = this.autoCompleteWorldName(args.get("world"));

		PermissionGroup group = plugin.getPermissionsManager().getGroup(groupName);

		if (group == null) {
			PEXAdventure.sendMessage(sender,ChatColor.RED + "Group \"" + groupName + "\" doesn't exist.");
			return;
		}

		List<String> parentNames = group.getParentIdentifiers(worldName);
		if (parentNames.isEmpty()) {
			PEXAdventure.sendMessage(sender,ChatColor.RED + "Group \"" + group.getIdentifier() + "\" has no parents.");
			return;
		}

		PEXAdventure.sendMessage(sender,"Group " + group.getIdentifier() + " parents:");

		for (String parent : parentNames) {
			PEXAdventure.sendMessage(sender,"  " + parent);
		}

	}

	@Command(name = "pex",
			syntax = "group <group> parents set <parents> [world]",
			permission = "permissions.manage.groups.inheritance.<group>",
			description = "Set parent(s) for <group> (single or comma-separated list)")
	public void groupSetParents(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
		String groupName = this.autoCompleteGroupName(args.get("group"));
		String worldName = this.autoCompleteWorldName(args.get("world"));

		PermissionGroup group = plugin.getPermissionsManager().getGroup(groupName);

		if (group == null) {
			PEXAdventure.sendMessage(sender,ChatColor.RED + "Group \"" + groupName + "\" doesn't exist.");
			return;
		}

		if (args.get("parents") != null) {
			String[] parents = args.get("parents").split(",");
			List<PermissionGroup> groups = new LinkedList<>();

			for (String parent : parents) {
				PermissionGroup parentGroup = plugin.getPermissionsManager().getGroup(this.autoCompleteGroupName(parent));

				if (parentGroup != null && !groups.contains(parentGroup)) {
					groups.add(parentGroup);
				}
			}

			group.setParents(groups, worldName);

			PEXAdventure.sendMessage(sender,ChatColor.WHITE + "Group " + group.getIdentifier() + " inheritance updated!");

			group.save();
		}
	}

	@Command(name = "pex",
			syntax = "group <group> parents add <parents> [world]",
			permission = "permissions.manage.groups.inheritance.<group>",
			description = "Set parent(s) for <group> (single or comma-separated list)")
	public void groupAddParents(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
		String groupName = this.autoCompleteGroupName(args.get("group"));
		String worldName = this.autoCompleteWorldName(args.get("world"));

		PermissionGroup group = plugin.getPermissionsManager().getGroup(groupName);

		if (group == null) {
			PEXAdventure.sendMessage(sender,ChatColor.RED + "Group \"" + groupName + "\" doesn't exist.");
			return;
		}

		if (args.get("parents") != null) {
			String[] parents = args.get("parents").split(",");
			List<PermissionGroup> groups = new LinkedList<>(group.getOwnParents(worldName));

			for (String parent : parents) {
				PermissionGroup parentGroup = plugin.getPermissionsManager().getGroup(this.autoCompleteGroupName(parent));

				if (parentGroup != null && !groups.contains(parentGroup)) {
					groups.add(parentGroup);
				}
			}

			group.setParents(groups, worldName);

			PEXAdventure.sendMessage(sender,ChatColor.WHITE + "Group " + group.getIdentifier() + " inheritance updated!");

			group.save();
		}
	}

	@Command(name = "pex",
			syntax = "group <group> parents remove <parents> [world]",
			permission = "permissions.manage.groups.inheritance.<group>",
			description = "Set parent(s) for <group> (single or comma-separated list)")
	public void groupRemoveParents(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
		String groupName = this.autoCompleteGroupName(args.get("group"));
		String worldName = this.autoCompleteWorldName(args.get("world"));

		PermissionGroup group = plugin.getPermissionsManager().getGroup(groupName);

		if (group == null) {
			PEXAdventure.sendMessage(sender,ChatColor.RED + "Group \"" + groupName + "\" doesn't exist.");
			return;
		}

		if (args.get("parents") != null) {
			String[] parents = args.get("parents").split(",");
			List<PermissionGroup> groups = new LinkedList<>(group.getOwnParents(worldName));

			for (String parent : parents) {
				PermissionGroup parentGroup = plugin.getPermissionsManager().getGroup(this.autoCompleteGroupName(parent));

				groups.remove(parentGroup);
			}

			group.setParents(groups, worldName);

			PEXAdventure.sendMessage(sender,ChatColor.WHITE + "Group \"" + group.getIdentifier() + "\" inheritance updated!");

			group.save();
		}
	}

	/**
	 * Group permissions
	 */
	@Command(name = "pex",
			syntax = "group <group>",
			permission = "permissions.manage.groups.permissions.<group>",
			description = "List all <group> permissions (alias)")
	public void groupListAliasPermissions(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
		this.groupListPermissions(plugin, sender, args);
	}

	@Command(name = "pex",
			syntax = "group <group> list [world]",
			permission = "permissions.manage.groups.permissions.<group>",
			description = "List all <group> permissions in [world]")
	public void groupListPermissions(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
		String groupName = this.autoCompleteGroupName(args.get("group"));
		String worldName = this.autoCompleteWorldName(args.get("world"));

		PermissionGroup group = plugin.getPermissionsManager().getGroup(groupName);

		if (group == null) {
			PEXAdventure.sendMessage(sender,ChatColor.RED + "Group \"" + groupName + "\" doesn't exist.");
			return;
		}

		PEXAdventure.sendMessage(sender,"'" + groupName + "' inherits the following groups:");
		printEntityInheritance(sender, group.getParents());

		Map<String, List<PermissionGroup>> parents = group.getAllParents();
		for (String world : parents.keySet()) {
			if (world == null) {
				continue;
			}

			PEXAdventure.sendMessage(sender,"  @" + world + ":");
			printEntityInheritance(sender, parents.get(world));
		}

		PEXAdventure.sendMessage(sender,"Group \"" + group.getIdentifier() + "\"'s permissions:");
		this.sendMessage(sender, this.mapPermissions(worldName, group, 0));

		PEXAdventure.sendMessage(sender,"Group \"" + group.getIdentifier() + "\"'s Options: ");
		for (Map.Entry<String, String> option : group.getOptions(worldName).entrySet()) {
			PEXAdventure.sendMessage(sender,"  " + option.getKey() + " = \"" + option.getValue() + "\"");
		}
	}

	@Command(name = "pex",
			syntax = "group <group> add <permission> [world]",
			permission = "permissions.manage.groups.permissions.<group>",
			description = "Add <permission> to <group> in [world]")
	public void groupAddPermission(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
		String groupName = this.autoCompleteGroupName(args.get("group"));
		String worldName = this.autoCompleteWorldName(args.get("world"));

		PermissionGroup group = plugin.getPermissionsManager().getGroup(groupName);

		if (group == null) {
			PEXAdventure.sendMessage(sender,ChatColor.RED + "Group \"" + groupName + "\" doesn't exist.");
			return;
		}

		group.addPermission(args.get("permission"), worldName);

		PEXAdventure.sendMessage(sender,ChatColor.WHITE + "Permission \"" + args.get("permission") + "\" added to group \"" + group.getIdentifier() + "\"!");

		this.informGroup(plugin, group, "Your permissions have been changed");
	}

	@Command(name = "pex",
			syntax = "group <group> set <option> <value> [world]",
			permission = "permissions.manage.groups.permissions.<group>",
			description = "Set <option> <value> for <group> in [world]")
	public void groupSetOption(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
		String groupName = this.autoCompleteGroupName(args.get("group"));
		String worldName = this.autoCompleteWorldName(args.get("world"));

		PermissionGroup group = plugin.getPermissionsManager().getGroup(groupName);

		if (group == null) {
			PEXAdventure.sendMessage(sender,ChatColor.RED + "Group \"" + groupName + "\" doesn't exist.");
			return;
		}

		group.setOption(args.get("option"), args.get("value"), worldName);

		if (args.containsKey("value") && args.get("value").isEmpty()) {
			PEXAdventure.sendMessage(sender,ChatColor.WHITE + "Option \"" + args.get("option") + "\" cleared!");
		} else {
			PEXAdventure.sendMessage(sender,ChatColor.WHITE + "Option \"" + args.get("option") + "\" set!");
		}

		this.informGroup(plugin, group, "Your permissions has been changed");
	}

	@Command(name = "pex",
			syntax = "group <group> remove <permission> [world]",
			permission = "permissions.manage.groups.permissions.<group>",
			description = "Remove <permission> from <group> in [world]")
	public void groupRemovePermission(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
		String groupName = this.autoCompleteGroupName(args.get("group"));
		String worldName = this.autoCompleteWorldName(args.get("world"));

		PermissionGroup group = plugin.getPermissionsManager().getGroup(groupName);

		if (group == null) {
			PEXAdventure.sendMessage(sender,ChatColor.RED + "Group \"" + groupName + "\" doesn't exist.");
			return;
		}

		String permission = this.autoCompletePermission(group, args.get("permission"), worldName);

		group.removePermission(permission, worldName);
		group.removeTimedPermission(permission, worldName);

		PEXAdventure.sendMessage(sender,ChatColor.WHITE + "Permission \"" + permission + "\" removed from group \"" + group.getIdentifier() + "\"!");

		this.informGroup(plugin, group, "Your permissions have been changed");
	}

	@Command(name = "pex",
			syntax = "group <group> swap <permission> <targetPermission> [world]",
			permission = "permissions.manage.groups.permissions.<group>",
			description = "Swap <permission> and <targetPermission> in permission list. Could be number or permission itself")
	public void userSwapPermission(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
		String groupName = this.autoCompleteGroupName(args.get("group"));
		String worldName = this.autoCompleteWorldName(args.get("world"));

		PermissionGroup group = plugin.getPermissionsManager().getGroup(groupName);

		if (group == null) {
			PEXAdventure.sendMessage(sender,ChatColor.RED + "Group \"" + groupName + "\" doesn't exist.");
			return;
		}


		List<String> permissions = group.getOwnPermissions(worldName);

		try {
			int sourceIndex = this.getPosition(this.autoCompletePermission(group, args.get("permission"), worldName, "permission"), permissions);
			int targetIndex = this.getPosition(this.autoCompletePermission(group, args.get("targetPermission"), worldName, "targetPermission"), permissions);

			String targetPermission = permissions.get(targetIndex);

			permissions.set(targetIndex, permissions.get(sourceIndex));
			permissions.set(sourceIndex, targetPermission);

			group.setPermissions(permissions, worldName);

			PEXAdventure.sendMessage(sender,"Permissions swapped!");
		} catch (Throwable e) {
			PEXAdventure.sendMessage(sender,ChatColor.RED + "Error: " + e.getMessage());
		}
	}

	@Command(name = "pex",
			syntax = "group <group> timed add <permission> [lifetime] [world]",
			permission = "permissions.manage.groups.permissions.timed.<group>",
			description = "Add timed <permission> to <group> with [lifetime] in [world]")
	public void groupAddTimedPermission(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
		String groupName = this.autoCompleteGroupName(args.get("group"));
		String worldName = this.autoCompleteWorldName(args.get("world"));

		int lifetime = 0;

		if (args.containsKey("lifetime")) {
			lifetime = DateUtils.parseInterval(args.get("lifetime"));
		}

		PermissionGroup group = plugin.getPermissionsManager().getGroup(groupName);

		if (group == null) {
			PEXAdventure.sendMessage(sender,ChatColor.RED + "Group \"" + groupName + "\" doesn't exist.");
			return;
		}

		group.addTimedPermission(args.get("permission"), worldName, lifetime);

		PEXAdventure.sendMessage(sender,ChatColor.WHITE + "Timed permission added!");
		this.informGroup(plugin, group, "Your permissions have been changed!");

		plugin.getLogger().info("Group " + groupName + " get timed permission \"" + args.get("permission") + "\" "
				+ (lifetime > 0 ? "for " + lifetime + " seconds " : " ") + "from " + sender.getName());
	}

	@Command(name = "pex",
			syntax = "group <group> timed remove <permission> [world]",
			permission = "permissions.manage.groups.permissions.timed.<group>",
			description = "Remove timed <permissions> for <group> in [world]")
	public void groupRemoveTimedPermission(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
		String groupName = this.autoCompleteGroupName(args.get("group"));
		String worldName = this.autoCompleteWorldName(args.get("world"));

		PermissionGroup group = plugin.getPermissionsManager().getGroup(groupName);

		if (group == null) {
			PEXAdventure.sendMessage(sender,ChatColor.RED + "Group \"" + groupName + "\" doesn't exist.");
			return;
		}

		group.removeTimedPermission(args.get("permission"), worldName);

		PEXAdventure.sendMessage(sender,ChatColor.WHITE + "Timed permission \"" + args.get("permission") + "\" removed!");
		this.informGroup(plugin, group, "Your permissions have been changed!");
	}

	/**
	 * Group users management
	 */
	@Command(name = "pex",
			syntax = "group <group> users",
			permission = "permissions.manage.membership.<group>",
			description = "List all users in <group>")
	public void groupUsersList(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
		String groupName = this.autoCompleteGroupName(args.get("group"));

		Set<PermissionUser> users = plugin.getPermissionsManager().getUsers(groupName);

		if (users == null || users.isEmpty()) {
			PEXAdventure.sendMessage(sender,ChatColor.RED + "Group \"" + groupName + "\" doesn't exist.");
			return;
		}

		if (users.isEmpty()) {
			PEXAdventure.sendMessage(sender,ChatColor.RED + "Group \"" + groupName + "\" has no users.");
			return;
		}

		PEXAdventure.sendMessage(sender,"Group \"" + groupName + "\"'s users (" + users.size() + "):");

		for (PermissionUser user : users) {
			PEXAdventure.sendMessage(sender,"   " + describeUser(user));
		}
	}

	@Command(name = "pex",
			syntax = "group <group> user add <user> [world]",
			permission = "permissions.manage.membership.<group>",
			description = "Add <user> (single or comma-separated list) to <group>")
	public void groupUsersAdd(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
		String groupName = this.autoCompleteGroupName(args.get("group"));
		String worldName = this.autoCompleteWorldName(args.get("world"));

		String users[];

		if (!args.get("user").contains(",")) {
			users = new String[]{args.get("user")};
		} else {
			users = args.get("user").split(",");
		}

		for (String userName : users) {
			userName = this.autoCompletePlayerName(userName);
			PermissionUser user = plugin.getPermissionsManager().getUser(userName);

			if (user == null) {
				PEXAdventure.sendMessage(sender,ChatColor.RED + "User \"" + userName + "\" doesn't exist.");
				return;
			}

			user.addGroup(groupName, worldName);

			PEXAdventure.sendMessage(sender,ChatColor.WHITE + "User " + user.getName() + " added to " + groupName + " !");
			this.informPlayer(plugin, user, "You are assigned to \"" + groupName + "\" group");
		}
	}

	@Command(name = "pex",
			syntax = "group <group> user remove <user> [world]",
			permission = "permissions.manage.membership.<group>",
			description = "Add <user> (single or comma-separated list) to <group>")
	public void groupUsersRemove(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
		String groupName = this.autoCompleteGroupName(args.get("group"));
		String worldName = this.autoCompleteWorldName(args.get("world"));

		String users[];

		if (!args.get("user").contains(",")) {
			users = new String[]{args.get("user")};
		} else {
			users = args.get("user").split(",");
		}

		for (String userName : users) {
			userName = this.autoCompletePlayerName(userName);
			PermissionUser user = plugin.getPermissionsManager().getUser(userName);

			if (user == null) {
				PEXAdventure.sendMessage(sender,ChatColor.RED + "User \"" + userName + "\" doesn't exist.");
				return;
			}

			user.removeGroup(groupName, worldName);

			PEXAdventure.sendMessage(sender,ChatColor.WHITE + "User " + user.getName() + " removed from " + args.get("group") + " !");
			this.informPlayer(plugin, user, "You were removed from \"" + groupName + "\" group");

		}
	}

	@Command(name = "pex",
			syntax = "default group [world]",
			permission = "permissions.manage.groups.inheritance",
			description = "Display default group for specified world")
	public void groupDefaultCheck(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
		String worldName = this.autoCompleteWorldName(args.get("world"));


		List<PermissionGroup> defaultGroups = plugin.getPermissionsManager().getDefaultGroups(worldName);
		PEXAdventure.sendMessage(sender,"Default groups in world \"" + worldName + "\" are:");
		for (PermissionGroup grp : defaultGroups) {
			PEXAdventure.sendMessage(sender,"  - " + grp.getIdentifier());
		}
	}

	@Command(name = "pex",
			syntax = "set default group <group> <value> [world]",
			permission = "permissions.manage.groups.inheritance",
			description = "Set default group for specified world")
	public void groupDefaultSet(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
		String groupName = this.autoCompleteGroupName(args.get("group"));
		boolean def = Boolean.parseBoolean(args.get("value"));
		String worldName = this.autoCompleteWorldName(args.get("world"));

		PermissionGroup group = plugin.getPermissionsManager().getGroup(groupName);

		if (group == null || group.isVirtual()) {
			PEXAdventure.sendMessage(sender,ChatColor.RED + "Group \"" + groupName + "\" doesn't exist.");
			return;
		}

		group.setDefault(def, worldName);
		PEXAdventure.sendMessage(sender,"Group \"" + groupName + "\" is " + (def ? "now" : "no longer") + " default in world \"" + worldName + "\"");
	}
}
