package dev.rono.permissions.api.runtime;

import dev.rono.permissions.api.permission.PermissionContext;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ContextResolverPlatformApiTest {

    @Test
    void bukkitChainIncludesWorldServerAndGlobal() {
        var resolver = new BukkitContextResolver();
        var chain = resolver.inheritanceChain(PermissionContext.of("world", "server", null, null));
        assertEquals(3, chain.size());
        assertEquals("world", resolver.world(chain.get(0)).orElseThrow());
        assertTrue(chain.get(2).isGlobal());
    }

    @Test
    void proxyChainOmitsWorldAndFallsBackStorageRealm() {
        var resolver = new VelocityContextResolver();
        assertTrue(resolver.world(PermissionContext.server("lobby")).isEmpty());
        assertEquals("lobby", resolver.storageRealm(PermissionContext.server("lobby")).orElseThrow());
        assertEquals(
                "fallback-world",
                resolver.storageRealm(PermissionContext.world("fallback-world")).orElseThrow());
    }

    @Test
    void spongeStripsDimensionWorldAndServer() {
        var resolver = new SpongeContextResolver();
        var chain = resolver.inheritanceChain(PermissionContext.of(Map.of(
                PermissionContext.DIMENSION, "the_nether",
                PermissionContext.WORLD, "world",
                PermissionContext.SERVER, "survival")));
        assertEquals(4, chain.size());
        assertTrue(chain.get(0).get(PermissionContext.DIMENSION).isPresent());
        assertFalse(chain.get(1).get(PermissionContext.DIMENSION).isPresent());
        assertTrue(chain.get(3).isGlobal());
    }

    @Test
    void switchableBusActivatesDelegate() {
        var bus = new SwitchablePlatformEventBus();
        assertFalse(bus.isActive());
        var received = new java.util.ArrayList<Object>();
        bus.activate(received::add);
        var dispatch = new dev.rono.permissions.api.bus.EntityDispatch(
                java.util.UUID.randomUUID(), "bob", "USER", dev.rono.permissions.api.bus.EntityMutation.SAVED);
        bus.publish(dispatch);
        assertTrue(received.contains(dispatch));
    }
}
