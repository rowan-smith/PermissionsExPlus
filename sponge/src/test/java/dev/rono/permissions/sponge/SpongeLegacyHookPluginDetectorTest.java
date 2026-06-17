package dev.rono.permissions.sponge;

import dev.rono.permissions.runtime.legacy.ProxyLegacyHookPluginDetector;
import org.junit.jupiter.api.Test;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.metadata.PluginMetadata;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SpongeLegacyHookPluginDetectorTest {

    @Test
    void skipsSelfAndReturnsNullWithoutHookRelationship() {
        PluginContainer self = mockContainer("permissionsexplus");
        PluginContainer other = mockContainer("vault-bridge");
        assertNull(SpongeLegacyHookPluginDetector.findHook(List.of(self, other), self));
    }

    @Test
    void emptyPluginListHasNoHook() {
        PluginContainer self = mockContainer("permissionsexplus");
        assertNull(SpongeLegacyHookPluginDetector.findHook(List.of(), self));
    }

    @Test
    void candidateUsesPluginId() {
        PluginContainer hook = mockContainer("hook-plugin");
        ProxyLegacyHookPluginDetector.Candidate candidate = new ProxyLegacyHookPluginDetector.Candidate(
                hook.metadata().id(), List.of(), List.of(), null);
        assertNull(ProxyLegacyHookPluginDetector.findHook(List.of(candidate)));
    }

    private static PluginContainer mockContainer(String id) {
        PluginContainer container = mock(PluginContainer.class);
        PluginMetadata metadata = mock(PluginMetadata.class);
        when(container.metadata()).thenReturn(metadata);
        when(metadata.id()).thenReturn(id);
        when(container.instance()).thenReturn(null);
        return container;
    }
}
