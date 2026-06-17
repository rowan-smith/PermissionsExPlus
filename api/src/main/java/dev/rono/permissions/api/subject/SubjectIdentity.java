package dev.rono.permissions.api.subject;

/**
 * Stable identity of a permission subject (user or group).
 *
 * <p>Internal role interface — composed by {@link PermissionSubject}. Implementations should treat
 * identity as read-only metadata obtained from the engine layer.</p>
 */
public interface SubjectIdentity {

    /**
     * Returns whether this subject is a user or a group.
     *
     * @return {@link SubjectType#USER} or {@link SubjectType#GROUP}
     */
    SubjectType type();

    /**
     * Returns the stable backend identifier for this subject.
     *
     * <p>UUID string for users; group name for groups.</p>
     *
     * @return subject identifier; never {@code null} for a live instance
     */
    String identifier();

    /**
     * Returns the display name for this subject.
     *
     * <p>May differ from {@link #identifier()} when a {@code name} option is set; otherwise falls back
     * to the identifier.</p>
     *
     * @return display name
     */
    String name();

    /**
     * Returns whether this subject exists only in memory and is not persisted to the backend.
     *
     * @return {@code true} if virtual (transient), {@code false} if backed by stored data
     */
    boolean virtual();
}
