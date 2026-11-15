package dev.rono.permissions.core.config;

import java.util.Locale;

public enum DdlGeneration {
    UPDATE, VALIDATE, NONE;

    public static DdlGeneration parse(String value) {
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException | NullPointerException error) {
            throw new IllegalStateException("Invalid schema.ddl-generation '" + value + "'; expected update, validate, or none", error);
        }
    }
}
