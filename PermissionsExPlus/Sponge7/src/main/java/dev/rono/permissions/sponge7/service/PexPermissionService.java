package dev.rono.permissions.sponge7.service;

import dev.rono.permissions.core.PexApiImpl;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.context.ContextCalculator;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectCollection;
import org.spongepowered.api.service.permission.SubjectReference;
import org.spongepowered.api.text.Text;

/**
 * Sponge permission service answered by PermissionsExPlus. Users and groups are
 * PermissionsExPlus data; every other collection Sponge or plugins ask for is
 * held
 * in memory.
 */
public final class PexPermissionService implements PermissionService {
    /** Identifier of the global default subject within the defaults collection. */
    private static final String GLOBAL_DEFAULT = "default";

    private final PexApiImpl<?> core;
    private final PexSubjectCollection.Users users;
    private final PexSubjectCollection.Groups groups;
    private final PexSubjectCollection.Memory defaults;
    private final Map<String, PexSubjectCollection> collections = new ConcurrentHashMap<>();
    private final List<ContextCalculator<Subject>> calculators = new CopyOnWriteArrayList<>();
    private final Map<String, PermissionDescription> descriptions = new ConcurrentHashMap<>();

    public PexPermissionService(PexApiImpl<?> core) {
        this.core = core;
        this.users = new PexSubjectCollection.Users(this);
        this.groups = new PexSubjectCollection.Groups(this);
        this.defaults = new PexSubjectCollection.Memory(this, SUBJECTS_DEFAULT);

        collections.put(SUBJECTS_USER, users);
        collections.put(SUBJECTS_GROUP, groups);
        collections.put(SUBJECTS_DEFAULT, defaults);
    }

    PexApiImpl<?> core() {
        return core;
    }

    PexSubjectCollection.Users users() {
        return users;
    }

    PexSubjectCollection.Groups groups() {
        return groups;
    }

    PexSubjectCollection.Memory defaultCollection() {
        return defaults;
    }

    /**
     * Active contexts: PermissionsExPlus runtime contexts plus registered
     * calculators.
     */
    Set<Context> activeContexts(Subject subject) {
        Set<Context> result = new LinkedHashSet<>();

        if (subject.getContainingCollection() == users) {
            result.addAll(Contexts.toSponge(core.contexts().contexts(UUID.fromString(subject.getIdentifier()))));
        }

        for (ContextCalculator<Subject> calculator : calculators) {
            calculator.accumulateContexts(subject, result);
        }

        return result;
    }

    @Override
    public SubjectCollection getUserSubjects() {
        return users;
    }

    @Override
    public SubjectCollection getGroupSubjects() {
        return groups;
    }

    @Override
    public Subject getDefaults() {
        return defaults.subject(GLOBAL_DEFAULT);
    }

    @Override
    public Predicate<String> getIdentifierValidityPredicate() {
        return value -> value != null && !value.trim().isEmpty();
    }

    private PexSubjectCollection collection(String identifier) {
        return collections.computeIfAbsent(identifier.toLowerCase(java.util.Locale.ROOT), id -> new PexSubjectCollection.Memory(this, id));
    }

    @Override
    public CompletableFuture<SubjectCollection> loadCollection(String identifier) {
        return CompletableFuture.<SubjectCollection>completedFuture(collection(identifier));
    }

    @Override
    public Optional<SubjectCollection> getCollection(String identifier) {
        return Optional.<SubjectCollection>ofNullable(collections.get(identifier.toLowerCase(java.util.Locale.ROOT)));
    }

    @Override
    public CompletableFuture<Boolean> hasCollection(String identifier) {
        return CompletableFuture.completedFuture(collections.containsKey(identifier.toLowerCase(java.util.Locale.ROOT)));
    }

    @Override
    public Map<String, SubjectCollection> getLoadedCollections() {
        return Collections.<String, SubjectCollection>unmodifiableMap(new LinkedHashMap<>(collections));
    }

    @Override
    public CompletableFuture<Set<String>> getAllIdentifiers() {
        return CompletableFuture.completedFuture((Set<String>) new LinkedHashSet<>(collections.keySet()));
    }

    @Override
    public SubjectReference newSubjectReference(String collectionIdentifier, String subjectIdentifier) {
        return new PexSubjectReference(this, collectionIdentifier, subjectIdentifier);
    }

    @Override
    public PermissionDescription.Builder newDescriptionBuilder(Object owner) {
        return new DescriptionBuilder(owner);
    }

    @Override
    public Optional<PermissionDescription> getDescription(String permission) {
        return Optional.ofNullable(descriptions.get(permission.toLowerCase(java.util.Locale.ROOT)));
    }

    @Override
    public Collection<PermissionDescription> getDescriptions() {
        return Collections.unmodifiableCollection(descriptions.values());
    }

    @Override
    public void registerContextCalculator(ContextCalculator<Subject> calculator) {
        calculators.add(calculator);
    }

    private final class DescriptionBuilder implements PermissionDescription.Builder {
        private final Object owner;
        private String id;
        private Text description;
        private final Map<String, Boolean> roles = new LinkedHashMap<>();

        DescriptionBuilder(Object owner) {
            this.owner = owner;
        }

        @Override
        public PermissionDescription.Builder id(String permissionId) {
            this.id = permissionId;
            return this;
        }

        @Override
        public PermissionDescription.Builder description(Text description) {
            this.description = description;
            return this;
        }

        @Override
        public PermissionDescription.Builder assign(String role, boolean value) {
            roles.put(role, value);
            return this;
        }

        @Override
        public PermissionDescription register() {
            if (id == null) {
                throw new IllegalStateException("Permission description needs an id");
            }

            // Role templates are plain in-memory subjects, as Sponge specifies.
            roles.forEach((role, value) -> collection(SUBJECTS_ROLE_TEMPLATE).loadSubject(role)
                    .thenAccept(subject -> subject.getTransientSubjectData().setPermission(org.spongepowered.api.service.permission.SubjectData.GLOBAL_CONTEXT, id, org.spongepowered.api.util.Tristate.fromBoolean(value))));

            PermissionDescription registered = new Description(id, description, owner);

            descriptions.put(id.toLowerCase(java.util.Locale.ROOT), registered);

            return registered;
        }
    }

    private final class Description implements PermissionDescription {
        private final String id;
        private final Text description;
        private final Object owner;

        Description(String id, Text description, Object owner) {
            this.id = id;
            this.description = description;
            this.owner = owner;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public Optional<Text> getDescription() {
            return Optional.ofNullable(description);
        }

        @Override
        public Optional<PluginContainer> getOwner() {
            if (owner instanceof PluginContainer) {
                return Optional.of((PluginContainer) owner);
            }

            return owner == null ? Optional.<PluginContainer>empty() : org.spongepowered.api.Sponge.getPluginManager().fromInstance(owner);
        }

        @Override
        public CompletableFuture<Map<SubjectReference, Boolean>> findAssignedSubjects(String collectionIdentifier) {
            return collection(collectionIdentifier).getAllWithPermission(id);
        }

        @Override
        public Map<Subject, Boolean> getAssignedSubjects(String collectionIdentifier) {
            return collection(collectionIdentifier).getLoadedWithPermission(id);
        }
    }
}
