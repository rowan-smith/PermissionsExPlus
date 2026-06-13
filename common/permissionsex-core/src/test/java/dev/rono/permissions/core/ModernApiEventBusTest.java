package dev.rono.permissions.core;

import dev.rono.permissions.api.bus.EntityMutation;
import dev.rono.permissions.api.bus.SystemMutation;
import dev.rono.permissions.api.event.PermissionEventListener;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/** PermissionEventBus subscription lifecycle and dispatch delivery. */
class ModernApiEventBusTest extends ModernApiTestSupport {

    @Test
    void subscribeReceivesEntityDispatches() {
        var count = new java.util.concurrent.atomic.AtomicInteger(0);
        var sub = api().getEventBus().subscribe(new PermissionEventListener() {
            @Override
            public void onEntity(dev.rono.permissions.api.bus.EntityDispatch dispatch) {
                count.incrementAndGet();
            }
        });

        var user = api().getUserManager().createUser("bus-entity-user");
        user.addPermission("bus.test", null);
        user.save();

        assertTrue(count.get() > 0);
        api().getEventBus().unsubscribe(sub);
    }

    @Test
    void unsubscribeStopsDelivery() {
        var count = new java.util.concurrent.atomic.AtomicInteger(0);
        var sub = api().getEventBus().subscribe(new PermissionEventListener() {
            @Override
            public void onEntity(dev.rono.permissions.api.bus.EntityDispatch dispatch) {
                count.incrementAndGet();
            }
        });
        api().getEventBus().unsubscribe(sub);

        var user = api().getUserManager().createUser("bus-unsub-user");
        user.addPermission("bus.unsub", null);
        user.save();

        assertEquals(0, count.get());
    }

    @Test
    void listenerCanObserveMutationKinds() {
        AtomicReference<EntityMutation> mutation = new AtomicReference<>();
        var sub = api().getEventBus().subscribe(new PermissionEventListener() {
            @Override
            public void onEntity(dev.rono.permissions.api.bus.EntityDispatch dispatch) {
                mutation.compareAndSet(null, dispatch.mutation());
            }
        });

        var user = api().getUserManager().createUser("bus-mutation-user");
        user.addPermission("bus.mutation", null);
        user.save();

        assertNotNull(mutation.get());
        api().getEventBus().unsubscribe(sub);
    }

    @Test
    void systemDispatchTypeExists() {
        assertNotNull(SystemMutation.RELOADED);
        assertNotNull(EntityMutation.SAVED);
    }
}
