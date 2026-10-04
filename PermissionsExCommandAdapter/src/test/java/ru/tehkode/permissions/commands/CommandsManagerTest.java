package ru.tehkode.permissions.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.command.ConsoleCommandSenderMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import ru.tehkode.permissions.bukkit.PermissionsEx;
import ru.tehkode.permissions.commands.exceptions.AutoCompleteChoicesException;

/**
 * Framework-level coverage for syntax selection, permission gating, and
 * autocomplete choice surfacing — independent of PermissionsExPlus mutations.
 */
class CommandsManagerTest {
    private ServerMock server;
    private PermissionsEx plugin;
    private CommandsManager manager;
    private ConsoleCommandSenderMock console;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        MockBukkit.createMockPlugin("PermissionsExPlus");
        plugin = MockBukkit.load(PermissionsEx.class);
        // Plugin disables itself without Plus API; rebuild a manager against the
        // still-constructed plugin instance for isolated dispatcher tests.
        manager = new CommandsManager(plugin);
        manager.register(new FixtureCommands());
        console = server.getConsoleSender();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void prefersLongestMatchingSyntax() {
        AtomicReference<String> chosen = FixtureCommands.lastHandler;
        chosen.set(null);

        assertTrue(manager.execute(console, command("pex"), new String[] {"user", "alex", "add", "a.b"}));
        assertEquals("add", chosen.get());

        chosen.set(null);
        assertTrue(manager.execute(console, command("pex"), new String[] {"user", "alex"}));
        assertEquals("info", chosen.get());
    }

    @Test
    void optionalArgumentsAreCaptured() {
        AtomicReference<String> chosen = FixtureCommands.lastHandler;
        chosen.set(null);

        assertTrue(manager.execute(console, command("pex"), new String[] {"user", "alex", "add", "a.b", "world_nether"}));
        assertEquals("add", chosen.get());
        assertEquals("world_nether", FixtureCommands.lastArgs.get().get("world"));
    }

    @Test
    void rejectsUnknownSyntax() {
        assertTrue(manager.execute(console, command("pex"), new String[] {"totally", "unknown"}));
        assertTrue(console.nextMessage().contains("Error in command syntax"));
    }

    @Test
    void playerPermissionGateBlocksDisallowedCommands() {
        PlayerMock player = server.addPlayer("helper");
        player.setOp(false);

        assertTrue(manager.execute(player, command("pex"), new String[] {"secret"}));
        assertEquals("Sorry, you don't have enough permissions.", strip(player.nextMessage()));
        assertFalse("secret".equals(FixtureCommands.lastHandler.get()));
    }

    @Test
    void autocompleteChoicesArePrinted() {
        assertTrue(manager.execute(console, command("pex"), new String[] {"choose", "x"}));
        assertTrue(console.nextMessage().contains("Autocomplete for <target>:"));
        assertTrue(console.nextMessage().contains("one"));
    }

    private static org.bukkit.command.Command command(String name) {
        return new org.bukkit.command.Command(name) {
            @Override
            public boolean execute(CommandSender sender, String commandLabel, String[] args) {
                return true;
            }
        };
    }

    private static String strip(String message) {
        return message == null ? null : org.bukkit.ChatColor.stripColor(message);
    }

    public static final class FixtureCommands implements CommandListener {
        static final AtomicReference<String> lastHandler = new AtomicReference<>();
        static final AtomicReference<Map<String, String>> lastArgs = new AtomicReference<>();

        @Override
        public void onRegistered(CommandsManager manager) {}

        @Command(name = "pex", syntax = "user <user>", description = "info")
        public void info(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
            lastHandler.set("info");
            lastArgs.set(args);
        }

        @Command(name = "pex", syntax = "user <user> add <permission> [world]", description = "add", permission = "permissions.manage.users.permissions.<user>")
        public void add(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
            lastHandler.set("add");
            lastArgs.set(args);
        }

        @Command(name = "pex", syntax = "secret", description = "secret", permission = "permissions.secret")
        public void secret(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
            lastHandler.set("secret");
            lastArgs.set(args);
        }

        @Command(name = "pex", syntax = "choose <target>", description = "choose")
        public void choose(PermissionsEx plugin, CommandSender sender, Map<String, String> args) {
            throw new AutoCompleteChoicesException(new String[] {"one", "two"}, "target");
        }
    }
}
