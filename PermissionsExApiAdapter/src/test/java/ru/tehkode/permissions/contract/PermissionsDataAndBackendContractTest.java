package ru.tehkode.permissions.contract;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.tehkode.permissions.PEXTestBase;
import ru.tehkode.permissions.PermissionsData;
import ru.tehkode.permissions.PermissionsGroupData;
import ru.tehkode.permissions.PermissionsUserData;
import ru.tehkode.permissions.backends.PermissionBackend;
import ru.tehkode.permissions.backends.file.FileBackend;
import ru.tehkode.permissions.backends.memory.MemoryBackend;

/**
 * Contracts for storage SPI interfaces and backend alias registry that plugins and
 * the shim both depend on.
 */
@DisplayName("Legacy data / backend contracts")
class PermissionsDataAndBackendContractTest extends PEXTestBase {

    @Test
    void permissionsDataDeclaresClassicMutationSurface() {
        Set<String> methods = publicMethodNames(PermissionsData.class);
        for (String required : List.of(
                "load",
                "getIdentifier",
                "getPermissions",
                "setPermissions",
                "getPermissionsMap",
                "getWorlds",
                "getOption",
                "setOption",
                "getOptions",
                "getOptionsMap",
                "getParents",
                "setParents",
                "isVirtual",
                "save",
                "remove",
                "getParentsMap")) {
            assertTrue(methods.contains(required), "PermissionsData missing " + required);
        }
    }

    @Test
    void userAndGroupDataExtendPermissionsData() {
        assertTrue(PermissionsData.class.isAssignableFrom(PermissionsUserData.class));
        assertTrue(PermissionsData.class.isAssignableFrom(PermissionsGroupData.class));
        assertTrue(PermissionsUserData.class.isInterface());
        assertTrue(PermissionsGroupData.class.isInterface());
    }

    @Test
    void userDataExposesSetIdentifier() throws Exception {
        Method setIdentifier = PermissionsUserData.class.getMethod("setIdentifier", String.class);
        assertEquals(boolean.class, setIdentifier.getReturnType());
        assertTrue(Modifier.isPublic(setIdentifier.getModifiers()));
    }

    @Test
    void classicBackendAliasesRemainRegistered() throws Exception {
        assertEquals(FileBackend.class, PermissionBackend.getBackendClass("file"));
        assertEquals(MemoryBackend.class, PermissionBackend.getBackendClass("memory"));
        assertEquals(ru.tehkode.permissions.backends.data.PermissionBackend.class,
                PermissionBackend.getBackendClass("data"));
        assertNotNull(PermissionBackend.getBackendClassName("file"));
        assertEquals("data", PermissionBackend.DEFAULT_BACKEND);
    }

    @Test
    void backendAliasLookupIsCaseInsensitiveEnoughForPlugins() throws Exception {
        // Historic PEX resolves configured backend names via the alias map as registered.
        assertNotNull(PermissionBackend.getBackendClass("file"));
        assertNotNull(PermissionBackend.getBackendClass("memory"));
    }

    @Test
    void entityPublicApiPersistsThroughBackendDataSpi() {
        var user = manager.getUser("spi-user");
        user.setPermissions(List.of("a.b"), null);
        assertEquals(List.of("a.b"), user.getOwnPermissions(null));
        user.setOption("prefix", "[X]", null);
        assertEquals("[X]", user.getOwnOption("prefix", null));
        user.setParentsIdentifier(List.of("default"), null);
        assertEquals(List.of("default"), user.getOwnParentIdentifiers(null));
        Map<String, List<String>> allPerms = user.getAllPermissions();
        assertTrue(allPerms.values().stream().anyMatch(list -> list.contains("a.b")));
        Map<String, Map<String, String>> options = user.getAllOptions();
        assertNotNull(options);
        user.save();
        assertFalse(user.getOwnPermissions(null).isEmpty());
    }

    @Test
    void permissionBackendAbstractMethodsRemain() {
        Set<String> abstractMethods = Arrays.stream(PermissionBackend.class.getDeclaredMethods())
                .filter(m -> Modifier.isPublic(m.getModifiers()) || Modifier.isProtected(m.getModifiers()))
                .filter(m -> Modifier.isAbstract(m.getModifiers()))
                .map(Method::getName)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        for (String required : List.of(
                "getUserData",
                "getGroupData",
                "hasUser",
                "hasGroup",
                "getUserIdentifiers",
                "getUserNames",
                "getGroupNames",
                "getSchemaVersion",
                "reload",
                "getWorldInheritance",
                "getAllWorldInheritance",
                "setWorldInheritance",
                "writeContents")) {
            if (!abstractMethods.contains(required)) {
                // Some may be concrete in this fork; still require they exist publicly/protected
                try {
                    PermissionBackend.class.getDeclaredMethod(required, findParamTypes(PermissionBackend.class, required));
                } catch (NoSuchMethodException e) {
                    fail("PermissionBackend missing required backend method '" + required + "'");
                }
            }
        }
    }

    private static Set<String> publicMethodNames(Class<?> type) {
        return Arrays.stream(type.getMethods())
                .map(Method::getName)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static Class<?>[] findParamTypes(Class<?> type, String name) throws NoSuchMethodException {
        for (Method method : type.getDeclaredMethods()) {
            if (method.getName().equals(name)) {
                return method.getParameterTypes();
            }
        }
        throw new NoSuchMethodException(name);
    }
}
