package dev.rono.permissions.core.commands;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PexCompactDurationTest {

    @Test
    void parsesCompactUnits() {
        assertEquals(604_800 + 7_200 + 600 + 5, PexCompactDuration.parseSeconds("7d2h10m5s"));
        assertEquals(90 * 60, PexCompactDuration.parseSeconds("90m"));
        assertEquals(48 * 3_600, PexCompactDuration.parseSeconds("48h"));
    }

    @Test
    void parsesPlainSeconds() {
        assertEquals(3600, PexCompactDuration.parseSeconds("3600"));
    }

    @Test
    void permanentSentinels() {
        assertEquals(-1, PexCompactDuration.parseSeconds("permanent"));
        assertEquals(-1, PexCompactDuration.parseSeconds("forever"));
    }

    @Test
    void rejectsInvalid() {
        assertThrows(IllegalArgumentException.class, () -> PexCompactDuration.parseSeconds("7x"));
    }
}
