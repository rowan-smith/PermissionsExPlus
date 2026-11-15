package dev.rono.permissions.core.config;

import dev.rono.permissions.core.platform.PlatformConfiguration;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

public record BackendConfiguration(
        BackendType type,
        String localStorageDirectory,
        String localFilename,
        DatabaseCredentials credentials,
        DatabasePool pool,
        DdlGeneration ddlGeneration) {
    public BackendConfiguration(
            BackendType type,
            String localFilename,
            DatabaseCredentials credentials,
            DatabasePool pool) {

        this(type, "data", localFilename, credentials, pool, DdlGeneration.UPDATE);
    }

    public static BackendConfiguration load(PlatformConfiguration platform) {
        Path file = platform.resolve("database.yml");

        try {
            Files.createDirectories(file.getParent());

            if (Files.notExists(file)) {
                platform.saveResource("database.yml", false);
            }

            if (Files.notExists(file)) {
                throw new IllegalStateException("Bundled database.yml was not saved to " + file);
            }

            var root = YamlConfigurationLoader.builder().path(file).build().load();
            if (root.node("data-version").getInt(1) != 1) {
                throw new IllegalStateException("Unsupported data-version; expected 1");
            }

            var type = BackendType.parse(required("type", root.node("type").getString()));

            var credentials = new DatabaseCredentials(
                    required("credentials.host", root.node("credentials", "host").getString()),
                    root.node("credentials", "port").getInt(3306),
                    required("credentials.database", root.node("credentials", "database").getString()),
                    required("credentials.username", root.node("credentials", "username").getString()),
                    root.node("credentials", "password").getString(""));

            var pool = new DatabasePool(
                    root.node("pool", "maximum-pool-size").getInt(10),
                    root.node("pool", "minimum-idle").getInt(2),
                    root.node("pool", "connection-timeout").getLong(5_000L),
                    root.node("pool", "max-lifetime").getLong(1_800_000L));

            if (pool.minimumIdle() < 0 || pool.maximumPoolSize() < 1 || pool.minimumIdle() > pool.maximumPoolSize()) {
                throw new IllegalStateException("Invalid database pool bounds");
            }

            if (pool.connectionTimeout() < 250L) {
                throw new IllegalStateException("pool.connection-timeout must be at least 250 milliseconds");
            }

            if (pool.maxLifetime() < 0L) {
                throw new IllegalStateException("pool.max-lifetime cannot be negative");
            }

            var storageDirectory = required("local.storage-directory", root.node("local", "storage-directory").getString("data"));

            validateRelativePath("local.storage-directory", storageDirectory, true);

            var filename = required("local.filename", root.node("local", "filename").getString());

            validateRelativePath("local.filename", filename, false);

            // `schema.ddl-generation`; `hibernate.ddl-generation` is still read from
            // configs
            // written before the JDBC store replaced Hibernate.
            var schemaNode = root.node("schema", "ddl-generation");
            var ddlGeneration = DdlGeneration.parse(schemaNode.virtual() ? root.node("hibernate", "ddl-generation").getString("update") : schemaNode.getString("update"));

            return new BackendConfiguration(type, storageDirectory, filename, credentials, pool, ddlGeneration);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load " + file, e);
        }
    }

    public Path resolveLocalStorageDirectory(Path dataDirectory) {
        var root = dataDirectory.toAbsolutePath().normalize();
        var resolved = root.resolve(localStorageDirectory).normalize();

        if (!resolved.startsWith(root)) {
            throw new IllegalStateException("local.storage-directory must remain inside the plugin data directory");
        }

        return resolved;
    }

    public Path resolveLocalFile(Path dataDirectory) {
        var directory = resolveLocalStorageDirectory(dataDirectory);
        var resolved = directory.resolve(localFilename).normalize();

        if (!resolved.startsWith(directory)) {
            throw new IllegalStateException("local.filename must remain inside local.storage-directory");
        }

        return resolved;
    }

    private static <T> T required(String key, T value) {
        if (value == null || (value instanceof String text && text.isBlank())) {
            throw new IllegalStateException("Missing required setting '" + key + "' in database.yml");
        }

        return value;
    }

    private static void validateRelativePath(String key, String value, boolean allowDirectories) {
        Path path;

        try {
            path = Path.of(value);
        } catch (RuntimeException error) {
            throw new IllegalStateException("Invalid path in '" + key + "'", error);
        }

        if (path.isAbsolute() || path.normalize().startsWith("..")) {
            throw new IllegalStateException("Setting '" + key + "' must be relative to the plugin data directory");
        }

        if (!allowDirectories && path.getNameCount() != 1) {
            throw new IllegalStateException("Setting '" + key + "' must be a base filename, not a path");
        }
    }
}
