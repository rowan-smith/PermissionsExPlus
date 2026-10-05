package dev.rono.permissions.core.engine;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import dev.rono.permissions.api.group.Group;
import dev.rono.permissions.api.permission.PermissionHolder;
import dev.rono.permissions.api.permission.PermissionResult;
import dev.rono.permissions.api.resolver.PermissionResolution;
import dev.rono.permissions.api.resolver.QueryOptions;
import dev.rono.permissions.api.user.User;
import dev.rono.permissions.core.resolver.ResolutionSupport;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * Revision-aware permission decision cache.
 *
 * <p>
 * Cache keys include global and per-subject revisions so full invalidation and
 * subject-scoped invalidation both discard stale decisions without flushing
 * unrelated holders. Context order does not affect identity. Entries
 * contributed by timed policy expire at or before the earliest relevant expiry.
 * </p>
 */
public final class CachedPermissionEngine implements PermissionEngine {
    private static final long DEFAULT_TTL_NANOS = TimeUnit.MINUTES.toNanos(30);

    private final PermissionEngine delegate;
    private final AtomicLong revision = new AtomicLong();
    private final ConcurrentHashMap<String, AtomicLong> subjectRevisions = new ConcurrentHashMap<>();
    private final Cache<PermissionCacheKey, CachedDecision> cache;
    private final LongAdder hits = new LongAdder();
    private final LongAdder misses = new LongAdder();

    public CachedPermissionEngine(PermissionEngine delegate) {
        this(delegate, 100_000);
    }

    public CachedPermissionEngine(PermissionEngine delegate, long maximumSize) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
        this.cache = Caffeine.newBuilder()
                .maximumSize(Math.max(1_000L, maximumSize))
                .expireAfter(new Expiry<PermissionCacheKey, CachedDecision>() {
                    @Override
                    public long expireAfterCreate(PermissionCacheKey key, CachedDecision value, long currentTime) {
                        return value.ttlNanos();
                    }

                    @Override
                    public long expireAfterUpdate(PermissionCacheKey key, CachedDecision value, long currentTime, long currentDuration) {
                        return value.ttlNanos();
                    }

                    @Override
                    public long expireAfterRead(PermissionCacheKey key, CachedDecision value, long currentTime, long currentDuration) {
                        return currentDuration;
                    }
                })
                .build();
    }

    public PermissionEngine delegate() {
        return delegate;
    }

    @Override
    public String id() {
        return "cached:" + delegate.id();
    }

    @Override
    public PermissionResult check(PermissionHolder holder, String permission, QueryOptions options) {
        Objects.requireNonNull(holder, "holder");
        Objects.requireNonNull(permission, "permission");
        Objects.requireNonNull(options, "options");

        var key = key(holder, permission, options);
        var cached = cache.getIfPresent(key);

        if (cached != null) {
            hits.increment();
            return cached.result();
        }

        misses.increment();

        var result = delegate.check(holder, permission, options);
        cache.put(key, new CachedDecision(result, ttlNanos(holder, options)));
        return result;
    }

    @Override
    public PermissionResolution explain(PermissionHolder holder, String permission, QueryOptions options) {
        return delegate.explain(holder, permission, options);
    }

    @Override
    public Optional<Instant> earliestPolicyExpiry(PermissionHolder holder, QueryOptions options) {
        return delegate.earliestPolicyExpiry(holder, options);
    }

    @Override
    public void rebuild() {
        delegate.rebuild();
        revision.incrementAndGet();
        subjectRevisions.clear();
        cache.invalidateAll();
    }

    @Override
    public void invalidate() {
        delegate.invalidate();
        revision.incrementAndGet();
        subjectRevisions.clear();
        cache.invalidateAll();
    }

    @Override
    public void invalidateSubject(String subjectKey) {
        Objects.requireNonNull(subjectKey, "subjectKey");

        delegate.invalidateSubject(subjectKey);
        subjectRevisions.computeIfAbsent(subjectKey, ignored -> new AtomicLong()).incrementAndGet();
        cache.asMap().keySet().removeIf(key -> key.holder().equals(subjectKey));
    }

    @Override
    public long revision() {
        return revision.get();
    }

    public long hitCount() {
        return hits.sum();
    }

    public long missCount() {
        return misses.sum();
    }

    public long estimatedSize() {
        return cache.estimatedSize();
    }

    private long ttlNanos(PermissionHolder holder, QueryOptions options) {
        return earliestPolicyExpiry(holder, options)
                .map(expiry -> Math.max(1L, Duration.between(Instant.now(), expiry).toNanos()))
                .orElse(DEFAULT_TTL_NANOS);
    }

    private PermissionCacheKey key(PermissionHolder holder, String permission, QueryOptions options) {
        var holderKey = holderKey(holder);
        return new PermissionCacheKey(
                holderKey,
                permission,
                ResolutionSupport.encodeContexts(options.contexts()),
                options.includeInheritance(),
                options.includeDefaults(),
                revision.get() + ":" + subjectRevision(holderKey));
    }

    private long subjectRevision(String holderKey) {
        var counter = subjectRevisions.get(holderKey);
        return counter == null ? 0L : counter.get();
    }

    private static String holderKey(PermissionHolder holder) {
        if (holder instanceof User user) {
            return "user:" + user.uniqueId();
        }

        if (holder instanceof Group group) {
            return "group:" + group.name();
        }

        return ResolutionSupport.subjectKey(holder);
    }

    private record PermissionCacheKey(
            String holder,
            String permission,
            String contexts,
            boolean includeInheritance,
            boolean includeDefaults,
            String revision) {}

    private record CachedDecision(PermissionResult result, long ttlNanos) {}
}
