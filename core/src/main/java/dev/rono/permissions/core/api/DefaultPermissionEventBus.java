package dev.rono.permissions.core.api;

import dev.rono.permissions.api.bus.EntityDispatch;
import dev.rono.permissions.api.bus.PermissionDispatch;
import dev.rono.permissions.api.bus.SystemDispatch;
import dev.rono.permissions.api.event.PermissionEventBus;
import dev.rono.permissions.api.event.PermissionEventListener;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

public final class DefaultPermissionEventBus implements PermissionEventBus {
    private final AtomicLong nextId = new AtomicLong();
    private final CopyOnWriteArrayList<Entry> listeners = new CopyOnWriteArrayList<>();

    public void dispatch(PermissionDispatch dispatch) {
        for (Entry entry : listeners) {
            if (dispatch instanceof EntityDispatch entity) {
                entry.listener.onEntity(entity);
            } else if (dispatch instanceof SystemDispatch system) {
                entry.listener.onSystem(system);
            }
        }
    }

    @Override
    public Subscription subscribe(PermissionEventListener listener) {
        Objects.requireNonNull(listener, "listener");
        Entry entry = new Entry(nextId.incrementAndGet(), listener);
        listeners.add(entry);
        return entry;
    }

    @Override
    public void unsubscribe(Subscription subscription) {
        if (subscription instanceof Entry entry) {
            listeners.remove(entry);
        }
    }

    private record Entry(long id, PermissionEventListener listener) implements Subscription {}
}
