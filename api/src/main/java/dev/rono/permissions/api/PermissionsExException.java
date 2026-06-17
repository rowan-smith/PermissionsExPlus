package dev.rono.permissions.api;

/**
 * Modern API failure (reload, backend operations, invalid subject state).
 *
 * <p>Checked exception type for operations that can fail due to configuration, I/O, or data
 * integrity problems in the permission layer.</p>
 */
public class PermissionsExException extends Exception {

    /**
     * Creates an exception with the given message.
     *
     * @param message human-readable description of the failure
     */
    public PermissionsExException(String message) {
        super(message);
    }

    /**
     * Creates an exception with the given message and underlying cause.
     *
     * @param message human-readable description of the failure
     * @param cause the underlying throwable that triggered this failure
     */
    public PermissionsExException(String message, Throwable cause) {
        super(message, cause);
    }
}
