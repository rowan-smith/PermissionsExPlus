package dev.rono.permissions.bungee;

import dev.rono.permissions.runtime.legacy.ProxyLegacyHookPluginDetector;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginDescription;
import net.md_5.bungee.api.plugin.PluginManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BungeeLegacyHookPluginDetectorTest {

    @Mock
    private PluginManager pluginManager;
    @Mock
    private Plugin self;
    @Mock
    private Plugin hook;
    @Mock
    private PluginDescription hookDescription;

    @Test
    void findsHookDeclaringPexDependency() {
        when(pluginManager.getPlugins()).thenReturn(List.of(self, hook));
        when(hook.getDescription()).thenReturn(hookDescription);
        when(hookDescription.getName()).thenReturn("HookPlugin");
        when(hookDescription.getDepends()).thenReturn(Set.of("PermissionsEx"));
        when(hookDescription.getSoftDepends()).thenReturn(Set.of());

        ProxyLegacyHookPluginDetector.Candidate found =
                BungeeLegacyHookPluginDetector.findHook(pluginManager, self);
        assertNotNull(found);
        assertNotNull(ProxyLegacyHookPluginDetector.findHook(List.of(found)));
    }

    @Test
    void ignoresUnrelatedPlugins() {
        when(pluginManager.getPlugins()).thenReturn(List.of(hook));
        when(hook.getDescription()).thenReturn(hookDescription);
        when(hookDescription.getName()).thenReturn("VaultBridge");
        when(hookDescription.getDepends()).thenReturn(Set.of("Vault"));
        when(hookDescription.getSoftDepends()).thenReturn(Set.of());

        assertNull(BungeeLegacyHookPluginDetector.findHook(pluginManager, self));
    }
}
