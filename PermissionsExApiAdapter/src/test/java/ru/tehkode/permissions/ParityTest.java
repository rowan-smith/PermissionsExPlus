package ru.tehkode.permissions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.rono.permissions.api.parent.ParentNode;
import dev.rono.permissions.api.user.User;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

/**
 * PermissionsEx behaviour that PermissionsExPlus must keep: timed group
 * memberships and world inheritance, both stored in PermissionsExPlus itself.
 */
public class ParityTest extends PEXTestBase {
    @Test
    public void aTimedGroupIsAMembershipThatExpires() {
        PermissionUser user = manager.getUser("TestUser");
        manager.getGroup("vip");

        user.addGroup("vip", null, 3600);

        assertTrue(user.inGroup("vip", false));

        ParentNode vip = plusUser().groups().stream().filter(node -> node.group().equals("vip")).findFirst().get();
        assertExpiresIn(Duration.ofHours(1), vip.expiry().get());
        // Plugins that read the PermissionsEx option still find it, expiring with the
        // membership.
        assertTrue(plusUser().explicitOptions().stream().anyMatch(option -> option.key().equals("group-vip-until") && option.expiry().isPresent()));
    }

    @Test
    public void rewritingTheGroupListKeepsATimedMembershipTimed() {
        PermissionUser user = manager.getUser("TestUser");
        manager.getGroup("vip");
        manager.getGroup("builder");

        user.addGroup("vip", null, 3600);
        user.addGroup("builder");

        assertTrue(user.inGroup("builder", false));
        assertTrue(plusUser().groups().stream().filter(node -> node.group().equals("vip")).findFirst().get().expiry().isPresent());
        assertFalse(plusUser().groups().stream().filter(node -> node.group().equals("builder")).findFirst().get().expiry().isPresent());
    }

    @Test
    public void worldInheritanceIsSavedAndAppliesToPermissionChecks() throws Exception {
        PermissionUser user = manager.getUser("TestUser");
        user.addPermission("world.build", "world");

        manager.setWorldInheritance("world_nether", Arrays.asList("world"));

        assertEquals(Arrays.asList("world"), manager.getWorldInheritance("world_nether"));
        assertEquals(Arrays.asList("world"), fixture.api().worlds().parents("world_nether"));
        assertTrue(new String(java.nio.file.Files.readAllBytes(tempDir.resolve("worlds.yml")), "UTF-8").contains("world_nether"));
        assertTrue(user.has("world.build", "world_nether"));
        assertFalse(user.has("world.build", "world_the_end"));
    }

    @Test
    public void convertingAUserStoredByNameToItsUuidKeepsItsData() throws Exception {
        PermissionUser byName = manager.getUser("Steve");
        byName.setPrefix("&a[Steve]", null);
        byName.addPermission("steve.node");

        // What PermissionManager.getUser(Player) does the first time a player joins.
        java.lang.reflect.Method getUser = PermissionManager.class.getDeclaredMethod("getUser", String.class, String.class, boolean.class);
        getUser.setAccessible(true);
        java.util.UUID id = java.util.UUID.randomUUID();
        PermissionUser converted = (PermissionUser) getUser.invoke(manager, id.toString(), "Steve", true);

        assertEquals(id.toString(), converted.getIdentifier());
        assertEquals("&a[Steve]", converted.getPrefix(null));
        assertTrue(converted.has("steve.node"));
        assertEquals("Steve", fixture.api().users().find(id).toCompletableFuture().join().get().name());
    }

    private User plusUser() {
        return fixture.api().users().find("TestUser").toCompletableFuture().join().get();
    }

    private static void assertExpiresIn(Duration expected, Instant expiry) {
        long seconds = Duration.between(Instant.now(), expiry).getSeconds();
        assertTrue(Math.abs(seconds - expected.getSeconds()) < 60, "expires in " + seconds + "s, expected about " + expected.getSeconds() + "s");
    }
}
