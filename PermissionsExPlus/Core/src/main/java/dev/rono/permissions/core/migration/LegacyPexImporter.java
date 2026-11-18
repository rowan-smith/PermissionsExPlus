package dev.rono.permissions.core.migration;

import lombok.var;

import dev.rono.permissions.api.context.ContextKeys;
import dev.rono.permissions.api.context.ContextSet;
import dev.rono.permissions.api.options.OptionNode;
import dev.rono.permissions.api.parent.ParentNode;
import dev.rono.permissions.api.permission.PermissionNode;
import dev.rono.permissions.api.permission.PermissionValue;
import dev.rono.permissions.api.util.Identifiers;
import dev.rono.permissions.core.PexApiImpl;
import dev.rono.permissions.core.migration.LegacyPexData.Entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Merges data read from an original PermissionsEx installation into
 * PermissionsExPlus. Existing PermissionsExPlus data is kept; imported nodes
 * are
 * added to it.
 *
 * <p>
 * Mapping:
 * <ul>
 * <li>{@code -node} becomes a denied {@code node}; per-world data gets a
 * {@code world} context.</li>
 * <li>A user's {@code group-<name>-until} option becomes an expiry on that
 * membership (and on the option); memberships that already expired are
 * skipped.</li>
 * <li>The {@code weight} option sets the group weight; {@code rank} and
 * {@code rank-ladder} build ladders, lowest rank first. All options are also
 * kept as options, for plugins that read them.</li>
 * <li>{@code worlds.<world>.inheritance} becomes world inheritance.</li>
 * </ul>
 * Users must be stored by UUID, as PermissionsEx 1.23 does; users stored by
 * name
 * are reported and skipped, because their UUID cannot be known offline.
 */
public final class LegacyPexImporter {
    private static final Pattern REGEX = Pattern.compile("[()\\[\\]|^$\\\\]");
    private static final String DEFAULT_LADDER = "default";
    private static final int EXAMPLES = 5;

    private LegacyPexImporter() {}

    /** What an import did. */
    public static final class Report {
        public int groupsCreated;
        public int groupsUpdated;
        public int usersImported;
        public int permissions;
        public int options;
        public int memberships;
        public int timedMemberships;
        public int expiredMemberships;
        public int ladders;
        public int worlds;
        public final List<String> warnings = new ArrayList<>();

        void warn(String warning) {
            warnings.add(warning);
        }
    }

    public static Report apply(PexApiImpl<?> core, LegacyPexData data) {
        var report = new Report();
        var now = Instant.now();

        importGroups(core, data, report);
        importLadders(core, data, report);
        importUsers(core, data, report, now);
        importWorlds(core, data, report);
        checkDefaultGroup(core, data, report);

        return report;
    }

    private static void importGroups(PexApiImpl<?> core, LegacyPexData data, Report report) {
        // Every group referenced as a parent or membership must exist before it is
        // linked.
        var names = new LinkedHashSet<>(data.groups.keySet());
        data.groups.values().forEach(group -> group.parents.values().forEach(names::addAll));
        data.users.values().forEach(user -> user.parents.values().forEach(names::addAll));

        for (var name : names) {
            if (core.groups().find(name).toCompletableFuture().join().isPresent()) {
                report.groupsUpdated++;
            } else {
                core.groups().create(name).toCompletableFuture().join();
                report.groupsCreated++;

                if (!data.groups.containsKey(name)) {
                    report.warn("Group '" + name + "' is used but not defined in PermissionsEx; created it empty");
                }
            }
        }

        var regexNodes = new ArrayList<String>();

        for (Map.Entry<String, Entity> entry : data.groups.entrySet()) {
            var group = entry.getValue();

            try {
                core.groups().modify(entry.getKey(), modifier -> {
                    forEachWorld(group.permissions, (contexts, values) -> values.forEach(permission -> {
                        modifier.setPermission(permission(permission, contexts, regexNodes));
                        report.permissions++;
                    }));

                    forEachWorld(group.options, (contexts, values) -> values.forEach((key, value) -> {
                        modifier.setOption(key, value, contexts);
                        report.options++;
                    }));

                    forEachWorld(group.parents, (contexts, values) -> values.forEach(parent -> modifier.addParent(parent, contexts)));

                    var weight = integer(group.option("weight"));
                    if (weight != null) {
                        modifier.setWeight(weight);
                    }
                }).toCompletableFuture().join();
            } catch (Exception error) {
                report.warn("Group '" + entry.getKey() + "' was not fully imported: " + message(error));
            }
        }

        if (!regexNodes.isEmpty()) {
            report.warn(regexNodes.size() + " permission(s) look like PermissionsEx regular expressions, which PermissionsExPlus matches literally: "
                    + examples(regexNodes));
        }
    }

