package dev.rono.permissions.api.permission;

import lombok.var;

import dev.rono.permissions.api.context.ContextSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PermissionNodeTest {

    @Test
    void permissionBuilderUsesExpectedDefaults() {
        var node = PermissionNode.builder()
                .permission("example.test")
                .build();

        assertTrue(node.allowed());

        assertFalse(node.contextual());

        assertFalse(node.temporary());
    }

    @Test
    void copyBuilderPreservesNodeProperties() {
        var original = PermissionNode.builder()
                .permission("example.test")
                .value(PermissionValue.DENY)
                .contexts(ContextSet.builder()
                        .add("world", "nether")
                        .build())
                .expiry(Instant.parse("2030-01-01T00:00:00Z"))
                .build();

        assertEquals(original, PermissionNode.builder(original).build());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\t", "\n"})
    void permissionBuilderRejectsBlankPermissions(String permission) {
        assertThrows(IllegalArgumentException.class, () -> PermissionNode.builder()
                .permission(permission));
    }

    @Test
    void permissionBuilderRejectsNullPermission() {
        assertThrows(NullPointerException.class, () -> PermissionNode.builder()
                .permission(null));
    }

    @Test
    void permissionBuilderRequiresPermissionBeforeBuild() {
        assertThrows(IllegalStateException.class, () -> PermissionNode.builder().build());
    }

    @Test
    void permissionBuilderRejectsNullProperties() {
        var builder = PermissionNode.builder().permission("example.test");

        assertAll(
                () -> assertThrows(NullPointerException.class, () -> builder.value(null)),
                () -> assertThrows(NullPointerException.class, () -> builder.contexts(null)),
                () -> assertThrows(NullPointerException.class, () -> builder.expiry(null)),
                () -> assertThrows(NullPointerException.class, () -> builder.duration(null)),
                () -> assertThrows(NullPointerException.class, () -> PermissionNode.builder(null)),
                () -> assertThrows(NullPointerException.class, () -> builder.build().expiredAt(null)));
    }

    @Test
    void durationMustBePositive() {
        var builder = PermissionNode.builder().permission("example.test");

        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> builder.duration(Duration.ZERO)),
                () -> assertThrows(IllegalArgumentException.class, () -> builder.duration(Duration.ofSeconds(-1))));
    }

    @Test
    void durationSetsAnExpiryRelativeToNow() {
        var earliestExpected = Instant.now().plus(Duration.ofMinutes(59));

        var node = PermissionNode.builder()
                .permission("example.test")
                .duration(Duration.ofHours(1))
                .build();

        var latestExpected = Instant.now().plus(Duration.ofMinutes(61));

        var expiry = node.expiry().get();

        assertTrue(!expiry.isBefore(earliestExpected) && !expiry.isAfter(latestExpected));
    }

    @Test
    void permanentClearsAnExistingExpiry() {
        var node = PermissionNode.builder()
                .permission("example.test")
                .expiry(Instant.parse("2030-01-01T00:00:00Z"))
                .permanent()
                .build();

        assertFalse(node.temporary());

        assertFalse(node.expiry().isPresent());
    }

    @Test
    void convenienceMethodsReflectNodeProperties() {
        var node = PermissionNode.builder()
                .permission("example.test")
                .value(PermissionValue.DENY)
                .contexts(ContextSet.builder().add("world", "nether").build())
                .expiry(Instant.parse("2030-01-01T00:00:00Z"))
                .build();

        assertAll(
                () -> assertFalse(node.allowed()),
                () -> assertTrue(node.denied()),
                () -> assertTrue(node.contextual()),
                () -> assertTrue(node.temporary()));
    }

    @Test
    void expiryIsInclusive() {
        var expiry = Instant.parse("2030-01-01T00:00:00Z");

        var node = PermissionNode.builder()
                .permission("example.test")
                .expiry(expiry)
                .build();

        assertAll(
                () -> assertFalse(node.expiredAt(expiry.minusNanos(1))),
                () -> assertTrue(node.expiredAt(expiry)),
                () -> assertTrue(node.expiredAt(expiry.plusNanos(1))));
    }

    @Test
    void builderReuseDoesNotChangePreviouslyBuiltNode() {
        var builder = PermissionNode.builder().permission("example.test");

        var first = builder.build();

        var second = builder.value(PermissionValue.DENY).build();

        assertTrue(first.allowed());

        assertTrue(second.denied());

        assertNotEquals(first, second);
    }

    @Test
    void copiedNodeCanBeChangedWithoutChangingOriginal() {
        var original = PermissionNode.builder()
                .permission("example.test")
                .value(PermissionValue.DENY)
                .build();

        var changed = PermissionNode.builder(original)
                .permission("example.other")
                .value(PermissionValue.ALLOW)
                .build();

        assertAll(
                () -> assertEquals("example.test", original.permission()),
                () -> assertTrue(original.denied()),
                () -> assertEquals("example.other", changed.permission()),
                () -> assertTrue(changed.allowed()),
                () -> assertNotEquals(original, changed));
    }
}
