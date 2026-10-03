package dev.rono.permissions.core.resolver;

import dev.rono.permissions.api.context.ContextSet;
import dev.rono.permissions.api.group.Group;
import dev.rono.permissions.api.options.OptionKeys;
import dev.rono.permissions.api.options.OptionNode;
import dev.rono.permissions.api.permission.PermissionHolder;
import dev.rono.permissions.api.permission.PermissionNode;
import dev.rono.permissions.api.permission.PermissionResult;
import dev.rono.permissions.api.resolver.DefaultGroupResolver;
import dev.rono.permissions.api.resolver.InheritanceResolver;
import dev.rono.permissions.api.resolver.OptionResolver;
import dev.rono.permissions.api.resolver.PermissionResolution;
import dev.rono.permissions.api.resolver.PermissionResolver;
import dev.rono.permissions.api.resolver.PrimaryGroupResolver;
import dev.rono.permissions.api.resolver.QueryOptions;
import dev.rono.permissions.api.resolver.ResolvedData;
import dev.rono.permissions.api.resolver.ResolvedMetaData;
import dev.rono.permissions.api.resolver.ResolvedPermissionData;
import dev.rono.permissions.api.resolver.ResolvedUserData;
import dev.rono.permissions.api.resolver.Resolvers;
import dev.rono.permissions.api.user.User;
import dev.rono.permissions.api.util.Identifiers;
import dev.rono.permissions.api.util.Node;
import dev.rono.permissions.core.config.MetaFormatting;
import dev.rono.permissions.core.config.PermissionConflictResolution;
import dev.rono.permissions.core.engine.PermissionEngine;
import dev.rono.permissions.core.engine.PermissionEngines;
import dev.rono.permissions.core.manager.GroupManagerImpl;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

public final class ResolverImpl implements Resolvers, PermissionResolver, OptionResolver, InheritanceResolver, DefaultGroupResolver {
    private final GroupManagerImpl groups;
    private final ResolutionSupport support;
    private final PermissionEngine permissionEngine;
    private final MetaFormatting metaFormatting;

    public ResolverImpl(GroupManagerImpl groups, int maxDepth) {
        this(groups, maxDepth, false, true, true, "default");
    }

    public ResolverImpl(
            GroupManagerImpl groups,
            int maxDepth,
            boolean caseSensitive,
            boolean wildcards,
            boolean negations) {

        this(groups, maxDepth, caseSensitive, wildcards, negations, "default");
    }

    public ResolverImpl(
            GroupManagerImpl groups,
            int maxDepth,
            boolean caseSensitive,
            boolean wildcards,
            boolean negations,
            String defaultGroup) {

        this(groups, maxDepth, caseSensitive, wildcards, negations, defaultGroup, PermissionConflictResolution.DENY_WINS, MetaFormatting.HIGHEST_WEIGHT, ignored -> {});
    }

    public ResolverImpl(
            GroupManagerImpl groups,
            int maxDepth,
            boolean caseSensitive,
            boolean wildcards,
            boolean negations,
            String defaultGroup,
            PermissionConflictResolution conflictResolution,
            MetaFormatting metaFormatting,
            Consumer<String> conflictWarning) {

        this(groups, maxDepth, caseSensitive, wildcards, negations, defaultGroup, conflictResolution, metaFormatting, conflictWarning, null);
    }

    public ResolverImpl(
            GroupManagerImpl groups,
            int maxDepth,
            boolean caseSensitive,
            boolean wildcards,
            boolean negations,
            String defaultGroup,
            PermissionConflictResolution conflictResolution,
            MetaFormatting metaFormatting,
            Consumer<String> conflictWarning,
            PermissionEngine permissionEngine) {

        this(
                groups,
                new ResolutionSupport(groups, maxDepth, caseSensitive, wildcards, negations, defaultGroup, conflictResolution, conflictWarning),
                metaFormatting,
                permissionEngine);
    }

    public ResolverImpl(
            GroupManagerImpl groups,
            ResolutionSupport support,
            MetaFormatting metaFormatting,
            PermissionEngine permissionEngine) {

        this.groups = Objects.requireNonNull(groups, "groups");
        this.support = Objects.requireNonNull(support, "support");
        this.metaFormatting = Objects.requireNonNull(metaFormatting, "metaFormatting");
        this.permissionEngine = permissionEngine != null ? permissionEngine : PermissionEngines.create(this.support);
    }

