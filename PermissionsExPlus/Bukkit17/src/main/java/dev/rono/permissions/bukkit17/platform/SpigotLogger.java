package dev.rono.permissions.bukkit17.platform;

import dev.rono.permissions.core.platform.PlatformLogger;

import java.util.logging.Level;
import java.util.logging.Logger;

public class SpigotLogger implements PlatformLogger {
    private final Logger logger;

    public SpigotLogger(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void info(String message) {
        logger.info(message);
    }

    @Override
    public void warn(String message) {
        logger.warning(message);
    }

    @Override
    public void error(String message, Throwable error) {
        logger.log(Level.SEVERE, message, error);
    }
}
