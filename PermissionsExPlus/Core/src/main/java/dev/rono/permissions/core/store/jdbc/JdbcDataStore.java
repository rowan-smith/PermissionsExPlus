package dev.rono.permissions.core.store.jdbc;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.rono.permissions.api.context.ContextBuilder;
import dev.rono.permissions.api.context.ContextSet;
import dev.rono.permissions.api.options.OptionNode;
import dev.rono.permissions.api.options.OptionNodeBuilder;
import dev.rono.permissions.api.parent.ParentNode;
import dev.rono.permissions.api.parent.ParentNodeBuilder;
import dev.rono.permissions.api.permission.PermissionNode;
import dev.rono.permissions.api.permission.PermissionNodeBuilder;
import dev.rono.permissions.api.permission.PermissionValue;
import dev.rono.permissions.core.config.DatabasePool;
import dev.rono.permissions.core.config.DdlGeneration;
import dev.rono.permissions.core.model.GroupSnapshot;
import dev.rono.permissions.core.model.LadderSnapshot;
import dev.rono.permissions.core.model.UserSnapshot;
import dev.rono.permissions.core.store.DataStore;
import dev.rono.permissions.core.store.PersistenceLogging;
import dev.rono.permissions.core.store.SnapshotCodec;
import dev.rono.permissions.core.store.StorageConflictException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Relational {@link DataStore} over plain JDBC and a HikariCP pool.
 *
 * <p>
 * Each user, group, and ladder is stored as typed rows across eight tables.
 * Saving a subject replaces all of its node rows in one transaction, matching
 * the whole-snapshot semantics of {@link DataStore}.
 * </p>
 *
 * <p>
 * Users, groups and ladders carry a revision that every save increments. A
 * snapshot read at revision {@code n} saves only while the row is still at
 * {@code n}, so two servers editing the same subject cannot silently overwrite
 * each other. A snapshot with revision 0 (new, or not read from here) is saved
 * unconditionally.
 * </p>
 *
 * <p>
 * Written against the Java 8 JDK so the same store serves every distribution.
 * </p>
 */
public final class JdbcDataStore implements DataStore {
    private final String name, url, driver, username, password;
    private final DatabasePool pool;
    private final DdlGeneration ddlGeneration;
    private final JdbcDialect dialect;
    private final boolean persistent;

    private HikariDataSource dataSource;

    /** The connection of the transaction open on this thread, if any. */
    private final ThreadLocal<Connection> transaction = new ThreadLocal<>();

    public JdbcDataStore(String name, String url, String driver, String username, String password, DatabasePool pool, DdlGeneration ddlGeneration, JdbcDialect dialect, boolean persistent) {
        this.name = name;
        this.url = url;
        this.driver = driver;
        this.username = username;
        this.password = password;
        this.pool = pool;
        this.ddlGeneration = ddlGeneration;
        this.dialect = dialect;
        this.persistent = persistent;
    }

    @Override
    public synchronized void open() {
        if (dataSource != null) {
            return;
        }

        PersistenceLogging.suppressRoutineMessages();

        HikariConfig config = new HikariConfig();
        config.setPoolName("PermissionsExPlus-" + name);
        config.setJdbcUrl(url);
        config.setDriverClassName(driver);

        if (!blank(username)) {
            config.setUsername(username);
        }

        if (!blank(password)) {
            config.setPassword(password);
        }

        int maximum = dialect.singleConnection() ? 1 : pool.maximumPoolSize();

        config.setMaximumPoolSize(maximum);
        config.setMinimumIdle(Math.min(pool.minimumIdle(), maximum));
        config.setConnectionTimeout(pool.connectionTimeout());
        config.setMaxLifetime(pool.maxLifetime());

        HikariDataSource created = new HikariDataSource(config);

        try {
            new JdbcSchema(dialect).apply(created, ddlGeneration);
        } catch (RuntimeException error) {
            created.close();
            throw error;
        }

        dataSource = created;
    }

