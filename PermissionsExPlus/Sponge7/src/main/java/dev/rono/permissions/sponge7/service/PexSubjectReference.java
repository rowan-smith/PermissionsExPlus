package dev.rono.permissions.sponge7.service;

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
    public String getCollectionIdentifier() {
        return collection;
    }

    @Override
    public String getSubjectIdentifier() {
        return identifier;
    }

    @Override
    public CompletableFuture<Subject> resolve() {
        return service.loadCollection(collection).thenCompose(value -> value.loadSubject(identifier));
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

        return collection.equalsIgnoreCase(that.getCollectionIdentifier()) && identifier.equalsIgnoreCase(that.getSubjectIdentifier());
    }

    @Override
    public int hashCode() {
        return Objects.hash(collection.toLowerCase(java.util.Locale.ROOT), identifier.toLowerCase(java.util.Locale.ROOT));
    }

    @Override
    public String toString() {
        return collection + ":" + identifier;
    }
}
