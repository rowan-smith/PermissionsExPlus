package dev.rono.permissions.core.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public final class CoreCloudSuggestionHelper {
    private CoreCloudSuggestionHelper() {}

    public static List<String> matchSuggestions(Collection<String> values, String input) {
        String query = input == null ? "" : input.trim().toLowerCase(Locale.ROOT);
        List<String> out = new ArrayList<>();
        for (String value : values) {
            if (value == null || value.isBlank()) {
                continue;
            }
            String normalized = value.toLowerCase(Locale.ROOT);
            if (query.isEmpty() || normalized.startsWith(query)) {
                out.add(value);
            }
        }
        return out;
    }

    public static List<String> matchCsvSuggestions(Collection<String> values, String input) {
        String rawInput = input == null ? "" : input;
        int commaIndex = rawInput.lastIndexOf(',');
        String token = commaIndex >= 0 ? rawInput.substring(commaIndex + 1).trim() : rawInput.trim();
        String prefix = commaIndex >= 0 ? rawInput.substring(0, commaIndex + 1) : "";
        boolean appendSpace = !prefix.isEmpty() && !prefix.endsWith(" ");

        List<String> out = new ArrayList<>();
        for (String value : matchSuggestions(values, token)) {
            out.add(appendSpace ? prefix + " " + value : prefix + value);
        }
        return out;
    }

    public static List<String> matchPermissionSuggestions(Collection<String> values, String input) {
        String rawInput = input == null ? "" : input.trim();
        if (rawInput.isEmpty()) {
            return matchSuggestions(values, "");
        }

        String normalizedInput = rawInput.toLowerCase(Locale.ROOT);
        int lastDot = rawInput.lastIndexOf('.');
        if (lastDot < 0) {
            return matchSuggestions(values, rawInput);
        }

        String basePrefix = rawInput.substring(0, lastDot + 1);
        String normalizedBase = normalizedInput.substring(0, lastDot + 1);
        String tailQuery = normalizedInput.substring(lastDot + 1);

        java.util.Set<String> segments = new java.util.TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (String permission : values) {
            if (permission == null || permission.isBlank()) {
                continue;
            }
            String normalizedPermission = permission.toLowerCase(Locale.ROOT);
            if (!normalizedPermission.startsWith(normalizedBase)) {
                continue;
            }

            String remainder = permission.substring(basePrefix.length());
            if (remainder.isEmpty()) {
                continue;
            }

            int nextDot = remainder.indexOf('.');
            String nextSegment = nextDot >= 0 ? remainder.substring(0, nextDot) : remainder;
            if (!nextSegment.isEmpty() && nextSegment.toLowerCase(Locale.ROOT).startsWith(tailQuery)) {
                segments.add(basePrefix + nextSegment);
            }
        }

        if (!segments.isEmpty()) {
            return new ArrayList<>(segments);
        }
        return matchSuggestions(values, rawInput);
    }
}
