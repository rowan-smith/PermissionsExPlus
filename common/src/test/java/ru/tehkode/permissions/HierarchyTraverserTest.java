package ru.tehkode.permissions;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HierarchyTraverserTest extends PEXTestBase {

    @Test
    public void testSimpleInheritance() {
        PermissionGroup parent = manager.getGroup("Parent");
        PermissionGroup child = manager.getGroup("Child");
        
        parent.addPermission("parent.perm");
        child.addPermission("child.perm");
        child.setParents(Arrays.asList(parent));
        
        List<String> permissions = child.getPermissions(null);
        assertTrue(permissions.contains("child.perm"));
        assertTrue(permissions.contains("parent.perm"));
    }

    @Test
    public void testDeepInheritance() {
        PermissionGroup g1 = manager.getGroup("G1");
        PermissionGroup g2 = manager.getGroup("G2");
        PermissionGroup g3 = manager.getGroup("G3");
        
        g1.addPermission("p1");
        g2.addPermission("p2");
        g3.addPermission("p3");
        
        g2.setParents(Arrays.asList(g1));
        g3.setParents(Arrays.asList(g2));
        
        List<String> permissions = g3.getPermissions(null);
        assertTrue(permissions.contains("p1"));
        assertTrue(permissions.contains("p2"));
        assertTrue(permissions.contains("p3"));
    }

    @Test
    public void testCircularInheritance() {
        PermissionGroup g1 = manager.getGroup("G1");
        PermissionGroup g2 = manager.getGroup("G2");
        
        g1.setParents(Arrays.asList(g2));
        g2.setParents(Arrays.asList(g1));
        
        // This should not throw StackOverflowError
        List<String> permissions = g1.getPermissions(null);
        assertNotNull(permissions);
    }
}