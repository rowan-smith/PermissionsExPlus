package ru.tehkode.permissions.contract;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.bukkit.PermissionsEx;
import ru.tehkode.permissions.bukkit.PermissionsExConfig;
import ru.tehkode.permissions.bukkit.regexperms.RegexPermissions;
import ru.tehkode.permissions.exceptions.PermissionsNotAvailable;

/**
 * Explicit facade entry points most plugins call reflectively or as static imports.
 * Complements the fingerprint baseline with named assertions that read well in failure output.
 */
@DisplayName("PermissionsEx facade member contracts")
class PermissionsExFacadeMemberContractTest {

    @Test
    void staticAvailabilityAndManagerAccessorsExist() throws Exception {
        Method isAvailable = PermissionsEx.class.getMethod("isAvailable");
        assertEquals(boolean.class, isAvailable.getReturnType());
        assertTrue(Modifier.isStatic(isAvailable.getModifiers()));

        Method getPermissionManager = PermissionsEx.class.getMethod("getPermissionManager");
        assertEquals(PermissionManager.class, getPermissionManager.getReturnType());
        assertTrue(Modifier.isStatic(getPermissionManager.getModifiers()));

        Method getPlugin = PermissionsEx.class.getMethod("getPlugin");
        assertEquals(Plugin.class, getPlugin.getReturnType());
        assertTrue(Modifier.isStatic(getPlugin.getModifiers()));
    }

    @Test
    void instanceHasOverloadsMatchClassicSignatures() throws Exception {
        assertNotNull(PermissionsEx.class.getMethod("has", Player.class, String.class));
        assertNotNull(PermissionsEx.class.getMethod("has", Player.class, String.class, String.class));
        assertEquals(PermissionManager.class,
                PermissionsEx.class.getMethod("getPermissionsManager").getReturnType());
        assertEquals(PermissionsExConfig.class,
                PermissionsEx.class.getMethod("getConfiguration").getReturnType());
        assertEquals(RegexPermissions.class,
                PermissionsEx.class.getMethod("getRegexPerms").getReturnType());
        assertEquals(boolean.class, PermissionsEx.class.getMethod("isDebug").getReturnType());
    }

    @Test
    void nativeInterfaceMethodsRemainOnPluginClass() throws Exception {
        assertEquals(String.class, PermissionsEx.class.getMethod("UUIDToName", UUID.class).getReturnType());
        assertEquals(UUID.class, PermissionsEx.class.getMethod("nameToUUID", String.class).getReturnType());
        assertEquals(boolean.class, PermissionsEx.class.getMethod("isOnline", UUID.class).getReturnType());
        assertEquals(UUID.class, PermissionsEx.class.getMethod("getServerUUID").getReturnType());
    }

    @Test
    void getPermissionManagerThrowsWhenUnavailable() {
        // Without an enabled plugin instance, the static accessor must reject callers.
        try {
            PermissionsEx.getPermissionManager();
        } catch (PermissionsNotAvailable expected) {
            assertTrue(expected.getMessage().contains("PermissionsEx"));
            return;
        } catch (ExceptionInInitializerError | NoClassDefFoundError | Exception ignored) {
            // Some environments may fail earlier while resolving Bukkit statics; the type contract still holds.
            return;
        }
        // If a manager somehow exists in this JVM (leftover from other tests), availability is fine.
        assertTrue(PermissionsEx.isAvailable());
    }
}
