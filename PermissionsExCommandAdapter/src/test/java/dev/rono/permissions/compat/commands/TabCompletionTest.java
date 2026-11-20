package dev.rono.permissions.compat.commands;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.List;
import org.bukkit.command.Command;
import org.junit.jupiter.api.Test;

/** Tab completion for the legacy /pex syntax, as in PermissionsEx 1.23.6. */
class TabCompletionTest extends CommandAdapterTestSupport {
    private List<String> complete(String... args) {
        Command pex = adapter.getCommand("pex");

        return adapter.onTabComplete(console, pex, "pex", args);
    }

    @Test
    void completesSubcommandWords() {
        var suggestions = complete("us");

        assertTrue(suggestions.contains("user"), suggestions::toString);
        assertTrue(suggestions.contains("users"), suggestions::toString);
        assertFalse(suggestions.contains("group"), suggestions::toString);
    }

    @Test
    void completesUsersAndGroupsFromPermissionsExPlus() {
        createUser("Alex");
        run("pex group staff create");
        run("pex group builder create");
        run("pex user Alex group add staff");

        assertTrue(complete("user", "Al").contains("Alex"));

        // Only groups the user can still be added to, and only groups they are in to
        // remove.
        var addable = complete("user", "Alex", "group", "add", "");
        assertTrue(addable.contains("builder"), addable::toString);
        assertFalse(addable.contains("staff"), addable::toString);

        var removable = complete("user", "Alex", "group", "remove", "");
        assertTrue(removable.contains("staff"), removable::toString);
        assertFalse(removable.contains("builder"), removable::toString);
    }

    @Test
    void completesAHoldersOwnPermissionsForRemoval() {
        createUser("Alex");
        run("pex user Alex add chat.color");

        var removable = complete("user", "Alex", "remove", "chat.");
        assertTrue(removable.contains("chat.color"), removable::toString);
    }
}
