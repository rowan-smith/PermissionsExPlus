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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import org.casbin.jcasbin.main.EnforceResult;
import org.casbin.jcasbin.main.Enforcer;
import org.casbin.jcasbin.model.Model;

/**
 * Authoritative jCasbin-backed permission engine.
 *
 * <p>
 * Domain permissions are compiled into the Casbin policy store. Matching is
 * performed by Casbin ({@code pexMatch}/{@code pexCtx}); matched policy rows
 * from {@link Enforcer#enforceEx} drive both {@link #check} and
 * {@link #explain}. Ternary ALLOW/DENY/UNDEFINED, precedence, and conflict
 * resolution are applied to that Casbin-matched set.
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
    private final PexCollectingEffector effector = new PexCollectingEffector();
    private final AtomicLong revision = new AtomicLong();
    private final ConcurrentHashMap<String, Integer> syncedHashes = new ConcurrentHashMap<>();
    private final Object policyLock = new Object();
    private volatile int compiledPolicies;

    public CasbinPermissionEngine(ResolutionSupport support) {
        this.support = Objects.requireNonNull(support, "support");

        var model = Model.newModelFromString(MODEL);
        this.enforcer = new Enforcer(model);
        this.enforcer.addFunction("pexMatch", new PexMatchFunction(support));
        this.enforcer.addFunction("pexCtx", new PexContextFunction());
        this.enforcer.setEffector(effector);
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
        invalidate();
    }

    @Override
    public void invalidate() {
        syncedHashes.clear();

        synchronized (policyLock) {
            enforcer.clearPolicy();
            compiledPolicies = 0;
        }

        revision.incrementAndGet();
    }

    @Override
    public void invalidateSubject(String subjectKey) {
        Objects.requireNonNull(subjectKey, "subjectKey");

        syncedHashes.remove(subjectKey);

        synchronized (policyLock) {
            enforcer.removeFilteredPolicy(0, subjectKey);
            compiledPolicies = enforcer.getPolicy().size();
        }

        // Do not bump the global revision — callers use subject-scoped decision cache keys.
    }

    @Override
    public long revision() {
        return revision.get();
    }

    private PermissionResolution evaluate(PermissionHolder holder, String permission, QueryOptions options) {
        Objects.requireNonNull(holder, "holder");
        Objects.requireNonNull(permission, "permission");
        Objects.requireNonNull(options, "options");

        var subject = ResolutionSupport.subjectKey(holder);
        var normalized = support.normalizePermission(permission);
        // Always compile from the live domain graph; cache only the Casbin sync hash
        // so unchanged subjects skip removeFilteredPolicy/addPolicy on the hot path.
        var compiled = List.copyOf(support.compile(holder, options));
        var encodedContexts = ResolutionSupport.encodeContexts(options.contexts());

        ensureSynced(subject, compiled);

        var collector = effector.begin();
        List<ResolutionSupport.CompiledPermission> matches;

        try {
            synchronized (policyLock) {
                EnforceResult casbinResult;

                try {
                    casbinResult = enforcer.enforceEx(subject, normalized, encodedContexts);
                } catch (RuntimeException error) {
                    throw new IllegalStateException(
                            "Casbin permission evaluation failed for " + subject + " / " + normalized, error);
                }

                matches = mapCasbinMatches(collector.matchedIndexes(), compiled, casbinResult, enforcer.getPolicy());
            }
        } finally {
            effector.end();
        }

        return resolve(normalized, matches, holder, options);
    }

    private void ensureSynced(String subject, List<ResolutionSupport.CompiledPermission> compiled) {
        var hash = compiled.hashCode();
        var current = syncedHashes.get(subject);

        if (current != null && current == hash) {
            return;
        }

        synchronized (policyLock) {
            current = syncedHashes.get(subject);
            if (current != null && current == hash) {
                return;
            }

            syncSubject(subject, compiled);
            syncedHashes.put(subject, hash);
        }
    }

    private List<ResolutionSupport.CompiledPermission> mapCasbinMatches(
            List<Integer> matchedIndexes,
            List<ResolutionSupport.CompiledPermission> compiled,
            EnforceResult casbinResult,
            List<List<String>> policies) {

        var matches = new ArrayList<ResolutionSupport.CompiledPermission>();

        for (var index : matchedIndexes) {
            if (index < 0 || index >= policies.size()) {
                continue;
            }

            var match = findCompiled(compiled, policies.get(index));

            if (match != null) {
                matches.add(match);
            }
        }

        // Fallback: if the collector missed rows but Casbin explained one, map it.
        if (matches.isEmpty() && casbinResult != null && casbinResult.getExplain() != null && casbinResult.getExplain().size() >= 7) {
            var match = findCompiled(compiled, casbinResult.getExplain());
            if (match != null) {
                matches.add(match);
            }
        }

        return matches;
    }

    private static ResolutionSupport.CompiledPermission findCompiled(
            List<ResolutionSupport.CompiledPermission> compiled,
            List<String> row) {

        // p = sub, obj, eft, specificity, distance, weight, ctx
        if (row.size() < 7) {
            return null;
        }

        var expression = row.get(1);
        var effect = "allow".equals(row.get(2)) ? PermissionResult.ALLOW : PermissionResult.DENY;
        var specificity = Integer.parseInt(row.get(3));
        var distance = Integer.parseInt(row.get(4));
        var weight = Integer.parseInt(row.get(5));
        var contexts = row.get(6);

        for (var policy : compiled) {
            if (policy.expression().equals(expression)
                    && policy.effect() == effect
                    && policy.specificity() == specificity
                    && policy.distance() == distance
                    && policy.weight() == weight
                    && policy.encodedContexts().equals(contexts)) {
                return policy;
            }
        }

        return null;
    }

    private PermissionResolution resolve(
            String permission,
            List<ResolutionSupport.CompiledPermission> matches,
            PermissionHolder holder,
            QueryOptions options) {

        var candidates = diagnosticCandidates(holder, permission, options, matches);
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

    private List<EngineCandidate> diagnosticCandidates(
            PermissionHolder holder,
            String permission,
            QueryOptions options,
            List<ResolutionSupport.CompiledPermission> matches) {

        var matchedNodes = matches.stream().map(ResolutionSupport.CompiledPermission::node).collect(Collectors.toSet());
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
                } else if (matchedNodes.contains(node)) {
                    // Casbin matched this node — eligible for WINNER/OUTRANKED/CONFLICT.
                    status = CandidateStatus.OUTRANKED;
                } else {
                    // Eligible in the domain graph but Casbin did not match (should be rare).
                    status = CandidateStatus.PERMISSION_MISMATCH;
                    detail = "not matched by Casbin";
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
