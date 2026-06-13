package ru.tehkode.permissions;

import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import ru.tehkode.permissions.bukkit.PermissionsEx;

import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

public class LegacyStubContractTest {

    @Test
    public void permissionsExStaticEntryPointsMatchClassicSurface() throws NoSuchMethodException {
        assertTrue(Modifier.isFinal(PermissionsEx.class.getModifiers()));
        PermissionsEx.class.getMethod("getPlugin");
        PermissionsEx.class.getMethod("isAvailable");
        PermissionsEx.class.getMethod("getApi");
        PermissionsEx.class.getMethod("getUser", Player.class);
        PermissionsEx.class.getMethod("getUser", String.class);
        assertThrows(NoSuchMethodException.class, () -> PermissionsEx.class.getDeclaredMethod("isEnabled"));
        assertThrows(NoSuchMethodException.class, () -> PermissionsEx.class.getDeclaredMethod("getBackend"));
        assertFalse(hasMethod(PermissionsEx.class, "getUser", java.util.UUID.class));
    }

    private static boolean hasMethod(Class<?> type, String name, Class<?>... params) {
        try {
            type.getMethod(name, params);
            return true;
        } catch (NoSuchMethodException ex) {
            return false;
        }
    }
}
