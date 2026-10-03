package dev.rono.permissions.core.resolver;

import dev.rono.permissions.api.context.ContextSet;
import dev.rono.permissions.api.group.Group;
import dev.rono.permissions.api.parent.ParentNode;
import dev.rono.permissions.api.permission.PermissionHolder;
import dev.rono.permissions.api.permission.PermissionNode;
import dev.rono.permissions.api.permission.PermissionResult;
import dev.rono.permissions.api.permission.PermissionValue;
import dev.rono.permissions.api.resolver.CandidateStatus;
import dev.rono.permissions.api.resolver.QueryOptions;
import dev.rono.permissions.api.user.User;
import dev.rono.permissions.api.util.Identifiers;
import dev.rono.permissions.api.util.Node;
import dev.rono.permissions.core.config.PermissionConflictResolution;
import dev.rono.permissions.core.manager.GroupManagerImpl;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Domain-graph helpers that compile PermissionsExPlus holders into Casbin
 * policies and supply matching/context utilities for the Casbin engine.
 */
public final class ResolutionSupport {
    private final GroupManagerImpl groups;
    private final int maxDepth;
    private final boolean caseSensitive, wildcards, negations;
    private final String defaultGroup;
    private final PermissionConflictResolution conflictResolution;
    private final Consumer<String> conflictWarning;

    public ResolutionSupport(
            GroupManagerImpl groups,
            int maxDepth,
            boolean caseSensitive,
            boolean wildcards,
            boolean negations,
            String defaultGroup,
            PermissionConflictResolution conflictResolution,
            Consumer<String> conflictWarning) {

        this.groups = Objects.requireNonNull(groups, "groups");
        this.maxDepth = Math.max(1, maxDepth);
        this.caseSensitive = caseSensitive;
        this.wildcards = wildcards;
        this.negations = negations;
        this.defaultGroup = Identifiers.group(defaultGroup);
        this.conflictResolution = Objects.requireNonNull(conflictResolution, "conflictResolution");
        this.conflictWarning = Objects.requireNonNull(conflictWarning, "conflictWarning");
    }

    public GroupManagerImpl groups() {
        return groups;
    }

    public int maxDepth() {
        return maxDepth;
    }

    public boolean caseSensitive() {
        return caseSensitive;
    }

    public boolean wildcards() {
        return wildcards;
    }

    public boolean negations() {
        return negations;
    }

    public String defaultGroup() {
        return defaultGroup;
    }

    public PermissionConflictResolution conflictResolution() {
        return conflictResolution;
    }

    public Optional<Group> resolveDefaultGroup() {
        return groups.cache().get(defaultGroup);
    }

    public Consumer<String> conflictWarning() {
        return conflictWarning;
    }

    public String normalizePermission(String requested) {
        return caseSensitive ? requested.trim() : Identifiers.permission(requested);
    }

    /**
     * Compiles eligible (non-excluded, non-expired) permission nodes for a holder.
     * Node context matching is deferred to Casbin evaluation so one compiled set can
     * serve multiple active context combinations for the same membership graph.
     */
    public List<CompiledPermission> compile(PermissionHolder holder, QueryOptions options) {
        Objects.requireNonNull(holder, "holder");
        Objects.requireNonNull(options, "options");

        var compiled = new ArrayList<CompiledPermission>();

        for (var source : sources(holder, options)) {
            if (source.excluded != null) {
                continue;
            }

            for (var node : source.holder.explicitPermissions()) {
                if (node.expired()) {
                    continue;
                }

                compiled.add(new CompiledPermission(
                        subjectKey(holder),
                        expression(node),
                        candidateResult(node),
                        specificity(node.contexts()),
                        source.distance,
                        source.weight,
                        encodeContexts(node.contexts()),
                        source.kind,
                        node.contexts(),
                        node,
                        source.holder));
            }
        }

        return List.copyOf(compiled);
    }

    public List<Source> sources(PermissionHolder holder, QueryOptions options) {
        var result = new ArrayList<Source>();

        result.add(new Source(holder, 0, weight(holder), null, null, SourceKind.DIRECT));

        if (holder instanceof User user) {
            var direct = applicableMemberships(user, options.contexts());

            if (user.groups().isEmpty()) {
                resolveDefaultGroup().ifPresent(group -> addGroupSource(group, 1, options, result, new HashSet<>(), options.includeDefaults() ? null : CandidateStatus.DEFAULTS_DISABLED, SourceKind.DEFAULT));
            } else {
                for (var membership : direct) {
                    groups.cache().get(membership.group()).ifPresent(group -> addGroupSource(group, 1, options, result, new HashSet<>(), null, SourceKind.DIRECT_GROUP));
                }
            }
        } else if (holder instanceof Group group) {
            for (var parent : group.parents()) {
                if (applicable(parent, options.contexts())) {
                    groups.cache().get(parent.group()).ifPresent(value -> addGroupSource(value, 1, options, result, new HashSet<>(), null, SourceKind.INHERITED));
                }
            }
        }

        return result;
    }

    public boolean matches(String expression, String permission) {
        return expression.equals(permission) || wildcards && (expression.equals("*") || expression.endsWith(".*") && permission.startsWith(expression.substring(0, expression.length() - 1)));
    }

