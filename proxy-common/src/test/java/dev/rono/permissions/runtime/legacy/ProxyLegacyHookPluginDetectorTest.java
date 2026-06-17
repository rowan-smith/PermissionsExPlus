package dev.rono.permissions.runtime.legacy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProxyLegacyHookPluginDetectorTest {

    @Test
    void detectsPexDependency() {
        var hook = ProxyLegacyHookPluginDetector.findHook(List.of(
                new ProxyLegacyHookPluginDetector.Candidate(
                        "HookPlugin",
                        List.of("PermissionsEx"),
                        List.of(),
                        null)));
        assertNotNull(hook);
        assertTrue(ProxyLegacyHookPluginDetector.declaresPexRelationship(hook));
    }

    @Test
    void ignoresUnrelatedPlugins() {
        var hook = ProxyLegacyHookPluginDetector.findHook(List.of(
                new ProxyLegacyHookPluginDetector.Candidate(
                        "Other",
                        List.of("Vault"),
                        List.of(),
                        null)));
        assertNull(hook);
    }

    @Test
    void emptyPluginListHasNoHook() {
        assertNull(ProxyLegacyHookPluginDetector.findHook(List.of()));
    }

    @Test
    void softDependCountsAsRelationship() {
        var candidate = new ProxyLegacyHookPluginDetector.Candidate(
                "Hook",
                List.of(),
                List.of("PermissionsEx"),
                null);
        assertTrue(ProxyLegacyHookPluginDetector.declaresPexRelationship(candidate));
        assertFalse(ProxyLegacyHookPluginDetector.declaresPexRelationship(
                new ProxyLegacyHookPluginDetector.Candidate("Other", List.of("Vault"), List.of(), null)));
    }
}
