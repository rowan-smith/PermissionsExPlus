package dev.rono.permissions.bootstrap;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class BootstrapArtifactsTest {

    @Test
    void namespaceAnchorLoads() {
        assertNotNull(BootstrapArtifacts.class);
    }
}
