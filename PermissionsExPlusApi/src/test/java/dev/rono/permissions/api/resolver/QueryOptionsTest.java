package dev.rono.permissions.api.resolver;

import lombok.var;

import dev.rono.permissions.api.context.ContextSet;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QueryOptionsTest {

    @Test
    void globalOptionsUseResolutionDefaults() {
        var options = QueryOptions.global();

        assertTrue(options.contexts().isEmpty());

        assertTrue(options.includeInheritance());

        assertTrue(options.includeDefaults());
    }

    @Test
    void copiedOptionsCanBeChangedIndependently() {
        var original = QueryOptions.builder()
                .contexts(ContextSet.builder().add("world", "nether").build())
                .build();

        var changed = QueryOptions.builder(original)
                .includeInheritance(false)
                .includeDefaults(false)
                .build();

        assertEquals(original.contexts(), changed.contexts());

        assertTrue(original.includeInheritance());

        assertTrue(original.includeDefaults());

        assertFalse(changed.includeInheritance());

        assertFalse(changed.includeDefaults());

        assertNotSame(original, changed);
    }
}