    private static void importLadders(PexApiImpl<?> core, LegacyPexData data, Report report) {
        var ladders = new LinkedHashMap<String, List<Map.Entry<String, Integer>>>();

        for (Map.Entry<String, Entity> entry : data.groups.entrySet()) {
            var rank = integer(entry.getValue().option("rank"));

            if (rank != null) {
                var ladder = entry.getValue().option("rank-ladder");
                ladders.computeIfAbsent(ladder == null ? DEFAULT_LADDER : ladder, ignored -> new ArrayList<>())
                        .add(new java.util.AbstractMap.SimpleEntry<>(entry.getKey(), rank));
            }
        }

        for (Map.Entry<String, List<Map.Entry<String, Integer>>> ladder : ladders.entrySet()) {
            // PermissionsEx ranks count down (1 is the top); PermissionsExPlus ladders list
            // the lowest first.
            var groups = new ArrayList<String>();
            ladder.getValue().stream().sorted(Comparator.comparing(Map.Entry<String, Integer>::getValue).reversed())
                    .forEach(group -> groups.add(group.getKey()));

            try {
                if (!core.ladders().find(ladder.getKey()).toCompletableFuture().join().isPresent()) {
                    core.ladders().create(ladder.getKey()).toCompletableFuture().join();
                }

                core.ladders().modify(ladder.getKey(), modifier -> modifier.setGroups(groups)).toCompletableFuture().join();
                report.ladders++;
            } catch (Exception error) {
                report.warn("Ladder '" + ladder.getKey() + "' was not imported: " + message(error));
            }
        }
    }

    private static void importUsers(PexApiImpl<?> core, LegacyPexData data, Report report, Instant now) {
        var byName = new ArrayList<String>();
        var regexNodes = new ArrayList<String>();

        for (Map.Entry<String, Entity> entry : data.users.entrySet()) {
            UUID id;

            try {
                id = UUID.fromString(entry.getKey());
            } catch (IllegalArgumentException notUuid) {
                byName.add(entry.getKey());
                continue;
            }

            var user = entry.getValue();
            var name = user.option("name");

            try {
                if (!core.users().find(id).toCompletableFuture().join().isPresent()) {
                    (name == null ? core.users().create(id) : core.users().create(id, name)).toCompletableFuture().join();
                }

                core.users().modify(id, modifier -> {
                    forEachWorld(user.permissions, (contexts, values) -> values.forEach(permission -> {
                        modifier.setPermission(permission(permission, contexts, regexNodes));
                        report.permissions++;
                    }));

                    forEachWorld(user.options, (contexts, values) -> values.forEach((key, value) -> {
                        // The name is the user's name, and timed-group options are applied with their
                        // memberships.
                        if (!"name".equalsIgnoreCase(key) && timedGroup(key) == null) {
                            modifier.setOption(key, value, contexts);
                            report.options++;
                        }
                    }));

                    forEachWorld(user.parents, (contexts, values) -> {
                        var world = contexts.values(ContextKeys.WORLD.key()).stream().findFirst().orElse(LegacyPexData.GLOBAL);
                        var options = user.options.getOrDefault(world, new LinkedHashMap<>());

                        for (var group : values) {
                            var key = "group-" + group + "-until";
                            var until = epochSeconds(options.get(key));
                            var membership = ParentNode.builder().group(group).contexts(contexts);

                            if (until != null && until > 0) {
                                var expiry = Instant.ofEpochSecond(until);

                                if (!expiry.isAfter(now)) {
                                    report.expiredMemberships++;
                                    continue;
                                }

                                membership.expiry(expiry);
                                modifier.setOption(OptionNode.builder().key(key).value(options.get(key)).contexts(contexts).expiry(expiry).build());
                                report.timedMemberships++;
                            }

                            modifier.addGroup(membership.build());
                            report.memberships++;
                        }
                    });
                }).toCompletableFuture().join();

                report.usersImported++;
            } catch (Exception error) {
                report.warn("User " + entry.getKey() + (name == null ? "" : " (" + name + ")") + " was not fully imported: " + message(error));
            }
        }

        if (!byName.isEmpty()) {
            report.warn(byName.size() + " user(s) are stored by name rather than UUID and were skipped; "
                    + "let PermissionsEx 1.23 convert them to UUIDs first: " + examples(byName));
        }

        if (!regexNodes.isEmpty()) {
            report.warn(regexNodes.size() + " user permission(s) look like PermissionsEx regular expressions, which PermissionsExPlus matches literally: "
                    + examples(regexNodes));
        }
    }

