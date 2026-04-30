package dev.rono.permissions.core.commands;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.exceptions.PermissionBackendException;
import ru.tehkode.permissions.exceptions.RankingException;
import ru.tehkode.utils.DateUtils;

/**
 * Shared command functionality that platform adapters can call.
 */
public class CoreCommandService {
    private final PermissionManager manager;

    public CoreCommandService(PermissionManager manager) {
        this.manager = manager;
    }

    public String reload(CoreConfigReloader reloader) throws PermissionBackendException {
        reloader.reload();
        manager.reset();
        return "Permissions reloaded";
    }

    public String currentBackend() {
        return "Current backend: " + manager.getBackend();
    }

    public String switchBackend(String backendName) throws PermissionBackendException {
        manager.setBackend(backendName);
        return "Permission backend changed!";
    }

    public List<String> knownUsers(int limit) {
        return manager.getUserNames().stream().sorted().limit(limit).collect(Collectors.toList());
    }

    public List<String> knownUsersLines() {
        List<String> lines = new ArrayList<>();
        lines.add("Currently registered users: ");
        for (PermissionUser user : manager.getUsers()) {
            lines.add(user.getIdentifier() + " (Last known username: " + user.getName() + ") [" + String.join(", ", user.getParentIdentifiers()) + "]");
        }
        return lines;
    }

    public List<String> knownGroups() {
        return manager.getGroupList().stream().map(PermissionGroup::getIdentifier).sorted().collect(Collectors.toList());
    }

    public List<String> knownLadders() {
        Set<String> ladders = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (PermissionGroup group : manager.getGroupList()) {
            String ladder = group.getRankLadder();
            if (ladder != null && !ladder.isBlank()) {
                ladders.add(ladder);
            }
        }
        if (ladders.isEmpty()) {
            ladders.add("default");
        }
        return new ArrayList<>(ladders);
    }

    public List<String> knownPermissions() {
        Set<String> permissions = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

        for (PermissionGroup group : manager.getGroupList()) {
            permissions.addAll(group.getOwnPermissions(null));
            for (String world : manager.getWorldNames()) {
                permissions.addAll(group.getOwnPermissions(world));
            }
        }

        for (PermissionUser user : manager.getUsers()) {
            permissions.addAll(user.getOwnPermissions(null));
            for (String world : manager.getWorldNames()) {
                permissions.addAll(user.getOwnPermissions(world));
            }
        }

        return new ArrayList<>(permissions);
    }

    public List<String> knownGroupsLines(String world) {
        List<String> lines = new ArrayList<>();
        lines.add("Registered groups: ");
        for (PermissionGroup group : manager.getGroupList()) {
            String rank = "";
            if (group.isRanked()) {
                rank = " (rank: " + group.getRank() + "@" + group.getRankLadder() + ") ";
            }
            lines.add("  " + group.getIdentifier() + " #" + group.getWeight() + rank + "[" + String.join(", ", group.getParentIdentifiers(world)) + "]");
        }
        return lines;
    }

    public List<String> worldNames() {
        return manager.getWorldNames().stream().sorted().collect(Collectors.toList());
    }

    public List<String> worldsTreeLines() {
        List<String> out = new java.util.ArrayList<>();
        out.add("Worlds on server: ");
        for (String world : worldNames()) {
            List<String> parents = manager.getWorldInheritance(world);
            String line = "  " + world;
            if (!parents.isEmpty()) {
                line += " [" + String.join(", ", parents) + "]";
            }
            out.add(line);
        }
        return out;
    }

    public List<String> worldInheritance(String world) {
        return manager.getWorldInheritance(world);
    }

    public List<String> worldInheritanceLines(String world) {
        List<String> parents = manager.getWorldInheritance(world);
        List<String> out = new java.util.ArrayList<>();
        if (parents.isEmpty()) {
            out.add("World \"" + world + "\" inherits nothing.");
            return out;
        }
        out.add("World \"" + world + "\" inherits:");
        for (String parent : parents) {
            List<String> pParents = manager.getWorldInheritance(parent);
            String line = "  " + parent;
            if (!pParents.isEmpty()) {
                line += " [" + String.join(", ", pParents) + "]";
            }
            out.add(line);
        }
        return out;
    }

