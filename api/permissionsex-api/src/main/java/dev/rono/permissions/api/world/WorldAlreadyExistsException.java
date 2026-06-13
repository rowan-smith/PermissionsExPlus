package dev.rono.permissions.api.world;

/**
 * Thrown when {@link WorldManager#createWorld} targets a name that is already registered.
 */
public final class WorldAlreadyExistsException extends RuntimeException {

    private final String name;

    /**
     * Creates an exception for a duplicate world registration.
     *
     * @param name world name that already exists
     */
    public WorldAlreadyExistsException(String name) {
        super("World already exists: " + name);
        this.name = name;
    }

    /**
     * Returns the conflicting world name.
     *
     * @return existing world identifier
     */
    public String getName() {
        return name;
    }
}
