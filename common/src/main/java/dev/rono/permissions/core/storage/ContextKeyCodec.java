package dev.rono.permissions.core.storage;

import dev.rono.permissions.api.permission.PermissionContext;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Encodes {@link PermissionContext} scopes into the {@code context_key} column and scores specificity.
 */
public final class ContextKeyCodec {

    private static final String SEPARATOR = "|";

    private ContextKeyCodec() {}

    public static String encode(PermissionContext context) {
        if (context == null || context.isGlobal()) {
            return null;
        }
        Map<String, String> attrs = new LinkedHashMap<>();
        context.get(PermissionContext.SERVER).ifPresent(v -> attrs.put(PermissionContext.SERVER, v));
        context.get(PermissionContext.WORLD).ifPresent(v -> attrs.put(PermissionContext.WORLD, v));
        context.get(PermissionContext.REGION).ifPresent(v -> attrs.put(PermissionContext.REGION, v));
        context.get(PermissionContext.GAMEMODE).ifPresent(v -> attrs.put(PermissionContext.GAMEMODE, v));
        if (attrs.isEmpty()) {
            return null;
        }
        List<String> parts = new ArrayList<>();
        for (Map.Entry<String, String> entry : attrs.entrySet()) {
            parts.add(entry.getKey() + ":" + entry.getValue());
        }
        return String.join(SEPARATOR, parts);
    }

    public static String encodeLegacyWorld(String world) {
        if (world == null || world.isEmpty()) {
            return null;
        }
        return PermissionContext.WORLD + ":" + world.toLowerCase(Locale.ROOT);
    }

    public static String decodeWorld(String contextKey) {
        if (contextKey == null || contextKey.isEmpty()) {
            return null;
        }
        for (String part : contextKey.split("\\|")) {
            int idx = part.indexOf(':');
            if (idx > 0 && part.substring(0, idx).equals(PermissionContext.WORLD)) {
                return part.substring(idx + 1);
            }
        }
        return null;
    }

    /**
     * Returns {@code true} when {@code entryKey} applies to {@code requestKey}.
     * Global entries (null/empty) match every request.
     */
    public static boolean matches(String requestKey, String entryKey) {
        if (entryKey == null || entryKey.isEmpty()) {
            return true;
        }
        if (requestKey == null || requestKey.isEmpty()) {
            return false;
        }
        Map<String, String> request = parse(requestKey);
        Map<String, String> entry = parse(entryKey);
        for (Map.Entry<String, String> e : entry.entrySet()) {
            String actual = request.get(e.getKey());
            if (!Objects.equals(actual, e.getValue())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Higher scores indicate more specific contexts.
     */
    public static int specificity(String contextKey) {
        if (contextKey == null || contextKey.isEmpty()) {
            return 0;
        }
        return parse(contextKey).size();
    }

    public static List<String> allWorldKeys() {
        return List.of();
    }

    private static Map<String, String> parse(String contextKey) {
        Map<String, String> out = new LinkedHashMap<>();
        for (String part : contextKey.split("\\|")) {
            int idx = part.indexOf(':');
            if (idx > 0) {
                out.put(part.substring(0, idx), part.substring(idx + 1));
            }
        }
        return out;
    }

    public static Comparator<String> mostSpecificFirst() {
        return Comparator.comparingInt(ContextKeyCodec::specificity).reversed()
                .thenComparing(Comparator.nullsLast(String::compareTo));
    }
}
