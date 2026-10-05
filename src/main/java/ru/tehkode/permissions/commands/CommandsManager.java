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

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import ru.tehkode.permissions.PermissionEntity;
import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;
import ru.tehkode.permissions.commands.exceptions.AutoCompleteChoicesException;
import ru.tehkode.utils.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author code
 */
public class CommandsManager {

	protected Map<String, Map<CommandSyntax, CommandBinding>> listeners = new LinkedHashMap<>();
	protected PermissionsEx plugin;

	public CommandsManager(PermissionsEx plugin) {
		this.plugin = plugin;
	}

	public void register(CommandListener listener) {
		for (Method method : listener.getClass().getMethods()) {
			if (!method.isAnnotationPresent(Command.class)) {
				continue;
			}

			Command cmdAnnotation = method.getAnnotation(Command.class);

			Map<CommandSyntax, CommandBinding> commandListeners = listeners.get(cmdAnnotation.name());
			if (commandListeners == null) {
				commandListeners = new LinkedHashMap<>();
				listeners.put(cmdAnnotation.name(), commandListeners);
			}

			commandListeners.put(new CommandSyntax(cmdAnnotation.syntax()), new CommandBinding(listener, method));
		}

		listener.onRegistered(this);
	}

	public boolean execute(CommandSender sender, org.bukkit.command.Command command, String[] args) {
		Map<CommandSyntax, CommandBinding> callMap = this.listeners.get(command.getName());

		if (callMap == null) { // No commands registered
			return false;
		}

		CommandBinding selectedBinding = null;
		int argumentsLength = 0;
		String arguments = StringUtils.implode(args, " ");

		for (Entry<CommandSyntax, CommandBinding> entry : callMap.entrySet()) {
			CommandSyntax syntax = entry.getKey();
			if (!syntax.isMatch(arguments)) {
				continue;
			}
			if (selectedBinding != null && syntax.getRegexp().length() < argumentsLength) { // match, but there already more fitted variant
				continue;
			}

			CommandBinding binding = entry.getValue();
			binding.setParams(syntax.getMatchedArguments(arguments));
			selectedBinding = binding;
		}

		if (selectedBinding == null) { // there is fitting handler
			sender.sendMessage(ChatColor.RED + "Error in command syntax. Check command help.");
			return true;
		}

		// Check permission
		if (sender instanceof Player) { // this method are not public and required permission
			if (!selectedBinding.checkPermissions((Player) sender)) {
				plugin.getLogger().warning("User " + sender.getName() + " tried to access chat command \""
						+ command.getName() + " " + arguments
						+ "\", but doesn't have permission to do this.");
				sender.sendMessage(ChatColor.RED + "Sorry, you don't have enough permissions.");
				return true;
			}
		}

		try {
			selectedBinding.call(this.plugin, sender, selectedBinding.getParams());
		} catch (InvocationTargetException e) {
			if (e.getTargetException() instanceof AutoCompleteChoicesException) {
				AutoCompleteChoicesException autocomplete = (AutoCompleteChoicesException) e.getTargetException();
				sender.sendMessage("Autocomplete for <" + autocomplete.getArgName() + ">:");
				sender.sendMessage("    " + StringUtils.implode(autocomplete.getChoices(), "   "));
			} else {
				throw new RuntimeException(e.getCause());
			}
		} catch (Exception e) {
			plugin.getLogger().severe("There is bogus command handler for " + command.getName() + " command. (Is appropriate plugin is update?)");
			if (e.getCause() != null) {
				e.getCause().printStackTrace();
			} else {
				e.printStackTrace();
			}
		}

		return true;
	}

	public List<CommandBinding> getCommands() {
		List<CommandBinding> commands = new LinkedList<>();

		for (Map<CommandSyntax, CommandBinding> map : this.listeners.values()) {
			commands.addAll(map.values());
		}

		return commands;
	}

