package dev.rono.permissions.core.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class DurationsTest {
    @Test
    void parsesSecondsAndUnits() {
        assertEquals(Duration.ofSeconds(3600), Durations.parse("3600"));
        assertEquals(Duration.ofDays(9), Durations.parse("1w2d"));
        assertEquals(Duration.ofMinutes(90), Durations.parse("1h30m"));
        assertEquals(Duration.ofSeconds(45), Durations.parse("45S"));
    }

    @Test
    void rejectsAnythingElse() {
        for (String input : new String[]{"", "0", "1x", "h1", "1h 30m", "1h30", "-5"}) {
            assertThrows(IllegalArgumentException.class, () -> Durations.parse(input), input);
        }
    }

    @Test
    void formatsCompactly() {
        assertEquals("45s", Durations.format(Duration.ofSeconds(45)));
        assertEquals("1h30m", Durations.format(Duration.ofMinutes(90)));
        assertEquals("6d23h", Durations.format(Duration.ofDays(7).minusMinutes(30)));
    }
}
