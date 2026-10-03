package dev.rono.permissions.core.config;

public enum BackendType {
    H2,
    MEMORY,
    SQLITE,
    YAML,
    JSON,
    POSTGRES,
    MYSQL;

    public String displayName() {
        return switch (this) {
            case H2 -> "H2";
            case MEMORY -> "Memory";
            case SQLITE -> "SQLite";
            case YAML -> "YAML";
            case JSON -> "JSON";
            case POSTGRES -> "Postgres";
            case MYSQL -> "MySQL";
        };
    }

    public boolean persistent() {
        return this != MEMORY;
    }

    public static BackendType parse(String value) {
        return switch (value.trim().toLowerCase(java.util.Locale.ROOT)) {
            case "h2" -> H2;
            case "memory" -> MEMORY;
            case "sqlite" -> SQLITE;
            case "yaml", "yml" -> YAML;
            case "json" -> JSON;
            case "postgres", "postgresql" -> POSTGRES;
            case "mysql", "mariadb", "maria" -> MYSQL;
            default -> throw new IllegalArgumentException("Unsupported backend type: " + value);
        };
    }
}
