package dev.rono.permissions.core.store.jdbc;

import dev.rono.permissions.core.config.DdlGeneration;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.sql.DataSource;

/**
 * Creates or validates the relational schema.
 *
 * <p>
 * {@code pex_schema} records the schema version. Future layout changes add a
 * numbered step to {@link #migrate}; a database stamped with a newer version
 * than this build understands is refused rather than silently misread.
 * </p>
 */
final class JdbcSchema {
    static final int CURRENT_VERSION = 1;

    private final JdbcDialect dialect;

    JdbcSchema(JdbcDialect dialect) {
        this.dialect = dialect;
    }

    void apply(DataSource dataSource, DdlGeneration mode) {
        if (mode == DdlGeneration.NONE) {
            return;
        }

        try (Connection connection = dataSource.getConnection()) {
            if (mode == DdlGeneration.UPDATE) {
                create(connection);
                migrate(connection);
            } else {
                validate(connection);
            }
        } catch (SQLException error) {
            throw new IllegalStateException("Unable to " + (mode == DdlGeneration.UPDATE ? "update" : "validate") + " the PermissionsExPlus schema", error);
        }
    }

    /** Table name to required columns, in creation order. */
    private Map<String, List<String>> tables() {
        String timestamp = dialect.timestampType();
        Map<String, List<String>> tables = new LinkedHashMap<>();

        tables.put("pex_users", Arrays.asList(
                "id VARCHAR(36) NOT NULL PRIMARY KEY",
                "name VARCHAR(64) NOT NULL UNIQUE"));
        tables.put("pex_groups", Arrays.asList(
                "name VARCHAR(64) NOT NULL PRIMARY KEY",
                "weight INTEGER"));
        tables.put("pex_ladders", Arrays.asList(
                "name VARCHAR(64) NOT NULL PRIMARY KEY"));
        tables.put("pex_ladder_groups", Arrays.asList(
                "id VARCHAR(36) NOT NULL PRIMARY KEY",
                "ladder_name VARCHAR(64) NOT NULL",
                "group_name VARCHAR(64) NOT NULL",
                "position INTEGER NOT NULL"));
        tables.put("pex_permissions", Arrays.asList(
                "id VARCHAR(36) NOT NULL PRIMARY KEY",
                "subject_type VARCHAR(16) NOT NULL",
                "subject_key VARCHAR(64) NOT NULL",
                "permission VARCHAR(255) NOT NULL",
                "permission_value VARCHAR(16) NOT NULL",
                "expiry " + timestamp));
        tables.put("pex_options", Arrays.asList(
                "id VARCHAR(36) NOT NULL PRIMARY KEY",
                "subject_type VARCHAR(16) NOT NULL",
                "subject_key VARCHAR(64) NOT NULL",
                "option_key VARCHAR(64) NOT NULL",
                "option_value VARCHAR(2048) NOT NULL",
                "expiry " + timestamp));
        tables.put("pex_parents", Arrays.asList(
                "id VARCHAR(36) NOT NULL PRIMARY KEY",
                "subject_type VARCHAR(16) NOT NULL",
                "subject_key VARCHAR(64) NOT NULL",
                "group_name VARCHAR(64) NOT NULL",
                "expiry " + timestamp));
        tables.put("pex_contexts", Arrays.asList(
                "id VARCHAR(36) NOT NULL PRIMARY KEY",
                "node_type VARCHAR(16) NOT NULL",
                "node_id VARCHAR(36) NOT NULL",
                "context_key VARCHAR(64) NOT NULL",
                "context_value VARCHAR(255) NOT NULL"));
        tables.put("pex_schema", Arrays.asList(
                "version INTEGER NOT NULL"));

        return tables;
    }

    /**
     * Index name to {table, columns}. Names match the earlier Hibernate mapping.
     */
    private static Map<String, String[]> indexes() {
        Map<String, String[]> indexes = new LinkedHashMap<>();

        indexes.put("idx_pex_permissions_subject", new String[]{"pex_permissions", "subject_type, subject_key"});
        indexes.put("idx_pex_options_subject", new String[]{"pex_options", "subject_type, subject_key"});
        indexes.put("idx_pex_parents_subject", new String[]{"pex_parents", "subject_type, subject_key"});
        indexes.put("idx_pex_contexts_node", new String[]{"pex_contexts", "node_type, node_id"});
        indexes.put("idx_pex_ladder_groups_ladder", new String[]{"pex_ladder_groups", "ladder_name, position"});

        return indexes;
    }

