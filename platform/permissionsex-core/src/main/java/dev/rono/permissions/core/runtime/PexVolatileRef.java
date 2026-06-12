package dev.rono.permissions.core.runtime;

import java.util.Objects;
import dev.rono.permissions.core.config.PexRef;

/** Thread-safe holder updated on reload. */
public final class PexVolatileRef<T> implements PexRef<T> {
    private volatile T value;

    public PexVolatileRef(T initial) {
        this.value = Objects.requireNonNull(initial, "initial");
    }

    public void replace(T next) {
        this.value = Objects.requireNonNull(next, "next");
    }

    @Override
    public T current() {
        return value;
    }
}