    public ResolutionSupport support() {
        return support;
    }

    public PermissionEngine permissionEngine() {
        return permissionEngine;
    }

    @Override
    public ResolvedData resolve(PermissionHolder holder, QueryOptions options) {
        Objects.requireNonNull(holder, "holder");
        Objects.requireNonNull(options, "options");

        return new Data(options, permissionData(holder, options), metaData(holder, options));
    }

    @Override
    public ResolvedUserData resolve(User user, QueryOptions options) {
        return new UserData(options, permissionData(user, options), metaData(user, options), groups(user, options), resolvePrimary(user, options));
    }

    @Override
    public PermissionResolver permissions() {
        return this;
    }

    @Override
    public OptionResolver options() {
        return this;
    }

    @Override
    public InheritanceResolver inheritance() {
        return this;
    }

    @Override
    public PrimaryGroupResolver primaryGroup() {
        return this::resolvePrimary;
    }

    @Override
    public DefaultGroupResolver defaultGroups() {
        return this;
    }

    @Override
    public PermissionResult check(PermissionHolder holder, String permission, QueryOptions options) {
        return permissionEngine.check(holder, permission, options);
    }

    @Override
    public PermissionResolution explain(PermissionHolder holder, String requested, QueryOptions options) {
        return permissionEngine.explain(holder, requested, options);
    }

    @Override
    public Optional<String> resolve(PermissionHolder holder, String key, QueryOptions options) {
        var normalized = Identifiers.optionKey(key);
        var candidates = optionCandidates(holder, normalized, options);

        if (metaFormatting == MetaFormatting.ACCUMULATED && (OptionKeys.PREFIX.equals(normalized) || OptionKeys.SUFFIX.equals(normalized))) {
            var accumulated = candidates.stream()
                    .sorted(optionComparator().reversed().thenComparing(OptionCandidate::source))
                    .map(candidate -> candidate.node.value()).reduce("", String::concat);

            return accumulated.isEmpty() ? Optional.empty() : Optional.of(accumulated);
        }

        return candidates.stream().max(optionComparator().thenComparing(OptionCandidate::source))
                .map(candidate -> candidate.node.value());
    }

    @Override
    public Set<Group> groups(User user, QueryOptions options) {
        var result = new LinkedHashSet<Group>();
        var direct = applicableMemberships(user, options.contexts());

        if (user.groups().isEmpty() && options.includeDefaults()) {
            resolve().ifPresent(result::add);
        } else {
            direct.forEach(node -> groups.cache().get(node.group()).ifPresent(result::add));
        }

        if (options.includeInheritance()) {
            for (var group : new ArrayList<>(result)) {
                collectParents(group, options.contexts(), result, new HashSet<>(), 0);
            }
        }

        return Set.copyOf(result);
    }

    @Override
    public Set<Group> parents(Group group, QueryOptions options) {
        var result = new LinkedHashSet<Group>();

        for (var parent : group.parents()) {
            if (applicable(parent, options.contexts())) {
                groups.cache().get(parent.group()).ifPresent(result::add);
            }
        }

        if (options.includeInheritance()) {
            for (var direct : new ArrayList<>(result)) {
                collectParents(direct, options.contexts(), result, new HashSet<>(), 0);
            }
        }

        return Set.copyOf(result);
    }

    @Override
    public boolean inherits(User user, String group, QueryOptions options) {
        var key = Identifiers.group(group);

        return groups(user, options).stream().anyMatch(value -> value.name().equals(key));
    }

    @Override
    public boolean inherits(Group group, String parent, QueryOptions options) {
        var key = Identifiers.group(parent);

        return parents(group, options).stream().anyMatch(value -> value.name().equals(key));
    }

    private Optional<Group> resolvePrimary(User user, QueryOptions options) {
        var direct = applicableMemberships(user, options.contexts()).stream()
                .map(node -> groups.cache().get(node.group()).orElse(null)).filter(Objects::nonNull).toList();

        var value = highest(direct);
        if (value.isPresent()) {
            return value;
        }

        if (options.includeInheritance()) {
            value = highest(groups(user, QueryOptions.builder(options).includeDefaults(false).build()));
            if (value.isPresent()) {
                return value;
            }
        }

        return user.groups().isEmpty() && options.includeDefaults() ? resolve() : Optional.empty();
    }

    @Override
    public Optional<Group> resolve() {
        return support.resolveDefaultGroup();
    }

