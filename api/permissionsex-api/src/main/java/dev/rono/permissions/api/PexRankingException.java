package dev.rono.permissions.api;

/**
 * Rank ladder promotion or demotion failure.
 *
 * <p>Thrown when a rank change cannot be applied (for example, already at the top or bottom of
 * a ladder, or the ladder configuration is invalid).</p>
 */
public class PexRankingException extends PermissionsExException {

    /**
     * Creates an exception with the given message.
     *
     * @param message human-readable description of the ranking failure
     */
    public PexRankingException(String message) {
        super(message);
    }

    /**
     * Creates an exception with the given message and underlying cause.
     *
     * @param message human-readable description of the ranking failure
     * @param cause the underlying throwable that triggered this failure
     */
    public PexRankingException(String message, Throwable cause) {
        super(message, cause);
    }
}