    @Override
    public <T> T transaction(Supplier<T> work) {
        if (transaction.get() != null) {
            return work.get();
        }

        try (Connection connection = connection()) {
            boolean autoCommit = connection.getAutoCommit();

            connection.setAutoCommit(false);
            transaction.set(connection);

            try {
                T result = work.get();

                connection.commit();

                return result;
            } catch (RuntimeException | Error error) {
                connection.rollback();
                throw error;
            } finally {
                transaction.remove();
                connection.setAutoCommit(autoCommit);
            }
        } catch (SQLException error) {
            throw new IllegalStateException(name + " storage transaction failed", error);
        }
    }

    @Override
    public synchronized void close() {
        if (dataSource != null) {
            dataSource.close();
            dataSource = null;
        }
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public boolean persistent() {
        return persistent;
    }

    // ---------------------------------------------------------------- generic
    // category API

    @Override
    public Optional<String> get(String category, String key) {
        switch (category) {
            case "users" :
                return getUser(UUID.fromString(key)).map(SnapshotCodec::user);
            case "groups" :
                return getGroup(key).map(SnapshotCodec::group);
            case "ladders" :
                return getLadder(key).map(SnapshotCodec::ladder);
            default :
                throw unknownCategory(category);
        }
    }

    @Override
    public Map<String, String> all(String category) {
        Map<String, String> result = new LinkedHashMap<>();

        switch (category) {
            case "users" :
                allUsers().forEach((key, value) -> result.put(key, SnapshotCodec.user(value)));
                break;
            case "groups" :
                allGroups().forEach((key, value) -> result.put(key, SnapshotCodec.group(value)));
                break;
            case "ladders" :
                allLadders().forEach((key, value) -> result.put(key, SnapshotCodec.ladder(value)));
                break;
            default :
                throw unknownCategory(category);
        }

        return Collections.unmodifiableMap(result);
    }

    @Override
    public void put(String category, String key, String payload) {
        switch (category) {
            case "users" :
                putUser(SnapshotCodec.user(payload));
                break;
            case "groups" :
                putGroup(SnapshotCodec.group(payload));
                break;
            case "ladders" :
                putLadder(SnapshotCodec.ladder(payload));
                break;
            default :
                throw unknownCategory(category);
        }
    }

    @Override
    public boolean remove(String category, String key) {
        switch (category) {
            case "users" :
                return removeUser(UUID.fromString(key));
            case "groups" :
                return removeGroup(key);
            case "ladders" :
                return removeLadder(key);
            default :
                throw unknownCategory(category);
        }
    }

    // ---------------------------------------------------------------- users

    @Override
    public Optional<UserSnapshot> getUser(UUID id) {
        return read(connection -> {
            String key = id.toString();
            String userName = null;
            long revision = 0;

            try (PreparedStatement statement = connection.prepareStatement("SELECT name, revision FROM pex_users WHERE id = ?")) {
                statement.setString(1, key);

                try (ResultSet rows = statement.executeQuery()) {
                    if (rows.next()) {
                        userName = rows.getString(1);
                        revision = rows.getLong(2);
                    }
                }
            }

            if (userName == null) {
                return Optional.<UserSnapshot>empty();
            }

            Nodes nodes = loadNodes(connection, "user", key).getOrDefault(key, Nodes.EMPTY);

            return Optional.of(new UserSnapshot(id, userName, nodes.permissions(), nodes.options(), nodes.parents(), revision));
        });
    }

    @Override
    public Map<String, UserSnapshot> allUsers() {
        return read(connection -> {
            Map<String, Nodes> nodes = loadNodes(connection, "user", null);
            Map<String, UserSnapshot> result = new LinkedHashMap<>();

            try (PreparedStatement statement = connection.prepareStatement("SELECT id, name, revision FROM pex_users"); ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    String key = rows.getString(1);
                    Nodes subject = nodes.getOrDefault(key, Nodes.EMPTY);

                    result.put(key, new UserSnapshot(UUID.fromString(key), rows.getString(2), subject.permissions(), subject.options(), subject.parents(), rows.getLong(3)));
                }
            }

            return Collections.unmodifiableMap(result);
        });
    }

