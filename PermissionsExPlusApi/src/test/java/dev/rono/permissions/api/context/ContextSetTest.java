package dev.rono.permissions.api.context;

import lombok.var;

import java.util.LinkedHashSet;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContextSetTest {

    @Test
    void equalContextSetsHaveValueEquality() {
        var first = ContextSet.builder()
                .add("world", "nether")
                .build();

        var second = ContextSet.builder()
                .add("world", "nether")
                .build();

        assertEquals(first, second);

        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void builtContextSetIsDetachedFromBuilder() {
        var builder = ContextSet.builder()
                .add("world", "overworld");

        var first = builder.build();

        builder.add("world", "nether");

        assertEquals(sets("overworld"), first.values("world"));
    }

    @Test
    void builderCanRemoveReplaceAndClearContexts() {
        var builder = ContextSet.builder()
                .add("world", "overworld")
                .add("world", "nether")
                .add("server", "survival")
                .remove("world", "nether")
                .set("server", "lobby");

        var contexts = builder.build();

        assertEquals(sets("overworld"), contexts.values("world"));

        assertEquals(sets("lobby"), contexts.values("server"));

        builder.clear();

        assertTrue(builder.build().isEmpty());

        assertFalse(contexts.isEmpty());
    }

    @Test
    void contextKeysAreNormalizedToLowercase() {
        var contexts = ContextSet.builder()
                .add("WORLD", "Nether")
                .build();

        assertTrue(contexts.contains("world", "Nether"));

        assertTrue(contexts.contains("WORLD", "Nether"));

        assertFalse(contexts.contains("world", "nether"));
    }

    @SafeVarargs
    private static <T> Set<T> sets(T... values) {
        return new LinkedHashSet<>(Arrays.asList(values));
    }
}
