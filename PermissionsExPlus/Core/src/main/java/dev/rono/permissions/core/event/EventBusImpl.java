package dev.rono.permissions.core.event;

import lombok.var;

import lombok.Value;
import lombok.experimental.Accessors;
import dev.rono.permissions.api.event.Event;
import dev.rono.permissions.api.event.EventBus;
import dev.rono.permissions.api.event.Subscription;

import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public final class EventBusImpl implements EventBus {
    private final CopyOnWriteArrayList<Listener<?>> listeners = new CopyOnWriteArrayList<>();
    private final Consumer<Throwable> errors;

    public EventBusImpl(Consumer<Throwable> errors) {
        this.errors = Objects.requireNonNull(errors, "errors");
    }

    @Override
    public <E extends Event> Subscription subscribe(Class<E> type, Consumer<? super E> listener) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(listener, "listener");

        var value = new Listener<>(type, listener);

        listeners.add(value);

        return () -> listeners.remove(value);
    }

    public void publish(Event event) {
        Objects.requireNonNull(event, "event");

        for (var listener : listeners) {
            if (listener.type.isInstance(event)) {
                listener.accept(event, errors);
            }
        }
    }

    @Value
    @Accessors(fluent = true)
    private static class Listener<E extends Event> {
        Class<E> type;
        Consumer<? super E> consumer;

        private Listener(Class<E> type, Consumer<? super E> consumer) {
            this.type = type;
            this.consumer = consumer;
        }

        void accept(Event event, Consumer<Throwable> errors) {
            try {
                consumer.accept(type.cast(event));
            } catch (Throwable error) {
                errors.accept(error);
            }
        }
    }
}