    private void create(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            for (Map.Entry<String, List<String>> table : tables().entrySet()) {
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + table.getKey() + " (" + String.join(", ", table.getValue()) + ")");
            }

            for (Map.Entry<String, String[]> index : indexes().entrySet()) {
                String table = index.getValue()[0];
                String columns = index.getValue()[1];

                if (dialect.createIndexIfNotExists()) {
                    statement.executeUpdate("CREATE INDEX IF NOT EXISTS " + index.getKey() + " ON " + table + " (" + columns + ")");
                } else if (!indexExists(connection, table, index.getKey())) {
                    statement.executeUpdate("CREATE INDEX " + index.getKey() + " ON " + table + " (" + columns + ")");
                }
            }
        }
    }

    private void migrate(Connection connection) throws SQLException {
        int version = version(connection);

        if (version > CURRENT_VERSION) {
            throw newerSchema(version);
        }

        if (version == 0) {
            // Fresh database, or one created by the earlier Hibernate store, which
            // had no version table but the same version-1 layout.
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("INSERT INTO pex_schema (version) VALUES (" + CURRENT_VERSION + ")");
            }
        }

        // Future migrations: if (version < 2) { ...; setVersion(connection, 2); }
    }

    private void validate(Connection connection) throws SQLException {
        DatabaseMetaData metadata = connection.getMetaData();
        List<String> problems = new ArrayList<>();

        for (Map.Entry<String, List<String>> table : tables().entrySet()) {
            if (table.getKey().equals("pex_schema")) {
                continue; // absent on databases created before versioning
            }

            Set<String> actual = columns(metadata, connection, table.getKey());

            if (actual.isEmpty()) {
                problems.add("missing table " + table.getKey());
                continue;
            }

            for (String definition : table.getValue()) {
                String column = definition.substring(0, definition.indexOf(' ')).toLowerCase(Locale.ROOT);

                if (!actual.contains(column)) {
                    problems.add("missing column " + table.getKey() + "." + column);
                }
            }
        }

        if (!problems.isEmpty()) {
            throw new IllegalStateException("PermissionsExPlus schema validation failed: " + String.join(", ", problems));
        }

        if (!columns(metadata, connection, "pex_schema").isEmpty()) {
            int version = version(connection);

            if (version > CURRENT_VERSION) {
                throw newerSchema(version);
            }
        }
    }

    private static int version(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement(); ResultSet rows = statement.executeQuery("SELECT MAX(version) FROM pex_schema")) {
            return rows.next() ? rows.getInt(1) : 0;
        }
    }

    private static IllegalStateException newerSchema(int version) {
        return new IllegalStateException("The PermissionsExPlus database uses schema version " + version
                + ", but this build only understands version " + CURRENT_VERSION + ". Update PermissionsExPlus.");
    }

    /**
     * Lower-cased column names of a table, or empty when the table does not exist.
     */
    private static Set<String> columns(DatabaseMetaData metadata, Connection connection, String table) throws SQLException {
        Set<String> result = new LinkedHashSet<>();

        // Identifier case differs by database (H2 upper-cases, Postgres lower-cases).
        for (String candidate : new String[]{table, table.toUpperCase(Locale.ROOT)}) {
            try (ResultSet rows = metadata.getColumns(connection.getCatalog(), schema(connection), candidate, null)) {
                while (rows.next()) {
                    result.add(rows.getString("COLUMN_NAME").toLowerCase(Locale.ROOT));
                }
            }

            if (!result.isEmpty()) {
                break;
            }
        }

        return result;
    }

    private static String schema(Connection connection) {
        try {
            return connection.getSchema();
        } catch (SQLException | AbstractMethodError | UnsupportedOperationException error) {
            return null;
        }
    }

    private static boolean indexExists(Connection connection, String table, String index) throws SQLException {
        // MySQL only: CREATE INDEX has no IF NOT EXISTS.
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT 1 FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = ? AND index_name = ?")) {
            statement.setString(1, table);
            statement.setString(2, index);

            try (ResultSet rows = statement.executeQuery()) {
                return rows.next();
            }
        }
    }
}
