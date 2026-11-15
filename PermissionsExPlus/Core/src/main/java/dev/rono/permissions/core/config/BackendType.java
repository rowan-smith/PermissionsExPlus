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
        switch (this) {
            case H2 :
                return "H2";
            case MEMORY :
                return "Memory";
            case SQLITE :
                return "SQLite";
            case YAML :
                return "YAML";
            case JSON :
                return "JSON";
            case POSTGRES :
                return "Postgres";
            case MYSQL :
                return "MySQL";
            default :
                throw new IllegalStateException("Unexpected value: " + this);
        }
    }

    public boolean persistent() {
        return this != MEMORY;
    }

    public static BackendType parse(String value) {
        switch (value.trim().toLowerCase(java.util.Locale.ROOT)) {
            case "h2" :
                return H2;
            case "memory" :
                return MEMORY;
            case "sqlite" :
                return SQLITE;
            case "yaml" :
            case "yml" :
                return YAML;
            case "json" :
                return JSON;
            case "postgres" :
            case "postgresql" :
                return POSTGRES;
            case "mysql" :
            case "mariadb" :
            case "maria" :
                return MYSQL;
            default :
                throw new IllegalArgumentException("Unsupported backend type: " + value);
        }
    }
}
