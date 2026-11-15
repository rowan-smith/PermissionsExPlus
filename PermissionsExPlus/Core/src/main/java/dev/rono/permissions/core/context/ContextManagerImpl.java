package dev.rono.permissions.core.context;

import lombok.var;

import dev.rono.permissions.api.context.ContextRegistry;
import dev.rono.permissions.api.context.ContextSet;
import dev.rono.permissions.api.platform.context.ContextCalculator;
import dev.rono.permissions.api.platform.context.ContextConsumer;
import dev.rono.permissions.api.platform.context.ContextManager;
import dev.rono.permissions.api.platform.context.ContextRegistration;
import dev.rono.permissions.api.resolver.QueryOptions;
import dev.rono.permissions.core.config.AdvancedConfiguration;
import java.util.Objects;
import java.util.UUID;

public final class ContextManagerImpl implements ContextManager<UUID> {
    private final ContextSet staticContexts;
    private final RuntimeContextRegistry registry;
    private final RuntimeStateTracker stateTracker;
    private final RuntimeContextCalculators calculators;

    public ContextManagerImpl(AdvancedConfiguration configuration) {
        this(configuration, new RuntimeContextRegistry(), new RuntimeStateTracker(new ContextPolicy(configuration)), new RuntimeContextCalculators());
    }

    public ContextManagerImpl(AdvancedConfiguration configuration, RuntimeContextRegistry registry) {
        this(configuration, registry, new RuntimeStateTracker(new ContextPolicy(configuration)), new RuntimeContextCalculators());
    }

    public ContextManagerImpl(AdvancedConfiguration configuration, RuntimeContextRegistry registry, RuntimeStateTracker stateTracker) {
        this(configuration, registry, stateTracker, new RuntimeContextCalculators());
    }

    public ContextManagerImpl(AdvancedConfiguration configuration, RuntimeContextRegistry registry, RuntimeStateTracker stateTracker, RuntimeContextCalculators calculators) {
        this.registry = Objects.requireNonNull(registry, "registry");
        this.stateTracker = Objects.requireNonNull(stateTracker, "stateTracker");
        this.calculators = Objects.requireNonNull(calculators, "calculators");

        stateTracker.configure(new ContextPolicy(configuration));

        var builder = ContextSet.builder().add("server", configuration.globalContextName());

        configuration.environment().forEach(builder::add);

        staticContexts = builder.build();
        staticContexts.asMap().forEach((key, values) -> registry.registerContextType(key, () -> values));
    }

    @Override
    public ContextRegistration registerCalculator(ContextCalculator<? super UUID> calculator) {
        Objects.requireNonNull(calculator, "calculator");
        return calculators.register(calculator);
    }

    @Override
    public ContextSet contexts(UUID subject) {
        var builder = ContextSet.builder(staticContexts);

        var value = stateTracker.contexts(subject);

        if (!value.isEmpty()) {
            value.asMap().forEach((key, values) -> values.forEach(entry -> builder.add(key, entry)));
        }

        ContextConsumer consumer = builder::add;

        calculators.calculate(subject, consumer);

        return builder.build();
    }

    @Override
    public QueryOptions queryOptions(UUID subject) {
        return QueryOptions.builder().contexts(contexts(subject)).build();
    }

    @Override
    public ContextSet staticContexts() {
        return staticContexts;
    }

    @Override
    public ContextRegistry registry() {
        return registry;
    }
}
