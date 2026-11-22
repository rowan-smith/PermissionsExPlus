package dev.rono.permissions.sponge8.service;

import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectReference;

final class PexSubjectReference implements SubjectReference {
    private final PexPermissionService service;
    private final String collection;
    private final String identifier;

    PexSubjectReference(PexPermissionService service, String collection, String identifier) {
        this.service = service;
        this.collection = collection;
        this.identifier = identifier;
    }

    @Override
    public String collectionIdentifier() {
        return collection;
    }

    @Override
    public String subjectIdentifier() {
        return identifier;
    }

    @Override
    public CompletableFuture<? extends Subject> resolve() {
        return service.collectionFor(collection).loadSubject(identifier);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof SubjectReference)) {
            return false;
        }

        SubjectReference that = (SubjectReference) other;

        return collection.equalsIgnoreCase(that.collectionIdentifier()) && identifier.equalsIgnoreCase(that.subjectIdentifier());
    }

    @Override
    public int hashCode() {
        return Objects.hash(collection.toLowerCase(Locale.ROOT), identifier.toLowerCase(Locale.ROOT));
    }

    @Override
    public String toString() {
        return collection + ":" + identifier;
    }
}