	public List<String> tabComplete(CommandSender sender, org.bukkit.command.Command command, String alias, String[] args) {
		Map<CommandSyntax, CommandBinding> callMap = this.listeners.get(command.getName());
		if (callMap == null || args.length == 0) {
			return Collections.emptyList();
		}

		String prefix = args[args.length - 1].toLowerCase();
		int argIndex = args.length - 1;
		Set<String> suggestions = new HashSet<>();

		for (Entry<CommandSyntax, CommandBinding> entry : callMap.entrySet()) {
			CommandSyntax syntax = entry.getKey();
			String[] syntaxTokens = syntax.originalSyntax.split("\\s+");

			boolean matches = true;
			Map<String, String> varValues = new HashMap<>();
			for (int i = 0; i < Math.min(argIndex, syntaxTokens.length); i++) {
				String syntaxToken = syntaxTokens[i];
				String typedToken = args[i];
				if (!syntaxToken.startsWith("<") && !syntaxToken.startsWith("[")) {
					if (!syntaxToken.equalsIgnoreCase(typedToken)) {
						matches = false;
						break;
					}
				} else {
					String name = syntaxToken.replaceAll("[<>\\[\\]]", "");
					varValues.put(name, typedToken);
				}
			}

			if (!matches || argIndex >= syntaxTokens.length) {
				continue;
			}

			String currentToken = syntaxTokens[argIndex];
			if (currentToken.startsWith("<") || currentToken.startsWith("[")) {
				String argName = currentToken.replaceAll("[<>\\[\\]]", "");
				String action = null;
			if (argName.equalsIgnoreCase("permission") || argName.equalsIgnoreCase("targetPermission") || argName.equalsIgnoreCase("parents") || argName.equalsIgnoreCase("option") || argName.equalsIgnoreCase("group") || argName.equalsIgnoreCase("groups")) {
				for (int i = argIndex - 1; i >= 0; i--) {
					String prev = syntaxTokens[i];
					if (!prev.startsWith("<") && !prev.startsWith("[")) {
						action = prev.toLowerCase();
						break;
					}
				}
			}
				suggestions.addAll(getCompletionsForArg(argName, prefix, varValues, action));
			} else {
				if (currentToken.toLowerCase().startsWith(prefix)) {
					suggestions.add(currentToken);
				}
			}
		}

		return new ArrayList<>(suggestions);
	}

