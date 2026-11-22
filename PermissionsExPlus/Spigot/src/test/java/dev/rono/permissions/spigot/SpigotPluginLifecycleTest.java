package dev.rono.permissions.spigot;

import dev.rono.permissions.bukkit.PermissionsExPlusBukkitPlugin;
import dev.rono.permissions.api.PexProvider;
import dev.rono.permissions.api.context.ContextSet;
import dev.rono.permissions.api.event.user.UserModifiedEvent;
import dev.rono.permissions.api.permission.PermissionResult;
import dev.rono.permissions.api.resolver.QueryOptions;
import dev.rono.permissions.core.PexApiImpl;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

import java.nio.file.Files;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SpigotPluginLifecycleTest {
    @BeforeEach
    void setUp() {
        MockBukkit.mock();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();

        assertFalse(PexProvider.available());
    }

    @Test
    void registersAndUnregistersThePublicApiAcrossThePluginLifecycle() throws Exception {
        var plugin = loadPlugin();

        assertTrue(plugin.isEnabled());
        assertTrue(PexProvider.available());
    }

    @Test
    void reloadReplacesRuntimeServicesWhileKeepingTheFacadeRegistered() throws Exception {
        var plugin = loadPlugin();
        var core = core(plugin);
        var previousUsers = core.users();
        var offlineUser = UUID.randomUUID();

        core.users().create(offlineUser, "offline-user").toCompletableFuture().join();
        core.contexts().registry().registerContextType("faction", () -> List.of("claimed"));

        var subject = UUID.randomUUID();

        var calculator = core.contexts().registerCalculator((uuid, consumer) -> {
            if (subject.equals(uuid)) {
                consumer.accept("flying", "true");
            }
        });

        core.reload();

        assertSame(core, PexProvider.get());
        assertNotSame(previousUsers, core.users());
        assertFalse(core.users().cache().isCached(offlineUser));
        assertTrue(core.users().storage().identifiers().toCompletableFuture().join().contains(offlineUser));
        assertTrue(core.contexts().registry().registeredKeys().contains("world"));
        assertEquals(List.of("claimed"), core.contexts().registry().validValues("faction"));
        assertTrue(core.contexts().contexts(subject).contains("flying", "true"));

        calculator.close();

        assertFalse(core.contexts().contexts(subject).contains("flying", "true"));
    }

    @Test
    void eventSubscriptionsSurviveReload() throws Exception {
        var plugin = loadPlugin();
        var core = core(plugin);
        var bus = core.events();
        var received = new java.util.concurrent.atomic.AtomicInteger();
        var reloads = new java.util.concurrent.atomic.AtomicInteger();

        var subscription = core.events().subscribe(UserModifiedEvent.class, event -> received.incrementAndGet());
        var reloadSubscription = core.onReload(reloads::incrementAndGet);

        core.reload();

        assertSame(bus, core.events(), "event bus must be stable for the plugin lifetime");
        assertEquals(1, reloads.get());

        var id = UUID.randomUUID();

        core.users().create(id, "alex").toCompletableFuture().join();
        core.users().modify("alex", modifier -> modifier.allowPermission("example.after.reload")).toCompletableFuture().join();

        assertTrue(received.get() >= 1, "subscriptions made before reload must receive the new runtime's events");

        // The new runtime's own invalidation subscriptions must be live too.
        var user = core.users().cache().get(id).orElseThrow();

        assertEquals(PermissionResult.ALLOW, core.resolvers().permissions().check(user, "example.after.reload", QueryOptions.global()));

        core.users().modify("alex", modifier -> modifier.denyPermission("example.after.reload")).toCompletableFuture().join();

        user = core.users().cache().get(id).orElseThrow();

        assertEquals(PermissionResult.DENY, core.resolvers().permissions().check(user, "example.after.reload", QueryOptions.global()));

        subscription.unsubscribe();
        reloadSubscription.unsubscribe();
    }

    @Test
    void legacyCommandModeSurvivesReload() throws Exception {
        var plugin = loadPlugin();
        var core = core(plugin);

        assertEquals(PexApiImpl.CommandMode.MODERN, core.commandMode());
        assertFalse(core.commands().manager().rootCommands().isEmpty());

        core.useLegacyCommands();

        assertEquals(PexApiImpl.CommandMode.LEGACY, core.commandMode());
        assertTrue(core.commands().manager().rootCommands().isEmpty());

        core.reload();

        assertEquals(PexApiImpl.CommandMode.LEGACY, core.commandMode());
        assertTrue(core.commands().manager().rootCommands().isEmpty(), "reload must not re-register the modern command tree");
    }

    @Test
    void failedReloadRestoresThePreviousUsableRuntime() throws Exception {
        var plugin = loadPlugin();
        var core = core(plugin);
        var previousUsers = core.users();

        Files.writeString(plugin.getDataFolder().toPath().resolve("database.yml"), "type: unsupported\n");

        assertThrows(RuntimeException.class, core::reload);
        assertSame(core, PexProvider.get());
        assertSame(previousUsers, core.users());
    }

    @Test
    void platformEventsMaintainAndReleaseCachedPlayerState() throws Exception {
        var plugin = loadPlugin();

        var server = MockBukkit.getMock();

        assertNotNull(server);
        var nether = server.addSimpleWorld("nether");
        var player = server.addPlayer("alex");
        var core = core(plugin);

        assertTrue(core.users().cache().isCached(player.getUniqueId()));
        assertTrue(core.contexts().contexts(player.getUniqueId()).contains("world", player.getWorld().getName()));

        player.setGameMode(GameMode.CREATIVE);

        assertTrue(core.contexts().contexts(player.getUniqueId()).contains("gamemode", "creative"));

        player.teleport(new Location(nether, 0, 64, 0));

        assertTrue(core.contexts().contexts(player.getUniqueId()).contains("world", "nether"));

        player.disconnect();

        assertTrue(core.users().cache().isCached(player.getUniqueId()));
        assertTrue(core.contexts().contexts(player.getUniqueId()).values("world").isEmpty());
        assertTrue(core.contexts().contexts(player.getUniqueId()).values("gamemode").isEmpty());
    }

    @Test
    void commandOutputUsesSectionsAndPosixContextFlags() throws Exception {
        var plugin = loadPlugin();

        assertNotNull(MockBukkit.getMock());
        MockBukkit.getMock().addSimpleWorld("nether");

        var core = core(plugin);
        var id = UUID.randomUUID();

        core.users().create(id, "alex").toCompletableFuture().join();

        var console = MockBukkit.getMock().getConsoleSender();
        var player = MockBukkit.getMock().addPlayer("helper");

        assertFalse(player.isOp());
        assertTrue(Objects.requireNonNull(MockBukkit.getMock().getCommandMap().getCommand("pex")).testPermissionSilent(player));

        var rootSuggestions = MockBukkit.getMock().getCommandTabComplete(console, "pex ");

        assertTrue(rootSuggestions.contains("user"));
        assertTrue(rootSuggestions.contains("group"));

        var flagSuggestions = MockBukkit.getMock().getCommandTabComplete(console, "pex user alex permissions add essentials.fly --");

        assertTrue(flagSuggestions.contains("--world"));
        assertTrue(flagSuggestions.contains("--gamemode"));
        assertFalse(flagSuggestions.contains("--context"));

        assertTrue(MockBukkit.getMock()
                .getCommandTabComplete(console, "pex user alex permissions add essentials.fly --world ")
                .contains("nether"));

        assertTrue(MockBukkit.getMock().dispatchCommand(console, "pex user alex permissions add essentials.fly --world nether"));

        var nether = ContextSet.builder().add("world", "nether").build();

        assertTrue(core.users().cache().get(id).orElseThrow().explicitlyAllows("essentials.fly", nether));
        assertTrue(MockBukkit.getMock().dispatchCommand(console, "pex user alex"));
        assertEquals("§aAdded §fessentials.fly §ato §ealex", console.nextMessage());
        assertEquals("§6User: §ealex (" + id + ")", console.nextMessage());
        assertEquals("§6Options:", console.nextMessage());
        assertEquals("§7(none)", console.nextMessage());
        assertEquals("§6Groups:", console.nextMessage());
        assertEquals("§7(none)", console.nextMessage());
        assertEquals("§6Permissions:", console.nextMessage());
        assertEquals("§7- §fessentials.fly [world=nether]", console.nextMessage());

        core.contexts().registry().registerContextType("faction", () -> List.of("claimed", "wilderness"));

        assertTrue(MockBukkit.getMock().dispatchCommand(console, "pex user alex permissions add essentials.build --faction claimed"));

        var claimed = ContextSet.builder().add("faction", "claimed").build();

        assertTrue(core.users().cache().get(id).orElseThrow().explicitlyAllows("essentials.build", claimed));
        assertEquals("§aAdded §fessentials.build §ato §ealex", console.nextMessage());

        var shorthand = "network.{survival,skyblock}.kit.{1-2}";

        assertTrue(MockBukkit.getMock().dispatchCommand(console, "pex user alex permissions add " + shorthand + " --world nether"));
        assertEquals("§aAdded §f" + shorthand + " §ato §ealex", console.nextMessage());

        for (var permission : List.of("network.survival.kit.1", "network.survival.kit.2", "network.skyblock.kit.1", "network.skyblock.kit.2")) {
            assertTrue(core.users().cache().get(id).orElseThrow().explicitlyAllows(permission, nether));
        }

        assertTrue(MockBukkit.getMock().dispatchCommand(console, "pex user alex permissions remove " + shorthand + " --world nether"));
        assertEquals("§aRemoved §f" + shorthand + " §afrom §ealex", console.nextMessage());
        assertTrue(core.users().cache().get(id).orElseThrow().explicitPermissions().stream().noneMatch(node -> node.permission().startsWith("network.")));

        core.groups().create("staff").toCompletableFuture().join();
        core.groups().modify("staff", modifier -> modifier.setWeight(100)).toCompletableFuture().join();

        assertTrue(MockBukkit.getMock().dispatchCommand(console, "pex group staff"));
        assertEquals("§6Group: §estaff", console.nextMessage());
        assertEquals("§6Options:", console.nextMessage());
        assertEquals("§7- §fweight:100", console.nextMessage());
        assertEquals("§6Parents:", console.nextMessage());
        assertEquals("§7(none)", console.nextMessage());
        assertEquals("§6Permissions:", console.nextMessage());
        assertEquals("§7(none)", console.nextMessage());
    }

    @SuppressWarnings("unchecked")
    private static PexApiImpl<CommandSender> core(SpigotPlugin plugin) throws Exception {
        var field = PermissionsExPlusBukkitPlugin.class.getDeclaredField("core");

        field.setAccessible(true);

        return (PexApiImpl<CommandSender>) field.get(plugin);
    }

    private static SpigotPlugin loadPlugin() throws Exception {
        var plugin = MockBukkit.load(SpigotPlugin.class);

        assertNotNull(MockBukkit.getMock());
        assertNotNull(MockBukkit.getMock().getCommandMap().getCommand("pex"), "Command must be registered synchronously during plugin enable");

        var deadline = System.nanoTime() + java.time.Duration.ofSeconds(15).toNanos();

        while (!PexProvider.available() && plugin.isEnabled() && System.nanoTime() < deadline) {
            MockBukkit.getMock().getScheduler().performOneTick();

            Thread.sleep(10);
        }

        assertTrue(PexProvider.available(), "API did not become ready before the timeout");

        return plugin;
    }
}
