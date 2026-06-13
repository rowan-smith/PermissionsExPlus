package dev.rono.permissions.core.commands.cloud.modern;

import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class PexCommandFlagsTest {

    @Test
    void parsesOptionalFlags() {
        Queue<String> input = new ArrayDeque<>();
        input.add("--world");
        input.add("survival");
        input.add("--server");
        input.add("lobby");
        PexCommandFlags flags = PexCommandFlags.parseOptional(input);
        assertEquals("survival", flags.get("world").orElseThrow());
        assertEquals("lobby", flags.get("server").orElseThrow());
        assertFalse(input.iterator().hasNext());
    }

    @Test
    void emptyWhenNoFlags() {
        Queue<String> input = new ArrayDeque<>();
        assertEquals(PexCommandFlags.EMPTY, PexCommandFlags.parseOptional(input));
    }
}
