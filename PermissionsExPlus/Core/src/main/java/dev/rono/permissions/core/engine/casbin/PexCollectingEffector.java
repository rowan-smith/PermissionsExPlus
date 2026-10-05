package dev.rono.permissions.core.engine.casbin;

import java.util.ArrayList;
import java.util.List;
import org.casbin.jcasbin.effect.DefaultEffector;
import org.casbin.jcasbin.effect.Effect;
import org.casbin.jcasbin.effect.Effector;
import org.casbin.jcasbin.effect.StreamEffector;
import org.casbin.jcasbin.effect.StreamEffectorResult;

/**
 * Casbin effector that collects every matched allow/deny policy index.
 *
 * <p>
 * jCasbin's default effectors short-circuit and only expose one explain rule.
 * PEX needs the full matched set for precedence, conflict modes, and explain
 * candidates — this effector never finishes early and records every match.
 * </p>
 */
final class PexCollectingEffector implements Effector {
    private final ThreadLocal<Collector> active = new ThreadLocal<>();

    Collector begin() {
        var collector = new Collector();
        active.set(collector);
        return collector;
    }

    void end() {
        active.remove();
    }

    @Override
    public boolean mergeEffects(String expr, Effect[] effects, float[] results) {
        // Unused when StreamEffector is available; keep a safe fallback.
        return new DefaultEffector().mergeEffects(expr, effects, results);
    }

    @Override
    public StreamEffector newStreamEffector(String expr) {
        var collector = active.get();
        if (collector == null) {
            collector = new Collector();
            active.set(collector);
        }

        return new CollectingStreamEffector(collector);
    }

    static final class Collector {
        private final List<Integer> matchedIndexes = new ArrayList<>();

        List<Integer> matchedIndexes() {
            return matchedIndexes;
        }
    }

    private static final class CollectingStreamEffector implements StreamEffector {
        private final Collector collector;
        private int lastMatched = -1;

        CollectingStreamEffector(Collector collector) {
            this.collector = collector;
        }

        @Override
        public StreamEffectorResult current() {
            return new StreamEffectorResult() {
                @Override
                public boolean hasEffect() {
                    // Decision is owned by CasbinPermissionEngine.decide — not the boolean.
                    return false;
                }

                @Override
                public boolean isDone() {
                    return false;
                }

                @Override
                public int getExplainIndex() {
                    return lastMatched;
                }
            };
        }

        @Override
        public boolean push(Effect effect, int currentIndex, int policySize) {
            if (effect == Effect.Allow || effect == Effect.Deny) {
                collector.matchedIndexes.add(currentIndex);
                lastMatched = currentIndex;
            }

            // Never short-circuit — every policy must be evaluated for PEX precedence.
            return false;
        }
    }
}
