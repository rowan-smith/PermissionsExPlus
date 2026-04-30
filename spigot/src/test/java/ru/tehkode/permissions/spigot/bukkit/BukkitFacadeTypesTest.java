package ru.tehkode.permissions.spigot.bukkit;

import java.lang.reflect.Modifier;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import ru.tehkode.permissions.bukkit.PermissionsEx;
import ru.tehkode.permissions.spigot.backends.FileBackend;
import ru.tehkode.permissions.spigot.backends.MemoryBackend;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Validates classic surface types that live in {@code ru.tehkode.permissions.backends} alongside Spigot.
 */
public final class BukkitFacadeTypesTest {

    @Test
    public void fileAndMemoryFacadesDelegateToHistoricalImplementations() {
        assertTrue(ru.tehkode.permissions.spigot.backends.file.FileBackend.class.isAssignableFrom(FileBackend.class));
        assertTrue(ru.tehkode.permissions.spigot.backends.memory.MemoryBackend.class.isAssignableFrom(MemoryBackend.class));
        assertFalse(Modifier.isFinal(FileBackend.class.getModifiers()));
        assertFalse(Modifier.isFinal(MemoryBackend.class.getModifiers()));
    }

    @Test
    public void pluginEntrypointsMatchOneTwentyThreeContracts() throws NoSuchMethodException {
        PermissionsEx.class.getMethod("getPlugin");
        PermissionsEx.class.getMethod("isEnabled");
        PermissionsEx.class.getMethod("isAvailable");
        PermissionsEx.class.getMethod("getPermissionManager");
        PermissionsEx.class.getMethod("getBackend");
        PermissionsEx.class.getMethod("getUser", Player.class);
        PermissionsEx.class.getMethod("getUser", String.class);
        PermissionsEx.class.getMethod("getUser", UUID.class);

        SpigotPermissionsExPlugin.class.getDeclaredMethod("getPermissionsManager");
        SpigotPermissionsExPlugin.class.getDeclaredMethod("has", Player.class, String.class);
        SpigotPermissionsExPlugin.class.getDeclaredMethod("has", Player.class, String.class, String.class);
    }
}
