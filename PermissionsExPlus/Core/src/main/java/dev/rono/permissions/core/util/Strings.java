package dev.rono.permissions.core.util;

/** Java 8 stand-ins for String methods added in Java 11. */
public final class Strings {
    private Strings() {
        throw new AssertionError();
    }

    /**
     * {@code String.isBlank()}: empty or only {@link Character#isWhitespace(int)}
     * code points.
     */
    public static boolean isBlank(String value) {
        return value.codePoints().allMatch(Character::isWhitespace);
    }
}
