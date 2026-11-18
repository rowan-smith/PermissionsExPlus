package dev.rono.permissions.core.migration;

import lombok.var;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import dev.rono.permissions.api.context.ContextKeys;
import dev.rono.permissions.api.context.ContextSet;
import dev.rono.permissions.api.group.Group;
import dev.rono.permissions.api.parent.ParentNode;
import dev.rono.permissions.api.permission.PermissionNode;
import dev.rono.permissions.api.permission.PermissionResult;
import dev.rono.permissions.api.permission.PermissionValue;
import dev.rono.permissions.api.resolver.QueryOptions;
import dev.rono.permissions.api.user.User;
import dev.rono.permissions.core.PexApiImpl;
import dev.rono.permissions.core.TestFiles;
import dev.rono.permissions.core.TestRuntime;
import dev.rono.permissions.core.util.Immutables;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LegacyPexImportTest {
    private static final UUID STEVE = UUID.fromString("069a79f4-44e9-4726-a5be-fca90e38aaf5");
    private static final UUID ALEX = UUID.fromString("853c80ef-3c37-49fd-aa49-938b674adae6");
    private static final long FUTURE = Instant.now().plusSeconds(86400).getEpochSecond();
    private static final long PAST = Instant.now().minusSeconds(60).getEpochSecond();
    private static final ContextSet NETHER = ContextSet.builder().add(ContextKeys.WORLD, "world_nether").build();
    private static final ContextSet WORLD = ContextSet.builder().add(ContextKeys.WORLD, "world").build();

    @TempDir
    Path directory;

    private PexApiImpl<Object> core;

    @BeforeEach
    void setUp() throws Exception {
        core = TestRuntime.start(directory.resolve("PermissionsExPlus"));
    }

    @AfterEach
    void tearDown() {
        core.stop();
    }

    @Test
    void importsAPermissionsYml() throws Exception {
        var pex = directory.resolve("PermissionsEx");
        java.nio.file.Files.createDirectories(pex);
        TestFiles.writeString(pex.resolve("config.yml"), "permissions:\n  backend: file\n  backends:\n    file:\n      type: file\n      file: permissions.yml\n");
        TestFiles.writeString(pex.resolve("permissions.yml"), ""
                + "groups:\n"
                + "  default:\n"
                + "    default: true\n"
                + "    options:\n"
                + "      rank: '1000'\n"
                + "    prefix: '&7'\n"
                + "    permissions:\n"
                + "    - chat.send\n"
                + "    - -chat.color\n"
                + "    worlds:\n"
                + "      world:\n"
                + "        permissions:\n"
                + "        - world.build\n"
                + "  vip:\n"
                + "    inheritance:\n"
                + "    - default\n"
                + "    options:\n"
                + "      rank: '500'\n"
                + "  admin:\n"
                + "    inheritance:\n"
                + "    - vip\n"
                + "    options:\n"
                + "      rank: '1'\n"
                + "      weight: '100'\n"
                + "    permissions:\n"
                + "    - modifyworld.(place|break)\n"
                + "users:\n"
                + "  " + STEVE + ":\n"
                + "    group:\n"
                + "    - vip\n"
                + "    options:\n"
                + "      name: Steve\n"
                + "      group-vip-until: '" + FUTURE + "'\n"
                + "    worlds:\n"
                + "      world_nether:\n"
                + "        group:\n"
                + "        - admin\n"
                + "        permissions:\n"
                + "        - nether.portal\n"
                + "  " + ALEX + ":\n"
                + "    group:\n"
                + "    - vip\n"
                + "    options:\n"
                + "      name: Alex\n"
                + "      group-vip-until: '" + PAST + "'\n"
                + "  OldName:\n"
                + "    group:\n"
                + "    - admin\n"
                + "worlds:\n"
                + "  world_nether:\n"
                + "    inheritance:\n"
                + "    - world\n");

        var source = LegacyPexReader.readInstallation(pex);
        var report = LegacyPexImporter.apply(core, source.data);

        // PermissionsExPlus creates "default" itself at startup.
        assertEquals(2, report.groupsCreated);
        assertEquals(1, report.groupsUpdated);
        assertEquals(2, report.usersImported);
        assertEquals(1, report.timedMemberships);
        assertEquals(1, report.expiredMemberships);
        assertEquals(1, report.ladders);
        assertEquals(1, report.worlds);

        var defaults = group("default");
        assertTrue(defaults.explicitPermissions().contains(allow("chat.send", ContextSet.empty())));
        assertTrue(defaults.explicitPermissions().contains(PermissionNode.builder().permission("chat.color").value(PermissionValue.DENY).build()));
        assertTrue(defaults.explicitPermissions().contains(allow("world.build", WORLD)));
        assertTrue(defaults.explicitOptions().stream().anyMatch(option -> option.key().equals("prefix") && option.value().equals("&7")));

        var admin = group("admin");
        assertEquals(100, admin.weight().getAsInt());
        assertTrue(admin.parents().contains(ParentNode.builder().group("vip").build()));

        assertEquals(Immutables.listOf("default", "vip", "admin"), core.ladders().find("default").toCompletableFuture().join().get().groups());
        assertEquals(Immutables.listOf("world"), core.worlds().parents("world_nether"));

        var steve = user(STEVE);
        assertEquals("Steve", steve.name());
        var vip = steve.groups().stream().filter(node -> node.group().equals("vip")).findFirst().get();
        assertEquals(Instant.ofEpochSecond(FUTURE), vip.expiry().get());
        assertTrue(steve.explicitOptions().stream().anyMatch(option -> option.key().equals("group-vip-until") && option.expiry().isPresent()));
        assertTrue(steve.groups().contains(ParentNode.builder().group("admin").contexts(NETHER).build()));
        assertTrue(user(ALEX).groups().isEmpty(), "an expired timed membership is skipped");

        // End to end: a world=world grant on an inherited group reaches Steve in
        // world_nether.
        var inNether = QueryOptions.builder().contexts(NETHER).build();
        assertEquals(PermissionResult.ALLOW, core.resolvers().permissions().check(steve, "world.build", inNether));
        assertEquals(PermissionResult.DENY, core.resolvers().permissions().check(steve, "chat.color", inNether));
        assertEquals(PermissionResult.ALLOW, core.resolvers().permissions().check(steve, "nether.portal", inNether));

        assertTrue(report.warnings.stream().anyMatch(warning -> warning.contains("stored by name") && warning.contains("OldName")));
        assertTrue(report.warnings.stream().anyMatch(warning -> warning.contains("regular expressions") && warning.contains("modifyworld.(place|break)")));
        assertFalse(report.warnings.stream().anyMatch(warning -> warning.contains("default group")));
    }

    @Test
    void readsTheSqlTables() throws Exception {
        try (Connection connection = DriverManager.getConnection("jdbc:h2:mem:pex;DB_CLOSE_DELAY=-1")) {
            try (var statement = connection.createStatement()) {
                statement.execute("CREATE TABLE permissions (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(50), type INT, permission VARCHAR(200), world VARCHAR(50), \"value\" VARCHAR(200))");
                statement.execute("CREATE TABLE permissions_entity (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(50), type INT, \"default\" INT)");
                statement.execute("CREATE TABLE permissions_inheritance (id INT AUTO_INCREMENT PRIMARY KEY, child VARCHAR(50), parent VARCHAR(50), type INT, world VARCHAR(50))");

                statement.execute("INSERT INTO permissions_entity (name, type, \"default\") VALUES ('default', 0, 1), ('vip', 0, 0), ('" + STEVE + "', 1, 0)");
                statement.execute("INSERT INTO permissions (name, type, permission, world, \"value\") VALUES "
                        + "('default', 0, 'chat.send', '', ''), "
                        + "('default', 0, 'prefix', '', '&7'), "
                        + "('vip', 0, '-chat.color', 'world', ''), "
                        + "('" + STEVE + "', 1, 'name', '', 'Steve')");
                statement.execute("INSERT INTO permissions_inheritance (child, parent, type, world) VALUES "
                        + "('vip', 'default', 0, NULL), "
                        + "('" + STEVE + "', 'vip', 1, 'world_nether'), "
                        + "('world_nether', 'world', 2, NULL)");
            }

            var data = LegacyPexReader.readSql(connection, Immutables.mapOf());

            assertTrue(data.groups.get("default").isDefault);
            assertEquals(Immutables.listOf("chat.send"), data.groups.get("default").permissions.get(LegacyPexData.GLOBAL));
            assertEquals("&7", data.groups.get("default").option("prefix"));
            assertEquals(Immutables.listOf("-chat.color"), data.groups.get("vip").permissions.get("world"));
            assertEquals(Immutables.listOf("default"), data.groups.get("vip").parents.get(LegacyPexData.GLOBAL));
            assertEquals(Immutables.listOf("vip"), data.users.get(STEVE.toString()).parents.get("world_nether"));
            assertEquals("Steve", data.users.get(STEVE.toString()).option("name"));
            assertEquals(Immutables.mapOf("world_nether", Immutables.listOf("world")), data.worldInheritance);
        }
    }

    private Group group(String name) {
        return core.groups().find(name).toCompletableFuture().join().get();
    }

    private User user(UUID id) {
        return core.users().find(id).toCompletableFuture().join().get();
    }

    private static PermissionNode allow(String permission, ContextSet contexts) {
        return PermissionNode.builder().permission(permission).contexts(contexts).build();
    }
}
