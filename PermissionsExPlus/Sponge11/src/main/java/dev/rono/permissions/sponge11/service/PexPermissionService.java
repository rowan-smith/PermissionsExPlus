package dev.rono.permissions.sponge11.service;

import dev.rono.permissions.core.PexApiImpl;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectData;
import org.spongepowered.api.service.permission.SubjectReference;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.plugin.PluginContainer;

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

    PexSubjectCollection collectionFor(String identifier) {
        return collections.computeIfAbsent(identifier.toLowerCase(Locale.ROOT), id -> new PexSubjectCollection.Memory(this, id));
    }

    @Override
    public PexSubjectCollection userSubjects() {
        return users;
    }

    @Override
    public PexSubjectCollection groupSubjects() {
        return groups;
    }

    @Override
    public Subject defaults() {
        return defaults.get(GLOBAL_DEFAULT);
    }

    @Override
    public Predicate<String> identifierValidityPredicate() {
        return value -> value != null && !value.trim().isEmpty();
    }

    @Override
    public CompletableFuture<PexSubjectCollection> loadCollection(String identifier) {
        return CompletableFuture.completedFuture(collectionFor(identifier));
    }

    @Override
    public Optional<PexSubjectCollection> collection(String identifier) {
        return Optional.ofNullable(collections.get(identifier.toLowerCase(Locale.ROOT)));
    }

    @Override
    public CompletableFuture<Boolean> hasCollection(String identifier) {
        return CompletableFuture.completedFuture(collections.containsKey(identifier.toLowerCase(Locale.ROOT)));
    }

    @Override
    public Map<String, PexSubjectCollection> loadedCollections() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(collections));
    }

    @Override
    public CompletableFuture<Set<String>> allIdentifiers() {
        return CompletableFuture.completedFuture((Set<String>) new LinkedHashSet<>(collections.keySet()));
    }

    @Override
    public SubjectReference newSubjectReference(String collectionIdentifier, String subjectIdentifier) {
        return new PexSubjectReference(this, collectionIdentifier, subjectIdentifier);
    }

    @Override
    public PermissionDescription.Builder newDescriptionBuilder(PluginContainer plugin) {
        return new DescriptionBuilder(plugin);
    }

    @Override
    public Optional<PermissionDescription> description(String permission) {
        return Optional.ofNullable(descriptions.get(permission.toLowerCase(Locale.ROOT)));
    }

    @Override
    public Collection<PermissionDescription> descriptions() {
        return Collections.unmodifiableCollection(descriptions.values());
    }

    private final class DescriptionBuilder implements PermissionDescription.Builder {
        private final PluginContainer owner;
        private String id;
        private Component description;
        private Tristate defaultValue = Tristate.UNDEFINED;
        private final Map<String, Boolean> roles = new LinkedHashMap<>();

        DescriptionBuilder(PluginContainer owner) {
            this.owner = owner;
        }

        @Override
        public PermissionDescription.Builder id(String permissionId) {
            this.id = permissionId;
            return this;
        }

        @Override
        public PermissionDescription.Builder description(Component description) {
            this.description = description;
            return this;
        }

        @Override
        public PermissionDescription.Builder assign(String role, boolean value) {
            roles.put(role, value);
            return this;
        }

        @Override
        public PermissionDescription.Builder defaultValue(Tristate defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        @Override
        public PermissionDescription register() {
            if (id == null) {
                throw new IllegalStateException("Permission description needs an id");
            }

            // Role templates are plain in-memory subjects, as Sponge specifies.
            roles.forEach((role, value) -> collectionFor(SUBJECTS_ROLE_TEMPLATE).loadSubject(role)
                    .thenAccept(subject -> subject.transientSubjectData().setPermission(SubjectData.GLOBAL_CONTEXT, id, Tristate.fromBoolean(value))));

            if (defaultValue != Tristate.UNDEFINED) {
                defaults().transientSubjectData().setPermission(SubjectData.GLOBAL_CONTEXT, id, defaultValue);
            }

            PermissionDescription registered = new Description(id, description, owner, defaultValue);

            descriptions.put(id.toLowerCase(Locale.ROOT), registered);

            return registered;
        }
    }

    private final class Description implements PermissionDescription {
        private final String id;
        private final Component description;
        private final PluginContainer owner;
        private final Tristate defaultValue;

        Description(String id, Component description, PluginContainer owner, Tristate defaultValue) {
            this.id = id;
            this.description = description;
            this.owner = owner;
            this.defaultValue = defaultValue;
        }

        @Override
        public String id() {
            return id;
        }

        @Override
        public Optional<Component> description() {
            return Optional.ofNullable(description);
        }

        @Override
        public Optional<PluginContainer> owner() {
            return Optional.ofNullable(owner);
        }

        @Override
        public Tristate defaultValue() {
            return defaultValue;
        }

        @Override
        public CompletableFuture<Map<SubjectReference, Boolean>> findAssignedSubjects(String collectionIdentifier) {
            return collectionFor(collectionIdentifier).allWithPermission(id);
        }

        @Override
        public Map<PexSubject, Boolean> assignedSubjects(String collectionIdentifier) {
            return collectionFor(collectionIdentifier).loadedWithPermission(id);
        }

        @Override
        public boolean query(Subject subject) {
            return subject.hasPermission(id);
        }

        @Override
        public boolean query(Subject subject, ResourceKey key) {
            return query(subject, key.namespace(), key.value());
        }

        @Override
        public boolean query(Subject subject, String... parameters) {
            // Templated ids use "<...>" placeholders; substitute in order.
            String permission = id;

            for (String parameter : parameters) {
                permission = permission.replaceFirst("<[^>]+>", java.util.regex.Matcher.quoteReplacement(parameter));
            }

            return subject.hasPermission(permission);
        }

        @Override
        public boolean query(Subject subject, String parameter) {
            return query(subject, new String[]{parameter});
        }
    }
}
