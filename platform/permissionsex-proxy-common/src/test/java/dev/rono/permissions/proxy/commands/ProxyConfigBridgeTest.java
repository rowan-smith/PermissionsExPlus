package dev.rono.permissions.proxy.commands;

import dev.rono.permissions.bungee.BungeePermissionsExConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProxyConfigBridgeTest {

    @TempDir
    Path tempDir;

    @Test
    void readsAndWritesConfigNodes() {
        BungeePermissionsExConfig config =
                new BungeePermissionsExConfig(tempDir.toFile(), Logger.getLogger("test"));
        ProxyConfigBridge bridge = new ProxyConfigBridge(config);

        bridge.set("permissions.debug", true);
        bridge.save();

        assertEquals(true, bridge.get("permissions.debug"));
    }
}
