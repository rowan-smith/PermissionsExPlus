package ru.tehkode.permissions.exceptions;

/**
 * Checked exception thrown when a rank promotion or demotion operation fails.
 *
 * <p>Carries references to the subject user and the promoter (the actor performing the rank change)
 * for use in error messages and logging.</p>
 */
public class RankingException extends Exception {
    private final Object user;
    private final Object promoter;

    /**
     * Creates a ranking exception with a message and context objects.
     *
     * @param message  human-readable error description
     * @param user     the user whose rank could not be changed
     * @param promoter the actor attempting the rank change
     */
    public RankingException(String message, Object user, Object promoter) {
        super(message);
        this.user = user;
        this.promoter = promoter;
    }

    /**
     * Returns the user whose rank change failed.
     *
     * @return subject user object (platform-specific type, for example {@code Player})
     */
    public Object getUser() {
        return user;
    }

    /**
     * Returns the user whose rank change failed.
     *
     * <p>Alias for {@link #getUser()} retained for backward compatibility.</p>
     *
     * @return subject user object (platform-specific type, for example {@code Player})
     */
    public Object getTarget() {
        return user;
    }

    /**
     * Returns the actor who attempted the rank change.
     *
     * @return promoter object (platform-specific type, for example {@code Player} or {@code CommandSender})
     */
    public Object getPromoter() {
        return promoter;
    }
}
