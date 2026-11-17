package dev.rono.permissions.sponge9.service;

import dev.rono.permissions.api.group.Group;
import dev.rono.permissions.api.util.Identifiers;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectCollection;
import org.spongepowered.api.service.permission.SubjectReference;
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
        return identifier.toLowerCase(Locale.ROOT);
    }

    @Override
    public final String identifier() {
        return identifier;
    }

    @Override
    public Predicate<String> identifierValidityPredicate() {
        return value -> value != null && !value.trim().isEmpty();
    }

    @Override
    public abstract CompletableFuture<PexSubject> loadSubject(String identifier);

    @Override
    public final Collection<PexSubject> loadedSubjects() {
        return Collections.unmodifiableCollection(loaded.values());
    }

    @Override
    public final CompletableFuture<Map<String, PexSubject>> loadSubjects(Iterable<String> identifiers) {
        Map<String, CompletableFuture<PexSubject>> futures = new LinkedHashMap<>();

        for (String id : identifiers) {
            futures.put(id, loadSubject(id));
        }

        return CompletableFuture.allOf(futures.values().toArray(new CompletableFuture<?>[0])).thenApply(ignored -> {
            Map<String, PexSubject> result = new LinkedHashMap<>();
            futures.forEach((id, future) -> result.put(id, future.join()));
            return result;
        });
    }

    @Override
    public final SubjectReference newSubjectReference(String subjectIdentifier) {
        return service.newSubjectReference(identifier, subjectIdentifier);
    }

    @Override
    public final CompletableFuture<Map<SubjectReference, Boolean>> allWithPermission(String permission) {
        return CompletableFuture.completedFuture(references(loadedWith(permission, Collections.<Context>emptySet())));
    }

    @Override
    public final CompletableFuture<Map<SubjectReference, Boolean>> allWithPermission(String permission, Cause cause) {
        return CompletableFuture.completedFuture(references(loadedWith(permission, Contexts.of(cause))));
    }

    @Override
    public final Map<PexSubject, Boolean> loadedWithPermission(String permission) {
        return loadedWith(permission, Collections.<Context>emptySet());
    }

    @Override
    public final Map<PexSubject, Boolean> loadedWithPermission(String permission, Cause cause) {
        return loadedWith(permission, Contexts.of(cause));
    }

    private Map<PexSubject, Boolean> loadedWith(String permission, Set<Context> contexts) {
        Map<PexSubject, Boolean> result = new LinkedHashMap<>();

        for (PexSubject subject : loaded.values()) {
            Tristate value = subject.permissionValue(permission, contexts);

            if (value != Tristate.UNDEFINED) {
                result.put(subject, value.asBoolean());
            }
        }

        return result;
    }

    private static Map<SubjectReference, Boolean> references(Map<PexSubject, Boolean> subjects) {
        Map<SubjectReference, Boolean> result = new LinkedHashMap<>();
        subjects.forEach((subject, value) -> result.put(subject.asSubjectReference(), value));
        return result;
    }

    @Override
    public final Subject defaults() {
        return service.defaultCollection().get(identifier);
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

        /** The subject with this identifier, created on first use. */
        PexSubject get(String id) {
            return loaded.computeIfAbsent(key(id), ignored -> new MemorySubject(service, this, id));
        }

        @Override
        public CompletableFuture<PexSubject> loadSubject(String id) {
            return CompletableFuture.completedFuture(get(id));
        }

        @Override
        public Optional<PexSubject> subject(String id) {
            return Optional.of(get(id));
        }

        @Override
        public CompletableFuture<Boolean> hasSubject(String id) {
            return CompletableFuture.completedFuture(loaded.containsKey(key(id)));
        }

        @Override
        public CompletableFuture<Set<String>> allIdentifiers() {
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
            super(service, PermissionService.SUBJECTS_USER);
        }

        @Override
        public Predicate<String> identifierValidityPredicate() {
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
        public CompletableFuture<PexSubject> loadSubject(String identifier) {
            UUID id = UUID.fromString(identifier);

            if (service.core().users().cache().get(id).isPresent()) {
                return CompletableFuture.completedFuture(wrap(id));
            }

            return service.core().users().loadOrCreateUser(id, name(id)).toCompletableFuture().thenApply(ignored -> wrap(id));
        }

        /**
         * Users need a unique name; use the account name when the profile cache knows
         * it.
         */
        private static String name(UUID id) {
            try {
                if (!Sponge.isServerAvailable()) {
                    return id.toString();
                }

                Optional<GameProfile> profile = Sponge.server().gameProfileManager().cache().findById(id);
                Optional<String> name = profile.isPresent() ? profile.get().name() : Optional.<String>empty();

                return name.isPresent() ? name.get() : id.toString();
            } catch (RuntimeException unavailable) {
                return id.toString(); // Sponge not ready; the name is updated when the player joins.
            }
        }

        @Override
        public Optional<PexSubject> subject(String identifier) {
            if (!uuid(identifier)) {
                return Optional.empty();
            }

            UUID id = UUID.fromString(identifier);

            return service.core().users().cache().get(id).isPresent() ? Optional.of(wrap(id)) : Optional.<PexSubject>empty();
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
        public CompletableFuture<Set<String>> allIdentifiers() {
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
            super(service, PermissionService.SUBJECTS_GROUP);
        }

        private PexSubject wrap(String name) {
            String normalized = Identifiers.group(name);

            return loaded.computeIfAbsent(key(normalized), ignored -> new HolderSubject.GroupSubject(service, this, normalized));
        }

        @Override
        public CompletableFuture<PexSubject> loadSubject(String identifier) {
            String name = Identifiers.group(identifier);

            if (service.core().groups().cache().get(name).isPresent()) {
                return CompletableFuture.completedFuture(wrap(name));
            }

            // Sponge expects loadSubject to create missing subjects.
            return service.core().groups().create(name).toCompletableFuture().handle((created, error) -> wrap(name));
        }

        @Override
        public Optional<PexSubject> subject(String identifier) {
            String name = Identifiers.group(identifier);

            return service.core().groups().cache().get(name).isPresent() ? Optional.of(wrap(name)) : Optional.<PexSubject>empty();
        }

        @Override
        public CompletableFuture<Boolean> hasSubject(String identifier) {
            return CompletableFuture.completedFuture(service.core().groups().cache().get(Identifiers.group(identifier)).isPresent());
        }

        @Override
        public CompletableFuture<Set<String>> allIdentifiers() {
            Set<String> names = new LinkedHashSet<>();

            for (Group group : service.core().groups().cache().all()) {
                names.add(group.name());
            }

            return CompletableFuture.completedFuture(names);
        }
    }
}
