package dev.rono.permissions.core;

import lombok.var;

import static dev.rono.permissions.core.RuntimeFixture.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.rono.permissions.api.context.ContextSet;
import dev.rono.permissions.api.permission.PermissionHolder;
import dev.rono.permissions.api.permission.PermissionResult;
import dev.rono.permissions.api.resolver.QueryOptions;
import dev.rono.permissions.core.engine.PermissionEngine;
import dev.rono.permissions.core.engine.PermissionEngines;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

/**
 * The Casbin engine stores one compiled policy set per subject. Two checks for
 * the same subject can compile different sets (for example a check racing a
 * modification, each holding a different snapshot), so synchronising and
 * enforcing must be atomic or one thread evaluates against the other's
 * policies.
 */
class CasbinConcurrencyTest {
    private static final int ITERATIONS = 5_000;

    @Test
    void concurrentChecksForTheSameSubjectSeeTheirOwnPolicySet() throws Exception {
        var runtime = new RuntimeFixture();
        // Uncached engine: every check goes through Casbin sync + enforce.
        var engine = PermissionEngines.create(runtime.support);

        var id = UUID.randomUUID();
        var original = await(runtime.users.create(id, "Alex"));

        // Same subject key ("user:<id>"), different compiled policy sets.
        var allowing = await(runtime.users.modify(original, modifier -> modifier.allowPermission("kit.use")));
        var denying = await(runtime.users.modify(allowing, modifier -> modifier.denyPermission("kit.use")));

        var options = QueryOptions.global();

        assertEquals(PermissionResult.ALLOW, engine.check(allowing, "kit.use", options));
        assertEquals(PermissionResult.DENY, engine.check(denying, "kit.use", options));

        var mismatches = new AtomicInteger();
        var start = new CountDownLatch(1);
        var pool = Executors.newFixedThreadPool(2);

        try {
            var futures = new ArrayList<Future<?>>();

            futures.add(pool.submit(() -> hammer(start, mismatches, engine, allowing, options, PermissionResult.ALLOW)));
            futures.add(pool.submit(() -> hammer(start, mismatches, engine, denying, options, PermissionResult.DENY)));

            start.countDown();

            for (var future : futures) {
                future.get(60, TimeUnit.SECONDS);
            }
        } finally {
            pool.shutdownNow();
        }

        assertEquals(0, mismatches.get(), "checks observed another thread's policy set");
    }

    @Test
    void contextualChecksStayCorrectUnderConcurrency() throws Exception {
        var runtime = new RuntimeFixture();
        var engine = PermissionEngines.create(runtime.support);

        var survival = ContextSet.builder().add("world", "survival").build();
        var creative = ContextSet.builder().add("world", "creative").build();

        var user = await(runtime.users.create(UUID.randomUUID(), "Alex"));

        var holder = await(runtime.users.modify(user, modifier -> {
            modifier.allowPermission("kit.use", survival);
            modifier.denyPermission("kit.use", creative);
        }));

        var survivalOptions = QueryOptions.builder().contexts(survival).build();
        var creativeOptions = QueryOptions.builder().contexts(creative).build();

        var mismatches = new AtomicInteger();
        var start = new CountDownLatch(1);
        var pool = Executors.newFixedThreadPool(2);

        try {
            var a = pool.submit(() -> hammer(start, mismatches, engine, holder, survivalOptions, PermissionResult.ALLOW));
            var b = pool.submit(() -> hammer(start, mismatches, engine, holder, creativeOptions, PermissionResult.DENY));

            start.countDown();

            a.get(60, TimeUnit.SECONDS);
            b.get(60, TimeUnit.SECONDS);
        } finally {
            pool.shutdownNow();
        }

        assertEquals(0, mismatches.get());
    }

    private static void hammer(CountDownLatch start, AtomicInteger mismatches, PermissionEngine engine, PermissionHolder holder, QueryOptions options, PermissionResult expected) {
        try {
            start.await();
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            return;
        }

        for (int i = 0; i < ITERATIONS; i++) {
            if (engine.check(holder, "kit.use", options) != expected) {
                mismatches.incrementAndGet();
            }
        }
    }
}
