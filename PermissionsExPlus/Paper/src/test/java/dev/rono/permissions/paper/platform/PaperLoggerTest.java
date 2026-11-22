package dev.rono.permissions.paper.platform;

import dev.rono.permissions.bukkit.platform.BukkitLogger;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.junit.jupiter.api.Test;

class PaperLoggerTest {

    @Test
    void forwardsInfoWarningsAndErrorsWithTheirOriginalCause() {
        var records = new ArrayList<LogRecord>();

        var logger = Logger.getLogger("pex-test-paper-" + System.nanoTime());

        logger.setUseParentHandlers(false);

        logger.addHandler(handler(records));

        var cause = new IllegalStateException("storage failed");

        var adapter = new BukkitLogger(logger);

        adapter.info("started");

        adapter.warn("degraded");

        adapter.error("failed", cause);

        assertEquals(List.of(Level.INFO, Level.WARNING, Level.SEVERE),
                records.stream().map(LogRecord::getLevel).toList());

        assertEquals(List.of("started", "degraded", "failed"), records.stream().map(LogRecord::getMessage).toList());

        assertSame(cause, records.get(2).getThrown());
    }

    private static Handler handler(java.util.List<LogRecord> records) {
        return new Handler() {
            @Override
            public void publish(LogRecord record) {
                records.add(record);
            }

            @Override
            public void flush() {}

            @Override
            public void close() {}
        };
    }
}
