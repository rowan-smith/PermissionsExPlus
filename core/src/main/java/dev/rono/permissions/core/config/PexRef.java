package dev.rono.permissions.core.config;

/** Current value of something that can change when config reloads (implemented by core’s {@code PexVolatileRef}). */
public interface PexRef<T> {
    T current();
}
