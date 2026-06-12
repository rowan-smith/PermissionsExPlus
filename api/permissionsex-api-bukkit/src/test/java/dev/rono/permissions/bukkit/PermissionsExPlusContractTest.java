package dev.rono.permissions.bukkit;

import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PermissionsExPlusContractTest {

    @Test
    void modernStaticEntryPointsMatchDocumentedSurface() throws NoSuchMethodException {
        assertTrue(Modifier.isFinal(PermissionsExPlus.class.getModifiers()));
        PermissionsExPlus.class.getMethod("getPlugin");
        PermissionsExPlus.class.getMethod("isAvailable");
        PermissionsExPlus.class.getMethod("getPermissionService");
        PermissionsExPlus.class.getMethod("getUser", Player.class);
        PermissionsExPlus.class.getMethod("getUser", String.class);
        PermissionsExPlus.class.getMethod("getUser", UUID.class);
        assertThrows(NoSuchMethodException.class, () -> PermissionsExPlus.class.getDeclaredMethod("getPermissionManager"));
    }
}
