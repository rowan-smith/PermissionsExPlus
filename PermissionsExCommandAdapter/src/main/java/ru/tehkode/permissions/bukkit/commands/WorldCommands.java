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

import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import ru.tehkode.permissions.bukkit.PermissionsEx;
import ru.tehkode.permissions.commands.Command;

public class WorldCommands extends PermissionsCommand {

    @Command(name = "pex", syntax = "worlds", description = "Print loaded worlds", isPrimary = true, permission = "permissions.manage.worlds")
    public void worldsList(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
        sender.sendMessage("Worlds on server: ");

        for (World world : Bukkit.getWorlds()) {
            sender.sendMessage(" " + world.getName());
        }
    }

    @Command(name = "pex", syntax = "world <world>", description = "Print world info", permission = "permissions.manage.worlds")
    public void worldInfo(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
        String world = args.get("world");
        World bukkitWorld = Bukkit.getWorld(world);

        if (bukkitWorld == null) {
            sender.sendMessage("Specified world \"" + world + "\" not found.");
            return;
        }

        sender.sendMessage("World \"" + bukkitWorld.getName() + "\" inherits nothing.");
    }

    @Command(name = "pex", syntax = "world <world> inherit <parentWorlds>", description = "Set world inheritance", permission = "permissions.manage.worlds.inheritance")
    public void worldInherit(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
        String world = args.get("world");

        if (Bukkit.getWorld(world) == null) {
            sender.sendMessage("Specified world \"" + world + "\" not found.");
            return;
        }

        sender.sendMessage(ChatColor.YELLOW + "World inheritance is not managed by the command adapter.");
        sender.sendMessage(ChatColor.DARK_GRAY + "Use PermissionsExPlus context configuration / permission contexts instead.");
    }
}
