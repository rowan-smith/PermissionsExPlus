package dev.rono.permissions.bootstrap;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlatformDescriptorTest {

    @Test
    void runtimeBannerLineFormatsFamilies() {
        assertEquals(
                "Paper 1.21.1 (build 123) (Bukkit family)",
                new PlatformDescriptor("Paper", "1.21.1", PlatformFamily.BUKKIT, "build 123")
                        .runtimeBannerLine());
        assertEquals(
                "Waterfall 1.21 (BungeeCord family)",
                new PlatformDescriptor("Waterfall", "1.21", PlatformFamily.BUNGEECORD, null)
                        .runtimeBannerLine());
        assertEquals(
                "Velocity 3.4.0 (Velocity family)",
                new PlatformDescriptor("Velocity", "3.4.0", PlatformFamily.VELOCITY, " ")
                        .runtimeBannerLine());
        assertEquals(
                "Sponge 8.2.0 (Sponge family)",
                new PlatformDescriptor("Sponge", "8.2.0", PlatformFamily.SPONGE, null).runtimeBannerLine());
        assertEquals(
                "Custom (Unknown family)",
                new PlatformDescriptor("Custom", null, PlatformFamily.UNKNOWN, null).runtimeBannerLine());
    }
}
