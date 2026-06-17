package dev.rono.permissions.api.ladder;

import dev.rono.permissions.api.RankingException;
import dev.rono.permissions.api.group.Group;
import dev.rono.permissions.api.user.User;
import java.util.Optional;
import java.util.function.Predicate;

/** Rank ladder registry with explicit find/get/create/exists lifecycle and promotion operations. */
public interface LadderManager {

    /**
     * Looks up a known ladder without creating a record.
     *
     * @param name ladder name
     * @return the ladder when referenced by stored groups; empty if unknown
     */
    Optional<Ladder> findLadder(String name);

    /**
     * Returns a known ladder by name.
     *
     * @param name ladder name
     * @return live ladder adapter
     * @throws LadderNotFoundException if no groups reference {@code name} as a rank ladder
     */
    Ladder getLadder(String name) throws LadderNotFoundException;

    /**
     * Registers a new rank ladder name.
     *
     * @param name ladder name
     * @return live ladder adapter
     * @throws LadderAlreadyExistsException if {@code name} is already known
     */
    Ladder createLadder(String name) throws LadderAlreadyExistsException;

    /**
     * Reports whether a ladder name is known.
     *
     * @param name ladder name
     * @return {@code true} if the ladder exists in the registry
     */
    boolean exists(String name);

    /**
     * Returns the number of rank ladders referenced by stored groups.
     *
     * @return total known ladder count
     */
    int count();

    /**
     * Returns how many known ladders match {@code filter}.
     *
     * @param filter predicate applied to each known ladder; must not be {@code null}
     * @return count of ladders for which the predicate is {@code true}
     */
    int count(Predicate<Ladder> filter);

    /**
     * Promotes {@code user} one step up on the named ladder without rank restrictions.
     *
     * @param user       user to promote
     * @param ladderName rank ladder name
     * @return the group the user was promoted into
     * @throws RankingException if the user is not on the ladder or no higher group exists
     */
    Group promote(User user, String ladderName) throws RankingException;

    /**
     * Promotes {@code user} one step up on the named ladder.
     *
     * @param actor      user authorizing the promotion, or {@code null} for unrestricted promotion
     * @param user       user to promote
     * @param ladderName rank ladder name
     * @return the group the user was promoted into
     * @throws RankingException if promotion is not allowed or no higher group exists
     */
    Group promote(User actor, User user, String ladderName) throws RankingException;

    /**
     * Promotes {@code user} one step up on the given ladder without rank restrictions.
     *
     * @param user   user to promote
     * @param ladder rank ladder
     * @return the group the user was promoted into
     * @throws RankingException if the user is not on the ladder or no higher group exists
     */
    default Group promote(User user, Ladder ladder) throws RankingException {
        return promote(user, ladder.getName());
    }

    /**
     * Promotes {@code user} one step up on the given ladder.
     *
     * @param actor  user authorizing the promotion, or {@code null} for unrestricted promotion
     * @param user   user to promote
     * @param ladder rank ladder
     * @return the group the user was promoted into
     * @throws RankingException if promotion is not allowed or no higher group exists
     */
    default Group promote(User actor, User user, Ladder ladder) throws RankingException {
        return promote(actor, user, ladder.getName());
    }

    /**
     * Demotes {@code user} one step down on the named ladder without rank restrictions.
     *
     * @param user       user to demote
     * @param ladderName rank ladder name
     * @return the group the user was demoted into
     * @throws RankingException if the user is not on the ladder or no lower group exists
     */
    Group demote(User user, String ladderName) throws RankingException;

    /**
     * Demotes {@code user} one step down on the named ladder.
     *
     * @param actor      user authorizing the demotion, or {@code null} for unrestricted demotion
     * @param user       user to demote
     * @param ladderName rank ladder name
     * @return the group the user was demoted into
     * @throws RankingException if demotion is not allowed or no lower group exists
     */
    Group demote(User actor, User user, String ladderName) throws RankingException;

    /**
     * Demotes {@code user} one step down on the given ladder without rank restrictions.
     *
     * @param user   user to demote
     * @param ladder rank ladder
     * @return the group the user was demoted into
     * @throws RankingException if the user is not on the ladder or no lower group exists
     */
    default Group demote(User user, Ladder ladder) throws RankingException {
        return demote(user, ladder.getName());
    }

    /**
     * Demotes {@code user} one step down on the given ladder.
     *
     * @param actor  user authorizing the demotion, or {@code null} for unrestricted demotion
     * @param user   user to demote
     * @param ladder rank ladder
     * @return the group the user was demoted into
     * @throws RankingException if demotion is not allowed or no lower group exists
     */
    default Group demote(User actor, User user, Ladder ladder) throws RankingException {
        return demote(actor, user, ladder.getName());
    }

    /**
     * Returns whether {@code user} holds a ranked group on the ladder.
     *
     * @param user       user to inspect
     * @param ladderName rank ladder name
     * @return {@code true} when {@link #rank(User, String)} would return a value greater than zero
     */
    boolean isRanked(User user, String ladderName);

    /**
     * Returns whether {@code user} holds a ranked group on the ladder.
     *
     * @param user   user to inspect
     * @param ladder rank ladder
     * @return {@code true} when {@link #rank(User, Ladder)} would return a value greater than zero
     */
    default boolean isRanked(User user, Ladder ladder) {
        return isRanked(user, ladder.getName());
    }

    /**
     * Returns the numeric rank of {@code user} on the ladder (lower numbers are higher standing).
     *
     * @param user       user to inspect
     * @param ladderName rank ladder name
     * @return rank value, or {@code 0} if not ranked
     */
    int rank(User user, String ladderName);

    /**
     * Returns the numeric rank of {@code user} on the ladder.
     *
     * @param user   user to inspect
     * @param ladder rank ladder
     * @return rank value, or {@code 0} if not ranked
     */
    default int rank(User user, Ladder ladder) {
        return rank(user, ladder.getName());
    }
}
