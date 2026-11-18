package dev.rono.permissions.core.migration;

import lombok.var;

import dev.rono.permissions.core.migration.LegacyPexData.Entity;
import dev.rono.permissions.core.util.Strings;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

/**
 * Reads data from an original PermissionsEx installation: its
 * {@code permissions.yml} (file backend) or its SQL tables, whichever its
 * {@code config.yml} selects.
 */
public final class LegacyPexReader {
    private static final int GROUP = 0;
    private static final int USER = 1;
    private static final int WORLD = 2;
    private static final Pattern TABLE_NAME = Pattern.compile("[A-Za-z0-9_]+");

    private LegacyPexReader() {}

    /** Describes where data was read from, for the import report. */
    public static final class Source {
        public final String description;
        public final LegacyPexData data;

        Source(String description, LegacyPexData data) {
            this.description = description;
            this.data = data;
        }
    }

    /**
     * Reads the PermissionsEx data folder {@code folder} (usually
     * {@code plugins/PermissionsEx}), using the backend its config.yml selects.
     */
    public static Source readInstallation(Path folder) throws Exception {
        var config = folder.resolve("config.yml");

        if (Files.notExists(config)) {
            var permissions = folder.resolve("permissions.yml");

            if (Files.notExists(permissions)) {
                throw new IllegalArgumentException("No PermissionsEx config.yml or permissions.yml in " + folder);
            }

            return new Source(permissions.toString(), readYaml(permissions));
        }

        var root = YamlConfigurationLoader.builder().path(config).build().load();
        var name = root.node("permissions", "backend").getString("file");
        var backend = root.node("permissions", "backends", name);
        var type = backend.node("type").getString(name).toLowerCase(Locale.ROOT);

        switch (type) {
            case "file" :
                var file = folder.resolve(backend.node("file").getString("permissions.yml"));
                return new Source(file.toString(), readYaml(file));
            case "sql" :
                var uri = backend.node("uri").getString("");
                var aliases = new java.util.LinkedHashMap<String, String>();

                for (Map.Entry<Object, ? extends ConfigurationNode> alias : backend.node("aliases").childrenMap().entrySet()) {
                    aliases.put(String.valueOf(alias.getKey()), alias.getValue().getString());
                }

                try (Connection connection = connect(uri, backend.node("user").getString(""), backend.node("password").getString(""))) {
                    return new Source("SQL " + redact(uri), readSql(connection, aliases));
                }

            default :
                throw new IllegalArgumentException("Unsupported PermissionsEx backend type '" + type + "'; supported: file, sql");
        }
    }

    /** Reads a PermissionsEx {@code permissions.yml}. */
    public static LegacyPexData readYaml(Path file) throws Exception {
        if (Files.notExists(file)) {
            throw new IllegalArgumentException("No such file: " + file);
        }

        var root = YamlConfigurationLoader.builder().path(file).build().load();
        var data = new LegacyPexData();

        for (Map.Entry<Object, ? extends ConfigurationNode> group : root.node("groups").childrenMap().entrySet()) {
            readEntity(group.getValue(), data.group(String.valueOf(group.getKey())), "inheritance");
        }

        for (Map.Entry<Object, ? extends ConfigurationNode> user : root.node("users").childrenMap().entrySet()) {
            readEntity(user.getValue(), data.user(String.valueOf(user.getKey())), "group");
        }

        for (Map.Entry<Object, ? extends ConfigurationNode> world : root.node("worlds").childrenMap().entrySet()) {
            var parents = strings(world.getValue().node("inheritance"));

            if (!parents.isEmpty()) {
                data.worldInheritance.put(String.valueOf(world.getKey()), parents);
            }
        }

        return data;
    }

