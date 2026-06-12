package dev.rono.permissions.core.backends.file;

import java.util.List;
import java.util.Map;
import ru.tehkode.permissions.exceptions.PermissionBackendException;

/**
 * Validates classic {@code permissions.yml} shape before the file backend loads data.
 */
public final class PexYamlValidator {
    private PexYamlValidator() {}

    public static void validateRoot(Map<String, Object> root) throws PermissionBackendException {
        if (root == null) {
            throw new PermissionBackendException("permissions.yml is empty or could not be parsed");
        }
        validateSection(root, YamlMaps.USERS, "users");
        validateSection(root, YamlMaps.GROUPS, "groups");
        Object schema = root.get(YamlMaps.SCHEMA_VERSION);
        if (schema != null && !(schema instanceof Number) && !(schema instanceof String)) {
            throw new PermissionBackendException(
                    "permissions.yml: 'schema-version' must be a number, got " + schema.getClass().getSimpleName());
        }
        Object worldInheritance = root.get(YamlMaps.WORLD_INHERITANCE_LEGACY);
        if (worldInheritance != null && !(worldInheritance instanceof Map)) {
            throw new PermissionBackendException(
                    "permissions.yml: 'world-inheritance' must be a map of world -> parent list");
        }
    }

    private static void validateSection(Map<String, Object> root, String key, String label)
            throws PermissionBackendException {
        Object section = root.get(key);
        if (section == null) {
            return;
        }
        if (!(section instanceof Map<?, ?> entities)) {
            throw new PermissionBackendException(
                    "permissions.yml: '" + label + "' must be a map of names to user/group sections, got "
                            + section.getClass().getSimpleName());
        }
        for (Map.Entry<?, ?> entry : entities.entrySet()) {
            if (!(entry.getKey() instanceof String name) || name.isBlank()) {
                throw new PermissionBackendException(
                        "permissions.yml: '" + label + "' keys must be non-empty strings");
            }
            if (!(entry.getValue() instanceof Map<?, ?> entity)) {
                throw new PermissionBackendException(
                        "permissions.yml: entry '" + name + "' in '" + label + "' must be a configuration map");
            }
            validateEntity(cast(entity), label, name);
        }
    }

    private static void validateEntity(Map<String, Object> entity, String label, String name)
            throws PermissionBackendException {
        validatePermissionsNode(entity.get(YamlMaps.PERMISSIONS), label, name, YamlMaps.PERMISSIONS);
        if (entity.containsKey(YamlMaps.WORLDS) && !(entity.get(YamlMaps.WORLDS) instanceof Map)) {
            throw new PermissionBackendException(
                    "permissions.yml: '" + name + "' in '" + label + "' has 'worlds' that is not a map");
        }
        Object worlds = entity.get(YamlMaps.WORLDS);
        if (worlds instanceof Map<?, ?> worldMap) {
            for (Map.Entry<?, ?> worldEntry : worldMap.entrySet()) {
                if (!(worldEntry.getValue() instanceof Map<?, ?> worldBucket)) {
                    throw new PermissionBackendException(
                            "permissions.yml: world '" + worldEntry.getKey() + "' for '" + name
                                    + "' must be a map section");
                }
                validatePermissionsNode(
                        cast(worldBucket).get(YamlMaps.PERMISSIONS),
                        label,
                        name,
                        "worlds." + worldEntry.getKey() + ".permissions");
            }
        }
    }

    private static void validatePermissionsNode(Object node, String label, String name, String path)
            throws PermissionBackendException {
        if (node == null) {
            return;
        }
        if (node instanceof List<?> list) {
            for (Object item : list) {
                if (!(item instanceof String)) {
                    throw new PermissionBackendException(
                            "permissions.yml: '" + path + "' for '" + name + "' in '" + label
                                    + "' must be a list of permission strings");
                }
            }
            return;
        }
        if (node instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!(entry.getKey() instanceof String)) {
                    throw new PermissionBackendException(
                            "permissions.yml: permission map keys under '" + path + "' must be strings");
                }
            }
            return;
        }
        throw new PermissionBackendException(
                "permissions.yml: '" + path + "' for '" + name + "' in '" + label
                        + "' must be a list or map of permissions, got " + node.getClass().getSimpleName());
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> cast(Map<?, ?> map) {
        return (Map<String, Object>) map;
    }
}
