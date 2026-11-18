package dev.rono.permissions.core.util;

import lombok.var;

import java.time.Duration;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Parses and formats command durations: plain seconds ({@code 3600}, as
 * PermissionsEx took them) or units ({@code 1w2d}, {@code 1h30m}, {@code 45s}).
 */
public final class Durations {
    private static final Pattern SECONDS = Pattern.compile("\\d+");
    private static final Pattern UNITS = Pattern.compile("(\\d+)([wdhms])");

    private Durations() {}

    public static Duration parse(String input) {
        var text = input.trim().toLowerCase(Locale.ROOT);

        if (SECONDS.matcher(text).matches()) {
            return positive(Duration.ofSeconds(Long.parseLong(text)), input);
        }

        var matcher = UNITS.matcher(text);
        var total = Duration.ZERO;
        var end = 0;

        while (matcher.find()) {
            if (matcher.start() != end) {
                break;
            }

            var amount = Long.parseLong(matcher.group(1));

            switch (matcher.group(2)) {
                case "w" :
                    total = total.plusDays(amount * 7);
                    break;
                case "d" :
                    total = total.plusDays(amount);
                    break;
                case "h" :
                    total = total.plusHours(amount);
                    break;
                case "m" :
                    total = total.plusMinutes(amount);
                    break;
                default :
                    total = total.plusSeconds(amount);
                    break;
            }

            end = matcher.end();
        }

        if (end == 0 || end != text.length()) {
            throw new IllegalArgumentException("Invalid duration '" + input + "'; use seconds or units such as 7d, 1h30m or 45s");
        }

        return positive(total, input);
    }

    /** Formats a remaining time compactly, for example {@code 6d23h}. */
    public static String format(Duration duration) {
        var seconds = Math.max(0, duration.getSeconds());

        if (seconds < 60) {
            return seconds + "s";
        }

        var days = seconds / 86400;
        var hours = seconds % 86400 / 3600;
        var minutes = seconds % 3600 / 60;
        var result = new StringBuilder();

        if (days > 0) {
            result.append(days).append('d');
        }

        if (hours > 0) {
            result.append(hours).append('h');
        }

        if (minutes > 0 && days == 0) {
            result.append(minutes).append('m');
        }

        return result.toString();
    }

    private static Duration positive(Duration duration, String input) {
        if (duration.isZero() || duration.isNegative()) {
            throw new IllegalArgumentException("Duration must be positive: " + input);
        }

        return duration;
    }
}
