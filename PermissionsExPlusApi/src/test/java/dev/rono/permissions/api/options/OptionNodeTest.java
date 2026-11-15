package dev.rono.permissions.api.options;

import lombok.var;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OptionNodeTest {

    @Test
    void optionNodesArePermanentByDefault() {
        var option = OptionNode.builder().option("prefix", "[Admin]").build();

        assertTrue(option.permanent());

        assertFalse(option.temporary());
    }

    @Test
    void optionNodesSupportExpiryAndCopying() {
        var expiry = Instant.parse("2030-01-01T00:00:00Z");

        var option = OptionNode.builder()
                .option("prefix", "[Admin]")
                .expiry(expiry)
                .build();

        assertEquals(expiry, option.expiry().get());

        assertEquals(option, OptionNode.builder(option).build());
    }

    @Test
    void permanentClearsAnOptionExpiry() {
        var option = OptionNode.builder()
                .option("prefix", "[Admin]")
                .expiry(Instant.parse("2030-01-01T00:00:00Z"))
                .permanent()
                .build();

        assertFalse(option.expiry().isPresent());
    }

    @Test
    void optionDurationMustBePositive() {
        var builder = OptionNode.builder().option("prefix", "[Admin]");

        assertThrows(IllegalArgumentException.class, () -> builder.duration(Duration.ZERO));

        assertThrows(IllegalArgumentException.class, () -> builder.duration(Duration.ofSeconds(-1)));

        assertThrows(NullPointerException.class, () -> builder.duration(null));
    }
}