    public String setWorldInheritance(String world, List<String> parents) {
        manager.setWorldInheritance(world, parents);
        return "World \"" + world + "\" inherits " + String.join(", ", parents);
    }

    public String toggleDebug() {
        manager.setDebug(!manager.isDebug());
        return "Debug mode " + (manager.isDebug() ? "enabled" : "disabled");
    }

    public UserView userView(String userIdentifier) {
        PermissionUser user = manager.getUser(userIdentifier);
        return new UserView(user.getIdentifier(), user.getName(), user.getParentIdentifiers(), user.getPermissions(null));
    }

    public List<String> userPermissionsLines(String userIdentifier, String world) {
        PermissionUser user = manager.getUser(userIdentifier);
        List<String> lines = new ArrayList<>();
        lines.add(user.getName() + "'s permissions:");
        for (String permission : user.getPermissions(world)) {
            lines.add("  " + permission);
        }
        return lines;
    }

    public String userHas(String userIdentifier, String permission, String world) {
        PermissionUser user = manager.getUser(userIdentifier);
        return "Has '" + permission + "' in " + world + ": " + user.has(permission, world);
    }

    public String userAddPermission(String userIdentifier, String permission, String world) {
        PermissionUser user = manager.getUser(userIdentifier);
        user.addPermission(permission, world);
        return "Permission \"" + permission + "\" added!";
    }

    public String userRemovePermission(String userIdentifier, String permission, String world) {
        PermissionUser user = manager.getUser(userIdentifier);
        user.removePermission(permission, world);
        user.removeTimedPermission(permission, world);
        return "Permission \"" + permission + "\" removed!";
    }

    public String userAddGroup(String userIdentifier, String group, String world) {
        PermissionUser user = manager.getUser(userIdentifier);
        user.addGroup(group, world);
        return "User \"" + user.getName() + "\" added to group \"" + group + "\"!";
    }

    public String userAddGroup(String userIdentifier, String group, String world, String lifetime) {
        PermissionUser user = manager.getUser(userIdentifier);
        int seconds = lifetime == null ? 0 : DateUtils.parseInterval(lifetime);
        if (seconds > 0) {
            user.addGroup(group, world, seconds);
        } else {
            user.addGroup(group, world);
        }
        return "User \"" + user.getName() + "\" added to group \"" + group + "\"!";
    }

    public String userRemoveGroup(String userIdentifier, String group, String world) {
        PermissionUser user = manager.getUser(userIdentifier);
        user.removeGroup(group, world);
        return "User \"" + user.getName() + "\" removed from group \"" + group + "\"!";
    }

    public String userSetOption(String userIdentifier, String key, String value, String world) {
        PermissionUser user = manager.getUser(userIdentifier);
        user.setOption(key, value, world);
        if (value != null && value.isEmpty()) {
            return "Option \"" + key + "\" cleared!";
        }
        return "Option \"" + key + "\" set!";
    }

    public String userGetOption(String userIdentifier, String option, String world) {
        PermissionUser user = manager.getUser(userIdentifier);
        String value = user.getOption(option, world, null);
        return "Player \"" + user.getName() + "\" @ " + world + " option \"" + option + "\" = \"" + value + "\"";
    }

    public String userPrefix(String userIdentifier, String newPrefix, String world) {
        PermissionUser user = manager.getUser(userIdentifier);
        if (newPrefix != null) {
            user.setPrefix(newPrefix, world);
            return user.getName() + "'s prefix" + (world != null ? " (in world \"" + world + "\") " : " ")
                    + "has been set to \"" + user.getPrefix() + "\"";
        }
        return user.getName() + "'s prefix" + (world != null ? " (in world \"" + world + "\") " : " ")
                + "is \"" + user.getPrefix() + "\"";
    }

    public String userSuffix(String userIdentifier, String newSuffix, String world) {
        PermissionUser user = manager.getUser(userIdentifier);
        if (newSuffix != null) {
            user.setSuffix(newSuffix, world);
            return user.getName() + "'s suffix" + (world != null ? " (in world \"" + world + "\") " : " ")
                    + "has been set to \"" + user.getSuffix() + "\"";
        }
        return user.getName() + "'s suffix" + (world != null ? " (in world \"" + world + "\") " : " ")
                + "is \"" + user.getSuffix() + "\"";
    }

