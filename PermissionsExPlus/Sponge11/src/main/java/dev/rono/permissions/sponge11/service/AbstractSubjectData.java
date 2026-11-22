package dev.rono.permissions.sponge11.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectData;
import org.spongepowered.api.service.permission.SubjectReference;
import org.spongepowered.api.service.permission.TransferMethod;
import org.spongepowered.api.util.Tristate;

/**
 * Bulk operations (set/copy/move with a {@link TransferMethod}) built from the
 * single-entry operations, plus fallback permission values. PermissionsExPlus
 * has
 * no fallback concept, so fallbacks are held in memory for every subject.
 */
abstract class AbstractSubjectData implements SubjectData {
    private final Supplier<Subject> subject;
    private final boolean transientData;
    private final Map<Set<Context>, Tristate> fallbacks = new ConcurrentHashMap<>();

    AbstractSubjectData(Supplier<Subject> subject, boolean transientData) {
        this.subject = subject;
        this.transientData = transientData;
    }

    static Set<Context> key(Set<Context> contexts) {
        return Collections.unmodifiableSet(new HashSet<>(contexts));
    }

    static CompletableFuture<Boolean> all(List<CompletableFuture<Boolean>> futures) {
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0])).thenApply(ignored -> {
            boolean changed = false;

            for (CompletableFuture<Boolean> future : futures) {
                changed |= future.join();
            }

            return changed;
        });
    }

    /** Most specific fallback among entries whose contexts are all active. */
    Tristate activeFallback(Set<Context> active) {
        Tristate result = Tristate.UNDEFINED;
        int best = -1;

        for (Map.Entry<Set<Context>, Tristate> entry : fallbacks.entrySet()) {
            if (active.containsAll(entry.getKey()) && entry.getKey().size() > best) {
                best = entry.getKey().size();
                result = entry.getValue();
            }
        }

        return result;
    }

    @Override
    public final Subject subject() {
        return subject.get();
    }

    @Override
    public final boolean isTransient() {
        return transientData;
    }

    @Override
    public final Tristate fallbackPermissionValue(Set<Context> contexts) {
        Tristate value = fallbacks.get(key(contexts));
        return value == null ? Tristate.UNDEFINED : value;
    }

    @Override
    public final Map<Set<Context>, Tristate> allFallbackPermissionValues() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(fallbacks));
    }

    @Override
    public final CompletableFuture<Boolean> setFallbackPermissionValue(Set<Context> contexts, Tristate fallback) {
        Tristate previous = fallback == Tristate.UNDEFINED ? fallbacks.remove(key(contexts)) : fallbacks.put(key(contexts), fallback);
        return CompletableFuture.completedFuture(previous != fallback);
    }

    @Override
    public final CompletableFuture<Boolean> clearFallbackPermissionValues() {
        boolean changed = !fallbacks.isEmpty();
        fallbacks.clear();
        return CompletableFuture.completedFuture(changed);
    }

    @Override
    public final CompletableFuture<Boolean> setPermissions(Set<Context> contexts, Map<String, Boolean> values, TransferMethod method) {
        CompletableFuture<Boolean> start = method == TransferMethod.OVERWRITE ? clearPermissions(contexts) : CompletableFuture.completedFuture(false);

        return start.thenCompose(cleared -> {
            List<CompletableFuture<Boolean>> futures = new ArrayList<>();
            values.forEach((permission, value) -> futures.add(setPermission(contexts, permission, Tristate.fromBoolean(value))));
            return all(futures).thenApply(changed -> changed || cleared);
        });
    }

    @Override
    public final CompletableFuture<Boolean> setParents(Set<Context> contexts, List<? extends SubjectReference> parents, TransferMethod method) {
        CompletableFuture<Boolean> start = method == TransferMethod.OVERWRITE ? clearParents(contexts) : CompletableFuture.completedFuture(false);

        return start.thenCompose(cleared -> {
            List<CompletableFuture<Boolean>> futures = new ArrayList<>();
            for (SubjectReference parent : parents) {
                futures.add(addParent(contexts, parent));
            }

            return all(futures).thenApply(changed -> changed || cleared);
        });
    }

    @Override
    public final CompletableFuture<Boolean> setOptions(Set<Context> contexts, Map<String, String> options, TransferMethod method) {
        CompletableFuture<Boolean> start = method == TransferMethod.OVERWRITE ? clearOptions(contexts) : CompletableFuture.completedFuture(false);

        return start.thenCompose(cleared -> {
            List<CompletableFuture<Boolean>> futures = new ArrayList<>();
            options.forEach((key, value) -> futures.add(setOption(contexts, key, value)));
            return all(futures).thenApply(changed -> changed || cleared);
        });
    }

    @Override
    public final CompletableFuture<Boolean> copyFrom(SubjectData other, TransferMethod method) {
        CompletableFuture<Boolean> start = method == TransferMethod.OVERWRITE
                ? all(java.util.Arrays.asList(clearPermissions(), clearParents(), clearOptions()))
                : CompletableFuture.completedFuture(false);

        return start.thenCompose(cleared -> {
            List<CompletableFuture<Boolean>> futures = new ArrayList<>();

            other.allPermissions().forEach((contexts, values) -> futures.add(setPermissions(contexts, values, TransferMethod.MERGE)));
            other.allParents().forEach((contexts, values) -> futures.add(setParents(contexts, values, TransferMethod.MERGE)));
            other.allOptions().forEach((contexts, values) -> futures.add(setOptions(contexts, values, TransferMethod.MERGE)));
            other.allFallbackPermissionValues().forEach((contexts, value) -> futures.add(setFallbackPermissionValue(contexts, value)));

            return all(futures).thenApply(changed -> changed || cleared);
        });
    }

    @Override
    public final CompletableFuture<Boolean> moveFrom(SubjectData other, TransferMethod method) {
        return copyFrom(other, method).thenCompose(changed -> all(java.util.Arrays.asList(
                other.clearPermissions(), other.clearParents(), other.clearOptions(), other.clearFallbackPermissionValues()))
                .thenApply(cleared -> changed || cleared));
    }
}
