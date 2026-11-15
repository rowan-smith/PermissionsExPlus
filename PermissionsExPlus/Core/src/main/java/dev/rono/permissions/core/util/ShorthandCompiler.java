package dev.rono.permissions.core.util;

import lombok.var;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/** Compiles brace lists and ranges into flat permission strings. */
public final class ShorthandCompiler {
    private static final int MAX_EXPANSIONS = 10_000;

    private static final Pattern NUMBER_RANGE = Pattern.compile("(-?\\d+)-(-?\\d+)");

    private static final Pattern CHARACTER_RANGE = Pattern.compile("([A-Za-z])-([A-Za-z])");

    private ShorthandCompiler() {
        throw new AssertionError();
    }

    public static List<String> expand(String input) {
        Objects.requireNonNull(input, "input");

        var output = new ArrayList<String>();

        try {
            expand(input, 0, output);
        } catch (ExpansionLimitException ignored) {
            return Immutables.listOf(input);
        }

        return Immutables.copyOfList(output);
    }

    private static void expand(String input, int scanFrom, List<String> output) {
        if (output.size() >= MAX_EXPANSIONS) {
            throw new ExpansionLimitException();
        }

        var open = findOpen(input, scanFrom);
        if (open < 0) {
            output.add(input);
            return;
        }

        var close = findClose(input, open);
        if (close < 0) {
            output.add(input);
            return;
        }

        var alternatives = alternatives(input.substring(open + 1, close));
        if (alternatives.isEmpty()) {
            expand(input, close + 1, output);
            return;
        }

        var prefix = input.substring(0, open);
        var suffix = input.substring(close + 1);

        for (var alternative : alternatives) {
            var expanded = prefix + alternative + suffix;

            expand(expanded, prefix.length() + alternative.length(), output);
        }
    }

    private static int findOpen(String input, int scanFrom) {
        for (var index = Math.max(0, scanFrom); index < input.length(); index++) {
            if (input.charAt(index) == '{' && !escaped(input, index)) {
                return index;
            }
        }

        return -1;
    }

    private static int findClose(String input, int open) {
        int depth = 0;

        for (var index = open; index < input.length(); index++) {
            if (escaped(input, index)) {
                continue;
            }

            if (input.charAt(index) == '{') {
                depth++;
            } else if (input.charAt(index) == '}' && --depth == 0) {
                return index;
            }
        }

        return -1;
    }

    private static boolean escaped(String input, int index) {
        int slashes = 0;

        for (int cursor = index - 1; cursor >= 0 && input.charAt(cursor) == '\\'; cursor--) {
            slashes++;
        }

        return slashes % 2 == 1;
    }

    private static List<String> alternatives(String token) {
        var numberMatcher = NUMBER_RANGE.matcher(token);

        if (numberMatcher.matches()) {
            try {
                return numericRange(Integer.parseInt(numberMatcher.group(1)), Integer.parseInt(numberMatcher.group(2)));
            } catch (NumberFormatException ignored) {
                return Immutables.listOf();
            }
        }

        var characterMatcher = CHARACTER_RANGE.matcher(token);

        if (characterMatcher.matches()) {
            return characterRange(characterMatcher.group(1).charAt(0), characterMatcher.group(2).charAt(0));
        }

        if (!token.contains(",")) {
            return Immutables.listOf();
        }

        var values = new ArrayList<String>();

        for (var value : token.split(",", -1)) {
            var trimmed = value.trim();
            if (trimmed.isEmpty() || trimmed.indexOf('{') >= 0 || trimmed.indexOf('}') >= 0) {
                return Immutables.listOf();
            }

            values.add(trimmed);
        }

        return Immutables.copyOfList(values);
    }

    private static List<String> numericRange(int start, int end) {
        var length = Math.abs((long) end - start) + 1;
        if (length > MAX_EXPANSIONS) {
            return Immutables.listOf();
        }

        var values = new ArrayList<String>((int) length);

        int step = start <= end ? 1 : -1;

        for (int value = start;; value += step) {
            values.add(Integer.toString(value));

            if (value == end) {
                return Immutables.copyOfList(values);
            }
        }
    }

    private static List<String> characterRange(char start, char end) {
        if (Character.isUpperCase(start) != Character.isUpperCase(end)) {
            return Immutables.listOf();
        }

        var values = new ArrayList<String>();

        int step = start <= end ? 1 : -1;

        for (char value = start;; value += step) {
            values.add(Character.toString(value));

            if (value == end) {
                return Immutables.copyOfList(values);
            }
        }
    }

    private static final class ExpansionLimitException extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }
}