    public String userToggleDebug(String userIdentifier) {
        PermissionUser user = manager.getUser(userIdentifier);
        user.setDebug(!user.isDebug());
        return "Debug mode for user " + user.getName() + " " + (user.isDebug() ? "enabled" : "disabled") + "!";
    }

    public String userCheck(String userIdentifier, String permission, String world) {
        PermissionUser user = manager.getUser(userIdentifier);
        String expression = user.getMatchingExpression(permission, world);
        if (expression == null) {
            return "Permission \"" + permission + "\" has not been set for Player \"" + user.getName() + "\"";
        }
        return "Player \"" + user.getName() + "\" " + (user.explainExpression(expression) ? "has" : "doesn't have")
                + " \"" + expression + "\"";
    }

    public String userDelete(String userIdentifier) {
        PermissionUser user = manager.getUser(userIdentifier);
        user.remove();
        manager.resetUser(user.getIdentifier());
        return "User \"" + user.getName() + "\" removed!";
    }

    public String userAddTimedPermission(String userIdentifier, String permission, String lifetime, String world) {
        PermissionUser user = manager.getUser(userIdentifier);
        int seconds = lifetime == null ? 0 : DateUtils.parseInterval(lifetime);
        user.addTimedPermission(permission, world, seconds);
        return "Timed permission \"" + permission + "\" added!";
    }

    public String userRemoveTimedPermission(String userIdentifier, String permission, String world) {
        PermissionUser user = manager.getUser(userIdentifier);
        user.removeTimedPermission(permission, world);
        return "Timed permission \"" + permission + "\" removed!";
    }

    public String userSwapPermission(String userIdentifier, String source, String target, String world) {
        PermissionUser user = manager.getUser(userIdentifier);
        List<String> permissions = new ArrayList<>(user.getOwnPermissions(world));
        int sourceIndex = permissions.indexOf(source);
        int targetIndex = permissions.indexOf(target);
        if (sourceIndex < 0 || targetIndex < 0) {
            throw new IllegalArgumentException("Permission not found");
        }
        String tmp = permissions.get(targetIndex);
        permissions.set(targetIndex, permissions.get(sourceIndex));
        permissions.set(sourceIndex, tmp);
        user.setPermissions(permissions, world);
        return "Permissions swapped!";
    }

    public String userSetGroups(String userIdentifier, String groupsCsv, String world) {
        PermissionUser user = manager.getUser(userIdentifier);
        List<PermissionGroup> groups = parseGroups(groupsCsv);
        if (groups.isEmpty()) {
            return "No groups set!";
        }
        user.setParents(groups, world);
        return "User groups set!";
    }

    public List<String> userGroupListLines(String userIdentifier, String world) {
        PermissionUser user = manager.getUser(userIdentifier);
        List<String> lines = new ArrayList<>();
        lines.add("User \"" + user.getName() + "\" @" + world + " currently in:");
        for (PermissionGroup group : user.getParents(world)) {
            lines.add("  " + group.getIdentifier());
        }
        return lines;
    }

    public GroupView groupView(String groupIdentifier) {
        if (groupIdentifier == null || groupIdentifier.isBlank()) {
            throw new IllegalArgumentException("Group name is required");
        }
        PermissionGroup group = manager.getGroup(groupIdentifier);
        if (group == null) {
            throw new IllegalArgumentException("Group \"" + groupIdentifier + "\" not found");
        }
        return new GroupView(group.getIdentifier(), group.getPermissions(null));
    }

    public List<String> groupPermissionsLines(String groupIdentifier, String world) {
        PermissionGroup group = manager.getGroup(groupIdentifier);
        List<String> lines = new ArrayList<>();
        lines.add("Group \"" + group.getIdentifier() + "\"'s permissions:");
        for (String permission : group.getPermissions(world)) {
            lines.add("  " + permission);
        }
        return lines;
    }

    public String groupAddPermission(String groupIdentifier, String permission, String world) {
        PermissionGroup group = manager.getGroup(groupIdentifier);
        group.addPermission(permission, world);
        return "Permission \"" + permission + "\" added to group \"" + group.getIdentifier() + "\"!";
    }

    public String groupRemovePermission(String groupIdentifier, String permission, String world) {
        PermissionGroup group = manager.getGroup(groupIdentifier);
        group.removePermission(permission, world);
        group.removeTimedPermission(permission, world);
        return "Permission \"" + permission + "\" removed from group \"" + group.getIdentifier() + "\"!";
    }

