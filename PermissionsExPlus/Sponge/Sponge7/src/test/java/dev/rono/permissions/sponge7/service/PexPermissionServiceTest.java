package dev.rono.permissions.sponge7.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cloud.commandframework.CommandManager;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.internal.CommandRegistrationHandler;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.meta.SimpleCommandMeta;
import dev.rono.permissions.core.PexApiImpl;
import dev.rono.permissions.core.platform.Platform;
import dev.rono.permissions.core.platform.PlatformConfiguration;
import dev.rono.permissions.core.platform.PlatformLogger;
import dev.rono.permissions.core.platform.PlatformScheduler;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectData;
import org.spongepowered.api.util.Tristate;

/**
 * Drives the Sponge permission service against a real PermissionsExPlus runtime
 * (memory backend), so the Sponge-to-PermissionsExPlus mapping is checked
 * without
 * a server or a connected player.
 */
class PexPermissionServiceTest {
    private static final Set<Context> GLOBAL = SubjectData.GLOBAL_CONTEXT;

    @TempDir
    Path directory;

    private PexApiImpl<Object> core;
    private PexPermissionService service;

    @BeforeEach
    void setUp() throws IOException {
        // The bundled database.yml with the in-memory backend.
        try (InputStream defaults = PexPermissionServiceTest.class.getClassLoader().getResourceAsStream("database.yml")) {
            String yaml = new String(readAll(defaults), StandardCharsets.UTF_8).replace("type: \"h2\"", "type: \"memory\"");

            Files.write(directory.resolve("database.yml"), yaml.getBytes(StandardCharsets.UTF_8));
        }

        core = new PexApiImpl<>(new TestPlatform(directory));
        core.start();
        service = new PexPermissionService(core);
    }

    @AfterEach
    void tearDown() {
        if (core != null) {
            core.stop();
        }
    }

    private static byte[] readAll(InputStream input) throws IOException {
        java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
        byte[] chunk = new byte[4096];

        for (int read; (read = input.read(chunk)) != -1;) {
            buffer.write(chunk, 0, read);
        }

        return buffer.toByteArray();
    }

    private Subject user() {
        return service.getUserSubjects().loadSubject(UUID.randomUUID().toString()).join();
    }

    @Test
    void persistentPermissionsAreStoredInPermissionsExPlus() {
        Subject user = user();

        assertTrue(user.isSubjectDataPersisted());
        assertTrue(user.getSubjectData().setPermission(GLOBAL, "example.fly", Tristate.TRUE).join());
        assertTrue(user.getSubjectData().setPermission(GLOBAL, "example.ban", Tristate.FALSE).join());

        assertEquals(Tristate.TRUE, user.getPermissionValue(GLOBAL, "example.fly"));
        assertEquals(Tristate.FALSE, user.getPermissionValue(GLOBAL, "example.ban"));
        assertEquals(Tristate.UNDEFINED, user.getPermissionValue(GLOBAL, "example.unset"));

        // The node lives in PermissionsExPlus, not in the Sponge layer.
        UUID id = UUID.fromString(user.getIdentifier());
        assertTrue(core.users().cache().get(id).get().explicitlyAllows("example.fly", dev.rono.permissions.api.context.ContextSet.empty()));

        user.getSubjectData().setPermission(GLOBAL, "example.fly", Tristate.UNDEFINED).join();
        assertEquals(Tristate.UNDEFINED, user.getPermissionValue(GLOBAL, "example.fly"));
    }

    @Test
    void groupParentsAreInheritedThroughPermissionsExPlus() {
        Subject staff = service.getGroupSubjects().loadSubject("staff").join();
        staff.getSubjectData().setPermission(GLOBAL, "staff.tools", Tristate.TRUE).join();

        Subject user = user();
        assertTrue(user.getSubjectData().addParent(GLOBAL, staff.asSubjectReference()).join());

        assertTrue(user.isChildOf(GLOBAL, staff.asSubjectReference()));
        assertEquals(Tristate.TRUE, user.getPermissionValue(GLOBAL, "staff.tools"));

        user.getSubjectData().removeParent(GLOBAL, staff.asSubjectReference()).join();
        assertFalse(user.isChildOf(GLOBAL, staff.asSubjectReference()));
        assertEquals(Tristate.UNDEFINED, user.getPermissionValue(GLOBAL, "staff.tools"));
    }

