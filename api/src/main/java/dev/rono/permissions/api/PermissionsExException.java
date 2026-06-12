package dev.rono.permissions.api;

/** Modern API failure (reload, backend, invalid subject state). */
public class PermissionsExException extends Exception {
    public PermissionsExException(String message) {
        super(message);
    }

    public PermissionsExException(String message, Throwable cause) {
        super(message, cause);
    }
}