    public String groupAddTimedPermission(String groupIdentifier, String permission, String lifetime, String world) {
        PermissionGroup group = manager.getGroup(groupIdentifier);
        int seconds = lifetime == null ? 0 : DateUtils.parseInterval(lifetime);
        group.addTimedPermission(permission, world, seconds);
        return "Timed permission added!";
    }

    public String groupRemoveTimedPermission(String groupIdentifier, String permission, String world) {
        PermissionGroup group = manager.getGroup(groupIdentifier);
        group.removeTimedPermission(permission, world);
        return "Timed permission \"" + permission + "\" removed!";
    }

    public String groupSwapPermission(String groupIdentifier, String source, String target, String world) {
        PermissionGroup group = manager.getGroup(groupIdentifier);
        List<String> permissions = new ArrayList<>(group.getOwnPermissions(world));
        int sourceIndex = permissions.indexOf(source);
        int targetIndex = permissions.indexOf(target);
        if (sourceIndex < 0 || targetIndex < 0) {
            throw new IllegalArgumentException("Permission not found");
        }
        String tmp = permissions.get(targetIndex);
        permissions.set(targetIndex, permissions.get(sourceIndex));
        permissions.set(sourceIndex, tmp);
        group.setPermissions(permissions, world);
        return "Permissions swapped!";
    }

    public List<String> groupParentListLines(String groupIdentifier, String world) {
        PermissionGroup group = manager.getGroup(groupIdentifier);
        List<String> parentNames = group.getParentIdentifiers(world);
        if (parentNames.isEmpty()) {
            return List.of("Group \"" + group.getIdentifier() + "\" has no parents.");
        }
        List<String> lines = new ArrayList<>();
        lines.add("Group " + group.getIdentifier() + " parents:");
        for (String parent : parentNames) {
            lines.add("  " + parent);
        }
        return lines;
    }

    public String groupSetParents(String groupIdentifier, String parentsCsv, String world) {
        PermissionGroup group = manager.getGroup(groupIdentifier);
        group.setParents(parseGroups(parentsCsv), world);
        group.save();
        return "Group " + group.getIdentifier() + " inheritance updated!";
    }

    public String groupAddParents(String groupIdentifier, String parentsCsv, String world) {
        PermissionGroup group = manager.getGroup(groupIdentifier);
        List<PermissionGroup> groups = new ArrayList<>(group.getOwnParents(world));
        for (PermissionGroup parent : parseGroups(parentsCsv)) {
            if (!groups.contains(parent)) {
                groups.add(parent);
            }
        }
        group.setParents(groups, world);
        group.save();
        return "Group " + group.getIdentifier() + " inheritance updated!";
    }

    public String groupRemoveParents(String groupIdentifier, String parentsCsv, String world) {
        PermissionGroup group = manager.getGroup(groupIdentifier);
        List<PermissionGroup> groups = new ArrayList<>(group.getOwnParents(world));
        groups.removeAll(parseGroups(parentsCsv));
        group.setParents(groups, world);
        group.save();
        return "Group \"" + group.getIdentifier() + "\" inheritance updated!";
    }

    public String groupRank(String groupIdentifier, String rankValue, String ladder) {
        PermissionGroup group = manager.getGroup(groupIdentifier);
        if (rankValue != null) {
            group.setRank(Integer.parseInt(rankValue));
            if (ladder != null) {
                group.setRankLadder(ladder);
            }
        }
        int rank = group.getRank();
        if (rank > 0) {
            return "Group " + group.getIdentifier() + " rank is " + rank + " (ladder = " + group.getRankLadder() + ")";
        }
        return "Group " + group.getIdentifier() + " is unranked";
    }

    public String groupWeight(String groupIdentifier, String weight) {
        PermissionGroup group = manager.getGroup(groupIdentifier);
        if (weight != null) {
            group.setWeight(Integer.parseInt(weight));
        }
        return "Group \"" + group.getIdentifier() + "\" has " + group.getWeight() + " calories.";
    }

