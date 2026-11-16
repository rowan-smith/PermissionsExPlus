package dev.rono.permissions.sponge7.service;

import dev.rono.permissions.api.group.Group;
import dev.rono.permissions.api.util.Identifiers;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectCollection;
import org.spongepowered.api.service.permission.SubjectReference;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.util.Tristate;

abstract class PexSubjectCollection implements SubjectCollection {
    protected final PexPermissionService service;
    private final String identifier;
    protected final Map<String, PexSubject> loaded = new ConcurrentHashMap<>();

    PexSubjectCollection(PexPermissionService service, String identifier) {
        this.service = service;
        this.identifier = identifier;
    }

    protected static String key(String identifier) {
        return identifier.toLowerCase(java.util.Locale.ROOT);
    }

    @Override
    public final String getIdentifier() {
        return identifier;
    }

    @Override
    public Predicate<String> getIdentifierValidityPredicate() {
        return value -> value != null && !value.trim().isEmpty();
    }

    @Override
    public final Collection<Subject> getLoadedSubjects() {
        return Collections.<Subject>unmodifiableCollection(loaded.values());
    }

    @Override
    public final CompletableFuture<Map<String, Subject>> loadSubjects(Set<String> identifiers) {
        Map<String, CompletableFuture<Subject>> futures = new LinkedHashMap<>();

        for (String id : identifiers) {
            futures.put(id, loadSubject(id));
        }

        return CompletableFuture.allOf(futures.values().toArray(new CompletableFuture<?>[0])).thenApply(ignored -> {
            Map<String, Subject> result = new LinkedHashMap<>();
            futures.forEach((id, future) -> result.put(id, future.join()));
            return result;
        });
    }

    @Override
    public final SubjectReference newSubjectReference(String subjectIdentifier) {
        return service.newSubjectReference(identifier, subjectIdentifier);
    }

    @Override
    public final CompletableFuture<Map<SubjectReference, Boolean>> getAllWithPermission(String permission) {
        return getAllWithPermission(Collections.<Context>emptySet(), permission);
    }

    @Override
    public final CompletableFuture<Map<SubjectReference, Boolean>> getAllWithPermission(Set<Context> contexts, String permission) {
        Map<SubjectReference, Boolean> result = new LinkedHashMap<>();

        getLoadedWithPermission(contexts, permission).forEach((subject, value) -> result.put(subject.asSubjectReference(), value));

        return CompletableFuture.completedFuture(result);
    }

    @Override
    public final Map<Subject, Boolean> getLoadedWithPermission(String permission) {
        return getLoadedWithPermission(Collections.<Context>emptySet(), permission);
    }

    @Override
    public final Map<Subject, Boolean> getLoadedWithPermission(Set<Context> contexts, String permission) {
        Map<Subject, Boolean> result = new LinkedHashMap<>();

        for (PexSubject subject : loaded.values()) {
            Tristate value = subject.getPermissionValue(contexts, permission);

            if (value != Tristate.UNDEFINED) {
                result.put(subject, value.asBoolean());
            }
        }

        return result;
    }

    @Override
    public final Subject getDefaults() {
        return service.defaultCollection().subject(identifier);
    }

    @Override
    public void suggestUnload(String identifier) {
        loaded.remove(key(identifier));
    }

    /**
     * Collections PermissionsExPlus does not persist; subjects are created on
     * demand.
     */
    static final class Memory extends PexSubjectCollection {
        Memory(PexPermissionService service, String identifier) {
            super(service, identifier);
        }

        PexSubject subject(String id) {
            return loaded.computeIfAbsent(key(id), ignored -> new MemorySubject(service, this, id));
        }

        @Override
        public CompletableFuture<Subject> loadSubject(String id) {
            return CompletableFuture.completedFuture(subject(id));
        }

        @Override
        public Optional<Subject> getSubject(String id) {
            return Optional.of(subject(id));
        }

        @Override
        public CompletableFuture<Boolean> hasSubject(String id) {
            return CompletableFuture.completedFuture(loaded.containsKey(key(id)));
        }

        @Override
        public CompletableFuture<Set<String>> getAllIdentifiers() {
            return CompletableFuture.completedFuture((Set<String>) new LinkedHashSet<>(loaded.keySet()));
        }

        @Override
        public void suggestUnload(String identifier) {
            // Memory subjects hold their only copy of the data; keep them.
        }
    }

