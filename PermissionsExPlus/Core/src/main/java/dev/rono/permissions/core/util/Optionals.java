package dev.rono.permissions.core.util;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

/** Java 8 stand-ins for Optional methods added in Java 9. */
public final class Optionals {
    private Optionals() {
        throw new AssertionError();
    }

    /** {@code Optional.stream()}. */
    public static <T> Stream<T> stream(Optional<T> optional) {
        return optional.isPresent() ? Stream.of(optional.get()) : Stream.<T>empty();
    }

    /** {@code Optional.or(supplier)}. */
    @SuppressWarnings("unchecked")
    public static <T> Optional<T> or(Optional<T> optional, Supplier<? extends Optional<? extends T>> supplier) {
        return optional.isPresent() ? optional : (Optional<T>) supplier.get();
    }
}