    public String groupToggleDebug(String groupIdentifier) {
        PermissionGroup group = manager.getGroup(groupIdentifier);
        group.setDebug(!group.isDebug());
        return "Debug mode for group " + group.getIdentifier() + " have been " + (group.isDebug() ? "enabled" : "disabled") + "!";
    }

    public String groupPrefix(String groupIdentifier, String newPrefix, String world) {
        PermissionGroup group = manager.getGroup(groupIdentifier);
        if (newPrefix != null) {
            group.setPrefix(newPrefix, world);
            return group.getIdentifier() + "'s prefix" + (world != null ? " (in world \"" + world + "\") " : " ")
                    + "has been set to \"" + group.getPrefix() + "\"";
        }
        return group.getIdentifier() + "'s prefix" + (world != null ? " (in world \"" + world + "\") " : " ")
                + "is \"" + group.getPrefix() + "\"";
    }

    public String groupSuffix(String groupIdentifier, String newSuffix, String world) {
        PermissionGroup group = manager.getGroup(groupIdentifier);
        if (newSuffix != null) {
            group.setSuffix(newSuffix, world);
            return group.getIdentifier() + "'s suffix" + (world != null ? " (in world \"" + world + "\") " : " ")
                    + "has been set to \"" + group.getSuffix() + "\"";
        }
        return group.getIdentifier() + "'s suffix" + (world != null ? " (in world \"" + world + "\") " : " ")
                + "is \"" + group.getSuffix() + "\"";
    }

    public String groupCreate(String groupIdentifier, String parentsCsv) {
        PermissionGroup group = manager.getGroup(groupIdentifier);
        if (!group.isVirtual()) {
            return "Group \"" + groupIdentifier + "\" already exists.";
        }
        if (parentsCsv != null && !parentsCsv.isBlank()) {
            group.setParents(parseGroups(parentsCsv), null);
        }
        group.save();
        return "Group \"" + group.getIdentifier() + "\" created!";
    }

    public String groupDelete(String groupIdentifier) {
        PermissionGroup group = manager.getGroup(groupIdentifier);
        String id = group.getIdentifier();
        group.remove();
        manager.resetGroup(id);
        return "Group \"" + id + "\" removed!";
    }

    public List<String> groupUsersLines(String groupIdentifier) {
        java.util.Set<PermissionUser> users = manager.getUsers(groupIdentifier);
        if (users.isEmpty()) {
            return List.of("Group \"" + groupIdentifier + "\" has no users.");
        }
        List<String> lines = new ArrayList<>();
        lines.add("Group \"" + groupIdentifier + "\" users (" + users.size() + "):");
        for (PermissionUser user : users) {
            lines.add("   " + user.getName());
        }
        return lines;
    }

    public String usersCleanup(String groupIdentifier, String thresholdDays) {
        PermissionGroup group = manager.getGroup(groupIdentifier);
        long threshold = 30L * 86400L;
        if (thresholdDays != null) {
            threshold = Long.parseLong(thresholdDays) * 86400L;
        }
        long deadline = (System.currentTimeMillis() / 1000L) - threshold;
        int removed = 0;
        for (PermissionUser user : group.getUsers()) {
            int lastLogin = user.getOwnOptionInteger("last-login-time", null, 0);
            if (lastLogin > 0 && lastLogin < deadline) {
                user.remove();
                removed++;
            }
        }
        return "Cleaned " + removed + " users";
    }

    public List<String> hierarchyLines(String world) {
        List<PermissionGroup> groups = manager.getGroupList();
        Map<String, List<PermissionGroup>> children = new HashMap<>();
        for (PermissionGroup group : groups) {
            for (String parent : group.getParentIdentifiers(world)) {
                children.computeIfAbsent(parent, k -> new ArrayList<>()).add(group);
            }
        }
        List<PermissionGroup> roots = groups.stream()
                .filter(group -> group.getParentIdentifiers(world).isEmpty())
                .sorted()
                .toList();

        List<String> lines = new ArrayList<>();
        lines.add("User/Group inheritance hierarchy:");
        for (PermissionGroup root : roots) {
            appendHierarchy(lines, children, root, 0);
        }
        return lines;
    }

    private void appendHierarchy(List<String> lines, Map<String, List<PermissionGroup>> children, PermissionGroup group, int depth) {
        lines.add("  ".repeat(Math.max(0, depth)) + "- " + group.getIdentifier());
        for (PermissionGroup child : children.getOrDefault(group.getIdentifier(), List.of()).stream().sorted().toList()) {
            appendHierarchy(lines, children, child, depth + 1);
        }
    }