    public int matchRank(String expression, String requested) {
        return expression.equals(requested) ? Integer.MAX_VALUE : expression.equals("*") ? 0 : expression.length();
    }

    public String expression(PermissionNode node) {
        return negations && node.permission().startsWith("-") ? node.permission().substring(1) : node.permission();
    }

    public PermissionResult candidateResult(PermissionNode node) {
        return negations && node.permission().startsWith("-") ? PermissionResult.DENY : node.value() == PermissionValue.ALLOW ? PermissionResult.ALLOW : PermissionResult.DENY;
    }

    public static boolean applies(ContextSet required, ContextSet active) {
        return required.asMap().entrySet().stream()
                .allMatch(entry -> active.values(entry.getKey()).containsAll(entry.getValue()));
    }

    public static int specificity(ContextSet contexts) {
        return contexts.asMap().values().stream().mapToInt(Set::size).sum();
    }

    public static String subjectKey(PermissionHolder holder) {
        if (holder instanceof Group group) {
            return "group:" + group.name();
        }

        if (holder instanceof User user) {
            return "user:" + user.uniqueId();
        }

        return holder.getClass().getName();
    }

    public static String encodeContexts(ContextSet contexts) {
        if (contexts.isEmpty()) {
            return "";
        }

        return contexts.asMap().entrySet().stream()
                .sorted(java.util.Map.Entry.comparingByKey())
                .map(entry -> entry.getKey() + '=' + entry.getValue().stream().sorted().collect(Collectors.joining(",")))
                .collect(Collectors.joining(";"));
    }

    public static ContextSet decodeContexts(String encoded) {
        if (encoded == null || encoded.isBlank()) {
            return ContextSet.empty();
        }

        var builder = ContextSet.builder();

        for (var part : encoded.split(";")) {
            var separator = part.indexOf('=');

            if (separator <= 0) {
                continue;
            }

            var key = part.substring(0, separator);
            var values = part.substring(separator + 1);

            if (values.isBlank()) {
                continue;
            }

            for (var value : values.split(",")) {
                if (!value.isBlank()) {
                    builder.add(key, value);
                }
            }
        }

        return builder.build();
    }

    private void addGroupSource(Group group, int distance, QueryOptions options, List<Source> result, Set<String> visited, CandidateStatus inheritedStatus, SourceKind rootKind) {
        if (distance > maxDepth || !visited.add(group.name())) {
            return;
        }

        var status = inheritedStatus != null ? inheritedStatus : distance > 1 && !options.includeInheritance() ? CandidateStatus.INHERITANCE_DISABLED : null;
        var kind = distance > 1 ? SourceKind.INHERITED : rootKind;

        result.add(new Source(group, distance, group.weight().orElse(0), status,
                status == CandidateStatus.INHERITANCE_DISABLED ? "inheritance disabled" : status == CandidateStatus.DEFAULTS_DISABLED ? "defaults disabled" : null,
                kind));

        for (var parent : group.parents()) {
            if (applicable(parent, options.contexts())) {
                groups.cache().get(parent.group()).ifPresent(value -> addGroupSource(value, distance + 1, options, result, visited, status, rootKind));
            }
        }
    }

    public Optional<Instant> earliestPolicyExpiry(PermissionHolder holder, QueryOptions options) {
        Objects.requireNonNull(holder, "holder");
        Objects.requireNonNull(options, "options");

        return sources(holder, options).stream()
                .filter(source -> source.excluded == null)
                .flatMap(source -> {
                    Stream<Node> nodes = Stream.concat(
                            source.holder.explicitPermissions().stream(),
                            source.holder.explicitOptions().stream());

                    if (source.holder instanceof User user) {
                        nodes = Stream.concat(nodes, user.groups().stream());
                    } else if (source.holder instanceof Group group) {
                        nodes = Stream.concat(nodes, group.parents().stream());
                    }

                    return nodes;
                })
                .map(Node::expiry)
                .flatMap(Optional::stream)
                .filter(expiry -> expiry.isAfter(Instant.now()))
                .min(Comparator.naturalOrder());
    }

    private static int weight(PermissionHolder holder) {
        return holder instanceof Group group ? group.weight().orElse(0) : Integer.MAX_VALUE;
    }

    private static List<ParentNode> applicableMemberships(User user, ContextSet contexts) {
        return user.groups().stream().filter(node -> applicable(node, contexts)).toList();
    }

    private static boolean applicable(Node node, ContextSet contexts) {
        return !node.expired() && applies(node.contexts(), contexts);
    }

    public enum SourceKind {
        DIRECT,
        DIRECT_GROUP,
        INHERITED,
        DEFAULT
    }

    public record Source(PermissionHolder holder, int distance, int weight, CandidateStatus excluded, String detail, SourceKind kind) {}

    public record CompiledPermission(
            String subject,
            String expression,
            PermissionResult effect,
            int specificity,
            int distance,
            int weight,
            String encodedContexts,
            SourceKind kind,
            ContextSet contexts,
            PermissionNode node,
            PermissionHolder source) {}
}
