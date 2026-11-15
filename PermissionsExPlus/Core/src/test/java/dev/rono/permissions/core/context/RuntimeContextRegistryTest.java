package dev.rono.permissions.core.context;

import lombok.var;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import dev.rono.permissions.core.util.Immutables;

class RuntimeContextRegistryTest {

    @Test
    void suppliersAreEvaluatedDynamically() {
        var values = new AtomicReference<>(Immutables.listOf("world"));
        var registry = new RuntimeContextRegistry();

        registry.registerContextType("WORLD", values::get);

        assertEquals(Immutables.listOf("world"), registry.validValues("world"));

        values.set(Immutables.listOf("world_nether", "world", "world_nether"));

        assertEquals(Immutables.listOf("world", "world_nether"), registry.validValues("WORLD"));
    }

    @Test
    void keysAreNormalizedAndReturnedInStableOrder() {
        var registry = new RuntimeContextRegistry();

        registry.registerContextType("world", Immutables::listOf);
        registry.registerContextType("faction", Immutables::listOf);

        assertEquals(Immutables.listOf("faction", "world"), registry.registeredKeys().stream().collect(Immutables.toList()));
    }

    @Test
    void universalContextFlagIsReserved() {
        var registry = new RuntimeContextRegistry();

        assertThrows(IllegalArgumentException.class, () -> registry.registerContextType("context", () -> Immutables.listOf("world=nether")));
    }

    @Test
    void registrationHandlesOnlyRemoveTheirOwnSupplier() {
        var registry = new RuntimeContextRegistry();

        var first = registry.registerContextType("flying", () -> Immutables.listOf("false"));
        var second = registry.registerContextType("flying", () -> Immutables.listOf("false", "true"));

        first.close();

        assertEquals(Immutables.listOf("false", "true"), registry.validValues("flying"));

        second.close();

        assertFalse(registry.registeredKeys().contains("flying"));
    }
}
