package dev.rono.permissions.spigot.platform;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

class SpigotEventPublisherTest {

    @Test
    void legacyListenerGuardsDefaultToFalseWithoutServer() {
        assertFalse(SpigotEventPublisher.hasEntityListeners());
        assertFalse(SpigotEventPublisher.hasSystemListeners());
    }
}
