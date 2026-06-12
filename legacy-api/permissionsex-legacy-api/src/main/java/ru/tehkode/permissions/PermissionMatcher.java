package ru.tehkode.permissions;

/**
 * Strategy for matching permission nodes against permission expressions.
 *
 * <p>Implementations define how wildcard and prefix rules are evaluated when checking whether a
 * subject holds a given permission string.</p>
 */
public interface PermissionMatcher {
    /**
     * Returns whether a permission string matches the given expression.
     *
     * <p>Expressions may include wildcards (for example {@code "essentials.*"}) depending on the
     * matcher implementation.</p>
     *
     * @param expression permission pattern or node to match against
     * @param permission permission string being checked
     * @return {@code true} if {@code permission} is granted by {@code expression}
     */
    public boolean isMatches(String expression, String permission);
}
