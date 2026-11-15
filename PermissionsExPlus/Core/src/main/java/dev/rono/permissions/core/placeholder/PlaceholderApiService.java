package dev.rono.permissions.core.placeholder;

import lombok.var;

import lombok.Value;
import lombok.experimental.Accessors;
import dev.rono.permissions.api.context.ContextSet;
import dev.rono.permissions.api.group.Group;
import dev.rono.permissions.api.ladder.Ladder;
import dev.rono.permissions.api.parent.ParentNode;
import dev.rono.permissions.api.resolver.QueryOptions;
import dev.rono.permissions.api.user.User;
import dev.rono.permissions.core.context.ContextManagerImpl;
import dev.rono.permissions.core.manager.GroupManagerImpl;
import dev.rono.permissions.core.manager.LadderManagerImpl;
import dev.rono.permissions.core.manager.UserManagerImpl;
import dev.rono.permissions.core.resolver.ResolverImpl;
import dev.rono.permissions.core.store.DataStore;

import java.util.Locale;
import java.util.UUID;
import dev.rono.permissions.core.util.Immutables;
import dev.rono.permissions.core.util.Strings;

public final class PlaceholderApiService {
    private final UserManagerImpl users;
    private final GroupManagerImpl groups;
    private final LadderManagerImpl ladders;

    private final ResolverImpl resolvers;
    private final ContextManagerImpl contexts;
    private final DataStore store;
    private final String messaging;

    public PlaceholderApiService(UserManagerImpl users, GroupManagerImpl groups, LadderManagerImpl ladders, ResolverImpl resolvers, ContextManagerImpl contexts, DataStore store, String messaging) {
        this.users = users;
        this.groups = groups;
        this.ladders = ladders;
        this.resolvers = resolvers;
        this.contexts = contexts;
        this.store = store;
        this.messaging = messaging;
    }

    public String resolve(UUID id, String placeholder) {
        var user = users.cache().get(id).orElse(null);

        if (user == null || placeholder == null || Strings.isBlank(placeholder)) {
            return "";
        }

        var parsed = query(id, placeholder);
        var key = parsed.identifier().toLowerCase(Locale.ROOT);
        var query = parsed.options();

        if (key.startsWith("user_option_")) {
            return resolvers.options().resolve(user, key.substring(12), query).orElse("");
        }

        if (key.startsWith("user_has_permission_")) {
            return Boolean.toString(resolvers.permissions().hasPermission(user, key.substring(20), query));
        }

        if (key.startsWith("group_prefix_")) {
            return groups.cache().get(key.substring(13)).flatMap(group -> resolvers.options().prefix(group, QueryOptions.global())).orElse("");
        }

        if (key.startsWith("group_suffix_")) {
            return groups.cache().get(key.substring(13)).flatMap(group -> resolvers.options().suffix(group, QueryOptions.global())).orElse("");
        }

        if (key.startsWith("group_weight_")) {
            return groups.cache().get(key.substring(13)).map(group -> Integer.toString(group.weight().orElse(0))).orElse("");
        }

        if (key.startsWith("group_parents_list_")) {
            return groups.cache().get(key.substring(19))
                    .map(group -> String.join(", ", group.parents().stream().map(ParentNode::group).sorted().collect(Immutables.toList())))
                    .orElse("");
        }

        if (key.startsWith("group_member_count_")) {
            var group = key.substring(19);

            if (!groups.cache().get(group).isPresent()) {
                return "";
            }

            return Long.toString(users.cache().all().stream().filter(value -> value.groups().stream().anyMatch(node -> node.group().equals(group))).count());
        }

        if (key.startsWith("group_option_")) {
            var remainder = key.substring(13);
            var split = remainder.indexOf('_');
            if (split < 1) {
                return "";
            }

            return groups.cache().get(remainder.substring(0, split))
                    .flatMap(group -> resolvers.options().resolve(group, remainder.substring(split + 1), query))
                    .orElse("");
        }

        if (key.startsWith("ladder_groups_list_")) {
            return ladders.cache().get(key.substring(19)).map(ladder -> String.join(" -> ", ladder.groups())).orElse("");
        }

        if (key.startsWith("user_ladder_next_group_")) {
            return ladderStep(user, key.substring(23), query, 1);
        }

        if (key.startsWith("user_ladder_previous_group_")) {
            return ladderStep(user, key.substring(27), query, -1);
        }

        if (key.startsWith("user_ladder_is_at_top_")) {
            return ladderBoundary(user, key.substring(22), query, true);
        }

        if (key.startsWith("user_ladder_is_at_bottom_")) {
            return ladderBoundary(user, key.substring(25), query, false);
        }

        if (key.startsWith("server_context_environment_")) {
            return contexts.staticContexts().values(key.substring(27)).stream().sorted().findFirst().orElse("");
        }

        switch (key) {
            case "user_name" :
                return user.name();
            case "user_uuid" :
                return user.uniqueId().toString();
            case "user_prefix" :
                return resolvers.options().prefix(user, query).orElse("");
            case "user_suffix" :
                return resolvers.options().suffix(user, query).orElse("");
            case "user_group" :
            case "user_primary_group" :
                return resolvers.primaryGroup().resolve(user, query).map(Group::name).orElse("");
            case "user_primary_group_display" :
                return resolvers.primaryGroup().resolve(user, query)
                        .flatMap(group -> resolvers.options().resolve(group, "display-name", query))
                        .orElseGet(() -> resolvers.primaryGroup().resolve(user, query)
                                .map(Group::name).orElse(""));
            case "user_groups_list" :
                return String.join(", ", resolvers.inheritance().groups(user, query).stream().map(Group::name).sorted().collect(Immutables.toList()));
            case "user_groups_list_display" :
                return String.join(", ", resolvers.inheritance().groups(user, query).stream()
                        .sorted(java.util.Comparator
                                .comparingInt((Group group) -> group.weight().orElse(0))
                                .reversed().thenComparing(Group::name))
                        .map(group -> resolvers.options().resolve(group, "display-name", query).orElse(group.name()))
                        .collect(Immutables.toList()));
            case "system_backend_type" :
                return store.name().toLowerCase(Locale.ROOT);
            case "system_messaging_type" :
                return messaging.toLowerCase(Locale.ROOT);
            case "system_cached_users_count" :
                return Integer.toString(users.cache().all().size());
            case "system_data_version" :
                return "2";
            default :
                return "";
        }
    }

