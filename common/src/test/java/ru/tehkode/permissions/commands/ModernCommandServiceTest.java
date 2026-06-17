package ru.tehkode.permissions.commands;

import dev.rono.permissions.core.commands.CoreCommandService;
import org.junit.jupiter.api.Test;
import ru.tehkode.permissions.PEXTestBase;
import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionUser;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ModernCommandServiceTest extends PEXTestBase {

    @Test
    void userHasReportsEffectiveResult() {
        CoreCommandService service = new CoreCommandService(manager);
        service.userAddPermission("Rono", "check.me", "survival");

        String result = service.userHas("Rono", "check.me", "survival");
        assertTrue(result.contains("Has 'check.me' in survival: true"));
    }

    @Test
    void userPermissionTraceIncludesMatchingExpression() {
        CoreCommandService service = new CoreCommandService(manager);
        PermissionUser user = manager.getUser("Rono");
        user.addPermission("trace.node", "survival");

        List<String> lines = service.userPermissionTraceLines("Rono", "trace.node", "survival");
        assertTrue(lines.stream().anyMatch(line -> line.contains("Matching expression")));
        assertTrue(lines.stream().anyMatch(line -> line.contains("Effective result: true")));
    }

    @Test
    void groupHasReportsEffectiveResult() {
        CoreCommandService service = new CoreCommandService(manager);
        PermissionGroup group = manager.getGroup("staff");
        group.addPermission("staff.use", null);

        String result = service.groupHas("staff", "staff.use", null);
        assertTrue(result.contains("Has 'staff.use' in global: true"));
    }

    @Test
    void groupPermissionTraceReportsDirectAndEffectiveNodes() {
        CoreCommandService service = new CoreCommandService(manager);
        PermissionGroup group = manager.getGroup("staff");
        group.addPermission("trace.group", null);

        List<String> lines = service.groupPermissionTraceLines("staff", "trace.group", null);
        assertTrue(lines.stream().anyMatch(line -> line.contains("Direct node: true")));
        assertTrue(lines.stream().anyMatch(line -> line.contains("Effective (with inheritance): true")));
    }

    @Test
    void userTimedPermissionAddAndRemoveRoundTrip() {
        CoreCommandService service = new CoreCommandService(manager);
        assertEquals(
                "Timed permission \"temp.node\" added!",
                service.userAddTimedPermission("Rono", "temp.node", "30", null));
        assertEquals(
                "Timed permission \"temp.node\" removed!",
                service.userRemoveTimedPermission("Rono", "temp.node", null));
    }

    @Test
    void userTimedGroupRemoveClearsMembership() {
        CoreCommandService service = new CoreCommandService(manager);
        service.userAddGroupSeconds("Rono", "default", null, 3600);

        String result = service.userRemoveTimedGroup("Rono", "default", null);
        assertTrue(result.contains("Timed group \"default\" removed"));
        assertFalse(manager.getUser("Rono").inGroup("default", null, false));
    }
}
