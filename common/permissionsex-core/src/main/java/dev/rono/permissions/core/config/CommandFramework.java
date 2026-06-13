package dev.rono.permissions.core.config;

/**
 * Which PEX command syntax tree is registered at startup.
 */
public enum CommandFramework {
    CLASSIC,
    MODERN;

    public static final String CONFIG_KEY = "command-framework";

    public static CommandFramework fromConfig(Object raw) {
        if (raw == null) {
            return MODERN;
        }
        String value = String.valueOf(raw).trim();
        if (value.isEmpty()) {
            return MODERN;
        }
        return switch (value.toLowerCase()) {
            case "classic", "legacy", "old" -> CLASSIC;
            case "modern", "new" -> MODERN;
            default -> MODERN;
        };
    }
}