    public List<String> defaultGroupsLines(String world) {
        List<String> lines = new ArrayList<>();
        lines.add("Default groups in world \"" + world + "\" are:");
        for (PermissionGroup group : manager.getDefaultGroups(world)) {
            lines.add("  - " + group.getIdentifier());
        }
        return lines;
    }

    public String setDefaultGroup(String groupIdentifier, boolean value, String world) {
        PermissionGroup group = manager.getGroup(groupIdentifier);
        group.setDefault(value, world);
        return "Group \"" + groupIdentifier + "\" is " + (value ? "now" : "no longer") + " default in world \"" + world + "\"";
    }

    public String importDataFromBackend(String backendName) throws PermissionBackendException {
        return importDataFromBackend(backendName, null);
    }

    public String importDataFromBackend(String backendName, ImportBridge importBridge) throws PermissionBackendException {
        if (importBridge != null && importBridge.supports(backendName)) {
            return importBridge.importIntoPex(backendName);
        }
        manager.getBackend().loadFrom(manager.createBackend(backendName));
        return "[PermissionsEx] Data from \"" + backendName + "\" loaded into currently active backend";
    }

    public String promote(String userIdentifier, PermissionUser actor, String ladder) throws RankingException {
        PermissionUser target = manager.getUser(userIdentifier);
        PermissionGroup result = target.promote(actor, ladder);
        return "User " + target.getName() + " promoted to " + result.getName() + " group";
    }

    public String demote(String userIdentifier, PermissionUser actor, String ladder) throws RankingException {
        PermissionUser target = manager.getUser(userIdentifier);
        PermissionGroup result = target.demote(actor, ladder);
        return "User " + target.getName() + " demoted to " + result.getName() + " group";
    }

    public String version(String version) {
        return "[PermissionsEx] version [" + version + "]";
    }

    public List<String> configNodeLines(ConfigBridge configBridge, String nodeName, String rawValue) {
        if (nodeName == null || nodeName.isBlank()) {
            throw new IllegalArgumentException("Config node is required");
        }

        if (rawValue != null) {
            configBridge.set(nodeName, parseValue(rawValue));
            configBridge.save();
        }

        Object node = configBridge.get(nodeName);
        List<String> lines = new ArrayList<>();
        if (node instanceof Map<?, ?> mapNode) {
            lines.add("Node \"" + nodeName + "\": ");
            for (Map.Entry<?, ?> entry : mapNode.entrySet()) {
                lines.add("  " + entry.getKey() + " = " + entry.getValue());
            }
        } else if (node instanceof List<?> listNode) {
            lines.add("Node \"" + nodeName + "\": ");
            for (Object item : listNode) {
                lines.add(" - " + item);
            }
        } else {
            lines.add("Node \"" + nodeName + "\" = \"" + node + "\"");
        }
        return lines;
    }

    public String convertUuid(UuidConversionBridge conversionBridge, boolean force) {
        return conversionBridge.convert(force);
    }

    private Object parseValue(String value) {
        if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
            return Boolean.parseBoolean(value);
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignore) {
        }

        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ignore) {
        }

        return value;
    }

    private List<PermissionGroup> parseGroups(String csv) {
        if (csv == null || csv.isBlank()) {
            return List.of();
        }
        List<PermissionGroup> groups = new ArrayList<>();
        for (String raw : csv.split(",")) {
            String name = raw.trim();
            if (name.isEmpty()) {
                continue;
            }
            PermissionGroup group = manager.getGroup(name);
            if (group != null && !groups.contains(group)) {
                groups.add(group);
            }
        }
        return groups;
    }

    public interface CoreConfigReloader {
        void reload();
    }

    public interface ConfigBridge {
        Object get(String path);

        void set(String path, Object value);

        void save();
    }

    public interface UuidConversionBridge {
        String convert(boolean force);
    }

    public interface ImportBridge {
        boolean supports(String source);

        String importIntoPex(String source) throws PermissionBackendException;
    }

    public record UserView(String identifier, String name, List<String> groups, List<String> permissions) {}

    public record GroupView(String name, List<String> permissions) {}
}
