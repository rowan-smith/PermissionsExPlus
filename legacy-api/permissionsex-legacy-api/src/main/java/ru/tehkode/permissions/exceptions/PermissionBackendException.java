package ru.tehkode.permissions.exceptions;

/**
 * Checked exception thrown when a permission backend operation fails.
 *
 * <p>Used for recoverable storage errors during reload, save, close, and similar backend lifecycle
 * operations. Initialization failures may be wrapped in {@link RuntimeException} by
 * {@link ru.tehkode.permissions.backends.PermissionBackend#getBackend}.</p>
 */
public class PermissionBackendException extends Exception {
    /**
     * Creates an exception with no detail message.
     */
    public PermissionBackendException() {
    }

    /**
     * Creates an exception with the given detail message.
     *
     * @param message human-readable error description
     */
    public PermissionBackendException(String message) {
        super(message);
    }

    /**
     * Creates an exception with the given detail message and cause.
     *
     * @param message human-readable error description
     * @param cause   underlying cause of the failure
     */
    public PermissionBackendException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates an exception wrapping the given cause.
     *
     * @param cause underlying cause of the failure
     */
    public PermissionBackendException(Throwable cause) {
        super(cause);
    }
}
