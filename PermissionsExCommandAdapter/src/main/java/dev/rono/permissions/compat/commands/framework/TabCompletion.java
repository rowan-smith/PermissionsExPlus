package dev.rono.permissions.compat.commands.framework;

import lombok.var;

import dev.rono.permissions.api.context.ContextSet;
import dev.rono.permissions.api.parent.ParentNode;
import dev.rono.permissions.api.permission.PermissionHolder;
import dev.rono.permissions.api.permission.PermissionNode;
import dev.rono.permissions.api.permission.PermissionValue;
import dev.rono.permissions.compat.commands.ApiWrapper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

/**
 * Tab completion for the legacy /pex syntax, as in PermissionsEx 1.23.6:
 * literal words of every matching syntax, then users, groups, ladders, worlds,
 * permissions and options for its arguments. Data comes from PermissionsExPlus.
 */
final class TabCompletion {
    private static final String[] OPTIONS = {"prefix", "suffix", "default", "name", "debug", "rank", "rank-ladder", "weight"};

    private TabCompletion() {}

    /**
     * @param syntaxes
     *            the syntax of every registered command for this label
     * @param args
     *            the arguments typed so far; the last one is being completed
     */
    static List<String> complete(Collection<String> syntaxes, String[] args) {
        if (args.length == 0) {
            return new ArrayList<>();
        }

        var index = args.length - 1;
        var prefix = args[index].toLowerCase(Locale.ROOT);
        var suggestions = new LinkedHashSet<String>();

        for (var syntax : syntaxes) {
            var tokens = syntax.trim().isEmpty() ? new String[0] : syntax.trim().split("\\s+");

            if (index >= tokens.length) {
                continue;
            }

            var typed = new java.util.HashMap<String, String>();
            var matches = true;

            for (var i = 0; i < index; i++) {
                if (isArgument(tokens[i])) {
                    typed.put(name(tokens[i]), args[i]);
                } else if (!tokens[i].equalsIgnoreCase(args[i])) {
                    matches = false;
                    break;
                }
            }

            if (!matches) {
                continue;
            }

            var token = tokens[index];

            if (isArgument(token)) {
                suggestions.addAll(argument(name(token), prefix, typed, action(tokens, index)));
            } else if (token.toLowerCase(Locale.ROOT).startsWith(prefix)) {
                suggestions.add(token);
            }
        }

        return new ArrayList<>(suggestions);
    }

    private static boolean isArgument(String token) {
        return token.startsWith("<") || token.startsWith("[");
    }

    private static String name(String token) {
        return token.replaceAll("[<>\\[\\]]", "");
    }

    /** The nearest literal word before {@code index}, such as "add" or "remove". */
    private static String action(String[] tokens, int index) {
        for (var i = index - 1; i >= 0; i--) {
            if (!isArgument(tokens[i])) {
                return tokens[i].toLowerCase(Locale.ROOT);
            }
        }

        return null;
    }

    private static Collection<String> argument(String name, String prefix, Map<String, String> typed, String action) {
        try {
            switch (name.toLowerCase(Locale.ROOT)) {
                case "permission" :
                case "targetpermission" :
                    return permissions(typed, action, prefix);
                case "parents" :
                    return parents(typed, action, prefix);
                case "ladder" :
                    return filter(ApiWrapper.ladders().cache().identifiers(), prefix);
                case "user" :
                case "users" :
                case "player" :
                    return users(prefix);
                case "option" :
                    return filter(java.util.Arrays.asList(OPTIONS), prefix);
                case "value" :
                    return filter(java.util.Arrays.asList("true", "false"), prefix);
                case "force" :
                    return filter(java.util.Collections.singletonList("force"), prefix);
                default :
                    break;
            }

            if (name.toLowerCase(Locale.ROOT).contains("group")) {
                return groups(typed, action, prefix);
            }

            if (name.toLowerCase(Locale.ROOT).contains("world")) {
                var worlds = new ArrayList<String>();

                for (World world : Bukkit.getWorlds()) {
                    worlds.add(world.getName());
                }

                return filter(worlds, prefix);
            }
        } catch (RuntimeException | LinkageError unavailable) {
            // Completion must never break the command line.
        }

        return new ArrayList<>();
    }

