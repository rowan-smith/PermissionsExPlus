package dev.rono.permissions.api.runtime;

import dev.rono.permissions.api.bus.EntityDispatch;
import dev.rono.permissions.api.bus.EntityMutation;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertSame;

class NoOpPlatformEventBusTest {

    @Test
    void instanceIsSingleton() {
        assertSame(NoOpPlatformEventBus.INSTANCE, NoOpPlatformEventBus.INSTANCE);
    }

    @Test
    void publishIsIgnored() {
        assertDoesNotThrow(() -> NoOpPlatformEventBus.INSTANCE.publish(new EntityDispatch(
                UUID.randomUUID(), "player", "USER", EntityMutation.SAVED)));
    }
}