    @Test
    void contextualPermissionsOnlyApplyInTheirContexts() {
        Set<Context> nether = Collections.singleton(new Context("world", "nether"));
        Set<Context> overworld = Collections.singleton(new Context("world", "overworld"));
        Subject user = user();

        user.getSubjectData().setPermission(nether, "example.build", Tristate.TRUE).join();

        assertEquals(Tristate.TRUE, user.getPermissionValue(nether, "example.build"));
        assertEquals(Tristate.UNDEFINED, user.getPermissionValue(overworld, "example.build"));
        assertEquals(Collections.singletonMap("example.build", true), user.getSubjectData().getPermissions(nether));
    }

    @Test
    void transientDataTakesPriorityOverPersistentData() {
        Subject user = user();

        user.getSubjectData().setPermission(GLOBAL, "example.chat", Tristate.TRUE).join();
        user.getTransientSubjectData().setPermission(GLOBAL, "example.chat", Tristate.FALSE).join();

        assertEquals(Tristate.FALSE, user.getPermissionValue(GLOBAL, "example.chat"));
    }

    @Test
    void defaultsApplyWhenNothingElseDecides() {
        service.getDefaults().getTransientSubjectData().setPermission(GLOBAL, "example.default", Tristate.TRUE).join();

        assertEquals(Tristate.TRUE, user().getPermissionValue(GLOBAL, "example.default"));
    }

    @Test
    void optionsResolveFromPermissionsExPlus() {
        Subject user = user();

        user.getSubjectData().setOption(GLOBAL, "prefix", "[Admin]").join();

        assertEquals("[Admin]", user.getOption(GLOBAL, "prefix").get());

        user.getSubjectData().setOption(GLOBAL, "prefix", null).join();
        assertFalse(user.getOption(GLOBAL, "prefix").isPresent());
    }

    @Test
    void unknownCollectionsAreHeldInMemory() {
        Subject block = service.loadCollection(PermissionService.SUBJECTS_COMMAND_BLOCK).join().loadSubject("block").join();

        assertFalse(block.isSubjectDataPersisted());
        block.getSubjectData().setPermission(GLOBAL, "example.block", Tristate.TRUE).join();
        assertEquals(Tristate.TRUE, block.getPermissionValue(GLOBAL, "example.block"));
    }

    /**
     * Minimal platform: direct scheduling, a temp config directory, no real
     * commands.
     */
    private static final class TestPlatform implements Platform<Object> {
        private final Path directory;

        TestPlatform(Path directory) {
            this.directory = directory;
        }

        @Override
        public PlatformLogger logger() {
            return new PlatformLogger() {
                @Override
                public void info(String message) {}

                @Override
                public void warn(String message) {}

                @Override
                public void error(String message, Throwable error) {
                    throw new AssertionError(message, error);
                }
            };
        }

        @Override
        public PlatformScheduler scheduler() {
            return new PlatformScheduler() {
                @Override
                public void execute(Runnable task) {
                    task.run();
                }

                @Override
                public void executeAsync(Runnable task) {
                    task.run();
                }

                @Override
                public void executeLater(Runnable task, Duration delay) {}

                @Override
                public void executeLaterAsync(Runnable task, Duration delay) {}

                @Override
                public void executeRepeating(Runnable task, Duration interval) {}

                @Override
                public void executeRepeatingAsync(Runnable task, Duration interval) {}

                @Override
                public boolean isMainThread() {
                    return true;
                }

                @Override
                public void cancelTask(int taskId) {}

                @Override
                public void cancelTasks() {}
            };
        }

        @Override
        public PlatformConfiguration configuration() {
            return new PlatformConfiguration() {
                @Override
                public Path dataDirectory() {
                    return directory;
                }

                @Override
                public void saveResource(String resource, boolean replace) {
                    Path target = directory.resolve(resource);

                    if (Files.exists(target) && !replace) {
                        return;
                    }

                    try (InputStream input = PexPermissionServiceTest.class.getClassLoader().getResourceAsStream(resource)) {
                        Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException error) {
                        throw new UncheckedIOException(error);
                    }
                }
            };
        }

        @Override
        public Class<Object> senderType() {
            return Object.class;
        }

        @Override
        public void sendMessage(Object sender, String message) {}

        @Override
        public CommandManager<Object> createCommandManager() {
            return new CommandManager<Object>(CommandExecutionCoordinator.simpleCoordinator(), CommandRegistrationHandler.nullCommandRegistrationHandler()) {
                @Override
                public boolean hasPermission(Object sender, String permission) {
                    return true;
                }

                @Override
                public CommandMeta createDefaultCommandMeta() {
                    return SimpleCommandMeta.empty();
                }
            };
        }
    }
}