    private static Collection<String> users(String prefix) {
        var names = new LinkedHashSet<String>();

        for (Player player : Bukkit.getOnlinePlayers()) {
            names.add(player.getName());
        }

        names.addAll(ApiWrapper.users().cache().names());
        return filter(names, prefix);
    }

    /**
     * Groups to add the typed user to, groups to remove them from, or all groups.
     */
    private static Collection<String> groups(Map<String, String> typed, String action, String prefix) {
        var all = ApiWrapper.groups().cache().identifiers();
        var userName = typed.get("user");

        if (userName == null || !("add".equals(action) || "remove".equals(action))) {
            return filter(all, prefix);
        }

        var user = ApiWrapper.findUser(userName);

        if (!user.isPresent()) {
            return filter(all, prefix);
        }

        var member = names(user.get().groups());
        var result = new ArrayList<String>();

        for (var group : all) {
            if (member.contains(group) == "remove".equals(action)) {
                result.add(group);
            }
        }

        return filter(result, prefix);
    }

    private static Collection<String> parents(Map<String, String> typed, String action, String prefix) {
        var groupName = typed.get("group");

        if (groupName == null) {
            return new ArrayList<>();
        }

        var group = ApiWrapper.findGroup(groupName);
        var existing = group.isPresent() ? names(group.get().parents()) : new LinkedHashSet<String>();

        if ("remove".equals(action)) {
            return filter(existing, prefix);
        }

        var result = new ArrayList<String>();

        for (var name : ApiWrapper.groups().cache().identifiers()) {
            if (!"add".equals(action) || !existing.contains(name)) {
                result.add(name);
            }
        }

        return filter(result, prefix);
    }

    /**
     * The holder's own permissions to remove or check; server permissions to add.
     */
    private static Collection<String> permissions(Map<String, String> typed, String action, String prefix) {
        PermissionHolder holder = null;

        if (typed.get("user") != null) {
            holder = ApiWrapper.findUser(typed.get("user")).orElse(null);
        } else if (typed.get("group") != null) {
            holder = ApiWrapper.findGroup(typed.get("group")).orElse(null);
        }

        if (holder == null) {
            return new ArrayList<>();
        }

        var world = typed.get("world");
        ContextSet contexts = world == null ? ContextSet.empty() : ApiWrapper.world(world);
        var own = new LinkedHashSet<String>();

        for (PermissionNode node : holder.explicitPermissions()) {
            if (node.contexts().equals(contexts) && !node.expired()) {
                own.add(node.value() == PermissionValue.DENY ? "-" + node.permission() : node.permission());
            }
        }

        if ("remove".equals(action) || "check".equals(action) || "swap".equals(action)) {
            return filter(own, prefix);
        }

        if (!"add".equals(action)) {
            return new ArrayList<>();
        }

        var result = new ArrayList<String>();

        for (Permission permission : Bukkit.getPluginManager().getPermissions()) {
            if (!own.contains(permission.getName())) {
                result.add(permission.getName());
            }
        }

        return filter(result, prefix);
    }

    private static Set<String> names(Collection<ParentNode> nodes) {
        var names = new LinkedHashSet<String>();

        for (var node : nodes) {
            if (!node.expired()) {
                names.add(node.group());
            }
        }

        return names;
    }

    private static List<String> filter(Collection<String> values, String prefix) {
        var result = new ArrayList<String>();

        for (var value : values) {
            if (value.toLowerCase(Locale.ROOT).startsWith(prefix)) {
                result.add(value);
            }
        }

        return result;
    }
}
