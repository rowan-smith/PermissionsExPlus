package dev.rono.permissions.api.permission;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PermissionAddRequestBuilderTest {

    private static final PermissionHolder HOLDER = new PermissionHolder() {
        @Override
        public UUID getId() {
            return UUID.randomUUID();
        }

        @Override
        public HolderType getType() {
            return HolderType.USER;
        }
    };

    @Test
    void buildRequiresHolderAndPermission() {
        assertThrows(IllegalStateException.class, () -> PermissionAddRequest.builder()
                .permission("node")
                .build());
        assertThrows(IllegalStateException.class, () -> PermissionAddRequest.builder()
                .holder(HOLDER)
                .permission("")
                .build());
    }

    @Test
    void rejectsDurationAndExpiresAtTogether() {
        assertThrows(IllegalStateException.class, () -> PermissionAddRequest.builder()
                .holder(HOLDER)
                .permission("node")
                .duration(Duration.ofHours(1))
                .expiresAt(Instant.now().plusSeconds(60))
                .build());
    }

    @Test
    void durationResolvesExpiry() {
        Instant before = Instant.now();
        var request = PermissionAddRequest.builder()
                .holder(HOLDER)
                .permission("timed.node")
                .duration(Duration.ofMinutes(5))
                .build();
        assertNotNull(request.expiresAt());
        assertTrue(request.expiresAt().isAfter(before));
        assertEquals(Duration.ofMinutes(5), request.duration());
    }

    @Test
    void contextDefaultsAndLegacyMap() {
        var request = PermissionAddRequest.builder()
                .holder(HOLDER)
                .permission("scoped.node")
                .context((PermissionContext) null)
                .build();
        assertTrue(request.context().isGlobal());

        var mapped = PermissionAddRequest.builder()
                .holder(HOLDER)
                .permission("scoped.node")
                .context(Map.of(PermissionContext.WORLD, "lobby"))
                .build();
        assertEquals("lobby", mapped.context().get(PermissionContext.WORLD).orElseThrow());
        assertEquals(Map.of(PermissionContext.WORLD, "lobby"), mapped.contextMap());
    }

    @Test
    void addContextRemovesBlankValues() {
        var request = PermissionAddRequest.builder()
                .holder(HOLDER)
                .permission("node")
                .addContext(PermissionContext.WORLD, "world")
                .addContext(PermissionContext.SERVER, "")
                .build();
        assertEquals("world", request.context().get(PermissionContext.WORLD).orElseThrow());
        assertTrue(request.context().get(PermissionContext.SERVER).isEmpty());
    }
}
