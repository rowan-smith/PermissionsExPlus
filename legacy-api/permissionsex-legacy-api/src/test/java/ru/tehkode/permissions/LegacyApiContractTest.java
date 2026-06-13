package ru.tehkode.permissions;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.junit.jupiter.api.Test;
import ru.tehkode.permissions.bukkit.PermissionsExConfig;
import ru.tehkode.permissions.events.PermissionEntityEvent;
import ru.tehkode.permissions.events.PermissionEvent;
import ru.tehkode.permissions.events.PermissionSystemEvent;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Guards the {@code ru.tehkode.*} compile contract against baseline {@code 628215f} / {@code 1.23.4}.
 */
public class LegacyApiContractTest {

    @Test
    public void permissionManagerMatchesClassicPublicSurface() throws NoSuchMethodException {
        assertEquals(0, PermissionManager.TRANSIENT_PERMISSION);

        assertMethod(PermissionManager.class, "shouldCreateUserRecords");
        assertMethod(PermissionManager.class, "getConfiguration");
        assertMethod(PermissionManager.class, "has", Player.class, String.class);
        assertMethod(PermissionManager.class, "has", Player.class, String.class, String.class);
        assertMethod(PermissionManager.class, "has", String.class, String.class, String.class);
        assertMethod(PermissionManager.class, "has", java.util.UUID.class, String.class, String.class);
        assertMethod(PermissionManager.class, "getUser", String.class);
        assertMethod(PermissionManager.class, "cacheUser", String.class, String.class);
        assertMethod(PermissionManager.class, "getUser", Player.class);
        assertMethod(PermissionManager.class, "getUser", java.util.UUID.class);
        assertMethod(PermissionManager.class, "getUsers");
        assertMethod(PermissionManager.class, "getActiveUsers");
        assertMethod(PermissionManager.class, "getUserIdentifiers");
        assertMethod(PermissionManager.class, "getUserNames");
        assertMethod(PermissionManager.class, "resetUser", String.class);
        assertMethod(PermissionManager.class, "resetUser", Player.class);
        assertMethod(PermissionManager.class, "clearUserCache", String.class);
        assertMethod(PermissionManager.class, "clearUserCache", java.util.UUID.class);
        assertMethod(PermissionManager.class, "clearUserCache", Player.class);
        assertMethod(PermissionManager.class, "getGroup", String.class);
        assertMethod(PermissionManager.class, "getGroupList");
        assertMethod(PermissionManager.class, "getDefaultGroups", String.class);
        assertMethod(PermissionManager.class, "resetGroup", String.class);
        assertMethod(PermissionManager.class, "setDebug", boolean.class);
        assertMethod(PermissionManager.class, "isDebug");
        assertMethod(PermissionManager.class, "getRankLadder", String.class);
        assertMethod(PermissionManager.class, "getWorldInheritance", String.class);
        assertMethod(PermissionManager.class, "setWorldInheritance", String.class, java.util.List.class);
        assertMethod(PermissionManager.class, "getBackend");
        assertMethod(PermissionManager.class, "setBackend", String.class);
        assertMethod(PermissionManager.class, "createBackend", String.class);
        assertMethod(PermissionManager.class, "reset");
        assertMethod(PermissionManager.class, "reset", boolean.class);
        assertMethod(PermissionManager.class, "end");
        assertMethod(PermissionManager.class, "initTimer");
        assertMethod(PermissionManager.class, "getPermissionMatcher");
        assertMethod(PermissionManager.class, "setPermissionMatcher", PermissionMatcher.class);
        assertMethod(PermissionManager.class, "getLogger");
        assertMethod(PermissionManager.class, "getExecutor");
        assertMethod(PermissionManager.class, "shouldSaveDefaultGroup");

        assertMethod(PermissionManager.class, "addPermission", dev.rono.permissions.api.permission.PermissionHolder.class, String.class);
        assertMethod(PermissionManager.class, "addPermission", dev.rono.permissions.api.permission.PermissionHolder.class, String.class, java.time.Duration.class);
        assertMethod(PermissionManager.class, "addPermission", dev.rono.permissions.api.permission.PermissionAddRequest.class);
        assertMethod(PermissionManager.class, "removePermission", dev.rono.permissions.api.permission.PermissionHolder.class, String.class);
        assertMethod(PermissionManager.class, "hasPermission", dev.rono.permissions.api.permission.PermissionHolder.class, String.class);
        assertMethod(PermissionManager.class, "hasPermission", dev.rono.permissions.api.permission.PermissionHolder.class, String.class, Map.class);
        assertMethod(PermissionManager.class, "getPermissions", dev.rono.permissions.api.permission.PermissionHolder.class);

        Set<String> declared = Arrays.stream(PermissionManager.class.getMethods())
                .filter(m -> m.getDeclaringClass() == PermissionManager.class)
                .map(LegacyApiContractTest::signature)
                .collect(Collectors.toSet());
        Set<String> expected = Set.of(
                "addPermission(PermissionHolder,String)",
                "addPermission(PermissionHolder,String,Duration)",
                "addPermission(PermissionAddRequest)",
                "removePermission(PermissionHolder,String)",
                "hasPermission(PermissionHolder,String)",
                "hasPermission(PermissionHolder,String,Map)",
                "hasPermission(PermissionHolder,String,PermissionContext)",
                "getPermissions(PermissionHolder)",
                "shouldCreateUserRecords()",
                "getConfiguration()",
                "has(Player,String)",
                "has(Player,String,String)",
                "has(String,String,String)",
                "has(UUID,String,String)",
                "getUser(String)",
                "cacheUser(String,String)",
                "getUser(Player)",
                "getUser(UUID)",
                "getUsers()",
                "getActiveUsers()",
                "getUserIdentifiers()",
                "getUserNames()",
                "getUsers(String,String)",
                "getUsers(String)",
                "getUsers(String,String,boolean)",
                "getUsers(String,boolean)",
                "resetUser(String)",
                "resetUser(Player)",
                "clearUserCache(String)",
                "clearUserCache(UUID)",
                "clearUserCache(Player)",
                "getGroup(String)",
                "getGroupList()",
                "getGroups()",
                "getGroupNames()",
                "getGroups(String,String)",
                "getGroups(String)",
                "getGroups(String,String,boolean)",
                "getGroups(String,boolean)",
                "getDefaultGroups(String)",
                "resetGroup(String)",
                "setDebug(boolean)",
                "isDebug()",
                "getRankLadder(String)",
                "getWorldInheritance(String)",
                "setWorldInheritance(String,List)",
                "getBackend()",
                "setBackend(String)",
                "createBackend(String)",
                "reset()",
                "reset(boolean)",
                "end()",
                "initTimer()",
                "getPermissionMatcher()",
                "setPermissionMatcher(PermissionMatcher)",
                "getLogger()",
                "getExecutor()",
                "shouldSaveDefaultGroup()");
        assertEquals(expected, declared);
    }

