package dev.rono.permissions.core.resolver;

import dev.rono.permissions.api.context.ContextSet;
import dev.rono.permissions.api.group.Group;
import dev.rono.permissions.api.parent.ParentNode;
import dev.rono.permissions.api.permission.PermissionHolder;
import dev.rono.permissions.api.permission.PermissionNode;
import dev.rono.permissions.api.permission.PermissionResult;
import dev.rono.permissions.api.permission.PermissionValue;
import dev.rono.permissions.api.resolver.CandidateStatus;
import dev.rono.permissions.api.resolver.PermissionResolution;
import dev.rono.permissions.api.resolver.QueryOptions;
import dev.rono.permissions.api.resolver.ResolutionCandidate;
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
 * Shared domain-graph permission evaluation used by {@link ResolverImpl} explain
 * traces and by the Casbin policy compiler.
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

    public String normalizePermission(String requested) {
        return caseSensitive ? requested.trim() : Identifiers.permission(requested);
    }

    public PermissionResult check(PermissionHolder holder, String permission, QueryOptions options) {
        return explain(holder, permission, options).result();
    }

    public PermissionResolution explain(PermissionHolder holder, String requested, QueryOptions options) {
        Objects.requireNonNull(holder, "holder");
        Objects.requireNonNull(options, "options");
        Objects.requireNonNull(requested, "requested");

        var permission = normalizePermission(requested);
        var candidates = new ArrayList<Candidate>();

        for (var source : sources(holder, options)) {
            for (var node : source.holder.explicitPermissions()) {
                CandidateStatus status;

                String detail = null;

                if (node.expired()) {
                    status = CandidateStatus.EXPIRED;
                    detail = "node expired";
                } else if (!applies(node.contexts(), options.contexts())) {
                    status = CandidateStatus.CONTEXT_MISMATCH;
                    detail = "node contexts are not active";
                } else if (!matches(expression(node), permission)) {
                    status = CandidateStatus.PERMISSION_MISMATCH;
                } else if (source.excluded != null) {
                    status = source.excluded;
                    detail = source.detail;
                } else {
                    status = CandidateStatus.OUTRANKED;
                }

                candidates.add(new Candidate(node, source.holder, source.distance, specificity(node.contexts()), status, Optional.ofNullable(detail), source.weight));
            }
        }

        var resolution = resolveCandidates(permission, candidates.stream().filter(value -> value.status == CandidateStatus.OUTRANKED).toList());
        return new Resolution(resolution.map(Candidate::result).orElse(PermissionResult.UNDEFINED), permission, resolution.map(value -> (ResolutionCandidate) value), List.copyOf(candidates));
    }

    /**
     * Compiles eligible (non-excluded, non-expired) permission nodes for a holder.
     * Node context matching is deferred to evaluation so one compiled set can serve
     * multiple active context combinations for the same membership graph.
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
                        node.contexts()));
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

    private Optional<Candidate> resolveCandidates(String permission, List<Candidate> candidates) {
        if (candidates.isEmpty()) {
            return Optional.empty();
        }

        var priority = candidateComparator(permission);

        var best = candidates.stream().max(priority).orElseThrow();

        var tied = candidates.stream().filter(candidate -> priority.compare(candidate, best) == 0).toList();

        var results = tied.stream().map(Candidate::result).collect(Collectors.toSet());

        if (results.size() > 1 && conflictResolution == PermissionConflictResolution.STRICT) {
            tied.forEach(candidate -> candidate.status = CandidateStatus.CONFLICT);

            conflictWarning.accept("Strict permission conflict for '" + permission + "' between " + tied.stream().map(candidate -> subjectKey(candidate.source)).sorted().distinct().collect(Collectors.joining(", ")) + "; returning undefined");

            return Optional.empty();
        }

        var preferred = results.size() == 1 ? results.iterator().next() : conflictResolution == PermissionConflictResolution.TRUE_WINS ? PermissionResult.ALLOW : PermissionResult.DENY;

        var winner = tied.stream().filter(candidate -> candidate.result() == preferred)
                .min(Comparator.comparing((Candidate candidate) -> subjectKey(candidate.source))
                        .thenComparing(candidate -> candidate.node.permission()))
                .orElseThrow();

        winner.status = CandidateStatus.WINNER;

        return Optional.of(winner);
    }

    private Comparator<Candidate> candidateComparator(String permission) {
        return Comparator.comparingInt((Candidate value) -> matchRank(expression(value.node), permission))
                .thenComparingInt(Candidate::contextSpecificity)
                .thenComparing(Comparator.comparingInt(Candidate::inheritanceDistance).reversed())
                .thenComparingInt(value -> value.weight);
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

    public PermissionResult decide(String permission, List<CompiledPermission> matches) {
        if (matches.isEmpty()) {
            return PermissionResult.UNDEFINED;
        }

        var priority = Comparator.comparingInt((CompiledPermission value) -> matchRank(value.expression(), permission))
                .thenComparingInt(CompiledPermission::specificity)
                .thenComparing(Comparator.comparingInt(CompiledPermission::distance).reversed())
                .thenComparingInt(CompiledPermission::weight);

        var best = matches.stream().max(priority).orElseThrow();
        var tied = matches.stream().filter(candidate -> priority.compare(candidate, best) == 0).toList();
        var results = tied.stream().map(CompiledPermission::effect).collect(Collectors.toSet());

        if (results.size() > 1 && conflictResolution == PermissionConflictResolution.STRICT) {
            conflictWarning.accept("Strict permission conflict for '" + permission + "'; returning undefined");
            return PermissionResult.UNDEFINED;
        }

        if (results.size() == 1) {
            return results.iterator().next();
        }

        return conflictResolution == PermissionConflictResolution.TRUE_WINS ? PermissionResult.ALLOW : PermissionResult.DENY;
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
            ContextSet contexts) {}

    private final class Candidate implements ResolutionCandidate {
        private final PermissionNode node;
        private final PermissionHolder source;
        private final int distance, specificity, weight;
        private CandidateStatus status;
        private final Optional<String> detail;

        Candidate(PermissionNode node, PermissionHolder source, int distance, int specificity, CandidateStatus status, Optional<String> detail, int weight) {
            this.node = node;
            this.source = source;
            this.distance = distance;
            this.specificity = specificity;
            this.status = status;
            this.detail = detail;
            this.weight = weight;
        }

        PermissionResult result() {
            return ResolutionSupport.this.candidateResult(node);
        }

        @Override
        public PermissionNode node() {
            return node;
        }

        @Override
        public PermissionHolder source() {
            return source;
        }

        @Override
        public int inheritanceDistance() {
            return distance;
        }

        @Override
        public int contextSpecificity() {
            return specificity;
        }

        @Override
        public CandidateStatus status() {
            return status;
        }

        @Override
        public Optional<String> detail() {
            return detail;
        }
    }

    private record Resolution(PermissionResult result, String requestedPermission, Optional<ResolutionCandidate> winner, List<ResolutionCandidate> candidates) implements PermissionResolution {}
}
