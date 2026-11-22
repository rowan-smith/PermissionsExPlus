package dev.rono.permissions.sponge9.service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectCollection;
import org.spongepowered.api.service.permission.SubjectReference;
import org.spongepowered.api.util.Tristate;

/**
 * Common Sponge subject behaviour. Lookups follow the order Sponge specifies:
 * transient data, persistent data, their fallback values, then the collection's
 * defaults, then the service-wide defaults.
 */
abstract class PexSubject implements Subject {
    protected final PexPermissionService service;
    private final PexSubjectCollection collection;
    private final String identifier;
    private final MemorySubjectData transientData = new MemorySubjectData(() -> this, true);

    PexSubject(PexPermissionService service, PexSubjectCollection collection, String identifier) {
        this.service = service;
        this.collection = collection;
        this.identifier = identifier;
    }

    /** Value from the persistent data, without fallbacks or defaults. */
    protected abstract Tristate persistentValue(String permission, Set<Context> contexts);

    protected abstract List<SubjectReference> persistentParents(Set<Context> contexts);

    protected abstract Optional<String> persistentOption(String key, Set<Context> contexts);

    @Override
    public abstract AbstractSubjectData subjectData();

    @Override
    public final String identifier() {
        return identifier;
    }

    @Override
    public Optional<?> associatedObject() {
        return Optional.empty();
    }

    @Override
    public final SubjectCollection containingCollection() {
        return collection;
    }

    @Override
    public final SubjectReference asSubjectReference() {
        return service.newSubjectReference(collection.identifier(), identifier);
    }

    @Override
    public final MemorySubjectData transientSubjectData() {
        return transientData;
    }

    private boolean isDefaults() {
        return collection == service.defaultCollection();
    }

    @Override
    public final Tristate permissionValue(String permission, Cause cause) {
        return permissionValue(permission, Contexts.of(cause));
    }

    @Override
    public final Tristate permissionValue(String permission, Set<Context> contexts) {
        Tristate value = transientData.permissionValue(contexts, permission);

        if (value == Tristate.UNDEFINED) {
            value = persistentValue(permission, contexts);
        }

        if (value == Tristate.UNDEFINED) {
            value = transientData.activeFallback(contexts);
        }

        if (value == Tristate.UNDEFINED) {
            value = subjectData().activeFallback(contexts);
        }

        if (value == Tristate.UNDEFINED && !isDefaults()) {
            value = collection.defaults().permissionValue(permission, contexts);

            if (value == Tristate.UNDEFINED) {
                value = service.defaults().permissionValue(permission, contexts);
            }
        }

        return value;
    }

    @Override
    public final boolean isChildOf(SubjectReference parent, Cause cause) {
        return isChildOf(parent, Contexts.of(cause));
    }

    @Override
    public final boolean isChildOf(SubjectReference parent, Set<Context> contexts) {
        return parents(contexts).contains(parent);
    }

    @Override
    public final List<SubjectReference> parents(Cause cause) {
        return parents(Contexts.of(cause));
    }

    @Override
    public final List<SubjectReference> parents(Set<Context> contexts) {
        Set<SubjectReference> parents = new LinkedHashSet<>(transientData.activeParents(contexts));

        parents.addAll(persistentParents(contexts));

        return new ArrayList<>(parents);
    }

    @Override
    public final Optional<String> option(String key, Cause cause) {
        return option(key, Contexts.of(cause));
    }

    @Override
    public final Optional<String> option(String key, Set<Context> contexts) {
        String value = transientData.option(contexts, key);

        if (value != null) {
            return Optional.of(value);
        }

        Optional<String> persistent = persistentOption(key, contexts);

        if (persistent.isPresent() || isDefaults()) {
            return persistent;
        }

        Optional<String> fallback = collection.defaults().option(key, contexts);

        return fallback.isPresent() ? fallback : service.defaults().option(key, contexts);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" + collection.identifier() + ":" + identifier + "}";
    }
}
