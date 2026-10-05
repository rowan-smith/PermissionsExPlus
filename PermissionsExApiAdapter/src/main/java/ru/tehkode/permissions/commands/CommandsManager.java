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
package ru.tehkode.permissions.commands;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.tehkode.permissions.bukkit.PermissionsEx;

/**
 * ABI shell. Command UX is owned by PermissionsExCommandAdapter.
 */
public class CommandsManager {

    protected Map<String, Map<CommandSyntax, CommandBinding>> listeners = new LinkedHashMap<>();
    protected PermissionsEx plugin;

    public CommandsManager(PermissionsEx plugin) {
        this.plugin = plugin;
    }

    public void register(CommandListener listener) {
        listener.onRegistered(this);
    }

    public boolean execute(CommandSender sender, org.bukkit.command.Command command, String[] args) {
        return false;
    }

    public List<CommandBinding> getCommands() {
        List<CommandBinding> commands = new LinkedList<>();
        for (Map<CommandSyntax, CommandBinding> map : this.listeners.values()) {
            commands.addAll(map.values());
        }
        return commands;
    }

    protected class CommandSyntax {
        protected String originalSyntax;
        protected String regexp;
        protected List<String> arguments = new LinkedList<>();

        public CommandSyntax(String syntax) {
            this.originalSyntax = syntax;
            this.regexp = syntax == null ? "" : syntax;
        }

        public String getRegexp() {
            return regexp;
        }

        public boolean isMatch(String str) {
            return false;
        }

        public Map<String, String> getMatchedArguments(String str) {
            return Collections.emptyMap();
        }
    }

    public class CommandBinding {
        protected Object object;
        protected Method method;
        protected Map<String, String> params = new HashMap<>();

        public CommandBinding(Object object, Method method) {
            this.object = object;
            this.method = method;
        }

        public Command getMethodAnnotation() {
            return this.method == null ? null : this.method.getAnnotation(Command.class);
        }

        public Map<String, String> getParams() {
            return params;
        }

        public void setParams(Map<String, String> params) {
            this.params = params;
        }

        public boolean checkPermissions(Player player) {
            return false;
        }

        public void call(Object... args) throws Exception {}
    }
}
