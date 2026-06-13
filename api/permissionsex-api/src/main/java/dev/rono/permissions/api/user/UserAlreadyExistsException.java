package dev.rono.permissions.api.user;

import java.util.UUID;

/**
 * Thrown when {@link UserManager#createUser} targets an identifier that already exists.
 */
public final class UserAlreadyExistsException extends RuntimeException {

    private final String identifier;

    /**
     * Creates an exception for a duplicate UUID-keyed user.
     *
     * @param uuid UUID that already exists
     */
    public UserAlreadyExistsException(UUID uuid) {
        super("User already exists: " + uuid);
        this.identifier = uuid.toString();
    }

    /**
     * Creates an exception for a duplicate name-keyed user.
     *
     * @param name name that already exists
     */
    public UserAlreadyExistsException(String name) {
        super("User already exists: " + name);
        this.identifier = name;
    }

    /**
     * Returns the identifier that conflicted.
     *
     * @return UUID string or name depending on which constructor was used
     */
    public String getIdentifier() {
        return identifier;
    }
}
