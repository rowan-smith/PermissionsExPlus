package dev.rono.permissions.core.storage;

import java.io.IOException;
import java.sql.SQLException;

final class LocalSqlSchemaUpgrader {

    static final int LATEST_VERSION = 2;

    private LocalSqlSchemaUpgrader() {}

    static void ensureLatest(LocalSqlRepository repository) throws SQLException, IOException {
        int version = repository.getSchemaVersion();
        if (version < 0) {
            if (!repository.tableExistsPublic("users")) {
                repository.deploySchema();
            }
            version = 0;
        }
        if (version < 2) {
            applyScripts(repository, "/sql/local/deploy-v2.sql");
            migrateUserGroupsToMemberships(repository);
            repository.setSchemaVersion(2);
        }
    }

    private static void migrateUserGroupsToMemberships(LocalSqlRepository repository) throws SQLException {
        if (repository.tableExistsPublic("user_group_memberships")) {
            return;
        }
        try (var conn = repository.openConnection(); var stmt = conn.createStatement()) {
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS user_group_memberships (
                        user_id TEXT NOT NULL,
                        group_id INT NOT NULL,
                        context_key TEXT NOT NULL DEFAULT '',
                        expires_at TIMESTAMP NULL,
                        PRIMARY KEY (user_id, group_id, context_key),
                        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                        FOREIGN KEY (group_id) REFERENCES "groups"(id) ON DELETE CASCADE
                    )
                    """);
            stmt.execute("""
                    INSERT INTO user_group_memberships (user_id, group_id, context_key, expires_at)
                    SELECT user_id, group_id, '', expires_at FROM user_groups
                    """);
        }
    }

    private static void applyScripts(LocalSqlRepository repository, String resource) throws SQLException, IOException {
        try (var conn = repository.openConnection(); var stmt = conn.createStatement()) {
            for (String sql : LocalSqlRepository.loadStatementsPublic(resource)) {
                stmt.execute(sql);
            }
        }
    }
}