    @Override
    public void putUser(UserSnapshot user) {
        write(connection -> {
            String key = user.uniqueId().toString();

            saveRow(connection, "user " + user.name() + " (" + key + ")", user.revision(),
                    "UPDATE pex_users SET name = ?, revision = revision + 1 WHERE id = ?",
                    "INSERT INTO pex_users (id, name, revision) VALUES (?, ?, 1)",
                    new Object[]{user.name(), key}, new Object[]{key, user.name()});

            replaceNodes(connection, "user", key, user.explicitPermissions(), user.explicitOptions(), user.groups());

            return null;
        });
    }

    @Override
    public boolean removeUser(UUID id) {
        return write(connection -> {
            String key = id.toString();

            deleteNodes(connection, "user", key);

            return update(connection, "DELETE FROM pex_users WHERE id = ?", key) > 0;
        });
    }

    // ---------------------------------------------------------------- groups

    @Override
    public Optional<GroupSnapshot> getGroup(String groupName) {
        return read(connection -> {
            OptionalInt weight = null;
            long revision = 0;

            try (PreparedStatement statement = connection.prepareStatement("SELECT weight, revision FROM pex_groups WHERE name = ?")) {
                statement.setString(1, groupName);

                try (ResultSet rows = statement.executeQuery()) {
                    if (rows.next()) {
                        weight = weight(rows, 1);
                        revision = rows.getLong(2);
                    }
                }
            }

            if (weight == null) {
                return Optional.<GroupSnapshot>empty();
            }

            Nodes nodes = loadNodes(connection, "group", groupName).getOrDefault(groupName, Nodes.EMPTY);

            return Optional.of(new GroupSnapshot(groupName, weight, nodes.permissions(), nodes.options(), nodes.parents(), revision));
        });
    }

