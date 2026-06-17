package dev.rono.permissions.velocity;

import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.PluginDescription;
import com.velocitypowered.api.plugin.PluginManager;
import com.velocitypowered.api.plugin.meta.PluginDependency;
import dev.rono.permissions.runtime.legacy.ProxyLegacyHookPluginDetector;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VelocityLegacyHookPluginDetectorTest {

    @Mock
    private PluginManager pluginManager;
    @Mock
    private PluginContainer self;
    @Mock
    private PluginContainer hook;
    @Mock
    private PluginDescription description;

    @Test
    void findsHookDeclaringPexDependency() {
        PluginDependency dependency = mock(PluginDependency.class);
        when(dependency.getId()).thenReturn("permissionsex");
        when(pluginManager.getPlugins()).thenReturn(List.of(self, hook));
        when(hook.getDescription()).thenReturn(description);
        when(description.getId()).thenReturn("hook-plugin");
        when(description.getDependencies()).thenReturn(List.of(dependency));

        ProxyLegacyHookPluginDetector.Candidate found =
                VelocityLegacyHookPluginDetector.findHook(pluginManager, self);
        assertNotNull(found);
        assertNotNull(ProxyLegacyHookPluginDetector.findHook(List.of(found)));
    }

    @Test
    void skipsPluginsWithoutDescription() {
        when(pluginManager.getPlugins()).thenReturn(List.of(hook));
        when(hook.getDescription()).thenReturn(null);
        assertNull(VelocityLegacyHookPluginDetector.findHook(pluginManager, self));
    }
}
