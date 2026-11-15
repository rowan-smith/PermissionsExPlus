package dev.rono.permissions.api.util;

import java.util.Locale;
import java.util.Objects;

/**
 * Shared identifier normalization required of API implementations and backends.
 */
public final class Identifiers {

    private Identifiers() {
        throw new AssertionError();
    }

    public static String permission(String value) {
        return lowercase(value, "permission");
    }

    public static String group(String value) {
        return lowercase(value, "group");
    }

    public static String ladder(String value) {
        return lowercase(value, "ladder");
    }

    public static String contextKey(String value) {
        return lowercase(value, "context key");
    }

    /** Built-in and namespaced custom option keys use the same lowercase policy. */
    public static String optionKey(String value) {
        return lowercase(value, "option key");
    }

    /**
     * Usernames preserve display spelling but use this value for lookup and
     * uniqueness.
     */
    public static String usernameLookup(String value) {
        return lowercase(value, "username");
    }

    private static String lowercase(String value, String description) {
        Objects.requireNonNull(value, description);

        if (value.codePoints().allMatch(Character::isWhitespace)) {
            throw new IllegalArgumentException(description + " cannot be blank");
        }

        return value.toLowerCase(Locale.ROOT);
    }
}
