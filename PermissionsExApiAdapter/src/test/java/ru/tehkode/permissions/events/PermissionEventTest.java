package ru.tehkode.permissions.events;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.tehkode.permissions.PEXTestBase;
import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionUser;

public class PermissionEventTest extends PEXTestBase {
    private List<PermissionEntityEvent> events;

    public class TestListener implements Listener {
        @EventHandler
        public void onEntityEvent(PermissionEntityEvent event) {
            events.add(event);
        }
    }

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        events = new ArrayList<>();
        // Register a listener to capture events
        server.getPluginManager().registerEvents(new TestListener(), null);
    }

    @Test
    public void testUserEvent() {
        PermissionUser user = manager.getUser("testUser");
        user.setPermissions(Collections.singletonList("test.perm"), null);

        assertEquals(1, events.size(), "One event should be fired");
        PermissionEntityEvent event = events.getFirst();
        assertEquals(user, event.getEntity());
        assertEquals(PermissionEntityEvent.Action.PERMISSIONS_CHANGED, event.getAction());
    }

    @Test
    public void testGroupEvent() {
        PermissionGroup group = manager.getGroup("testGroup");
        group.setOption("test-opt", "val", null);

        assertEquals(1, events.size(), "One event should be fired");
        PermissionEntityEvent event = events.getFirst();
        assertEquals(group, event.getEntity());
        assertEquals(PermissionEntityEvent.Action.OPTIONS_CHANGED, event.getAction());
    }

    @Test
    public void testInheritanceEvent() {
        PermissionUser user = manager.getUser("testUser");
        PermissionGroup group = manager.getGroup("testGroup");

        user.addGroup(group);

        // One for adding group
        assertTrue(events.stream().anyMatch(e -> e.getEntity().equals(user) && e.getAction() == PermissionEntityEvent.Action.INHERITANCE_CHANGED));
    }
}
