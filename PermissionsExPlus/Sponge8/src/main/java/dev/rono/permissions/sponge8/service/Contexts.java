package dev.rono.permissions.sponge8.service;

import dev.rono.permissions.api.context.ContextBuilder;
import dev.rono.permissions.api.context.ContextSet;
import dev.rono.permissions.api.permission.PermissionResult;
import dev.rono.permissions.api.resolver.QueryOptions;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.util.Tristate;

/**
 * Conversions between Sponge and PermissionsExPlus context and result types.
 */
final class Contexts {
    private Contexts() {
        throw new AssertionError();
    }

    /** Contexts Sponge's context service computes for a cause. */
    static Set<Context> of(Cause cause) {
        return Sponge.server().serviceProvider().contextService().contextsFor(cause);
    }

    static ContextSet toPex(Set<Context> contexts) {
        if (contexts.isEmpty()) {
            return ContextSet.empty();
        }

        ContextBuilder builder = ContextSet.builder();

        for (Context context : contexts) {
            builder.add(context.getKey(), context.getValue());
        }

        return builder.build();
    }

    static Set<Context> toSponge(ContextSet contexts) {
        if (contexts.isEmpty()) {
            return Collections.emptySet();
        }

        Set<Context> result = new LinkedHashSet<>();

        for (Map.Entry<String, Set<String>> entry : contexts.asMap().entrySet()) {
            for (String value : entry.getValue()) {
                result.add(new Context(entry.getKey(), value));
            }
        }

        return Collections.unmodifiableSet(result);
    }

    static QueryOptions query(Set<Context> contexts) {
        return QueryOptions.builder().contexts(toPex(contexts)).build();
    }

    static Tristate tristate(PermissionResult result) {
        switch (result) {
            case ALLOW :
                return Tristate.TRUE;
            case DENY :
                return Tristate.FALSE;
            default :
                return Tristate.UNDEFINED;
        }
    }

    /** Sponge treats a node as applicable when all of its contexts are active. */
    static boolean applies(ContextSet node, ContextSet active) {
        for (Map.Entry<String, Set<String>> entry : node.asMap().entrySet()) {
            if (!active.values(entry.getKey()).containsAll(entry.getValue())) {
                return false;
            }
        }

        return true;
    }
}
