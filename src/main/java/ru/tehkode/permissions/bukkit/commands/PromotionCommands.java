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
import org.bukkit.entity.Player;
import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PEXAdventure;
import ru.tehkode.permissions.bukkit.PermissionsEx;
import ru.tehkode.permissions.commands.Command;
import ru.tehkode.permissions.exceptions.RankingException;

import java.util.Map;

public class PromotionCommands extends PermissionsCommand {

	@Command(name = "pex",
			syntax = "group <group> rank [rank] [ladder]",
			description = "Get or set <group> [rank] [ladder]",
			isPrimary = true,
			permission = "permissions.groups.rank.<group>")
	public void rankGroup(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
		String groupName = this.autoCompleteGroupName(args.get("group"));

		PermissionGroup group = plugin.getPermissionsManager().getGroup(groupName);

		if (group == null) {
			PEXAdventure.sendMessage(sender,ChatColor.RED + "Group \"" + groupName + "\" not found");
			return;
		}

		if (args.get("rank") != null) {
			String newRank = args.get("rank").trim();

			try {
				group.setRank(Integer.parseInt(newRank));
			} catch (NumberFormatException e) {
				PEXAdventure.sendMessage(sender,"Wrong rank. Make sure it's number.");
			}

			if (args.containsKey("ladder")) {
				group.setRankLadder(args.get("ladder"));
			}
		}

		int rank = group.getRank();

		if (rank > 0) {
			PEXAdventure.sendMessage(sender,"Group " + group.getIdentifier() + " rank is " + rank + " (ladder = " + group.getRankLadder() + ")");
		} else {
			PEXAdventure.sendMessage(sender,"Group " + group.getIdentifier() + " is unranked");
		}
	}

	@Command(name = "pex",
			syntax = "promote <user> [ladder]",
			description = "Promotes <user> to next group on [ladder]",
			isPrimary = true)
	public void promoteUser(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
		String userName = this.autoCompletePlayerName(args.get("user"));
		PermissionUser user = plugin.getPermissionsManager().getUser(userName);

		if (user == null) {
			PEXAdventure.sendMessage(sender,"Specified user \"" + args.get("user") + "\" not found!");
			return;
		}

		String promoterName = "console";
		String ladder = "default";

		if (args.containsKey("ladder")) {
			ladder = args.get("ladder");
		}

		PermissionUser promoter = null;
		if (sender instanceof Player) {
			promoter = plugin.getPermissionsManager().getUser((Player) sender);
			if (promoter == null || !promoter.has("permissions.user.promote." + ladder, ((Player) sender).getWorld().getName())) {
				PEXAdventure.sendMessage(sender,ChatColor.RED + "You don't have enough permissions to promote on this ladder");
				return;
			}

			promoterName = promoter.getName();
		}

		try {
			PermissionGroup targetGroup = user.promote(promoter, ladder);

			this.informPlayer(plugin, user, "You have been promoted on " + targetGroup.getRankLadder() + " ladder to " + targetGroup.getIdentifier() + " group");
			PEXAdventure.sendMessage(sender,"User " + describeUser(user) + " promoted to " + targetGroup.getIdentifier() + " group");
			plugin.getLogger().info("User " + describeUser(user) + " has been promoted to " + targetGroup.getIdentifier() + " group on " + targetGroup.getRankLadder() + " ladder by " + promoterName);
		} catch (RankingException e) {
			PEXAdventure.sendMessage(sender,ChatColor.RED + "Promotion error: " + e.getMessage());
			plugin.getLogger().severe("Ranking Error (" + promoterName + " > " + describeUser(e.getTarget()) + "): " + e.getMessage());
		}
	}

	@Command(name = "pex",
			syntax = "demote <user> [ladder]",
			description = "Demotes <user> to previous group on [ladder]",
			isPrimary = true)
	public void demoteUser(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
		String userName = this.autoCompletePlayerName(args.get("user"));
		PermissionUser user = plugin.getPermissionsManager().getUser(userName);

		if (user == null) {
			PEXAdventure.sendMessage(sender,ChatColor.RED + "Specified user \"" + args.get("user") + "\" not found!");
			return;
		}

		String demoterName = "console";
		String ladder = "default";

		if (args.containsKey("ladder")) {
			ladder = args.get("ladder");
		}

		PermissionUser demoter = null;
		if (sender instanceof Player) {
			demoter = plugin.getPermissionsManager().getUser((Player) sender);

			if (demoter == null || !demoter.has("permissions.user.demote." + ladder, ((Player) sender).getWorld().getName())) {
				PEXAdventure.sendMessage(sender,ChatColor.RED + "You don't have enough permissions to demote on this ladder");
				return;
			}

			demoterName = demoter.getName();
		}

		try {
			PermissionGroup targetGroup = user.demote(demoter, ladder);

			this.informPlayer(plugin, user, "You have been demoted on " + targetGroup.getRankLadder() + " ladder to " + targetGroup.getIdentifier() + " group");
			PEXAdventure.sendMessage(sender,"User " + describeUser(user) + " demoted to " + targetGroup.getIdentifier() + " group");
			plugin.getLogger().info("User " + describeUser(user) + " has been demoted to " + targetGroup.getIdentifier() + " group on " + targetGroup.getRankLadder() + " ladder by " + demoterName);
		} catch (RankingException e) {
			PEXAdventure.sendMessage(sender,ChatColor.RED + "Demotion error: " + e.getMessage());
			plugin.getLogger().severe("Ranking Error (" + demoterName + " demotes " + describeUser(e.getTarget()) + "): " + e.getMessage());
		}
	}

	@Command(name = "promote",
			syntax = "<user> [ladder]",
			description = "Promotes <user> to next group",
			isPrimary = true,
			permission = "permissions.user.rank.promote")
	public void promoteUserAlias(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
		this.promoteUser(plugin, sender, args);
	}

	@Command(name = "demote",
			syntax = "<user> [ladder]",
			description = "Demotes <user> to previous group",
			isPrimary = true,
			permission = "permissions.user.rank.demote")
	public void demoteUserAlias(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
		this.demoteUser(plugin, sender, args);
	}
}
