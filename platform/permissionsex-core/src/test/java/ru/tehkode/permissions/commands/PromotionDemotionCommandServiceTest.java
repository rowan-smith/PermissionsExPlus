package ru.tehkode.permissions.commands;

import dev.rono.permissions.core.commands.CoreCommandService;
import org.junit.jupiter.api.Test;
import ru.tehkode.permissions.PEXTestBase;
import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.exceptions.RankingException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PromotionDemotionCommandServiceTest extends PEXTestBase {

    @Test
    void promoteMovesUserToHigherRankGroup() throws RankingException {
        PermissionGroup admin = manager.getGroup("admin");
        admin.setRankLadder("default");
        admin.setRank(1);

        PermissionGroup mod = manager.getGroup("mod");
        mod.setRankLadder("default");
        mod.setRank(2);

        PermissionUser user = manager.getUser("Rono");
        user.setParents(List.of(mod));

        CoreCommandService service = new CoreCommandService(manager);
        assertEquals("PexUser Rono promoted to admin group", service.promote("Rono", null, "default"));
        assertTrue(user.inGroup("admin"));
    }

    @Test
    void demoteMovesUserToLowerRankGroup() throws RankingException {
        PermissionGroup admin = manager.getGroup("admin");
        admin.setRankLadder("default");
        admin.setRank(1);

        PermissionGroup mod = manager.getGroup("mod");
        mod.setRankLadder("default");
        mod.setRank(2);

        PermissionUser user = manager.getUser("Rono");
        user.setParents(List.of(admin));

        CoreCommandService service = new CoreCommandService(manager);
        assertEquals("PexUser Rono demoted to mod group", service.demote("Rono", null, "default"));
        assertTrue(user.inGroup("mod"));
    }

    @Test
    void promoteFailsAtTopOfLadder() {
        PermissionGroup admin = manager.getGroup("admin");
        admin.setRankLadder("default");
        admin.setRank(1);

        PermissionUser user = manager.getUser("Rono");
        user.setParents(List.of(admin));

        CoreCommandService service = new CoreCommandService(manager);
        assertThrows(RankingException.class, () -> service.promote("Rono", null, "default"));
    }

    @Test
    void demoteFailsAtBottomOfLadder() {
        PermissionGroup mod = manager.getGroup("mod");
        mod.setRankLadder("default");
        mod.setRank(2);

        PermissionUser user = manager.getUser("Rono");
        user.setParents(List.of(mod));

        CoreCommandService service = new CoreCommandService(manager);
        assertThrows(RankingException.class, () -> service.demote("Rono", null, "default"));
    }

    @Test
    void promoteThenDemoteRoundTrip() throws RankingException {
        PermissionGroup admin = manager.getGroup("admin");
        admin.setRankLadder("default");
        admin.setRank(1);

        PermissionGroup mod = manager.getGroup("mod");
        mod.setRankLadder("default");
        mod.setRank(2);

        PermissionUser user = manager.getUser("Rono");
        user.setParents(List.of(mod));

        CoreCommandService service = new CoreCommandService(manager);
        assertEquals("PexUser Rono promoted to admin group", service.promote("Rono", null, "default"));
        assertTrue(manager.getUser("Rono").inGroup("admin"));
        assertEquals("PexUser Rono demoted to mod group", service.demote("Rono", null, "default"));
        assertTrue(manager.getUser("Rono").inGroup("mod"));
    }

    @Test
    void demoteThenPromoteRoundTrip() throws RankingException {
        PermissionGroup admin = manager.getGroup("admin");
        admin.setRankLadder("default");
        admin.setRank(1);

        PermissionGroup mod = manager.getGroup("mod");
        mod.setRankLadder("default");
        mod.setRank(2);

        PermissionUser user = manager.getUser("Rono");
        user.setParents(List.of(admin));

        CoreCommandService service = new CoreCommandService(manager);
        assertEquals("PexUser Rono demoted to mod group", service.demote("Rono", null, "default"));
        assertTrue(manager.getUser("Rono").inGroup("mod"));
        assertEquals("PexUser Rono promoted to admin group", service.promote("Rono", null, "default"));
        assertTrue(manager.getUser("Rono").inGroup("admin"));
    }
}
