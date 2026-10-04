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

import dev.rono.permissions.api.ladder.PromotionResult;
import dev.rono.permissions.api.ladder.PromotionStatus;
import java.util.ArrayList;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import ru.tehkode.permissions.bukkit.ApiWrapper;
import ru.tehkode.permissions.bukkit.PermissionsEx;
import ru.tehkode.permissions.commands.Command;

public class PromotionCommands extends PermissionsCommand {

    @Command(name = "pex", syntax = "group <group> rank [rank] [ladder]", description = "Get or set group rank on ladder", isPrimary = true, permission = "permissions.groups.rank.<group>")
    public void groupRank(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
        try {
            String groupName = autoCompleteGroupName(args.get("group"));
            String ladderName = args.getOrDefault("ladder", "default");
            ApiWrapper.requireOrCreateGroup(groupName);

            if (!args.containsKey("rank")) {
                var ladder = ApiWrapper.await(ApiWrapper.ladders().find(ladderName));

                if (ladder.isEmpty()) {
                    sender.sendMessage("Group " + groupName + " is unranked");
                    return;
                }

                var position = ladder.get().positionOf(groupName);

                if (position.isEmpty()) {
                    sender.sendMessage("Group " + groupName + " is unranked");
                } else {
                    sender.sendMessage("Group " + groupName + " rank is " + position.getAsInt() + " (ladder = " + ladderName + ")");
                }

                return;
            }

            int rank = parseInteger(args.get("rank"));
            var ladder = ApiWrapper.await(ApiWrapper.ladders().find(ladderName))
                    .orElseGet(() -> ApiWrapper.await(ApiWrapper.ladders().create(ladderName)));

            ApiWrapper.await(ApiWrapper.ladders().modify(ladder.name(), modifier -> {
                var groups = new ArrayList<>(ladder.groups());
                groups.removeIf(name -> name.equalsIgnoreCase(groupName));

                int position = Math.max(0, Math.min(rank, groups.size()));
                groups.add(position, groupName);
                modifier.setGroups(groups);
            }));

            sender.sendMessage("Group " + groupName + " rank is " + rank + " (ladder = " + ladderName + ")");
        } catch (Exception error) {
            sendError(sender, error);
        }
    }

    @Command(name = "pex", syntax = "promote <user> [ladder]", description = "Promotes user on ladder", isPrimary = true)
    public void promoteUser(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
        try {
            var user = ApiWrapper.requireUser(autoCompletePlayerName(args.get("user")));
            String ladder = args.getOrDefault("ladder", "default");
            PromotionResult result = ApiWrapper.await(ApiWrapper.ladders().promote(user, ladder));
            sendPromotion(plugin, sender, user.name(), ladder, result, true);
        } catch (Exception error) {
            sendError(sender, error);
        }
    }

    @Command(name = "pex", syntax = "demote <user> [ladder]", description = "Demotes user on ladder", isPrimary = true)
    public void demoteUser(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
        try {
            var user = ApiWrapper.requireUser(autoCompletePlayerName(args.get("user")));
            String ladder = args.getOrDefault("ladder", "default");
            PromotionResult result = ApiWrapper.await(ApiWrapper.ladders().demote(user, ladder));
            sendPromotion(plugin, sender, user.name(), ladder, result, false);
        } catch (Exception error) {
            sendError(sender, error);
        }
    }

    @Command(name = "promote", syntax = "<user> [ladder]", description = "Promotes user", isPrimary = true, permission = "permissions.user.rank.promote")
    public void promoteAlias(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
        promoteUser(plugin, sender, args);
    }

    @Command(name = "demote", syntax = "<user> [ladder]", description = "Demotes user", isPrimary = true, permission = "permissions.user.rank.demote")
    public void demoteAlias(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
        demoteUser(plugin, sender, args);
    }

    private void sendPromotion(PermissionsEx plugin, CommandSender sender, String user, String ladder, PromotionResult result, boolean promote) {
        if (result.status() == PromotionStatus.PROMOTED || result.status() == PromotionStatus.DEMOTED) {
            String newGroup = result.newGroup().orElse("?");
            sender.sendMessage("User " + user + " " + (promote ? "promoted" : "demoted") + " to " + newGroup + " group");
            informPlayer(plugin, user, "You have been " + (promote ? "promoted" : "demoted") + " on " + ladder
                    + " ladder to " + newGroup + " group");
            return;
        }

        String label = promote ? "Promotion" : "Demotion";
        sender.sendMessage(ChatColor.RED + label + " error: " + result.status().name());
    }
}
