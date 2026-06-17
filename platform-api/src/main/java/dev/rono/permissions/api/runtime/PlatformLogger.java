package dev.rono.permissions.api.runtime;

/**
 * Platform logging bridge. Implementations forward to the host logger (Bukkit, proxy, Sponge, etc.).
 */
public interface PlatformLogger {

    void info(String message);

    void warning(String message);

    void severe(String message, Throwable cause);
}
