package dev.rono.permissions.api.world;

/**
 * Thrown when a world realm is not registered.
 *
 * <p>Raised by {@link WorldManager#getWorld(String)}.</p>
 */
public final class WorldNotFoundException extends RuntimeException {

    private final String name;

    /**
     * Creates an exception for a missing world.
     *
     * @param name world name that was not found
     */
    public WorldNotFoundException(String name) {
        super("World not found: " + name);
        this.name = name;
    }

    /**
     * Returns the world name that was looked up.
     *
     * @return missing world identifier
     */
    public String getName() {
        return name;
    }
}
