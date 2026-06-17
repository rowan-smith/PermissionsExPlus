package dev.rono.permissions.api.runtime;

import dev.rono.permissions.api.permission.PermissionContext;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ContextResolverTest {

    @Test
    void bukkitInheritanceChain() {
        var resolver = new BukkitContextResolver();
        var ctx = PermissionContext.of("world", "server", null, null);
        var chain = resolver.inheritanceChain(ctx);
        assertEquals(3, chain.size());
        assertEquals("world", resolver.world(chain.get(0)).orElseThrow());
        assertTrue(chain.get(1).get(PermissionContext.SERVER).isPresent());
        assertTrue(chain.get(2).isGlobal());
    }

    @Test
    void proxyInheritanceChainHasNoWorld() {
        var resolver = new VelocityContextResolver();
        assertTrue(resolver.world(PermissionContext.server("lobby")).isEmpty());
        var chain = resolver.inheritanceChain(PermissionContext.server("lobby"));
        assertEquals(2, chain.size());
        assertEquals("lobby", resolver.storageRealm(chain.get(0)).orElseThrow());
    }

    @Test
    void spongeStripsDimensionThenWorldThenServer() {
        var resolver = new SpongeContextResolver();
        var ctx = PermissionContext.of(Map.of(
                PermissionContext.DIMENSION, "the_nether",
                PermissionContext.WORLD, "world",
                PermissionContext.SERVER, "survival"));
        var chain = resolver.inheritanceChain(ctx);
        assertEquals(4, chain.size());
        assertTrue(chain.get(0).get(PermissionContext.DIMENSION).isPresent());
        assertFalse(chain.get(1).get(PermissionContext.DIMENSION).isPresent());
        assertTrue(chain.get(1).get(PermissionContext.WORLD).isPresent());
        assertTrue(chain.get(2).get(PermissionContext.SERVER).isPresent());
        assertTrue(chain.get(3).isGlobal());
    }
}