    public void invalidate(UUID ignored) {}

    public void invalidateAll() {}

    private ParsedQuery query(UUID id, String raw) {
        var marker = "_server:";

        var index = raw.toLowerCase(Locale.ROOT).lastIndexOf(marker);
        if (index < 0) {
            return new ParsedQuery(raw, contexts.queryOptions(id));
        }

        var server = raw.substring(index + marker.length());
        if (Strings.isBlank(server)) {
            return new ParsedQuery(raw.substring(0, index), contexts.queryOptions(id));
        }

        var overridden = ContextSet.builder(contexts.contexts(id)).set("server", server).build();

        return new ParsedQuery(raw.substring(0, index), QueryOptions.builder().contexts(overridden).build());
    }

    private String ladderStep(User user, String name, QueryOptions query, int direction) {
        var ladder = ladders.cache().get(name).orElse(null);
        if (ladder == null || ladder.groups().isEmpty()) {
            return "";
        }

        var current = ladderPosition(user, ladder, query);
        var target = current < 0 && direction > 0 ? 0 : current + direction;
        return target >= 0 && target < ladder.groups().size() ? ladder.groups().get(target) : "";
    }

    private String ladderBoundary(User user, String name, QueryOptions query, boolean top) {
        var ladder = ladders.cache().get(name).orElse(null);
        if (ladder == null || ladder.groups().isEmpty()) {
            return "";
        }

        var current = ladderPosition(user, ladder, query);
        return Boolean.toString(current >= 0 && current == (top ? ladder.groups().size() - 1 : 0));
    }

    private int ladderPosition(User user, Ladder ladder, QueryOptions query) {
        var active = query.contexts();

        var memberships = user.groups().stream()
                .filter(node -> !node.expired() && node.contexts().asMap().entrySet().stream()
                        .allMatch(entry -> active.values(entry.getKey()).containsAll(entry.getValue())))
                .map(ParentNode::group).collect(java.util.stream.Collectors.toSet());

        var result = -1;

        for (var index = 0; index < ladder.groups().size(); index++) {
            if (memberships.contains(ladder.groups().get(index))) {
                result = index;
            }
        }

        return result;
    }

    @Value
    @Accessors(fluent = true)
    private static class ParsedQuery {
        String identifier;
        QueryOptions options;

        private ParsedQuery(String identifier, QueryOptions options) {
            this.identifier = identifier;
            this.options = options;
        }
    }
}
