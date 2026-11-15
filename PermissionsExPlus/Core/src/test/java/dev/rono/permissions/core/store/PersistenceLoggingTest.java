package dev.rono.permissions.core.store;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.jupiter.api.Test;

class PersistenceLoggingTest {

    @Test
    void suppressesRoutineJulMessagesWithoutHidingWarnings() {
        var hikari = Logger.getLogger("com.zaxxer.hikari");

        var previousHikari = hikari.getLevel();

        try {
            hikari.setLevel(Level.INFO);

            PersistenceLogging.suppressRoutineMessages();

            assertEquals(Level.WARNING, hikari.getLevel());
        } finally {
            hikari.setLevel(previousHikari);
        }
    }
}
