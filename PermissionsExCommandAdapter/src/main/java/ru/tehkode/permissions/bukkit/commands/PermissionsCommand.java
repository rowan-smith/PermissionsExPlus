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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.tehkode.permissions.bukkit.ApiWrapper;
import ru.tehkode.permissions.bukkit.PermissionsEx;
import ru.tehkode.permissions.commands.CommandListener;
import ru.tehkode.permissions.commands.CommandsManager;
import ru.tehkode.permissions.commands.exceptions.AutoCompleteChoicesException;
import ru.tehkode.utils.StringUtils;

public abstract class PermissionsCommand implements CommandListener {
    protected CommandsManager manager;

    @Override
    public void onRegistered(CommandsManager manager) {
        this.manager = manager;
    }

    protected void informPlayer(PermissionsEx plugin, String userName, String message) {
        Player player = Bukkit.getPlayerExact(userName);

        if (player == null) {
            try {
                UUID id = UUID.fromString(userName);
                player = Bukkit.getPlayer(id);
            } catch (IllegalArgumentException ignored) {}
        }

        if (player != null) {
            player.sendMessage(ChatColor.BLUE + "[PermissionsEx] " + ChatColor.RESET + message);
        }
    }

    protected String autoCompletePlayerName(String playerName) {
        return autoCompletePlayerName(playerName, "user");
    }

    private String nameToUuid(String name) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(name);
        return player.getUniqueId() != null ? player.getUniqueId().toString() : name;
    }

    protected String autoCompletePlayerName(String playerName, String argName) {
        if (playerName == null) {
            return null;
        }

        if (playerName.startsWith("#")) {
            return nameToUuid(playerName.substring(1));
        }

        List<String> players = new LinkedList<>();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getName().equalsIgnoreCase(playerName)) {
                return player.getUniqueId().toString();
            }

            if (player.getName().toLowerCase(Locale.ROOT).startsWith(playerName.toLowerCase(Locale.ROOT))) {
                players.add(player.getUniqueId().toString());
            }
        }

        for (String name : ApiWrapper.users().cache().names()) {
            if (name.equalsIgnoreCase(playerName)) {
                return ApiWrapper.users().cache().get(name).map(user -> user.uniqueId().toString()).orElse(name);
            }

            if (name.toLowerCase(Locale.ROOT).startsWith(playerName.toLowerCase(Locale.ROOT))) {
                ApiWrapper.users().cache().get(name).ifPresent(user -> {
                    String id = user.uniqueId().toString();

                    if (!players.contains(id)) {
                        players.add(id);
                    }
                });
            }
        }

        if (players.size() > 1) {
            throw new AutoCompleteChoicesException(players.toArray(new String[0]), argName);
        }

        if (players.size() == 1) {
            return players.getFirst();
        }

        return playerName;
    }

    protected String autoCompleteGroupName(String groupName) {
        return autoCompleteGroupName(groupName, "group");
    }

    protected String autoCompleteGroupName(String groupName, String argName) {
        if (groupName == null || groupName.startsWith("#")) {
            return groupName;
        }

        List<String> groups = new ArrayList<>();

        for (String name : ApiWrapper.groups().cache().identifiers()) {
            if (name.equalsIgnoreCase(groupName)) {
                return name;
            }

            if (name.toLowerCase(Locale.ROOT).startsWith(groupName.toLowerCase(Locale.ROOT))) {
                groups.add(name);
            }
        }

        if (groups.size() > 1) {
            throw new AutoCompleteChoicesException(groups.toArray(new String[0]), argName);
        }

        if (groups.size() == 1) {
            return groups.getFirst();
        }

        return groupName;
    }

    protected String getSafeWorldName(String worldName, String userName) {
        if (worldName == null) {
            Player player = Bukkit.getPlayerExact(userName);

            if (player != null) {
                return player.getWorld().getName();
            }

            return null;
        }

        return worldName;
    }

    protected String describeUser(dev.rono.permissions.api.user.User user) {
        return user.name();
    }

    protected String worldClause(String worldName) {
        return worldName != null ? " (in world \"" + worldName + "\") " : "";
    }

    protected void sendError(CommandSender sender, Exception error) {
        sender.sendMessage(ChatColor.RED + (error.getMessage() != null ? error.getMessage() : error.toString()));
    }

    protected String describePermission(dev.rono.permissions.api.permission.PermissionNode node) {
        StringBuilder builder = new StringBuilder(node.permission());

        if (node.value() == dev.rono.permissions.api.permission.PermissionValue.DENY && !node.permission().startsWith("-")) {
            builder.insert(0, '-');
        }

        if (!node.contexts().isEmpty()) {
            builder.append(" @").append(node.contexts());
        }

        node.expiry().ifPresent(expiry -> builder.append(" (expires ").append(expiry).append(')'));
        return builder.toString();
    }

    protected String join(Map<String, String> args, String key, String fallback) {
        String value = args.get(key);
        return value != null ? value : fallback;
    }

    protected int parseInteger(String value) {
        if (value == null || value.isBlank()) {
            return 0;
        }

        return StringUtils.toInteger(value, 0);
    }
}
