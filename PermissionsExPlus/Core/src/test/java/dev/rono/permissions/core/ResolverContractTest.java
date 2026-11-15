package dev.rono.permissions.core;

import lombok.var;

import static dev.rono.permissions.core.RuntimeFixture.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import dev.rono.permissions.api.context.ContextSet;
import dev.rono.permissions.api.permission.PermissionResult;
import dev.rono.permissions.api.resolver.CandidateStatus;
import dev.rono.permissions.api.resolver.QueryOptions;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ResolverContractTest {
    private RuntimeFixture runtime;

    @BeforeEach
    void setUp() {
        runtime = new RuntimeFixture();
    }

    @Test
    void directPermissionOverridesInheritedPermission() {
        await(runtime.groups.create("staff"));
        await(runtime.groups.modify("staff", modifier -> modifier.allowPermission("chat.send")));

        var user = await(runtime.users.create(UUID.randomUUID(), "Alex"));

        user = await(runtime.users.modify(user, modifier -> {
            modifier.addGroup("staff");
            modifier.denyPermission("chat.send");
        }));

        assertEquals(PermissionResult.DENY, runtime.resolvers.permissions().check(user, "chat.send", QueryOptions.global()));
    }

    @Test
    void exactPermissionBeatsWildcardBeforeDenyTieBreak() {
        var user = await(runtime.users.create(UUID.randomUUID(), "Alex"));

        user = await(runtime.users.modify(user, modifier -> {
            modifier.denyPermission("world.*");
            modifier.allowPermission("world.build");
        }));

        assertEquals(PermissionResult.ALLOW, runtime.resolvers.permissions().check(user, "world.build", QueryOptions.global()));
    }

    @Test
    void denyWinsAnOtherwiseEqualTie() {
        await(runtime.groups.create("allowed"));
        await(runtime.groups.create("denied"));
        await(runtime.groups.modify("allowed", modifier -> modifier.allowPermission("server.manage")));
        await(runtime.groups.modify("denied", modifier -> modifier.denyPermission("server.manage")));

        var user = await(runtime.users.create(UUID.randomUUID(), "Alex"));

        user = await(runtime.users.modify(user, modifier -> {
            modifier.addGroup("allowed");
            modifier.addGroup("denied");
        }));

        assertEquals(PermissionResult.DENY, runtime.resolvers.permissions().check(user, "server.manage", QueryOptions.global()));
    }

    @Test
    void contextMismatchDoesNotAffectEvaluationAndAppearsInTrace() {
        var survival = ContextSet.builder().add("world", "survival").build();
        var creative = ContextSet.builder().add("world", "creative").build();

        var user = await(runtime.users.create(UUID.randomUUID(), "Alex"));

        user = await(runtime.users.modify(user, modifier -> modifier.allowPermission("kit.use", survival)));

        assertEquals(PermissionResult.ALLOW, runtime.resolvers.permissions().check(user, "kit.use", survival));

        var trace = runtime.resolvers.permissions().explain(user, "kit.use", creative);

        assertEquals(PermissionResult.UNDEFINED, trace.result());
        assertEquals(CandidateStatus.CONTEXT_MISMATCH, trace.candidates().get(0).status());
    }

    @Test
    void expiredNodesAreExcludedAndReported() {
        var user = await(runtime.users.create(UUID.randomUUID(), "Alex"));

        user = await(runtime.users.modify(user, modifier -> modifier.allowTimedPermission("temporary.use", Duration.ofMillis(1))));

        try {
            Thread.sleep(5);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();

            fail(interrupted);
        }

        var trace = runtime.resolvers.permissions().explain(user, "temporary.use", QueryOptions.global());

        assertEquals(PermissionResult.UNDEFINED, trace.result());
        assertEquals(CandidateStatus.EXPIRED, trace.candidates().get(0).status());
    }

    @Test
    void inheritanceAndDefaultsCanBeDisabledIndependently() {
        await(runtime.groups.create("default"));
        await(runtime.groups.create("member"));
        await(runtime.groups.modify("default", modifier -> modifier.allowPermission("default.use")));
        await(runtime.groups.modify("member", modifier -> modifier.addParent("default")));

        var user = await(runtime.users.create(UUID.randomUUID(), "Alex"));
        var noDefaults = QueryOptions.builder().includeDefaults(false).build();

        assertEquals(PermissionResult.UNDEFINED, runtime.resolvers.permissions().check(user, "default.use", noDefaults));

        user = await(runtime.users.modify(user, modifier -> modifier.addGroup("member")));

        var noInheritance = QueryOptions.builder().includeInheritance(false).build();

        assertEquals(PermissionResult.UNDEFINED, runtime.resolvers.permissions().check(user, "default.use", noInheritance));
    }

    @Test
    void optionResolutionUsesContextSpecificityThenWeight() {
        var survival = ContextSet.builder().add("world", "survival").build();

        await(runtime.groups.create("low"));
        await(runtime.groups.create("high"));

        await(runtime.groups.modify("low", modifier -> {
            modifier.setWeight(1);
            modifier.setPrefix("[Low]");
        }));

        await(runtime.groups.modify("high", modifier -> {
            modifier.setWeight(10);
            modifier.setPrefix("[High]");
            modifier.setPrefix("[Survival]", survival);
        }));

        var user = await(runtime.users.create(UUID.randomUUID(), "Alex"));

        user = await(runtime.users.modify(user, modifier -> {
            modifier.addGroup("low");
            modifier.addGroup("high");
        }));

        assertEquals("[High]", runtime.resolvers.options().prefix(user, QueryOptions.global()).get());
        assertEquals("[Survival]", runtime.resolvers.options().prefix(user, survival).get());
    }

    @Test
    void primaryGroupUsesHighestWeightEffectiveGroup() {
        await(runtime.groups.create("member"));
        await(runtime.groups.create("staff"));
        await(runtime.groups.modify("member", modifier -> modifier.setWeight(5)));
        await(runtime.groups.modify("staff", modifier -> modifier.setWeight(50)));

        var user = await(runtime.users.create(UUID.randomUUID(), "Alex"));

        user = await(runtime.users.modify(user, modifier -> {
            modifier.addGroup("member");
            modifier.addGroup("staff");
        }));

        assertEquals("staff", runtime.resolvers.primaryGroup().resolve(user, QueryOptions.global()).get().name());
    }
}
