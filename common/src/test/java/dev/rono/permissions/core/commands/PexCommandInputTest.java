package dev.rono.permissions.core.commands;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PexCommandInputTest {

    @Test
    void stripRemovesLeadingAndTrailingWhitespace() {
        assertEquals("pex user Rono add test", PexCommandInput.strip("  pex user Rono add test  "));
    }

    @Test
    void stripPreservesInternalSpacing() {
        assertEquals("pex user Rono add test", PexCommandInput.strip("pex user Rono add test"));
    }

    @Test
    void stripNullReturnsNull() {
        assertNull(PexCommandInput.strip(null));
    }
}
