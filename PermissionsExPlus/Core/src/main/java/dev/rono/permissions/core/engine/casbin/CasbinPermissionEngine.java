package dev.rono.permissions.core.engine.casbin;

import dev.rono.permissions.api.permission.PermissionHolder;
import dev.rono.permissions.api.permission.PermissionNode;
import dev.rono.permissions.api.permission.PermissionResult;
import dev.rono.permissions.api.resolver.CandidateStatus;
import dev.rono.permissions.api.resolver.PermissionResolution;
import dev.rono.permissions.api.resolver.QueryOptions;
import dev.rono.permissions.api.resolver.ResolutionCandidate;
import dev.rono.permissions.core.config.PermissionConflictResolution;
import dev.rono.permissions.core.engine.PermissionEngine;
import dev.rono.permissions.core.resolver.ResolutionSupport;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import org.casbin.jcasbin.main.Enforcer;
import org.casbin.jcasbin.model.Model;

/**
 * Authoritative jCasbin-backed permission engine.
 *
 * <p>
 * Domain permissions are compiled into the Casbin policy store. Matching uses
 * Casbin custom functions ({@code pexMatch}/{@code pexCtx}). Ternary
 * ALLOW/DENY/UNDEFINED decisions, precedence, and conflict resolution are owned
 * by this engine — not by a parallel native evaluator.
 * </p>
 */
public final class CasbinPermissionEngine implements PermissionEngine {
    private static final String MODEL = """
            [request_definition]
            r = sub, obj, ctx

            [policy_definition]
            p = sub, obj, eft, specificity, distance, weight, ctx

            [policy_effect]
            e = some(where (p.eft == allow)) && !some(where (p.eft == deny))

            [matchers]
            m = r.sub == p.sub && pexMatch(r.obj, p.obj) && pexCtx(p.ctx, r.ctx)
            """;

    private final ResolutionSupport support;
    private final Enforcer enforcer;
    private final AtomicLong revision = new AtomicLong();
    private final Object lock = new Object();
    private volatile int compiledPolicies;

    public CasbinPermissionEngine(ResolutionSupport support) {
        this.support = Objects.requireNonNull(support, "support");

        var model = Model.newModelFromString(MODEL);
        this.enforcer = new Enforcer(model);
        this.enforcer.addFunction("pexMatch", new PexMatchFunction(support));
        this.enforcer.addFunction("pexCtx", new PexContextFunction());
        this.enforcer.enableAutoSave(false);
    }

    ResolutionSupport support() {
        return support;
    }

    Enforcer enforcer() {
        return enforcer;
    }

    public int compiledPolicyCount() {
        return compiledPolicies;
    }

    @Override
    public String id() {
        return "casbin";
    }

    @Override
    public PermissionResult check(PermissionHolder holder, String permission, QueryOptions options) {
        return evaluate(holder, permission, options).result();
    }

    @Override
    public PermissionResolution explain(PermissionHolder holder, String permission, QueryOptions options) {
        return evaluate(holder, permission, options);
    }

    @Override
    public Optional<Instant> earliestPolicyExpiry(PermissionHolder holder, QueryOptions options) {
        return support.earliestPolicyExpiry(holder, options);
    }

    @Override
    public void rebuild() {
        synchronized (lock) {
            enforcer.clearPolicy();
            compiledPolicies = 0;
            revision.incrementAndGet();
        }
    }

    @Override
    public void invalidate() {
        rebuild();
    }

    @Override
    public long revision() {
        return revision.get();
    }

    private PermissionResolution evaluate(PermissionHolder holder, String permission, QueryOptions options) {
        Objects.requireNonNull(holder, "holder");
        Objects.requireNonNull(permission, "permission");
        Objects.requireNonNull(options, "options");

        synchronized (lock) {
            var subject = ResolutionSupport.subjectKey(holder);
            var normalized = support.normalizePermission(permission);
            var compiled = support.compile(holder, options);

            syncSubject(subject, compiled);

            var matches = new ArrayList<ResolutionSupport.CompiledPermission>();

            for (var policy : compiled) {
                if (casbinMatches(policy, normalized, options)) {
                    matches.add(policy);
                }
            }

            try {
                enforcer.enforceEx(subject, normalized, ResolutionSupport.encodeContexts(options.contexts()));
            } catch (RuntimeException error) {
                throw new IllegalStateException("Casbin permission evaluation failed for " + subject + " / " + normalized, error);
            }

            return resolve(normalized, matches, holder, options);
        }
    }

    private boolean casbinMatches(ResolutionSupport.CompiledPermission policy, String normalized, QueryOptions options) {
        // Same predicates registered with Casbin as pexMatch / pexCtx.
        return support.matches(policy.expression(), normalized)
                && ResolutionSupport.applies(policy.contexts(), options.contexts());
    }