    /**
     * Reads the PermissionsEx SQL tables. {@code aliases} maps the default table
     * names (permissions, permissions_entity, permissions_inheritance) to the
     * names configured in PermissionsEx, if they differ.
     */
    public static LegacyPexData readSql(Connection connection, Map<String, String> aliases) throws SQLException {
        var data = new LegacyPexData();
        var entities = table(aliases, "permissions_entity");
        var permissions = table(aliases, "permissions");
        var inheritance = table(aliases, "permissions_inheritance");

        try (var statement = connection.createStatement()) {
            try (ResultSet rows = statement.executeQuery("SELECT * FROM " + entities)) {
                while (rows.next()) {
                    var entity = entity(data, rows.getInt("type"), rows.getString("name"));

                    if (entity != null) {
                        entity.isDefault = rows.getBoolean("default");
                    }
                }
            }

            try (ResultSet rows = statement.executeQuery("SELECT * FROM " + permissions + " ORDER BY id")) {
                while (rows.next()) {
                    var entity = entity(data, rows.getInt("type"), rows.getString("name"));

                    if (entity == null) {
                        continue;
                    }

                    var world = world(rows.getString("world"));
                    var key = rows.getString("permission");
                    var value = rows.getString("value");

                    // PermissionsEx keeps options in the permissions table: a row with a value is
                    // an option.
                    if (value == null || value.isEmpty()) {
                        entity.permission(world, key);
                    } else {
                        entity.option(world, key, value);
                    }
                }
            }

            try (ResultSet rows = statement.executeQuery("SELECT * FROM " + inheritance + " ORDER BY id")) {
                while (rows.next()) {
                    var type = rows.getInt("type");
                    var child = rows.getString("child");
                    var parent = rows.getString("parent");

                    if (type == WORLD) {
                        var parents = data.worldInheritance.computeIfAbsent(child, ignored -> new ArrayList<>());

                        if (!parents.contains(parent)) {
                            parents.add(parent);
                        }
                    } else {
                        var entity = entity(data, type, child);

                        if (entity != null) {
                            entity.parent(world(rows.getString("world")), parent);
                        }
                    }
                }
            }
        }

        return data;
    }

    private static void readEntity(ConfigurationNode node, Entity entity, String parentsKey) {
        entity.isDefault = node.node("default").getBoolean(false);
        readSection(node, entity, LegacyPexData.GLOBAL, parentsKey);

        for (Map.Entry<Object, ? extends ConfigurationNode> world : node.node("worlds").childrenMap().entrySet()) {
            readSection(world.getValue(), entity, String.valueOf(world.getKey()), parentsKey);
        }
    }

    private static void readSection(ConfigurationNode node, Entity entity, String world, String parentsKey) {
        strings(node.node("permissions")).forEach(permission -> entity.permission(world, permission));

        for (Map.Entry<Object, ? extends ConfigurationNode> option : node.node("options").childrenMap().entrySet()) {
            var value = option.getValue().getString();

            if (value != null) {
                entity.option(world, String.valueOf(option.getKey()), value);
            }
        }

        // Older PermissionsEx files keep prefix and suffix beside the options.
        for (var key : new String[]{"prefix", "suffix"}) {
            var value = node.node(key).getString();

            if (value != null && !entity.options.getOrDefault(world, new java.util.LinkedHashMap<>()).containsKey(key)) {
                entity.option(world, key, value);
            }
        }

        strings(node.node(parentsKey)).forEach(parent -> entity.parent(world, parent));
    }

    private static List<String> strings(ConfigurationNode node) {
        var values = new ArrayList<String>();

        if (node.isList()) {
            for (var child : node.childrenList()) {
                var value = child.getString();

                if (value != null && !Strings.isBlank(value)) {
                    values.add(value);
                }
            }
        } else if (!node.virtual() && !node.isMap()) {
            var value = node.getString();

            if (value != null && !Strings.isBlank(value)) {
                values.add(value);
            }
        }

        return values;
    }

    private static Entity entity(LegacyPexData data, int type, String name) {
        if (name == null || Strings.isBlank(name)) {
            return null;
        }

        switch (type) {
            case GROUP :
                return data.group(name);
            case USER :
                return data.user(name);
            default :
                return null;
        }
    }

    private static String world(String world) {
        return world == null || Strings.isBlank(world) ? LegacyPexData.GLOBAL : world;
    }

    private static String table(Map<String, String> aliases, String name) {
        var table = aliases.getOrDefault(name, name);

        if (table == null || !TABLE_NAME.matcher(table).matches()) {
            throw new IllegalArgumentException("Invalid PermissionsEx table name: " + table);
        }

        return table;
    }

    private static Connection connect(String uri, String user, String password) throws Exception {
        var url = uri.startsWith("jdbc:") ? uri.substring("jdbc:".length()) : uri;
        var scheme = url.substring(0, Math.max(0, url.indexOf(':'))).toLowerCase(Locale.ROOT);
        String driver;

        switch (scheme) {
            case "mysql" :
            case "mariadb" :
                driver = "com.mysql.cj.jdbc.Driver";
                url = "mysql" + url.substring(scheme.length());
                break;
            case "postgresql" :
                driver = "org.postgresql.Driver";
                break;
            case "sqlite" :
                driver = "org.sqlite.JDBC";
                break;
            default :
                throw new IllegalArgumentException("Unsupported PermissionsEx SQL uri '" + redact(uri) + "'; supported: mysql, mariadb, postgresql, sqlite");
        }

        // Load the bundled driver explicitly: DriverManager does not search plugin
        // class loaders.
        Class.forName(driver);

        return DriverManager.getConnection("jdbc:" + url, user, password);
    }

    private static String redact(String uri) {
        return uri.replaceAll("(?i)(password=)[^&;]*", "$1***");
    }
}
