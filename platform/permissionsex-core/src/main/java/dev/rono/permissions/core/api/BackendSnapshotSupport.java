package dev.rono.permissions.core.api;

import dev.rono.permissions.api.PermissionsExException;
import dev.rono.permissions.core.DefaultPermissionManager;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import ru.tehkode.permissions.PEXBackendConfiguration;
import ru.tehkode.permissions.backends.PermissionBackend;
import ru.tehkode.permissions.exceptions.PermissionBackendException;

public final class BackendSnapshotSupport {
    private BackendSnapshotSupport() {}

    public static String export(PermissionBackend backend) throws PermissionsExException {
        StringWriter writer = new StringWriter();
        try {
            backend.writeContents(writer);
        } catch (IOException e) {
            throw new PermissionsExException("Failed to export backend data", e);
        }
        return writer.toString();
    }

    public static PermissionBackend snapshotFromYaml(DefaultPermissionManager manager, String document)
            throws PermissionsExException {
        try {
            var tempFile = Files.createTempFile("pex-import-", ".yml");
            Files.writeString(tempFile, document, StandardCharsets.UTF_8);

            Map<String, Object> values = new HashMap<>();
            values.put("type", "file");
            values.put("file", tempFile.toAbsolutePath().toString());

            PEXBackendConfiguration config = new MapBackendConfiguration("import-snapshot", values);
            return PermissionBackend.getBackend("file", manager, config);
        } catch (IOException | PermissionBackendException e) {
            throw new PermissionsExException("Failed to parse import document", e);
        }
    }

    private static final class MapBackendConfiguration implements PEXBackendConfiguration {
        private final String name;
        private final Map<String, Object> values;

        MapBackendConfiguration(String name, Map<String, Object> values) {
            this.name = name;
            this.values = values;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getString(String path) {
            Object value = values.get(path);
            return value instanceof String s ? s : null;
        }

        @Override
        public String getString(String path, String def) {
            String value = getString(path);
            return value != null ? value : def;
        }

        @Override
        public void set(String path, Object value) {
            values.put(path, value);
        }

        @Override
        @SuppressWarnings("unchecked")
        public java.util.List<String> getStringList(String path) {
            Object value = values.get(path);
            if (value instanceof java.util.List<?> list) {
                return (java.util.List<String>) list;
            }
            return java.util.Collections.emptyList();
        }

        @Override
        public java.util.Map<String, Object> getValues(boolean deep) {
            return deep ? new java.util.HashMap<>(values) : new java.util.HashMap<>(values);
        }

        @Override
        public PEXBackendConfiguration getConfigurationSection(String path) {
            return null;
        }

        @Override
        public PEXBackendConfiguration createSection(String path) {
            return this;
        }

        @Override
        public boolean isConfigurationSection(String path) {
            return false;
        }
    }
}