	private List<String> getCompletionsForArg(String argName, String prefix, Map<String, String> prevArgs, String action) {
		List<String> results = new ArrayList<>();
		String lowerPrefix = prefix.toLowerCase();

		if (argName.equalsIgnoreCase("permission") || argName.equalsIgnoreCase("targetPermission")) {
			return getPermissionCompletions(prevArgs, action, prefix);
		}

		if (argName.equalsIgnoreCase("ladder")) {
			try {
				Set<String> ladders = new LinkedHashSet<>();
				PermissionManager manager = PermissionsEx.getPermissionManager();
				for (String groupName : manager.getGroupNames()) {
					PermissionGroup group = manager.getGroup(groupName);
					if (group != null && !group.isVirtual() && group.getRank() > 0) {
						String ladder = group.getRankLadder();
						if (ladder != null && !ladder.isEmpty() && ladder.toLowerCase().startsWith(lowerPrefix)) {
							ladders.add(ladder);
						}
					}
				}
				results.addAll(ladders);
			} catch (Exception ignored) { }
			return results;
		}

		if (argName.equalsIgnoreCase("parents")) {
			return getParentCompletions(prevArgs, action, prefix);
		}

		if (argName.equalsIgnoreCase("group") || argName.equalsIgnoreCase("groups") || argName.contains("group")) {
			try {
				PermissionManager manager = PermissionsEx.getPermissionManager();

				if (prevArgs != null && prevArgs.containsKey("user") && action != null) {
					String userName = prevArgs.get("user");
					PermissionUser user = manager.getUser(userName);
					if (user != null && !user.isVirtual()) {
						Set<String> userGroupNames = new HashSet<>();
						for (PermissionGroup g : user.getParents(null)) {
							userGroupNames.add(g.getIdentifier().toLowerCase());
						}
						if ("add".equals(action)) {
							for (String group : manager.getGroupNames()) {
								if (!userGroupNames.contains(group.toLowerCase()) && group.toLowerCase().startsWith(lowerPrefix)) {
									results.add(group);
								}
							}
						} else if ("remove".equals(action)) {
							for (String group : manager.getGroupNames()) {
								if (userGroupNames.contains(group.toLowerCase()) && group.toLowerCase().startsWith(lowerPrefix)) {
									results.add(group);
								}
							}
						} else {
							for (String group : manager.getGroupNames()) {
								if (group.toLowerCase().startsWith(lowerPrefix)) {
									results.add(group);
								}
							}
						}
						return results;
					}
				}

				for (String group : manager.getGroupNames()) {
					if (group.toLowerCase().startsWith(lowerPrefix)) {
						results.add(group);
					}
				}
			} catch (Exception ignored) { }
		}

		if (argName.equalsIgnoreCase("user") || argName.equalsIgnoreCase("users") || argName.equalsIgnoreCase("player")) {
			for (Player player : Bukkit.getServer().getOnlinePlayers()) {
				if (player.getName().toLowerCase().startsWith(lowerPrefix)) {
					results.add(player.getName());
				}
			}
			try {
				for (String user : PermissionsEx.getPermissionManager().getUserNames()) {
					if (user.toLowerCase().startsWith(lowerPrefix)) {
						results.add(user);
					}
				}
			} catch (Exception ignored) { }
		}

		if (argName.equalsIgnoreCase("world") || argName.contains("world")) {
			for (World world : Bukkit.getServer().getWorlds()) {
				if (world.getName().toLowerCase().startsWith(lowerPrefix)) {
					results.add(world.getName());
				}
			}
		}

		if (argName.equalsIgnoreCase("backend")) {
			for (String backend : new String[]{"sql", "file", "memory", "multi"}) {
				if (backend.startsWith(lowerPrefix)) {
					results.add(backend);
				}
			}
		}

		if (argName.equalsIgnoreCase("force")) {
			if ("force".startsWith(lowerPrefix)) {
				results.add("force");
			}
		}

		if (argName.equalsIgnoreCase("value")) {
			String option = prevArgs.get("option");
			if (option != null && (option.equalsIgnoreCase("default") || option.equalsIgnoreCase("debug"))) {
				if ("true".startsWith(lowerPrefix)) results.add("true");
				if ("false".startsWith(lowerPrefix)) results.add("false");
			} else if (option == null) {
				if ("true".startsWith(lowerPrefix)) results.add("true");
				if ("false".startsWith(lowerPrefix)) results.add("false");
			}
		}

		if (argName.equalsIgnoreCase("option")) {
			for (String opt : new String[]{"prefix", "suffix", "default", "name", "debug", "rank", "rank-ladder", "weight"}) {
				if (opt.startsWith(lowerPrefix)) {
					results.add(opt);
				}
			}
		}

		return results;
	}

	private List<String> getParentCompletions(Map<String, String> prevArgs, String action, String prefix) {
		List<String> results = new ArrayList<>();
		String lowerPrefix = prefix.toLowerCase();

		if (prevArgs == null || prevArgs.isEmpty()) {
			return results;
		}

		String groupName = prevArgs.get("group");
		if (groupName == null || groupName.isEmpty()) {
			return results;
		}

		PermissionManager manager;
		try {
			manager = PermissionsEx.getPermissionManager();
		} catch (Exception e) {
			return results;
		}

		if ("remove".equals(action)) {
			PermissionGroup group = manager.getGroup(groupName);
			if (group != null && !group.isVirtual()) {
				for (PermissionGroup parent : group.getOwnParents(prevArgs.get("world"))) {
					if (parent.getIdentifier().toLowerCase().startsWith(lowerPrefix)) {
						results.add(parent.getIdentifier());
					}
				}
			}
		} else if ("add".equals(action)) {
			Set<String> existing = new HashSet<>();
			PermissionGroup group = manager.getGroup(groupName);
			if (group != null && !group.isVirtual()) {
				for (PermissionGroup parent : group.getOwnParents(prevArgs.get("world"))) {
					existing.add(parent.getIdentifier().toLowerCase());
				}
			}
			for (String name : manager.getGroupNames()) {
				if (!existing.contains(name.toLowerCase()) && name.toLowerCase().startsWith(lowerPrefix)) {
					results.add(name);
				}
			}
		} else {
			for (String name : manager.getGroupNames()) {
				if (name.toLowerCase().startsWith(lowerPrefix)) {
					results.add(name);
				}
			}
		}

		return results;
	}