    /** {@code user}: PermissionsExPlus users, identified by UUID. */
    static final class Users extends PexSubjectCollection {
        Users(PexPermissionService service) {
            super(service, org.spongepowered.api.service.permission.PermissionService.SUBJECTS_USER);
        }

        @Override
        public Predicate<String> getIdentifierValidityPredicate() {
            return Users::uuid;
        }

        private static boolean uuid(String value) {
            try {
                UUID.fromString(value);
                return true;
            } catch (RuntimeException invalid) {
                return false;
            }
        }

        private PexSubject wrap(UUID id) {
            return loaded.computeIfAbsent(key(id.toString()), ignored -> new HolderSubject.UserSubject(service, this, id));
        }

        @Override
        public CompletableFuture<Subject> loadSubject(String identifier) {
            UUID id = UUID.fromString(identifier);

            if (service.core().users().cache().get(id).isPresent()) {
                return CompletableFuture.completedFuture(wrap(id));
            }

            return service.core().users().loadOrCreateUser(id, name(id)).toCompletableFuture().thenApply(ignored -> wrap(id));
        }

        /** Users need a unique name; use the account name when Sponge knows it. */
        private static String name(UUID id) {
            try {
                Optional<UserStorageService> storage = Sponge.getServiceManager().provide(UserStorageService.class);
                Optional<User> user = storage.isPresent() ? storage.get().get(id) : Optional.<User>empty();

                return user.isPresent() ? user.get().getName() : id.toString();
            } catch (RuntimeException unavailable) {
                return id.toString(); // Sponge not ready; the name is updated when the player joins.
            }
        }

        @Override
        public Optional<Subject> getSubject(String identifier) {
            if (!uuid(identifier)) {
                return Optional.empty();
            }

            UUID id = UUID.fromString(identifier);

            return service.core().users().cache().get(id).isPresent() ? Optional.<Subject>of(wrap(id)) : Optional.<Subject>empty();
        }

        @Override
        public CompletableFuture<Boolean> hasSubject(String identifier) {
            if (!uuid(identifier)) {
                return CompletableFuture.completedFuture(false);
            }

            UUID id = UUID.fromString(identifier);

            return service.core().users().storage().identifiers().toCompletableFuture().thenApply(ids -> ids.contains(id));
        }

        @Override
        public CompletableFuture<Set<String>> getAllIdentifiers() {
            return service.core().users().storage().identifiers().toCompletableFuture().thenApply(ids -> {
                Set<String> result = new LinkedHashSet<>();

                for (UUID id : ids) {
                    result.add(id.toString());
                }

                return result;
            });
        }
    }

    /** {@code group}: PermissionsExPlus groups, identified by name. */
    static final class Groups extends PexSubjectCollection {
        Groups(PexPermissionService service) {
            super(service, org.spongepowered.api.service.permission.PermissionService.SUBJECTS_GROUP);
        }

        private PexSubject wrap(String name) {
            String normalized = Identifiers.group(name);

            return loaded.computeIfAbsent(key(normalized), ignored -> new HolderSubject.GroupSubject(service, this, normalized));
        }

        @Override
        public CompletableFuture<Subject> loadSubject(String identifier) {
            String name = Identifiers.group(identifier);

            if (service.core().groups().cache().get(name).isPresent()) {
                return CompletableFuture.completedFuture(wrap(name));
            }

            // Sponge expects loadSubject to create missing subjects.
            return service.core().groups().create(name).toCompletableFuture()
                    .handle((created, error) -> wrap(name));
        }

        @Override
        public Optional<Subject> getSubject(String identifier) {
            String name = Identifiers.group(identifier);

            return service.core().groups().cache().get(name).isPresent() ? Optional.<Subject>of(wrap(name)) : Optional.<Subject>empty();
        }

        @Override
        public CompletableFuture<Boolean> hasSubject(String identifier) {
            return CompletableFuture.completedFuture(service.core().groups().cache().get(Identifiers.group(identifier)).isPresent());
        }

        @Override
        public CompletableFuture<Set<String>> getAllIdentifiers() {
            Set<String> names = new LinkedHashSet<>();

            for (Group group : service.core().groups().cache().all()) {
                names.add(group.name());
            }

            return CompletableFuture.completedFuture(names);
        }
    }
}
