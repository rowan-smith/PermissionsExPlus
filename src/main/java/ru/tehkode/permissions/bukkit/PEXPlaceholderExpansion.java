package ru.tehkode.permissions.bukkit;

import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.PermissionUser;

import java.util.List;

public class PEXPlaceholderExpansion extends PlaceholderExpansion {

    private final PermissionsEx plugin;

    public PEXPlaceholderExpansion(PermissionsEx plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NonNull String getIdentifier() {
        return "pex";
    }

    @Override
    public @NonNull String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public @NonNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NonNull String identifier) {
        if (player == null) {
            return null;
        }

        PermissionManager manager = plugin.getPermissionsManager();
        if (manager == null) {
            return null;
        }

        PermissionUser user = manager.getUser(player);
        if (user == null) {
            return null;
        }

        String worldName = player.getWorld().getName();

        String result = processPlaceholder(identifier, user, worldName, manager);

        return resolvePapiPlaceholders(result, player);
    }

    /**
     * Resolve any PAPI placeholders embedded in the result value.
     * This allows prefix/suffix/option values stored in PEX to contain
     * PAPI placeholders from other plugins (e.g. %player_name%, %vault_prefix%).
     */
    protected String resolvePapiPlaceholders(String result, Player player) {
        if (result == null || result.isEmpty()) {
            return result;
        }

        return PlaceholderAPI.setPlaceholders(player, result);
    }

    private String processPlaceholder(String identifier, PermissionUser user, String worldName, PermissionManager manager) {
        // Handle group queries: pex_group_<groupname>_<placeholder>
        if (identifier.startsWith("group_") && identifier.contains("_")) {
            int firstUnderscore = identifier.indexOf('_');
            int secondUnderscore = identifier.indexOf('_', firstUnderscore + 1);
            
            if (secondUnderscore > firstUnderscore + 1) {
                String potentialGroupName = identifier.substring(firstUnderscore + 1, secondUnderscore);
                String restOfPlaceholder = identifier.substring(secondUnderscore + 1);
                
                PermissionGroup group = manager.getGroup(potentialGroupName);
                if (group != null) {
                    return processGroupPlaceholder(restOfPlaceholder, group, worldName);
                }
            }
        }

        // Handle direct user placeholders without explicit username
        if (identifier.startsWith("user_")) {
            String restOfPlaceholder = identifier.substring("user_".length());
            String directResult = processUserPlaceholder(restOfPlaceholder, user, worldName, manager);
            if (directResult != null) {
                return directResult;
            }
        }

        // Handle explicit user queries: pex_user_<username>_<placeholder>
        // Tried after direct user to avoid ambiguity with placeholders containing underscores
        if (identifier.startsWith("user_") && identifier.contains("_")) {
            int firstUnderscore = identifier.indexOf('_');
            int secondUnderscore = identifier.indexOf('_', firstUnderscore + 1);
            
            if (secondUnderscore > firstUnderscore + 1) {
                String potentialUsername = identifier.substring(firstUnderscore + 1, secondUnderscore);
                String restOfPlaceholder = identifier.substring(secondUnderscore + 1);
                
                PermissionUser targetUser = manager.getUser(potentialUsername);
                if (targetUser != null) {
                    return processUserPlaceholder(restOfPlaceholder, targetUser, worldName, manager);
                }
            }
        }

        return null;
    }

    private String processUserPlaceholder(String placeholder, PermissionUser user, String worldName, PermissionManager manager) {
        // Extract world context if present
        String extractedWorld = worldName;
        if (placeholder.contains("_world_")) {
            int lastWorldIndex = placeholder.lastIndexOf("_world_");
            extractedWorld = placeholder.substring(lastWorldIndex + 7);
            placeholder = placeholder.substring(0, lastWorldIndex);
        }

        switch (placeholder) {
            case "name":
                return user.getName();

            case "primary_group":
                List<PermissionGroup> groups = user.getParents(extractedWorld);
                return groups.isEmpty() ? "" : groups.get(0).getName();

            case "groups":
                return String.join(",", user.getParentIdentifiers(extractedWorld));

            case "direct_groups":
                return String.join(",", user.getOwnParentIdentifiers(extractedWorld));

            case "group_count":
                return String.valueOf(user.getParentIdentifiers(extractedWorld).size());

            case "direct_group_count":
                return String.valueOf(user.getOwnParentIdentifiers(extractedWorld).size());

            case "prefix":
                return user.getPrefix(extractedWorld);

            case "suffix":
                return user.getSuffix(extractedWorld);

            case "permission_count":
                return String.valueOf(user.getPermissions(extractedWorld).size());

            case "direct_permission_count":
                return String.valueOf(user.getOwnPermissions(extractedWorld).size());

            default:
                // Handle option_<option>
                if (placeholder.startsWith("option_")) {
                    String optionName = placeholder.substring("option_".length());
                    String value = user.getOption(optionName, extractedWorld, "");
                    return value == null ? "" : value;
                }

                // Handle has_permission_<permission>
                if (placeholder.startsWith("has_permission_")) {
                    String permission = placeholder.substring("has_permission_".length());
                    return String.valueOf(user.has(permission, extractedWorld));
                }

                // Handle in_group_direct_<group> — must be checked before in_group_ to avoid false prefix match
                if (placeholder.startsWith("in_group_direct_")) {
                    String groupName = placeholder.substring("in_group_direct_".length());
                    return String.valueOf(user.getOwnParentIdentifiers(extractedWorld).contains(groupName));
                }

                // Handle in_group_<group> (effective)
                if (placeholder.startsWith("in_group_")) {
                    String groupName = placeholder.substring("in_group_".length());
                    PermissionGroup group = manager.getGroup(groupName);
                    if (group != null) {
                        return String.valueOf(user.inGroup(group, extractedWorld, false));
                    }
                    return "false";
                }

                return null;
        }
    }

    private String processGroupPlaceholder(String placeholder, PermissionGroup group, String worldName) {
        // Extract world context if present
        String extractedWorld = worldName;
        if (placeholder.contains("_world_")) {
            int lastWorldIndex = placeholder.lastIndexOf("_world_");
            extractedWorld = placeholder.substring(lastWorldIndex + 7);
            placeholder = placeholder.substring(0, lastWorldIndex);
        }

        switch (placeholder) {
            case "exists":
                return "true";

            case "prefix":
                String prefix = group.getOwnPrefix(extractedWorld);
                return prefix == null ? "" : prefix;

            case "suffix":
                String suffix = group.getOwnSuffix(extractedWorld);
                return suffix == null ? "" : suffix;

            case "parents":
                return String.join(",", group.getParentIdentifiers(extractedWorld));

            case "parent_count":
                return String.valueOf(group.getParentIdentifiers(extractedWorld).size());

            case "permission_count":
                return String.valueOf(group.getOwnPermissions(extractedWorld).size());

            case "effective_permission_count":
                return String.valueOf(group.getPermissions(extractedWorld).size());

            default:
                // Handle option_<option> (effective)
                if (placeholder.startsWith("option_")) {
                    String optionName = placeholder.substring("option_".length());
                    String value = group.getOption(optionName, extractedWorld, "");
                    return value == null ? "" : value;
                }

                // Handle has_permission_<permission> (effective)
                if (placeholder.startsWith("has_permission_")) {
                    String permission = placeholder.substring("has_permission_".length());
                    return String.valueOf(group.has(permission, extractedWorld));
                }

                return null;
        }
    }
}