    @Test
    public void nativeInterfaceMatchesClassicSurface() throws NoSuchMethodException {
        assertMethod(NativeInterface.class, "UUIDToName", java.util.UUID.class);
        assertMethod(NativeInterface.class, "nameToUUID", String.class);
        assertMethod(NativeInterface.class, "isOnline", java.util.UUID.class);
        assertMethod(NativeInterface.class, "getServerUUID");
        assertMethod(NativeInterface.class, "callEvent", PermissionEvent.class);
        assertEquals(5, NativeInterface.class.getMethods().length);
    }

    @Test
    public void permissionsExConfigMatchesClassicSurface() throws NoSuchMethodException {
        assertMethod(PermissionsExConfig.class, "useNetEvents");
        assertMethod(PermissionsExConfig.class, "isDebug");
        assertMethod(PermissionsExConfig.class, "allowOps");
        assertMethod(PermissionsExConfig.class, "userAddGroupsLast");
        assertMethod(PermissionsExConfig.class, "getDefaultBackend");
        assertMethod(PermissionsExConfig.class, "shouldLogPlayers");
        assertMethod(PermissionsExConfig.class, "createUserRecords");
        assertMethod(PermissionsExConfig.class, "saveDefaultGroup");
        assertMethod(PermissionsExConfig.class, "updaterEnabled");
        assertMethod(PermissionsExConfig.class, "alwaysUpdate");
        assertMethod(PermissionsExConfig.class, "informPlayers");
        assertMethod(PermissionsExConfig.class, "getServerTags");
        assertMethod(PermissionsExConfig.class, "getBasedir");
        assertMethod(PermissionsExConfig.class, "getBackendConfig", String.class);
        assertEquals(ConfigurationSection.class, PermissionsExConfig.class.getMethod("getBackendConfig", String.class).getReturnType());
        assertMethod(PermissionsExConfig.class, "save");
    }

    @Test
    public void permissionEntityEventMatchesClassicShape() throws NoSuchMethodException {
        assertTrue(Event.class.isAssignableFrom(PermissionEvent.class));
        assertTrue(java.io.Serializable.class.isAssignableFrom(PermissionEvent.class));
        assertMethod(PermissionEntityEvent.class, "getAction");
        assertMethod(PermissionEntityEvent.class, "getEntity");
        assertMethod(PermissionEntityEvent.class, "getEntityIdentifier");
        assertMethod(PermissionEntityEvent.class, "getType");
        assertMethod(PermissionEntityEvent.class, "getHandlers");
        assertMethod(PermissionEntityEvent.class, "getHandlerList");
        assertFalse(hasMethod(PermissionEntityEvent.class, "getEntityType"));
        assertEquals(
                1,
                Arrays.stream(PermissionEntityEvent.class.getConstructors())
                        .filter(c -> !Modifier.isPrivate(c.getModifiers()))
                        .count());
        assertEquals(
                PermissionEntity.class,
                PermissionEntityEvent.class
                        .getConstructor(java.util.UUID.class, PermissionEntity.class, PermissionEntityEvent.Action.class)
                        .getParameterTypes()[1]);
    }

    @Test
    public void permissionSystemEventHasDedicatedHandlerList() throws NoSuchMethodException {
        assertMethod(PermissionSystemEvent.class, "getHandlers");
        assertMethod(PermissionSystemEvent.class, "getHandlerList");
        assertEquals(HandlerList.class, PermissionSystemEvent.class.getMethod("getHandlers").getReturnType());
    }

    private static void assertMethod(Class<?> type, String name, Class<?>... params) throws NoSuchMethodException {
        type.getMethod(name, params);
    }

    private static boolean hasMethod(Class<?> type, String name, Class<?>... params) {
        try {
            type.getMethod(name, params);
            return true;
        } catch (NoSuchMethodException ex) {
            return false;
        }
    }

    private static String signature(Method method) {
        String params = Arrays.stream(method.getParameterTypes())
                .map(Class::getSimpleName)
                .collect(Collectors.joining(","));
        return method.getName() + "(" + params + ")";
    }
}
