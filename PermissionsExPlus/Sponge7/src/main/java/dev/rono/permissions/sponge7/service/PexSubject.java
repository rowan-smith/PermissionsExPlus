package dev.rono.permissions.sponge7.service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectCollection;
import org.spongepowered.api.service.permission.SubjectData;
import org.spongepowered.api.service.permission.SubjectReference;
import org.spongepowered.api.util.Tristate;

/**
 * Common Sponge subject behaviour. Lookups follow the order Sponge specifies:
 * transient data, then persistent data, then the collection's defaults, then
 * the
 * service-wide defaults.
 */
abstract class PexSubject implements Subject {
    protected final PexPermissionService service;
    private final PexSubjectCollection collection;
    private final String identifier;
    private final MemorySubjectData transientData = new MemorySubjectData();

    PexSubject(PexPermissionService service, PexSubjectCollection collection, String identifier) {
        this.service = service;
        this.collection = collection;
        this.identifier = identifier;
    }

    /** Value from the persistent (PermissionsExPlus) data, without defaults. */
    protected abstract Tristate persistentValue(Set<Context> contexts, String permission);

    protected abstract List<SubjectReference> persistentParents(Set<Context> contexts);

    protected abstract Optional<String> persistentOption(Set<Context> contexts, String key);

    @Override
    public final String getIdentifier() {
        return identifier;
    }

    @Override
    public Optional<CommandSource> getCommandSource() {
        return Optional.empty();
    }

    @Override
    public final SubjectCollection getContainingCollection() {
        return collection;
    }

    @Override
    public final SubjectReference asSubjectReference() {
        return service.newSubjectReference(collection.getIdentifier(), identifier);
    }

    @Override
    public final SubjectData getTransientSubjectData() {
        return transientData;
    }

    @Override
    public Set<Context> getActiveContexts() {
        return service.activeContexts(this);
    }

    private boolean isDefaults() {
        return collection == service.defaultCollection();
    }

    @Override
    public final Tristate getPermissionValue(Set<Context> contexts, String permission) {
        Tristate value = transientData.permissionValue(contexts, permission);

        if (value == Tristate.UNDEFINED) {
            value = persistentValue(contexts, permission);
        }

        if (value == Tristate.UNDEFINED && !isDefaults()) {
            value = collection.getDefaults().getPermissionValue(contexts, permission);

            if (value == Tristate.UNDEFINED) {
                value = service.getDefaults().getPermissionValue(contexts, permission);
            }
        }

        return value;
    }

    @Override
    public final boolean isChildOf(Set<Context> contexts, SubjectReference parent) {
        return getParents(contexts).contains(parent);
    }

    @Override
    public final List<SubjectReference> getParents(Set<Context> contexts) {
        Set<SubjectReference> parents = new LinkedHashSet<>(transientData.activeParents(contexts));

        parents.addAll(persistentParents(contexts));

        return new ArrayList<>(parents);
    }

    @Override
    public final Optional<String> getOption(Set<Context> contexts, String key) {
        String value = transientData.option(contexts, key);

        if (value != null) {
            return Optional.of(value);
        }

        Optional<String> persistent = persistentOption(contexts, key);

        if (persistent.isPresent() || isDefaults()) {
            return persistent;
        }

        Optional<String> fallback = collection.getDefaults().getOption(contexts, key);

        return fallback.isPresent() ? fallback : service.getDefaults().getOption(contexts, key);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" + collection.getIdentifier() + ":" + identifier + "}";
    }
}
