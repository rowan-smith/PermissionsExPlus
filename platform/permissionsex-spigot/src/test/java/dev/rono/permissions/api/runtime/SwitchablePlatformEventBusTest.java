package dev.rono.permissions.api.runtime;

import dev.rono.permissions.api.bus.EntityDispatch;
import dev.rono.permissions.api.bus.EntityMutation;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SwitchablePlatformEventBusTest {

    @Test
    void startsInactiveAndActivatesDelegate() {
        var bus = new SwitchablePlatformEventBus();
        assertFalse(bus.isActive());

        var received = new ArrayList<Object>();
        bus.activate(dispatch -> received.add(dispatch));
        assertTrue(bus.isActive());

        var dispatch = new EntityDispatch(
                UUID.randomUUID(),
                "alice",
                "USER",
                EntityMutation.PERMISSIONS_CHANGED);
        bus.publish(dispatch);
        assertTrue(received.contains(dispatch));
    }

    @Test
    void noOpBeforeActivation() {
        var bus = new SwitchablePlatformEventBus();
        bus.publish(new EntityDispatch(
                UUID.randomUUID(),
                "bob",
                "USER",
                EntityMutation.SAVED));
        assertFalse(bus.isActive());
    }
}
