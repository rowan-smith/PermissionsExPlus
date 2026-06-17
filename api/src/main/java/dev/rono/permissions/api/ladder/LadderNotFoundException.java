package dev.rono.permissions.api.ladder;

/**
 * Thrown when a rank ladder is not defined.
 *
 * <p>Raised by {@link LadderManager#getLadder(String)}.</p>
 */
public final class LadderNotFoundException extends RuntimeException {

    private final String name;

    /**
     * Creates an exception for a missing ladder.
     *
     * @param name ladder name that was not found
     */
    public LadderNotFoundException(String name) {
        super("Ladder not found: " + name);
        this.name = name;
    }

    /**
     * Returns the ladder name that was looked up.
     *
     * @return missing ladder identifier
     */
    public String getName() {
        return name;
    }
}
