package dev.rono.permissions.core;

import lombok.var;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import dev.rono.permissions.api.context.ContextKeys;
import dev.rono.permissions.core.config.AdvancedConfiguration;
import dev.rono.permissions.core.context.ContextManagerImpl;
import dev.rono.permissions.core.context.ContextPolicy;
import dev.rono.permissions.core.context.RuntimeContextRegistry;
import dev.rono.permissions.core.context.RuntimeStateTracker;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import dev.rono.permissions.core.util.Immutables;

class ContextContractTest {
    private ContextManagerImpl contexts;
    private RuntimeStateTracker stateTracker;

    @BeforeEach
    void setUp() {
        var configuration = configuration(true, true, true, true);

        stateTracker = new RuntimeStateTracker(new ContextPolicy(configuration));

        contexts = new ContextManagerImpl(configuration, new RuntimeContextRegistry(), stateTracker);
    }

    @Test
    void combinesStaticDynamicAndCalculatedContexts() {
        var subject = UUID.randomUUID();

        stateTracker.updateState(subject, ContextKeys.WORLD, "survival");

        contexts.registerCalculator((id, consumer) -> consumer.accept(ContextKeys.GAMEMODE.key(), "creative"));

        var result = contexts.contexts(subject);

        assertTrue(result.contains("server", "global"));
        assertTrue(result.contains("environment", "test"));
        assertTrue(result.contains(ContextKeys.WORLD, "survival"));
        assertTrue(result.contains(ContextKeys.GAMEMODE, "creative"));
    }

    @Test
    void registrationCanBeClosedWithoutAffectingOtherCalculators() {
        var subject = UUID.randomUUID();

        var first = contexts.registerCalculator((id, consumer) -> consumer.accept("first", "yes"));

        contexts.registerCalculator((id, consumer) -> consumer.accept("second", "yes"));

        first.close();

        assertFalse(contexts.contexts(subject).contains("first", "yes"));
        assertTrue(contexts.contexts(subject).contains("second", "yes"));
    }

    @Test
    void replaceAndClearManageSubjectContexts() {
        var subject = UUID.randomUUID();

        stateTracker.replaceState(subject, Immutables.mapOf(ContextKeys.WORLD.key(), "nether", ContextKeys.PROXY.key(), "proxy-a"));

        assertTrue(contexts.contexts(subject).contains(ContextKeys.WORLD, "nether"));

        stateTracker.clearState(subject);

        assertFalse(contexts.contexts(subject).contains(ContextKeys.WORLD, "nether"));
    }

    @Test
    void queryOptionsContainTheSameActiveContexts() {
        var subject = UUID.randomUUID();

        stateTracker.updateState(subject, ContextKeys.WORLD, "survival");

        assertEquals(contexts.contexts(subject), contexts.queryOptions(subject).contexts());
    }

    @Test
    void policyFiltersDisabledAndUnknownPlatformValues() {
        var policy = new ContextPolicy(configuration(true, false, true, false));

        var selected = policy.select(Immutables.mapOf(ContextKeys.WORLD.key(), "survival", ContextKeys.GAMEMODE.key(), "creative", ContextKeys.SERVER.key(), "hub", "unknown", "value"));

        assertEquals(Immutables.mapOf(ContextKeys.WORLD.key(), "survival", ContextKeys.SERVER.key(), "hub"), selected);
    }

    private static AdvancedConfiguration configuration(boolean worlds, boolean gameModes, boolean servers, boolean proxies) {
        return new AdvancedConfiguration("none", 10, 30, "offline", "global", worlds, servers, gameModes, proxies, Immutables.mapOf("environment", "test"), 10, "localhost", 6379, "", "channel", 2000, true);
    }
}
