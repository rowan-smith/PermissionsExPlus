package dev.rono.permissions.core.storage.migration;

import dev.rono.permissions.core.storage.LocalSqlRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
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