	private List<String> getPermissionCompletions(Map<String, String> prevArgs, String action, String prefix) {
		List<String> results = new ArrayList<>();
		String lowerPrefix = prefix.toLowerCase();

		if (prevArgs == null || prevArgs.isEmpty()) {
			return results;
		}

		String userName = prevArgs.get("user");
		String groupName = prevArgs.get("group");
		String worldName = prevArgs.get("world");

		PermissionEntity entity = null;
		try {
			if (userName != null && !userName.isEmpty()) {
				entity = PermissionsEx.getPermissionManager().getUser(userName);
			} else if (groupName != null && !groupName.isEmpty()) {
				entity = PermissionsEx.getPermissionManager().getGroup(groupName);
			}
		} catch (Exception ignored) { }

		if (entity == null || entity.isVirtual()) {
			return results;
		}

		if ("remove".equals(action) || "check".equals(action) || "swap".equals(action)) {
			for (String perm : entity.getOwnPermissions(worldName)) {
				if (perm.toLowerCase().startsWith(lowerPrefix)) {
					results.add(perm);
				}
			}
		} else if ("add".equals(action)) {
			Set<String> existing = new HashSet<>(entity.getOwnPermissions(worldName));
			for (Permission perm : Bukkit.getServer().getPluginManager().getPermissions()) {
				String name = perm.getName();
				if (!existing.contains(name) && name.toLowerCase().startsWith(lowerPrefix)) {
					results.add(name);
				}
			}
		}

		return results;
	}

	protected class CommandSyntax {

		protected String originalSyntax;
		protected String regexp;
		protected List<String> arguments = new LinkedList<>();

		public CommandSyntax(String syntax) {
			this.originalSyntax = syntax;

			this.regexp = this.prepareSyntaxRegexp(syntax);
		}

		public String getRegexp() {
			return regexp;
		}

		private String prepareSyntaxRegexp(String syntax) {
			String expression = syntax;

			Matcher argMatcher = Pattern.compile("(?:[\\s]+)?((\\<|\\[)([^\\>\\]]+)(?:\\>|\\]))").matcher(expression);
			//Matcher argMatcher = Pattern.compile("(\\<|\\[)([^\\>\\]]+)(?:\\>|\\])").matcher(expression);

			int index = 0;
			while (argMatcher.find()) {
				if (argMatcher.group(2).equals("[")) {
					expression = expression.replace(argMatcher.group(0), "(?:(?:[\\s]+)(\"[^\"]+\"|[^\\s]+))?");
				} else {
					expression = expression.replace(argMatcher.group(1), "(\"[^\"]+\"|[\\S]+)");
				}

				arguments.add(index++, argMatcher.group(3));
			}

			return expression;
		}

		public boolean isMatch(String str) {
			return str.matches(this.regexp);
		}

		public Map<String, String> getMatchedArguments(String str) {
			Map<String, String> matchedArguments = new HashMap<>(this.arguments.size());

			if (this.arguments.size() > 0) {
				Matcher argMatcher = Pattern.compile(this.regexp).matcher(str);

				if (argMatcher.find()) {
					for (int index = 1; index <= argMatcher.groupCount(); index++) {
						String argumentValue = argMatcher.group(index);
						if (argumentValue == null || argumentValue.isEmpty()) {
							continue;
						}

						if (argumentValue.startsWith("\"") && argumentValue.endsWith("\"")) { // Trim boundary colons
							argumentValue = argumentValue.substring(1, argumentValue.length() - 1);
						}

						matchedArguments.put(this.arguments.get(index - 1), argumentValue);
					}
				}
			}
			return matchedArguments;
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
			return this.method.getAnnotation(Command.class);
		}

		public Map<String, String> getParams() {
			return params;
		}

		public void setParams(Map<String, String> params) {
			this.params = params;
		}

		public boolean checkPermissions(Player player) {
			PermissionManager manager = PermissionsEx.getPermissionManager();

			String permission = this.getMethodAnnotation().permission();


			if (permission.contains("<")) {
				for (Entry<String, String> entry : this.getParams().entrySet()) {
					if (entry.getValue() != null) {
						permission = permission.replace("<" + entry.getKey() + ">", entry.getValue().toLowerCase());
					}
				}
			}

			return manager.has(player, permission);

		}

		public void call(Object... args) throws Exception {
			this.method.invoke(object, args);
		}
	}
}
