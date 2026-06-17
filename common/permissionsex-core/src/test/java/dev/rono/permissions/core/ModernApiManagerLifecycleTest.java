package dev.rono.permissions.core;

import dev.rono.permissions.api.group.GroupAlreadyExistsException;
import dev.rono.permissions.api.group.GroupNotFoundException;
import dev.rono.permissions.api.ladder.LadderAlreadyExistsException;
import dev.rono.permissions.api.ladder.LadderNotFoundException;
import dev.rono.permissions.api.user.UserAlreadyExistsException;
import dev.rono.permissions.api.user.UserNotFoundException;
import dev.rono.permissions.api.world.WorldAlreadyExistsException;
import dev.rono.permissions.api.world.WorldNotFoundException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** Manager find/get/create/exists lifecycle and exception contracts. */
class ModernApiManagerLifecycleTest extends ModernApiTestSupport {

    @Test
    void userFindDoesNotMaterializeMissingRecord() {
        var uuid = UUID.randomUUID();
        assertFalse(api().getUserManager().exists(uuid));
        assertTrue(api().getUserManager().findUser(uuid).isEmpty());
        assertTrue(api().getUserManager().findUser("nonexistent-user-name").isEmpty());
    }

    @Test
    void userGetThrowsWhenMissing() {
        assertThrows(UserNotFoundException.class, () -> api().getUserManager().getUser(UUID.randomUUID()));
        assertThrows(UserNotFoundException.class, () -> api().getUserManager().getUser("missing-user"));
    }

    @Test
    void userCreateDuplicateThrows() {
        var user = api().getUserManager().createUser("dup-user");
        user.save();
        assertThrows(UserAlreadyExistsException.class, () -> api().getUserManager().createUser("dup-user"));
        user.delete();
    }

    @Test
    void groupFindGetCreateLifecycle() {
        assertTrue(api().getGroupManager().findGroup("missing-group").isEmpty());
        assertThrows(GroupNotFoundException.class, () -> api().getGroupManager().getGroup("missing-group"));

        var group = api().getGroupManager().createGroup("lifecycle-group");
        group.save();
        assertTrue(api().getGroupManager().exists("lifecycle-group"));
        assertEquals("lifecycle-group", api().getGroupManager().getGroup("lifecycle-group").getName());

        assertThrows(GroupAlreadyExistsException.class, () -> api().getGroupManager().createGroup("lifecycle-group"));
    }

    @Test
    void worldFindGetCreateLifecycle() {
        assertTrue(api().getWorldManager().findWorld("missing-world").isEmpty());
        assertThrows(WorldNotFoundException.class, () -> api().getWorldManager().getWorld("missing-world"));

        api().getWorldManager().createWorld("lifecycle-world");
        assertTrue(api().getWorldManager().exists("lifecycle-world"));
        assertEquals("lifecycle-world", api().getWorldManager().getWorld("lifecycle-world").getName());

        assertThrows(WorldAlreadyExistsException.class, () -> api().getWorldManager().createWorld("lifecycle-world"));
    }

    @Test
    void ladderFindGetCreateLifecycle() {
        assertTrue(api().getLadderManager().findLadder("missing-ladder-xyz").isEmpty());
        assertThrows(LadderNotFoundException.class, () -> api().getLadderManager().getLadder("missing-ladder-xyz"));

        api().getLadderManager().createLadder("lifecycle-ladder");
        assertTrue(api().getLadderManager().exists("lifecycle-ladder"));
        assertEquals("lifecycle-ladder", api().getLadderManager().getLadder("lifecycle-ladder").getName());

        var group = api().getGroupManager().createGroup("ladder-ref-group");
        group.setRank(1, "lifecycle-ladder");
        group.save();

        assertTrue(api().getLadderManager().exists("lifecycle-ladder"));
        assertEquals("lifecycle-ladder", api().getLadderManager().getLadder("lifecycle-ladder").getName());

        assertThrows(LadderAlreadyExistsException.class, () -> api().getLadderManager().createLadder("lifecycle-ladder"));
    }

    @Test
    void managerCountsReflectBackend() {
        var beforeUsers = api().getUserManager().count();
        var user = api().getUserManager().createUser(UUID.randomUUID());
        user.save();
        assertEquals(beforeUsers + 1, api().getUserManager().count());
        assertEquals(1, api().getUserManager().count(u -> u.getId().equals(user.getId())));
    }
}
