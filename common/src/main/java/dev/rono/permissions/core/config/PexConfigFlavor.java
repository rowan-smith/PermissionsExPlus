package dev.rono.permissions.core.config;

/** Defaults that differ between Spigot and Bungee when binding {@link PexConfigData}. */
public enum PexConfigFlavor {
    SPIGOT,
    BUNGEE;

    public boolean defaultCreateUserRecords() {
        return this == BUNGEE;
    }

    public String defaultBackend() {
        return PexConfigData.H2_BACKEND;
    }
}
