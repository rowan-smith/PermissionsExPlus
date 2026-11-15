package dev.rono.permissions.core.store;

import lombok.var;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class PersistenceLogging {
    private static final String[] CATEGORIES = {"com.zaxxer.hikari"};

    private PersistenceLogging() {
        throw new AssertionError();
    }

    public static void suppressRoutineMessages() {
        for (var category : CATEGORIES) {
            Logger.getLogger(category).setLevel(Level.WARNING);

            configureLog4j2(category);
            configureLogback(category);
            configureLog4j(category);
        }
    }

    private static void configureLog4j2(String category) {
        try {
            var levelType = Class.forName("org.apache.logging.log4j.Level");
            var warning = levelType.getField("WARN").get(null);
            var configurator = Class.forName("org.apache.logging.log4j.core.config.Configurator");

            configurator.getMethod("setLevel", String.class, levelType).invoke(null, category, warning);
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchFieldException | NoSuchMethodException | InvocationTargetException | LinkageError ignored) {}
    }

    private static void configureLogback(String category) {
        try {
            var factory = Class.forName("org.slf4j.LoggerFactory");
            var logger = factory.getMethod("getLogger", String.class).invoke(null, category);
            var levelType = Class.forName("ch.qos.logback.classic.Level");
            var warning = levelType.getField("WARN").get(null);

            logger.getClass().getMethod("setLevel", levelType).invoke(logger, warning);
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchFieldException | NoSuchMethodException | InvocationTargetException | LinkageError ignored) {}
    }

    private static void configureLog4j(String category) {
        try {
            var loggerType = Class.forName("org.apache.log4j.Logger");
            var logger = loggerType.getMethod("getLogger", String.class).invoke(null, category);
            var levelType = Class.forName("org.apache.log4j.Level");
            var warning = levelType.getField("WARN").get(null);

            loggerType.getMethod("setLevel", levelType).invoke(logger, warning);
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchFieldException | NoSuchMethodException | InvocationTargetException | LinkageError ignored) {}
    }
}
