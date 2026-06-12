package ru.tehkode.permissions.commands;

import dev.rono.permissions.core.commands.CoreCommandService;
import java.util.List;
import org.junit.jupiter.api.Test;
import ru.tehkode.permissions.PEXTestBase;
import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.exceptions.PermissionBackendException;
import ru.tehkode.permissions.exceptions.RankingException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CoreCommandServiceTest extends PEXTestBase {

    @Test
    void usersAndGroupsAreListedAsNames() {
        PermissionUser user = manager.getUser("Rono");
        user.setOption("name", "Rono", null);
        manager.getGroup("default");

        CoreCommandService service = new CoreCommandService(manager);
        List<String> users = service.knownUsers(10);
        List<String> groups = service.knownGroups();

        assertTrue(users.contains("rono") || users.contains("Rono"));
        assertTrue(groups.contains("default"));
    }

    @Test
    void userPermissionAddAndRemoveRoundTrip() {
        CoreCommandService service = new CoreCommandService(manager);
        String user = "Rono";

        assertEquals("Permission \"test.perm\" added!", service.userAddPermission(user, "test.perm", "proxy"));
        assertTrue(manager.getUser(user).has("test.perm", "proxy"));

        assertEquals("Permission \"test.perm\" removed!", service.userRemovePermission(user, "test.perm", "proxy"));
        assertFalse(manager.getUser(user).has("test.perm", "proxy"));
    }

    @Test
    void groupPermissionAddAndRemoveRoundTrip() {
        CoreCommandService service = new CoreCommandService(manager);
        assertEquals("Permission \"*\" added to group \"default\"!", service.groupAddPermission("default", "*", null));
        assertTrue(manager.getGroup("default").getPermissions(null).contains("*"));

        assertEquals("Permission \"*\" removed from group \"default\"!", service.groupRemovePermission("default", "*", null));
        assertFalse(manager.getGroup("default").getPermissions(null).contains("*"));
    }

    @Test
    void userGroupAssignmentRoundTrip() {
        CoreCommandService service = new CoreCommandService(manager);
        manager.getGroup("admin");

        assertEquals("User \"Rono\" added to group \"admin\"!", service.userAddGroup("Rono", "admin", null));
        assertTrue(manager.getUser("Rono").inGroup("admin"));

        assertEquals("User \"Rono\" removed from group \"admin\"!", service.userRemoveGroup("Rono", "admin", null));
        assertFalse(manager.getUser("Rono").inGroup("admin"));
    }

    @Test
    void advancedUserAndGroupCommandsKeepLegacyWording() {
        CoreCommandService service = new CoreCommandService(manager);
        manager.getGroup("admin");
        manager.getGroup("mod");

        assertEquals("Option \"prefix\" set!", service.userSetOption("Rono", "prefix", "[A]", null));
        assertEquals("Timed permission \"perm.time\" added!", service.userAddTimedPermission("Rono", "perm.time", "30", null));
        assertEquals("Timed permission \"perm.time\" removed!", service.userRemoveTimedPermission("Rono", "perm.time", null));

        service.userAddPermission("Rono", "one", null);
        service.userAddPermission("Rono", "two", null);
        assertEquals("Permissions swapped!", service.userSwapPermission("Rono", "one", "two", null));

        assertEquals("User groups set!", service.userSetGroups("Rono", "admin,mod", null));
        assertEquals("User \"Rono\" @null currently in:", service.userGroupListLines("Rono", null).getFirst());

        assertEquals("Timed permission added!", service.groupAddTimedPermission("default", "g.perm", "30", null));
        assertEquals("Timed permission \"g.perm\" removed!", service.groupRemoveTimedPermission("default", "g.perm", null));
        assertEquals("Group default is unranked", service.groupRank("default", null, null));
        assertEquals("Group default inheritance updated!", service.groupSetParents("default", "admin", null));
        assertEquals("Group default inheritance updated!", service.groupAddParents("default", "mod", null));
        assertEquals("Group \"default\" inheritance updated!", service.groupRemoveParents("default", "admin", null));
    }

    @Test
    void userViewContainsIdentityAndState() {
        CoreCommandService service = new CoreCommandService(manager);
        manager.getGroup("default");
        service.userAddGroup("Rono", "default", null);
        service.userAddPermission("Rono", "perm.a", null);

        CoreCommandService.UserView view = service.userView("Rono");
        assertEquals("Rono", view.name());
        assertTrue(view.groups().contains("default"));
        assertTrue(view.permissions().contains("perm.a"));
    }

    @Test
    void groupViewRejectsMissingGroupIdentifier() {
        CoreCommandService service = new CoreCommandService(manager);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.groupView(null));
        assertEquals("Group name is required", ex.getMessage());
    }

    @Test
    void promoteMessageMatchesLegacyWording() throws RankingException {
        PermissionGroup admin = manager.getGroup("admin");
        admin.setRankLadder("default");
        admin.setRank(1);

        PermissionGroup mod = manager.getGroup("mod");
        mod.setRankLadder("default");
        mod.setRank(2);

        PermissionUser user = manager.getUser("Rono");
        user.setParents(List.of(mod));

        CoreCommandService service = new CoreCommandService(manager);
        assertEquals("User Rono promoted to admin group", service.promote("Rono", null, "default"));
    }

    @Test
    void demoteMessageMatchesLegacyWording() throws RankingException {
        PermissionGroup admin = manager.getGroup("admin");
        admin.setRankLadder("default");
        admin.setRank(1);

        PermissionGroup mod = manager.getGroup("mod");
        mod.setRankLadder("default");
        mod.setRank(2);

        PermissionUser user = manager.getUser("Rono");
        user.setParents(List.of(admin));

        CoreCommandService service = new CoreCommandService(manager);
        assertEquals("User Rono demoted to mod group", service.demote("Rono", null, "default"));
    }

    @Test
    void configNodeSetAndReadBackUsesBridge() {
        java.util.Map<String, Object> config = new java.util.HashMap<>();
        CoreCommandService.ConfigBridge bridge = new CoreCommandService.ConfigBridge() {
            @Override
            public Object get(String path) {
                return config.get(path);
            }

            @Override
            public void set(String path, Object value) {
                config.put(path, value);
            }

            @Override
            public void save() {
            }
        };

        CoreCommandService service = new CoreCommandService(manager);
        List<String> lines = service.configNodeLines(bridge, "permissions.debug", "true");
        assertEquals("Node \"permissions.debug\" = \"true\"", lines.getFirst());
    }

    @Test
    void convertUuidDelegatesToBridge() {
        CoreCommandService service = new CoreCommandService(manager);
        String result = service.convertUuid(force -> force ? "forced" : "normal", true);
        assertEquals("forced", result);
    }

    @Test
    void worldInheritanceAndDebugAndVersion() throws PermissionBackendException {
        CoreCommandService service = new CoreCommandService(manager);
        assertTrue(service.worldNames().contains("world"));
        assertEquals("Worlds on server: ", service.worldsTreeLines().getFirst());

        String inheritanceResult = service.setWorldInheritance("world", List.of("lobby", "survival"));
        assertTrue(inheritanceResult.contains("inherits"));
        assertEquals(List.of("lobby", "survival"), service.worldInheritance("world"));
        assertTrue(service.worldInheritanceLines("world").getFirst().contains("World \"world\" inherits:"));

        String debugToggle = service.toggleDebug();
        assertTrue(debugToggle.toLowerCase().contains("debug mode"));

        assertEquals("Permissions reloaded", service.reload(() -> {}));
        assertEquals("[PermissionsEx] version [1.0.0-test]", service.version("1.0.0-test"));
    }
}
