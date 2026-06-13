package ru.tehkode.permissions;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class InheritedChildPermissionsTest extends PEXTestBase {

    @Test
    public void childNodesFromOwnPermissionsAreIncludedInHierarchy() {
        PermissionUser user = manager.getUser("childPermUser");
        user.addPermission("essentials", null);
        user.addPermission("essentials.home", null);

        List<String> perms = user.getPermissions(null);
        assertTrue(perms.contains("essentials"));
        assertTrue(perms.contains("essentials.home"));
    }
}
