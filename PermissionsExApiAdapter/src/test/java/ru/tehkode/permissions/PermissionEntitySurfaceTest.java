package ru.tehkode.permissions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Broader coverage of the original PermissionsEx entity/manager surface that
 * third-party plugins compile against.
 */
public class PermissionEntitySurfaceTest extends PEXTestBase {

    @Test
    public void userPermissionAddRemoveAndOwnListing() {
        PermissionUser user = manager.getUser(UUID.randomUUID().toString());

        user.addPermission("alpha.one");
        user.addPermission("alpha.two");
        user.removePermission("alpha.one");

        List<String> own = user.getOwnPermissions(null);
        assertTrue(own.contains("alpha.two"));
        assertFalse(own.contains("alpha.one"));
        assertTrue(user.has("alpha.two"));
        assertFalse(user.has("alpha.one"));
    }

    @Test
    public void groupParentsOptionsAndDefaultToggle() {
        PermissionGroup parent = manager.getGroup("Parent");
        PermissionGroup child = manager.getGroup("Child");

        parent.addPermission("parent.perm");
        parent.setOption("prefix", "[P]", null);
        child.setParents(java.util.Collections.singletonList(parent), null);
        child.setOption("suffix", "[C]", null);

        assertTrue(child.has("parent.perm"));
        assertEquals("[P]", child.getPrefix());
        assertEquals("[C]", child.getSuffix());

        PermissionGroup configuredDefault = manager.getGroup("default");
        assertTrue(configuredDefault.isDefault(null));
        assertTrue(manager.getDefaultGroups(null).stream().anyMatch(group -> group.getName().equals("default")));
    }

    @Test
    public void userOptionMap() {
        PermissionUser user = manager.getUser("OptionUser");

        user.addPermission("option.perm");
        user.setOption("rank", "10", "world");
        user.setOption("title", "hero", null);

        Map<String, String> global = user.getOptions(null);
        Map<String, String> world = user.getOptions("world");

        assertEquals("hero", global.get("title"));
        assertEquals("10", world.get("rank"));
        assertTrue(user.has("option.perm"));
    }

    @Test
    public void managerResetKeepsBackend() throws Exception {
        PermissionUser user = manager.getUser("ReloadUser");
        user.addPermission("reload.keep");

        assertNotNull(manager.getBackend());
        manager.reset();
        waitForExecutor();

        assertNotNull(manager.getUser("ReloadUser"));
        assertNotNull(manager.getBackend());
    }

    @Test
    public void negationBeatsAllowOnSameNode() {
        PermissionUser user = manager.getUser("NegationUser");
        PermissionGroup group = manager.getGroup("AllowGroup");

        group.addPermission("chat.send");
        user.addGroup(group);
        user.addPermission("-chat.send");

        assertFalse(user.has("chat.send"));
    }
}
