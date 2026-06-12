package ru.tehkode.permissions.exceptions;

/**
 * Unchecked exception thrown when PermissionsEx is not installed, disabled, or not yet initialized.
 *
 * <p>Typically raised by {@link ru.tehkode.permissions.bukkit.PermissionsEx#getPermissionManager()}
 * when the plugin or its {@link ru.tehkode.permissions.PermissionManager} service is unavailable.</p>
 */
public class PermissionsNotAvailable extends RuntimeException {
    /**
     * Creates an exception with no detail message.
     */
    public PermissionsNotAvailable() {
    }

    /**
     * Creates an exception with the given detail message.
     *
     * @param message human-readable error description
     */
    public PermissionsNotAvailable(String message) {
        super(message);
    }
}
