package dev.rono.permissions.api.ladder;

/**
 * Thrown when {@link LadderManager#createLadder} targets a name that already exists.
 */
public final class LadderAlreadyExistsException extends RuntimeException {

    private final String name;

    /**
     * Creates an exception for a duplicate ladder registration.
     *
     * @param name ladder name that already exists
     */
    public LadderAlreadyExistsException(String name) {
        super("Ladder already exists: " + name);
        this.name = name;
    }

    /**
     * Returns the conflicting ladder name.
     *
     * @return existing ladder identifier
     */
    public String getName() {
        return name;
    }
}
