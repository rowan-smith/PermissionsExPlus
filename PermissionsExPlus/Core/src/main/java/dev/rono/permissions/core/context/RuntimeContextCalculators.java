package dev.rono.permissions.core.context;

import lombok.var;

import dev.rono.permissions.api.platform.context.ContextCalculator;
import dev.rono.permissions.api.platform.context.ContextConsumer;
import dev.rono.permissions.api.platform.context.ContextRegistration;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public final class RuntimeContextCalculators {
    private final CopyOnWriteArrayList<ContextCalculator<? super UUID>> calculators = new CopyOnWriteArrayList<>();

    public ContextRegistration register(ContextCalculator<? super UUID> calculator) {
        var registered = Objects.requireNonNull(calculator, "calculator");

        calculators.add(registered);

        return () -> calculators.remove(registered);
    }

    public void calculate(UUID subject, ContextConsumer consumer) {
        calculators.forEach(calculator -> calculator.calculate(subject, consumer));
    }
}
