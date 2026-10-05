package ru.tehkode.permissions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import dev.rono.permissions.core.PexImplProvider;
import dev.rono.permissions.core.manager.LadderManagerImpl;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

public class PermissionGroupTest extends PEXTestBase {

    @Test
    public void testGroupPermissions() {
        PermissionGroup group = manager.getGroup("TestGroup");
        group.addPermission("test.permission");

        assertTrue(group.has("test.permission"), "Group should have test.permission");
    }

    @Test
    public void testGroupInheritance() {
        PermissionGroup parent = manager.getGroup("ParentGroup");
        PermissionGroup child = manager.getGroup("ChildGroup");

        parent.addPermission("parent.permission");
        child.setParents(Collections.singletonList(parent));

        assertTrue(child.has("parent.permission"), "Child group should inherit parent.permission");
    }

    @Test
    public void testRanking() throws Exception {
        PermissionGroup group1 = manager.getGroup("Group1");
        PermissionGroup group2 = manager.getGroup("Group2");

        // Touch groups so Plus has them before ladder creation.
        assertEquals("Group1", group1.getIdentifier());
        assertEquals("Group2", group2.getIdentifier());

        var ladders = (LadderManagerImpl) PexImplProvider.get().ladders();
        ladders.create("default").toCompletableFuture().join();
        ladders.modify("default", modifier -> modifier.setGroups(List.of("group1", "group2")))
                .toCompletableFuture()
                .join();

        PermissionUser user = manager.getUser("TestUser");
        user.addGroup(group1);

        assertTrue(user.inGroup(group1));

        user.promote(null, "default");

        assertTrue(user.inGroup(group2), "User should be promoted to Group2");
        assertFalse(user.inGroup(group1), "User should no longer be in Group1");
    }
}