    private List<OptionCandidate> optionCandidates(PermissionHolder holder, String key, QueryOptions options) {
        var values = new ArrayList<OptionCandidate>();

        for (var source : support.sources(holder, options)) {
            if (source.excluded() == null) {
                for (var node : source.holder().explicitOptions()) {
                    if (!node.expired() && node.key().equals(key) && ResolutionSupport.applies(node.contexts(), options.contexts())) {
                        values.add(new OptionCandidate(node, source.distance(), ResolutionSupport.specificity(node.contexts()), source.weight(), ResolutionSupport.subjectKey(source.holder())));
                    }
                }
            }
        }

        return values;
    }

    private ResolvedPermissionData permissionData(PermissionHolder holder, QueryOptions options) {
        var map = new LinkedHashMap<String, PermissionResult>();

        var expressions = support.sources(holder, options).stream().filter(source -> source.excluded() == null)
                .flatMap(source -> source.holder().explicitPermissions().stream())
                .filter(node -> !node.expired() && ResolutionSupport.applies(node.contexts(), options.contexts()))
                .map(PermissionNode::permission).distinct().toList();

        for (var expression : expressions) {
            map.put(expression, check(holder, expression, options));
        }

        return new PermissionData(holder, options, Map.copyOf(map), this);
    }

    private ResolvedMetaData metaData(PermissionHolder holder, QueryOptions options) {
        var keys = support.sources(holder, options).stream().filter(source -> source.excluded() == null)
                .flatMap(source -> source.holder().explicitOptions().stream())
                .filter(node -> !node.expired() && ResolutionSupport.applies(node.contexts(), options.contexts())).map(OptionNode::key)
                .distinct().toList();

        var map = new LinkedHashMap<String, String>();

        for (var key : keys) {
            resolve(holder, key, options).ifPresent(value -> map.put(key, value));
        }

        return new MetaData(Map.copyOf(map));
    }

    private void collectParents(Group group, ContextSet contexts, Set<Group> result, Set<String> visited, int depth) {
        if (depth >= support.maxDepth() || !visited.add(group.name())) {
            return;
        }

        for (var parent : group.parents()) {
            if (applicable(parent, contexts)) {
                groups.cache().get(parent.group()).ifPresent(value -> {
                    if (result.add(value)) {
                        collectParents(value, contexts, result, visited, depth + 1);
                    }
                });
            }
        }
    }

    private static List<dev.rono.permissions.api.parent.ParentNode> applicableMemberships(User user, ContextSet contexts) {
        return user.groups().stream().filter(node -> applicable(node, contexts)).toList();
    }

    private static boolean applicable(Node node, ContextSet contexts) {
        return !node.expired() && ResolutionSupport.applies(node.contexts(), contexts);
    }

    private static Optional<Group> highest(Collection<Group> values) {
        return values.stream().max(Comparator.comparingInt((Group value) -> value.weight().orElse(0)).thenComparing(Group::name));
    }

    private static Comparator<OptionCandidate> optionComparator() {
        return Comparator.comparingInt(OptionCandidate::specificity)
                .thenComparing(Comparator.comparingInt(OptionCandidate::distance).reversed())
                .thenComparingInt(OptionCandidate::weight);
    }

    private record OptionCandidate(OptionNode node, int distance, int specificity, int weight, String source) {}

    private record Data(QueryOptions queryOptions, ResolvedPermissionData permissions, ResolvedMetaData meta) implements ResolvedData {}

    private record UserData(QueryOptions queryOptions, ResolvedPermissionData permissions, ResolvedMetaData meta, Set<Group> groups, Optional<Group> primaryGroup) implements ResolvedUserData {}

    private record PermissionData(PermissionHolder holder, QueryOptions options, Map<String, PermissionResult> permissionMap, ResolverImpl resolver) implements ResolvedPermissionData {
        @Override
        public PermissionResult check(String permission) {
            return resolver.check(holder, permission, options);
        }
    }

    private record MetaData(Map<String, String> options) implements ResolvedMetaData {
        @Override
        public Optional<String> option(String key) {
            return Optional.ofNullable(options.get(Identifiers.optionKey(key)));
        }

        @Override
        public Optional<String> prefix() {
            return option(OptionKeys.PREFIX);
        }

        @Override
        public Optional<String> suffix() {
            return option(OptionKeys.SUFFIX);
        }
    }
}
