package dev.rono.permissions.core.storage.migration;

import dev.rono.permissions.core.storage.LocalSqlRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

class YamlToSqlMigratorTest {

    private final YamlToSqlMigrator migrator = new YamlToSqlMigrator(Logger.getLogger("test"));

    @TempDir
    Path tempDir;

    @AfterEach
    void tearDown() {
    }

    @Test
    void migratesGroupsUsersAndLadders() throws Exception {
        File yaml = tempDir.resolve("permissions.yml").toFile();
        Files.writeString(yaml.toPath(), """
                schema-version: 1
                groups:
                  default:
                    permissions:
                    - spawn
                    options:
                      default: true
                  vip:
                    inheritance:
                    - default
                    permissions:
                    - fly
                    options:
                      prefix: '[VIP] '
                      rank: '10'
                      rank-ladder: staff
                users:
                  steve:
                    group:
                    - vip
                    permissions:
                    - special.node
                """);

        LocalSqlRepository repository = LocalSqlRepository.inMemory("yaml-migrate-" + System.nanoTime());
        YamlToSqlMigrator.MigrationResult result = migrator.migrate(yaml, repository);

        assertTrue(result.migrated());
        assertFalse(Files.exists(yaml.toPath()));
        assertTrue(Files.exists(tempDir.resolve("permissions.yml.migrated")));

        assertTrue(repository.findGroupByName("default").isPresent());
        assertTrue(repository.findGroupByName("vip").isPresent());
        assertTrue(repository.findUserByName("steve").isPresent());
        assertEquals(List.of("fly"), repository.getGroupPermissions(
                repository.findGroupId("vip").orElseThrow(), null));
        assertEquals(List.of("special.node"), repository.getUserPermissions(
                repository.findUserByName("steve").orElseThrow().getId(), null));
        assertEquals(1, repository.loadLadders().size());
    }

    @Test
    void migratesGroupsWithWeightPrefixAndDefaultOptions() throws Exception {
        File yaml = tempDir.resolve("permissions.yml").toFile();
        Files.writeString(yaml.toPath(), """
                schema-version: 1
                groups:
                  Member:
                    options:
                      weight: '50'
                      default: true
                      prefix: '&7[&eMember&7] &7'
                    permissions:
                    - menu.open.profile
                  Helper:
                    options:
                      weight: '7'
                      default: false
                      prefix: '&7[&aHelper&7] &7'
                    inheritance:
                    - Member
                    permissions:
                    - essentials.afk
                users:
                  87674864-8ba1-4f85-8afa-6c3cdebf1d7a:
                    permissions:
                    - '*'
                    options:
                      name: Rono
                  1db9be2e-dd6b-4eff-8796-0a2028512c41:
                    permissions:
                    - '*'
                    options:
                      name: SkellyX
                    group:
                    - Helper
                """);

        LocalSqlRepository repository = LocalSqlRepository.inMemory("yaml-weight-" + System.nanoTime());
        YamlToSqlMigrator.MigrationResult result = migrator.migrate(yaml, repository);

        assertTrue(result.migrated());
        int memberId = repository.findGroupId("Member").orElseThrow();
        int helperId = repository.findGroupId("Helper").orElseThrow();
        assertEquals(50, repository.loadGroup(memberId).getWeight());
        assertTrue(repository.loadGroup(memberId).isDefaultGroup());
        assertEquals(7, repository.loadGroup(helperId).getWeight());
        assertEquals("&7[&eMember&7] &7", repository.loadGroup(memberId).getOptions().getPrefix());
        assertEquals(List.of("Member"), repository.getGroupParents(helperId));
        UUID ronoId = UUID.fromString("87674864-8ba1-4f85-8afa-6c3cdebf1d7a");
        assertEquals("Rono", repository.getUserEntityOptions(ronoId, null).get("name"));
    }

    @Test
    void skipsWhenDatabaseAlreadyPopulated() throws Exception {
        File yaml = tempDir.resolve("permissions.yml").toFile();
        Files.writeString(yaml.toPath(), "groups: {}\n");

        LocalSqlRepository repository = LocalSqlRepository.inMemory("yaml-skip-" + System.nanoTime());
        repository.deploySchema();
        repository.setSchemaVersion(LocalSqlRepository.SCHEMA_VERSION);
        repository.upsertGroup("existing", 0, false);

        YamlToSqlMigrator.MigrationResult result = migrator.migrate(yaml, repository);
        assertFalse(result.migrated());
        assertTrue(Files.exists(yaml.toPath()));
    }
}
