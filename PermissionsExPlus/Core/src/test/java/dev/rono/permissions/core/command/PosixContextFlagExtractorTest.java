package dev.rono.permissions.core.command;

import lombok.var;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import dev.rono.permissions.core.context.RuntimeContextRegistry;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import dev.rono.permissions.core.util.Immutables;

class PosixContextFlagExtractorTest {
    private final RuntimeContextRegistry registry = new RuntimeContextRegistry();
    private final PosixContextFlagExtractor<Object> extractor = new PosixContextFlagExtractor<>(registry);
    private final AtomicReference<List<String>> worlds = new AtomicReference<>(Immutables.listOf("creative", "survival"));

    @BeforeEach
    void registerContexts() {
        registry.registerContextType("world", worlds::get);
        registry.registerContextType("combat-tag", () -> Immutables.listOf("false", "true"));
    }

    @Test
    void stripsTrailingDirectFlagsWhileScanningBackwards() {
        var input = new LinkedList<>(Immutables.listOf("pex", "user", "steve", "permissions", "add", "*", "--world", "creative", "--combat-tag", "true"));

        var result = extractor.extract(input, false);

        assertEquals(Immutables.listOf("pex", "user", "steve", "permissions", "add", "*"), input);
        assertTrue(result.contexts().contains("world", "creative"));
        assertTrue(result.contexts().contains("combat-tag", "true"));
    }

    @Test
    void rejectsUniversalUnknownAndInvalidFlags() {
        assertThrows(IllegalArgumentException.class, () -> extractor.extract(new LinkedList<>(Immutables.listOf("pex", "--context", "world=creative")), false));
        assertThrows(IllegalArgumentException.class, () -> extractor.extract(new LinkedList<>(Immutables.listOf("pex", "--faction", "claimed")), false));
        assertThrows(IllegalArgumentException.class, () -> extractor.extract(new LinkedList<>(Immutables.listOf("pex", "--world", "missing")), false));
    }

    @Test
    void providesDynamicKeyAndValueCompletions() {
        assertEquals(Immutables.listOf("--combat-tag", "--world"), extractor.keySuggestions(""));
        assertEquals(Immutables.listOf("creative"), extractor.valueSuggestions("world", "cr"));

        worlds.set(Immutables.listOf("creative", "world_nether"));

        assertEquals(Immutables.listOf("world_nether"), extractor.valueSuggestions("world", "world_"));
    }

    @Test
    void suggestionExtractionRemovesIncompleteFlagsFromTheCoreCommand() {
        var input = new LinkedList<>(Immutables.listOf("pex", "user", "steve", "permissions", "add", "*", "--world", "cr"));

        var result = extractor.extract(input, true);

        assertEquals(Immutables.listOf("pex", "user", "steve", "permissions", "add", "*"), input);
        assertEquals("world", result.pendingKey());
        assertEquals("cr", result.pendingValue());
    }

    @Test
    void suggestionExtractionAcceptsTrailingSpaceAfterCompleteContextFlag() {
        var input = new LinkedList<>(Immutables.listOf("pex", "user", "steve", "permissions", "add", "*", "--world", "creative", ""));

        var result = extractor.extract(input, true);

        assertEquals(Immutables.listOf("pex", "user", "steve", "permissions", "add", "*"), input);
        assertTrue(result.contexts().contains("world", "creative"));
        assertNull(result.pendingKey());
        assertNull(result.pendingValue());
    }

    @Test
    void suggestionExtractionSuppressesCompletionsAfterBareFlagPrefixAndWhitespace() {
        var input = new LinkedList<>(Immutables.listOf("pex", "user", "steve", "permissions", "add", "*", "--", " "));

        var result = extractor.extract(input, true);

        assertEquals(Immutables.listOf("pex", "user", "steve", "permissions", "add", "*"), input);
        assertTrue(result.contexts().isEmpty());
        assertNull(result.pendingKey());
        assertNull(result.pendingValue());
    }

    @Test
    void suggestionExtractionAcceptsSingleDashAfterCompleteContextFlag() {
        var input = new LinkedList<>(Immutables.listOf("pex", "user", "steve", "permissions", "add", "*", "--world", "creative", "-"));

        var result = extractor.extract(input, true);

        assertEquals(Immutables.listOf("pex", "user", "steve", "permissions", "add", "*"), input);
        assertTrue(result.contexts().contains("world", "creative"));
        assertEquals("", result.pendingKey());
        assertNull(result.pendingValue());
    }
}