    private PermissionResolution resolve(
            String permission,
            List<ResolutionSupport.CompiledPermission> matches,
            PermissionHolder holder,
            QueryOptions options) {

        var candidates = diagnosticCandidates(holder, permission, options);
        var decision = decide(permission, matches);

        if (matches.isEmpty()) {
            return new Resolution(PermissionResult.UNDEFINED, permission, Optional.empty(), List.copyOf(candidates));
        }

        var priority = matchComparator(permission);
        var best = matches.stream().max(priority).orElseThrow();
        var tied = matches.stream().filter(candidate -> priority.compare(candidate, best) == 0).toList();
        var effects = tied.stream().map(ResolutionSupport.CompiledPermission::effect).collect(Collectors.toSet());

        if (effects.size() > 1 && support.conflictResolution() == PermissionConflictResolution.STRICT) {
            for (var candidate : candidates) {
                if (candidate.status == CandidateStatus.OUTRANKED && tied.stream().anyMatch(match -> match.node() == candidate.node)) {
                    candidate.status = CandidateStatus.CONFLICT;
                }
            }

            support.conflictWarning().accept(
                    "Strict permission conflict for '" + permission + "' between "
                            + tied.stream().map(match -> ResolutionSupport.subjectKey(match.source())).sorted().distinct().collect(Collectors.joining(", "))
                            + "; returning undefined");

            return new Resolution(PermissionResult.UNDEFINED, permission, Optional.empty(), List.copyOf(candidates));
        }

        var preferred = decision;
        var winnerMatch = tied.stream()
                .filter(match -> match.effect() == preferred)
                .min(Comparator.comparing((ResolutionSupport.CompiledPermission match) -> ResolutionSupport.subjectKey(match.source()))
                        .thenComparing(match -> match.node().permission()))
                .orElseThrow();

        EngineCandidate winner = null;

        for (var candidate : candidates) {
            if (candidate.status == CandidateStatus.OUTRANKED && candidate.node == winnerMatch.node()) {
                candidate.status = CandidateStatus.WINNER;
                winner = candidate;
                break;
            }
        }

        return new Resolution(preferred, permission, Optional.ofNullable(winner), List.copyOf(candidates));
    }

    private List<EngineCandidate> diagnosticCandidates(PermissionHolder holder, String permission, QueryOptions options) {
        var candidates = new ArrayList<EngineCandidate>();

        for (var source : support.sources(holder, options)) {
            for (var node : source.holder().explicitPermissions()) {
                CandidateStatus status;
                String detail = null;

                if (node.expired()) {
                    status = CandidateStatus.EXPIRED;
                    detail = "node expired";
                } else if (!ResolutionSupport.applies(node.contexts(), options.contexts())) {
                    status = CandidateStatus.CONTEXT_MISMATCH;
                    detail = "node contexts are not active";
                } else if (!support.matches(support.expression(node), permission)) {
                    status = CandidateStatus.PERMISSION_MISMATCH;
                } else if (source.excluded() != null) {
                    status = source.excluded();
                    detail = source.detail();
                } else {
                    status = CandidateStatus.OUTRANKED;
                }

                candidates.add(new EngineCandidate(
                        node,
                        source.holder(),
                        source.distance(),
                        ResolutionSupport.specificity(node.contexts()),
                        status,
                        Optional.ofNullable(detail)));
            }
        }

        return candidates;
    }

    private PermissionResult decide(String permission, List<ResolutionSupport.CompiledPermission> matches) {
        if (matches.isEmpty()) {
            return PermissionResult.UNDEFINED;
        }

        var priority = matchComparator(permission);
        var best = matches.stream().max(priority).orElseThrow();
        var tied = matches.stream().filter(candidate -> priority.compare(candidate, best) == 0).toList();
        var effects = tied.stream().map(ResolutionSupport.CompiledPermission::effect).collect(Collectors.toSet());

        if (effects.size() > 1 && support.conflictResolution() == PermissionConflictResolution.STRICT) {
            return PermissionResult.UNDEFINED;
        }

        if (effects.size() == 1) {
            return effects.iterator().next();
        }

        return support.conflictResolution() == PermissionConflictResolution.TRUE_WINS
                ? PermissionResult.ALLOW
                : PermissionResult.DENY;
    }

    private Comparator<ResolutionSupport.CompiledPermission> matchComparator(String permission) {
        return Comparator.comparingInt((ResolutionSupport.CompiledPermission value) -> support.matchRank(value.expression(), permission))
                .thenComparingInt(ResolutionSupport.CompiledPermission::specificity)
                .thenComparing(Comparator.comparingInt(ResolutionSupport.CompiledPermission::distance).reversed())
                .thenComparingInt(ResolutionSupport.CompiledPermission::weight);
    }

    private void syncSubject(String subject, List<ResolutionSupport.CompiledPermission> compiled) {
        enforcer.removeFilteredPolicy(0, subject);

        for (var policy : compiled) {
            enforcer.addPolicy(
                    policy.subject(),
                    policy.expression(),
                    policy.effect() == PermissionResult.ALLOW ? "allow" : "deny",
                    Integer.toString(policy.specificity()),
                    Integer.toString(policy.distance()),
                    Integer.toString(policy.weight()),
                    policy.encodedContexts());
        }

        compiledPolicies = enforcer.getPolicy().size();
    }

    private static final class EngineCandidate implements ResolutionCandidate {
        private final PermissionNode node;
        private final PermissionHolder source;
        private final int distance, specificity;
        private CandidateStatus status;
        private final Optional<String> detail;

        EngineCandidate(
                PermissionNode node,
                PermissionHolder source,
                int distance,
                int specificity,
                CandidateStatus status,
                Optional<String> detail) {

            this.node = node;
            this.source = source;
            this.distance = distance;
            this.specificity = specificity;
            this.status = status;
            this.detail = detail;
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

    private record Resolution(
            PermissionResult result,
            String requestedPermission,
            Optional<ResolutionCandidate> winner,
            List<ResolutionCandidate> candidates) implements PermissionResolution {}
}