    @Override
    public Map<String, GroupSnapshot> allGroups() {
        return read(connection -> {
            Map<String, Nodes> nodes = loadNodes(connection, "group", null);
            Map<String, GroupSnapshot> result = new LinkedHashMap<>();

            try (PreparedStatement statement = connection.prepareStatement("SELECT name, weight, revision FROM pex_groups"); ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    String key = rows.getString(1);
                    Nodes subject = nodes.getOrDefault(key, Nodes.EMPTY);

                    result.put(key, new GroupSnapshot(key, weight(rows, 2), subject.permissions(), subject.options(), subject.parents(), rows.getLong(3)));
                }
            }

            return Collections.unmodifiableMap(result);
        });
    }

    @Override
    public void putGroup(GroupSnapshot group) {
        write(connection -> {
            Integer weight = group.weight().isPresent() ? group.weight().getAsInt() : null;

            saveRow(connection, "group " + group.name(), group.revision(),
                    "UPDATE pex_groups SET weight = ?, revision = revision + 1 WHERE name = ?",
                    "INSERT INTO pex_groups (name, weight, revision) VALUES (?, ?, 1)",
                    new Object[]{weight, group.name()}, new Object[]{group.name(), weight});

            replaceNodes(connection, "group", group.name(), group.explicitPermissions(), group.explicitOptions(), group.parents());

            return null;
        });
    }

    @Override
    public boolean removeGroup(String groupName) {
        return write(connection -> {
            deleteNodes(connection, "group", groupName);

            return update(connection, "DELETE FROM pex_groups WHERE name = ?", groupName) > 0;
        });
    }

    // ---------------------------------------------------------------- ladders

    @Override
    public Optional<LadderSnapshot> getLadder(String ladderName) {
        return read(connection -> {
            boolean exists;
            long revision = 0;

            try (PreparedStatement statement = connection.prepareStatement("SELECT revision FROM pex_ladders WHERE name = ?")) {
                statement.setString(1, ladderName);

                try (ResultSet rows = statement.executeQuery()) {
                    exists = rows.next();

                    if (exists) {
                        revision = rows.getLong(1);
                    }
                }
            }

            if (!exists) {
                return Optional.<LadderSnapshot>empty();
            }

            List<String> groups = loadLadderGroups(connection, ladderName).getOrDefault(ladderName, Collections.<String>emptyList());

            return Optional.of(new LadderSnapshot(ladderName, groups, revision));
        });
    }

    @Override
    public Map<String, LadderSnapshot> allLadders() {
        return read(connection -> {
            Map<String, List<String>> groups = loadLadderGroups(connection, null);
            Map<String, LadderSnapshot> result = new LinkedHashMap<>();

            try (PreparedStatement statement = connection.prepareStatement("SELECT name, revision FROM pex_ladders"); ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    String key = rows.getString(1);

                    result.put(key, new LadderSnapshot(key, groups.getOrDefault(key, Collections.<String>emptyList()), rows.getLong(2)));
                }
            }

            return Collections.unmodifiableMap(result);
        });
    }

    @Override
    public void putLadder(LadderSnapshot ladder) {
        write(connection -> {
            saveRow(connection, "ladder " + ladder.name(), ladder.revision(),
                    "UPDATE pex_ladders SET revision = revision + 1 WHERE name = ?",
                    "INSERT INTO pex_ladders (name, revision) VALUES (?, 1)",
                    new Object[]{ladder.name()}, new Object[]{ladder.name()});

            update(connection, "DELETE FROM pex_ladder_groups WHERE ladder_name = ?", ladder.name());

            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO pex_ladder_groups (id, ladder_name, group_name, position) VALUES (?, ?, ?, ?)")) {
                for (int index = 0; index < ladder.groups().size(); index++) {
                    statement.setString(1, UUID.randomUUID().toString());
                    statement.setString(2, ladder.name());
                    statement.setString(3, ladder.groups().get(index));
                    statement.setInt(4, index);
                    statement.addBatch();
                }

                statement.executeBatch();
            }

            return null;
        });
    }

    @Override
    public boolean removeLadder(String ladderName) {
        return write(connection -> {
            update(connection, "DELETE FROM pex_ladder_groups WHERE ladder_name = ?", ladderName);

            return update(connection, "DELETE FROM pex_ladders WHERE name = ?", ladderName) > 0;
        });
    }

    /**
     * Updates or inserts a subject's row. With a non-zero {@code revision}, the
     * update applies only while the row is still at that revision.
     */
    private static void saveRow(Connection connection, String subject, long revision, String updateSql, String insertSql, Object[] updateParameters, Object[] insertParameters) throws SQLException {
        if (revision > 0) {
            Object[] checked = Arrays.copyOf(updateParameters, updateParameters.length + 1);
            checked[updateParameters.length] = revision;

            if (update(connection, updateSql + " AND revision = ?", checked) == 0) {
                throw new StorageConflictException("The " + subject + " was changed by another server");
            }

            return;
        }

        if (update(connection, updateSql, updateParameters) == 0) {
            update(connection, insertSql, insertParameters);
        }
    }

    private static Map<String, List<String>> loadLadderGroups(Connection connection, String ladderName) throws SQLException {
        String sql = "SELECT ladder_name, group_name FROM pex_ladder_groups" + (ladderName == null ? "" : " WHERE ladder_name = ?") + " ORDER BY ladder_name, position";
        Map<String, List<String>> result = new LinkedHashMap<>();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            if (ladderName != null) {
                statement.setString(1, ladderName);
            }

            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    result.computeIfAbsent(rows.getString(1), key -> new ArrayList<>()).add(rows.getString(2));
                }
            }
        }

        Map<String, List<String>> frozen = new LinkedHashMap<>();
        result.forEach((key, value) -> frozen.put(key, Collections.unmodifiableList(value)));

        return frozen;
    }

    // ---------------------------------------------------------------- permission /
    // option / parent nodes

    /**
     * Loads every node of one subject type, or of a single subject when
     * {@code subjectKey} is non-null. Uses one query per table rather than one
     * per node, so loading all groups at startup stays a handful of queries.
     */
    private Map<String, Nodes> loadNodes(Connection connection, String subjectType, String subjectKey) throws SQLException {
        Map<String, NodesBuilder> builders = new LinkedHashMap<>();

        Map<String, ContextBuilder> permissionContexts = loadContexts(connection, "permission", "pex_permissions", subjectType, subjectKey);
        try (PreparedStatement statement = subjectQuery(connection, "SELECT id, subject_key, permission, permission_value, expiry FROM pex_permissions", subjectType, subjectKey); ResultSet rows = statement.executeQuery()) {
            while (rows.next()) {
                PermissionNodeBuilder builder = PermissionNode.builder()
                        .permission(rows.getString(3))
                        .value(PermissionValue.valueOf(rows.getString(4)))
                        .contexts(contexts(permissionContexts, rows.getString(1)));

                Instant expiry = instant(rows, 5);
                if (expiry != null) {
                    builder.expiry(expiry);
                }

                builders.computeIfAbsent(rows.getString(2), key -> new NodesBuilder()).permissions.add(builder.build());
            }
        }

        Map<String, ContextBuilder> optionContexts = loadContexts(connection, "option", "pex_options", subjectType, subjectKey);
        try (PreparedStatement statement = subjectQuery(connection, "SELECT id, subject_key, option_key, option_value, expiry FROM pex_options", subjectType, subjectKey); ResultSet rows = statement.executeQuery()) {
            while (rows.next()) {
                OptionNodeBuilder builder = OptionNode.builder()
                        .key(rows.getString(3))
                        .value(rows.getString(4))
                        .contexts(contexts(optionContexts, rows.getString(1)));

                Instant expiry = instant(rows, 5);
                if (expiry != null) {
                    builder.expiry(expiry);
                }

                builders.computeIfAbsent(rows.getString(2), key -> new NodesBuilder()).options.add(builder.build());
            }
        }

        Map<String, ContextBuilder> parentContexts = loadContexts(connection, "parent", "pex_parents", subjectType, subjectKey);
        try (PreparedStatement statement = subjectQuery(connection, "SELECT id, subject_key, group_name, expiry FROM pex_parents", subjectType, subjectKey); ResultSet rows = statement.executeQuery()) {
            while (rows.next()) {
                ParentNodeBuilder builder = ParentNode.builder()
                        .group(rows.getString(3))
                        .contexts(contexts(parentContexts, rows.getString(1)));

                Instant expiry = instant(rows, 4);
                if (expiry != null) {
                    builder.expiry(expiry);
                }

                builders.computeIfAbsent(rows.getString(2), key -> new NodesBuilder()).parents.add(builder.build());
            }
        }

        Map<String, Nodes> result = new LinkedHashMap<>();
        builders.forEach((key, builder) -> result.put(key, builder.build()));

        return result;
    }

    private static Map<String, ContextBuilder> loadContexts(Connection connection, String nodeType, String nodeTable, String subjectType, String subjectKey) throws SQLException {
        String sql = "SELECT c.node_id, c.context_key, c.context_value FROM pex_contexts c JOIN " + nodeTable + " n ON n.id = c.node_id"
                + " WHERE c.node_type = ? AND n.subject_type = ?" + (subjectKey == null ? "" : " AND n.subject_key = ?");
        Map<String, ContextBuilder> result = new LinkedHashMap<>();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, nodeType);
            statement.setString(2, subjectType);

            if (subjectKey != null) {
                statement.setString(3, subjectKey);
            }

            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    result.computeIfAbsent(rows.getString(1), key -> ContextSet.builder()).add(rows.getString(2), rows.getString(3));
                }
            }
        }

        return result;
    }

    private static ContextSet contexts(Map<String, ContextBuilder> contexts, String nodeId) {
        ContextBuilder builder = contexts.get(nodeId);

        return builder == null ? ContextSet.empty() : builder.build();
    }

    private static PreparedStatement subjectQuery(Connection connection, String select, String subjectType, String subjectKey) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(select + " WHERE subject_type = ?" + (subjectKey == null ? "" : " AND subject_key = ?"));

        statement.setString(1, subjectType);

        if (subjectKey != null) {
            statement.setString(2, subjectKey);
        }

        return statement;
    }

    private void replaceNodes(Connection connection, String subjectType, String subjectKey, Set<PermissionNode> permissions, Set<OptionNode> options, Set<ParentNode> parents) throws SQLException {
        deleteNodes(connection, subjectType, subjectKey);

        try (PreparedStatement nodes = connection.prepareStatement("INSERT INTO pex_permissions (id, subject_type, subject_key, permission, permission_value, expiry) VALUES (?, ?, ?, ?, ?, ?)");
                PreparedStatement contexts = contextInsert(connection)) {
            for (PermissionNode node : permissions) {
                String id = UUID.randomUUID().toString();

                nodes.setString(1, id);
                nodes.setString(2, subjectType);
                nodes.setString(3, subjectKey);
                nodes.setString(4, node.permission());
                nodes.setString(5, node.value().name());
                setInstant(nodes, 6, node.expiry().orElse(null));
                nodes.addBatch();

                addContexts(contexts, "permission", id, node.contexts());
            }

            nodes.executeBatch();
            contexts.executeBatch();
        }

        try (PreparedStatement nodes = connection.prepareStatement("INSERT INTO pex_options (id, subject_type, subject_key, option_key, option_value, expiry) VALUES (?, ?, ?, ?, ?, ?)");
                PreparedStatement contexts = contextInsert(connection)) {
            for (OptionNode node : options) {
                String id = UUID.randomUUID().toString();

                nodes.setString(1, id);
                nodes.setString(2, subjectType);
                nodes.setString(3, subjectKey);
                nodes.setString(4, node.key());
                nodes.setString(5, node.value());
                setInstant(nodes, 6, node.expiry().orElse(null));
                nodes.addBatch();

                addContexts(contexts, "option", id, node.contexts());
            }

            nodes.executeBatch();
            contexts.executeBatch();
        }

        try (PreparedStatement nodes = connection.prepareStatement("INSERT INTO pex_parents (id, subject_type, subject_key, group_name, expiry) VALUES (?, ?, ?, ?, ?)");
                PreparedStatement contexts = contextInsert(connection)) {
            for (ParentNode node : parents) {
                String id = UUID.randomUUID().toString();

                nodes.setString(1, id);
                nodes.setString(2, subjectType);
                nodes.setString(3, subjectKey);
                nodes.setString(4, node.group());
                setInstant(nodes, 5, node.expiry().orElse(null));
                nodes.addBatch();

                addContexts(contexts, "parent", id, node.contexts());
            }

            nodes.executeBatch();
            contexts.executeBatch();
        }
    }

    private static PreparedStatement contextInsert(Connection connection) throws SQLException {
        return connection.prepareStatement("INSERT INTO pex_contexts (id, node_type, node_id, context_key, context_value) VALUES (?, ?, ?, ?, ?)");
    }

    private static void addContexts(PreparedStatement statement, String nodeType, String nodeId, ContextSet contexts) throws SQLException {
        for (Map.Entry<String, Set<String>> entry : contexts.asMap().entrySet()) {
            for (String value : entry.getValue()) {
                statement.setString(1, UUID.randomUUID().toString());
                statement.setString(2, nodeType);
                statement.setString(3, nodeId);
                statement.setString(4, entry.getKey());
                statement.setString(5, value);
                statement.addBatch();
            }
        }
    }

    private static void deleteNodes(Connection connection, String subjectType, String subjectKey) throws SQLException {
        String[][] tables = {{"permission", "pex_permissions"}, {"option", "pex_options"}, {"parent", "pex_parents"}};

        for (String[] table : tables) {
            update(connection, "DELETE FROM pex_contexts WHERE node_type = ? AND node_id IN (SELECT id FROM " + table[1] + " WHERE subject_type = ? AND subject_key = ?)", table[0], subjectType, subjectKey);
            update(connection, "DELETE FROM " + table[1] + " WHERE subject_type = ? AND subject_key = ?", subjectType, subjectKey);
        }
    }

    // ---------------------------------------------------------------- JDBC
    // plumbing

    private interface Work<T> {
        T run(Connection connection) throws SQLException;
    }

    private <T> T read(Work<T> work) {
        Connection open = transaction.get();

        if (open != null) {
            try {
                return work.run(open);
            } catch (SQLException error) {
                throw new IllegalStateException(name + " storage read failed", error);
            }
        }

        try (Connection connection = connection()) {
            return work.run(connection);
        } catch (SQLException error) {
            throw new IllegalStateException(name + " storage read failed", error);
        }
    }

    private <T> T write(Work<T> work) {
        Connection open = transaction.get();

        if (open != null) {
            try {
                return work.run(open);
            } catch (SQLException error) {
                throw new IllegalStateException(name + " storage write failed", error);
            }
        }

        try (Connection connection = connection()) {
            boolean autoCommit = connection.getAutoCommit();

            connection.setAutoCommit(false);

            try {
                T result = work.run(connection);

                connection.commit();

                return result;
            } catch (SQLException | RuntimeException error) {
                connection.rollback();
                throw error;
            } finally {
                connection.setAutoCommit(autoCommit);
            }
        } catch (SQLException error) {
            throw new IllegalStateException(name + " storage write failed", error);
        }
    }

    private Connection connection() throws SQLException {
        HikariDataSource current = dataSource;

        if (current == null) {
            throw new IllegalStateException("Storage is not open");
        }

        return current.getConnection();
    }

    private static int update(Connection connection, String sql, Object... parameters) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int index = 0; index < parameters.length; index++) {
                Object value = parameters[index];

                if (value == null) {
                    statement.setNull(index + 1, Types.INTEGER);
                } else if (value instanceof Integer) {
                    statement.setInt(index + 1, (Integer) value);
                } else if (value instanceof Long) {
                    statement.setLong(index + 1, (Long) value);
                } else {
                    statement.setString(index + 1, value.toString());
                }
            }

            return statement.executeUpdate();
        }
    }

    private static OptionalInt weight(ResultSet rows, int column) throws SQLException {
        int value = rows.getInt(column);

        return rows.wasNull() ? OptionalInt.empty() : OptionalInt.of(value);
    }

    private Instant instant(ResultSet rows, int column) throws SQLException {
        return dialect.getInstant(rows, column);
    }

    private void setInstant(PreparedStatement statement, int column, Instant value) throws SQLException {
        dialect.setInstant(statement, column, value);
    }

    private static boolean blank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static IllegalArgumentException unknownCategory(String category) {
        return new IllegalArgumentException("Unsupported relational storage category: " + category);
    }

    private static final class NodesBuilder {
        final Set<PermissionNode> permissions = new LinkedHashSet<>();
        final Set<OptionNode> options = new LinkedHashSet<>();
        final Set<ParentNode> parents = new LinkedHashSet<>();

        Nodes build() {
            return new Nodes(Collections.unmodifiableSet(permissions), Collections.unmodifiableSet(options), Collections.unmodifiableSet(parents));
        }
    }

    private static final class Nodes {
        static final Nodes EMPTY = new Nodes(Collections.<PermissionNode>emptySet(), Collections.<OptionNode>emptySet(), Collections.<ParentNode>emptySet());

        private final Set<PermissionNode> permissions;
        private final Set<OptionNode> options;
        private final Set<ParentNode> parents;

        Nodes(Set<PermissionNode> permissions, Set<OptionNode> options, Set<ParentNode> parents) {
            this.permissions = permissions;
            this.options = options;
            this.parents = parents;
        }

        Set<PermissionNode> permissions() {
            return permissions;
        }

        Set<OptionNode> options() {
            return options;
        }

        Set<ParentNode> parents() {
            return parents;
        }
    }
}
