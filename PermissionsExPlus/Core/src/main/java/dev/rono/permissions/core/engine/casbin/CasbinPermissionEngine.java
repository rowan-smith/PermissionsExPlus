package dev.rono.permissions.core.engine.casbin;

import dev.rono.permissions.api.permission.PermissionHolder;
import dev.rono.permissions.api.permission.PermissionResult;
import dev.rono.permissions.api.resolver.QueryOptions;
import dev.rono.permissions.core.engine.PermissionEngine;
import dev.rono.permissions.core.resolver.ResolutionSupport;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.casbin.jcasbin.main.Enforcer;
import org.casbin.jcasbin.model.Model;

/**
 * jCasbin-backed permission engine.
 *
 * <p>
 * PermissionsExPlus domain objects are compiled into an in-memory Casbin policy
 * set. Matching and ternary ALLOW/DENY/UNDEFINED decisions preserve existing
 * PermissionsExPlus precedence (exact over wildcard, context specificity,
 * inheritance distance, weight, conflict resolution). jCasbin types never leave
 * this package.
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
                if (support.matches(policy.expression(), normalized) && ResolutionSupport.applies(policy.contexts(), options.contexts())) {
                    matches.add(policy);
                }
            }

            var decided = support.decide(normalized, matches);

            // Keep Casbin's enforce path warm and fail visibly on engine errors.
            // Ternary semantics (especially STRICT / UNDEFINED) remain owned by
            // PermissionsExPlus decide(), because Casbin's boolean effect cannot
            // express UNDEFINED.
            try {
                enforcer.enforceEx(subject, normalized, ResolutionSupport.encodeContexts(options.contexts()));
            } catch (RuntimeException error) {
                throw new IllegalStateException("Casbin permission evaluation failed for " + subject + " / " + normalized, error);
            }

            return decided;
        }
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
}
