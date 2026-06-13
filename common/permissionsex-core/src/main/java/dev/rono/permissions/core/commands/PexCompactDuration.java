package dev.rono.permissions.core.commands;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses compact duration tokens such as {@code 7d2h10m5s} for modern timed commands.
 */
public final class PexCompactDuration {
    private static final Pattern COMPACT = Pattern.compile("(\\d+)([dhms])", Pattern.CASE_INSENSITIVE);

    private PexCompactDuration() {}

    /**
     * @param raw duration expression
     * @return seconds, or {@code -1} when {@code permanent}/{@code forever} is given
     */
    public static int parseSeconds(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Duration is required");
        }
        String trimmed = raw.trim();
        if (isPermanent(trimmed)) {
            return -1;
        }
        if (trimmed.matches("^\\d+$")) {
            return Integer.parseInt(trimmed);
        }
        Matcher matcher = COMPACT.matcher(trimmed);
        int total = 0;
        int lastEnd = 0;
        while (matcher.find()) {
            if (matcher.start() != lastEnd) {
                throw new IllegalArgumentException("Invalid duration: " + raw);
            }
            int amount = Integer.parseInt(matcher.group(1));
            total += amount * unitSeconds(matcher.group(2));
            lastEnd = matcher.end();
        }
        if (lastEnd != trimmed.length()) {
            throw new IllegalArgumentException("Invalid duration: " + raw);
        }
        if (total <= 0) {
            throw new IllegalArgumentException("Duration must be positive");
        }
        return total;
    }

    public static boolean isPermanent(String raw) {
        if (raw == null) {
            return false;
        }
        String lower = raw.trim().toLowerCase();
        return "permanent".equals(lower) || "forever".equals(lower);
    }

    private static int unitSeconds(String unit) {
        return switch (unit.toLowerCase()) {
            case "d" -> 86_400;
            case "h" -> 3_600;
            case "m" -> 60;
            case "s" -> 1;
            default -> throw new IllegalArgumentException("Unknown duration unit: " + unit);
        };
    }
}
