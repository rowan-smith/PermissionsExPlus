package ru.tehkode.permissions.bukkit;

import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

/**
 * Thin ABI facade. Classic PEX posted reports remotely; the adapter only logs.
 */
public class ErrorReport {
    private static final Logger LOGGER = Logger.getLogger("PermissionsEx");

    private final String name;
    private final Throwable error;
    private final String userDescription;

    private ErrorReport(String name, String userDescription, Throwable error) {
        this.name = name;
        this.error = error;
        this.userDescription = userDescription;
    }

    public String getMainUserError() {
        return userDescription == null ? (error == null ? "Unknown error" : error.toString()) : userDescription;
    }

    public void send() {
        LOGGER.log(Level.SEVERE, "PermissionsEx error [" + name + "]: " + getMainUserError(), error);
    }

    public static void shutdown() {
        // No remote reporter to shut down.
    }

    public static ErrorReport withException(String userMessage, Throwable error) {
        return new ErrorReport("Exception", userMessage, error);
    }

    public static void handleError(String msg, Throwable t) {
        handleError(msg, t, null);
    }

    public static void handleError(String msg, Throwable t, CommandSender sender) {
        if (sender != null) {
            sender.sendMessage("PermissionsEx error: " + msg);
        }
        withException(msg, t).send();
    }

    public static void handleError(CommandSender sender, String msg, Plugin plugin, Callable<?> code) {
        try {
            code.call();
        } catch (Throwable t) {
            handleError(msg, t, sender);
        }
    }

    public static void handleError(CommandSender sender, String msg, Plugin plugin, Runnable code) {
        handleError(sender, msg, plugin, () -> {
            code.run();
            return null;
        });
    }

    public static void handleError(final String msg, Plugin plugin, Callable<?> code) {
        handleError(null, msg, plugin, code);
    }

    public static class ExceptionHandler implements Thread.UncaughtExceptionHandler {
        private final Plugin plugin;

        public ExceptionHandler(Plugin plugin) {
            this.plugin = plugin;
        }

        @Override
        public void uncaughtException(Thread t, Throwable e) {
            handleError("Uncaught " + e.getClass().getSimpleName() + ": " + e.getMessage(), e);
        }
    }

    public static class Builder {
        private final Plugin plugin;
        private String name;
        private String userDescription;
        private Throwable error;

        public Builder(Plugin plugin) {
            this.plugin = plugin;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder userDescription(String userDescription) {
            this.userDescription = userDescription;
            return this;
        }

        public Builder error(Throwable error) {
            this.error = error;
            return this;
        }

        public ErrorReport build() {
            return new ErrorReport(name == null ? plugin.getName() : name, userDescription, error);
        }
    }
}