    private static void importWorlds(PexApiImpl<?> core, LegacyPexData data, Report report) {
        for (Map.Entry<String, List<String>> world : data.worldInheritance.entrySet()) {
            var parents = new LinkedHashSet<>(core.worlds().parents(world.getKey()));
            parents.addAll(world.getValue());

            core.worlds().set(world.getKey(), new ArrayList<>(parents));
            report.worlds++;
        }
    }

    private static void checkDefaultGroup(PexApiImpl<?> core, LegacyPexData data, Report report) {
        var defaults = new ArrayList<String>();

        data.groups.forEach((name, group) -> {
            if (group.isDefault) {
                defaults.add(name);
            }
        });

        var configured = core.config().general().defaultGroup();

        if (defaults.size() > 1) {
            report.warn("PermissionsEx marks several groups as default (" + String.join(", ", defaults)
                    + "); PermissionsExPlus uses one, default-group in config.yml (currently '" + configured + "')");
        } else if (defaults.size() == 1 && !Identifiers.group(defaults.get(0)).equals(Identifiers.group(configured))) {
            report.warn("PermissionsEx's default group is '" + defaults.get(0) + "'; set default-group: \"" + defaults.get(0)
                    + "\" in config.yml (currently '" + configured + "') to keep it");
        }
    }

    private static PermissionNode permission(String permission, ContextSet contexts, List<String> regexNodes) {
        var denied = permission.startsWith("-") && permission.length() > 1;
        var node = denied ? permission.substring(1) : permission;

        if (REGEX.matcher(node).find()) {
            regexNodes.add(permission);
        }

        return PermissionNode.builder().permission(node).value(denied ? PermissionValue.DENY : PermissionValue.ALLOW).contexts(contexts).build();
    }

    private static <T> void forEachWorld(Map<String, T> values, java.util.function.BiConsumer<ContextSet, T> action) {
        values.forEach((world, value) -> action.accept(contexts(world), value));
    }

    private static ContextSet contexts(String world) {
        return LegacyPexData.GLOBAL.equals(world) ? ContextSet.empty() : ContextSet.builder().add(ContextKeys.WORLD, world).build();
    }

    private static String timedGroup(String option) {
        return option.startsWith("group-") && option.endsWith("-until") && option.length() > "group--until".length()
                ? option.substring("group-".length(), option.length() - "-until".length())
                : null;
    }

    private static Integer integer(String value) {
        if (value == null) {
            return null;
        }

        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static Long epochSeconds(String value) {
        if (value == null) {
            return null;
        }

        try {
            return Long.valueOf(value.trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static String examples(List<String> values) {
        var shown = String.join(", ", values.subList(0, Math.min(EXAMPLES, values.size())));
        return values.size() > EXAMPLES ? shown + ", ..." : shown;
    }

    private static String message(Throwable error) {
        var cause = error;

        while (cause.getCause() != null && (cause instanceof java.util.concurrent.CompletionException || cause.getMessage() == null)) {
            cause = cause.getCause();
        }

        return cause.getMessage() == null ? cause.getClass().getSimpleName() : cause.getMessage();
    }
}
