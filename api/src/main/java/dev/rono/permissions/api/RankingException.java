package dev.rono.permissions.api;

/** Rank ladder promotion/demotion failure. */
public class RankingException extends PermissionsExException {
    public RankingException(String message) {
        super(message);
    }

    public RankingException(String message, Throwable cause) {
        super(message, cause);
    }
}
