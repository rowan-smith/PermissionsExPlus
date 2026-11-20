/*
 * PexCommandAdapter - Permissions plugin for Bukkit
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
package dev.rono.permissions.compat.commands.handlers;

import dev.rono.permissions.compat.commands.util.PEXAdventure;

import lombok.var;

import java.util.List;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import dev.rono.permissions.compat.commands.ApiWrapper;
import dev.rono.permissions.core.migration.LegacyPexImporter;
import dev.rono.permissions.compat.commands.PexCommandAdapter;
import dev.rono.permissions.compat.commands.framework.Command;
import dev.rono.permissions.compat.commands.framework.CommandsManager.CommandBinding;

public class UtilityCommands extends PermissionsCommand {

    @Command(name = "pex", syntax = "reload", permission = "permissions.manage.reload", description = "Reload environment")
    public void reload(PexCommandAdapter plugin, CommandSender sender, Map<String, String> args) {
        try {
            ApiWrapper.core().reload();
            PEXAdventure.sendMessage(sender, ChatColor.WHITE + "Permissions reloaded");
        } catch (Exception error) {
            sendError(sender, error);
        }
    }

    @Command(name = "pex", syntax = "report", permission = "permissions.manage.reportbug", description = "Create an issue template")
    public void report(PexCommandAdapter plugin, CommandSender sender, Map<String, String> args) {
        PEXAdventure.sendMessage(sender, "Report issues at: https://github.com/rowan-smith/PermissionsExPlus/issues");
    }

    @Command(name = "pex", syntax = "config <node> [value]", permission = "permissions.manage.config", description = "Print or set config node")
    public void config(PexCommandAdapter plugin, CommandSender sender, Map<String, String> args) {
        PEXAdventure.sendMessage(sender, ChatColor.YELLOW + "Runtime config editing is not exposed by the command adapter.");
        PEXAdventure.sendMessage(sender, ChatColor.DARK_GRAY + "Edit config.yml / advanced.yml / database.yml and use /pex reload.");
    }

    @Command(name = "pex", syntax = "backend", permission = "permissions.manage.backend", description = "Print currently used backend")
    public void backendInfo(PexCommandAdapter plugin, CommandSender sender, Map<String, String> args) {
        var backend = ApiWrapper.api().backend().current();
        PEXAdventure.sendMessage(sender, "Current backend: " + backend.name());
    }

    @Command(name = "pex", syntax = "backend <backend>", permission = "permissions.manage.backend", description = "Change backend")
    public void backendSet(PexCommandAdapter plugin, CommandSender sender, Map<String, String> args) {
        PEXAdventure.sendMessage(sender, ChatColor.YELLOW + "Live backend switching is not supported. Change database.yml and reload/restart.");
        backendInfo(plugin, sender, args);
    }

    @Command(name = "pex", syntax = "hierarchy [world]", permission = "permissions.manage.users", description = "Print hierarchy")
    public void hierarchy(PexCommandAdapter plugin, CommandSender sender, Map<String, String> args) {
        PEXAdventure.sendMessage(sender, "User/Group inheritance hierarchy:");

        for (var group : ApiWrapper.groups().cache().all()) {
            String parents = group.parents().stream().map(node -> node.group()).reduce((a, b) -> a + ", " + b).orElse("-");
            PEXAdventure.sendMessage(sender, " " + group.name() + " <- " + parents);
        }
    }

    @Command(name = "pex", syntax = "convert uuid [force]", permission = "permissions.convert", description = "Bulk convert to UUID")
    public void convertUuid(PexCommandAdapter plugin, CommandSender sender, Map<String, String> args) {
        PEXAdventure.sendMessage(sender, ChatColor.YELLOW + "UUID conversion is handled by PermissionsExPlus identity resolution; no bulk convert command is needed.");
    }

    @Command(name = "pex", syntax = "import <backend>", permission = "permissions.dump", description = "Import data from backend")
    public void importData(PexCommandAdapter plugin, CommandSender sender, Map<String, String> args) {
        if ("permissionsex".equalsIgnoreCase(args.get("backend"))) {
            // Original PermissionsEx data from plugins/PermissionsEx, using the backend its
            // config.yml selects.
            LegacyPexImporter.importInstallation(ApiWrapper.core(), null, message -> PEXAdventure.sendMessage(sender, message),
                    source -> ApiWrapper.core().logger().audit(sender.getName(), "IMPORTED PermissionsEx data from " + source));
            return;
        }

        PEXAdventure.sendMessage(sender, ChatColor.YELLOW + "PermissionsExPlus keeps one backend, so there is nothing to import from '" + args.get("backend") + "'.");
        PEXAdventure.sendMessage(sender, ChatColor.YELLOW + "To import an original PermissionsEx installation: /pex import permissionsex");
    }

    @Command(name = "pex", syntax = "toggle debug", permission = "permissions.debug", description = "Enable/disable debug mode")
    public void toggleDebug(PexCommandAdapter plugin, CommandSender sender, Map<String, String> args) {
        PEXAdventure.sendMessage(sender, ChatColor.YELLOW + "Toggle debug via PermissionsExPlus config (debug / verbose) and /pex reload.");
    }

    @Command(name = "pex", syntax = "help [page] [count]", permission = "permissions.manage", description = "PermissionsEx commands help")
    public void help(PexCommandAdapter plugin, CommandSender sender, Map<String, String> args) {
        List<CommandBinding> commands = plugin.getCommandsManager().getCommands();
        int count = Math.max(1, parseInteger(args.getOrDefault("count", "7")));
        int page = Math.max(1, parseInteger(args.getOrDefault("page", "1")));
        int totalPages = Math.max(1, (commands.size() + count - 1) / count);
        int from = (page - 1) * count;
        int to = Math.min(from + count, commands.size());

        if (from >= commands.size()) {
            PEXAdventure.sendMessage(sender, ChatColor.RED + "Page couldn't be lower than 1");
            return;
        }

        PEXAdventure.sendMessage(sender, ChatColor.BLUE + "PermissionsEx" + ChatColor.WHITE + " commands (page "
                + ChatColor.GOLD + page + "/" + totalPages + ChatColor.WHITE + "): ");

        for (int index = from; index < to; index++) {
            CommandBinding binding = commands.get(index);
            var annotation = binding.getMethodAnnotation();
            String commandName = "/" + annotation.name() + " " + annotation.syntax();
            PEXAdventure.sendMessage(sender, ChatColor.GOLD + commandName);
            PEXAdventure.sendMessage(sender, ChatColor.AQUA + "    " + annotation.description());
        }
    }

    @Command(name = "pex", syntax = "version", permission = "permissions.manage", description = "Display version")
    public void version(PexCommandAdapter plugin, CommandSender sender, Map<String, String> args) {
        PEXAdventure.sendMessage(sender, "[PermissionsEx] version [" + plugin.getDescription().getVersion() + "]");
    }
}
