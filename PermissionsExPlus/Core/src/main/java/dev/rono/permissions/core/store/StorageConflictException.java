package dev.rono.permissions.core.store;

/**
 * Thrown when a revision-checked save finds that another writer, usually
 * another server on the same database, saved the subject first.
 */
public final class StorageConflictException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public StorageConflictException(String message) {
        super(message);
    }
}
