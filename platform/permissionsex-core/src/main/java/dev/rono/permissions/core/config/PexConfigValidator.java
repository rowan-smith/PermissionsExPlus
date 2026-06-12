package dev.rono.permissions.core.config;

import java.util.Map;
import ru.tehkode.permissions.exceptions.PermissionBackendException;

/**
 * Validates root {@code config.yml} {@code permissions} section values.
 */
public final class PexConfigValidator {
    private PexConfigValidator() {}

    public static void validatePermissionsSection(Map<String, Object> permissions) throws PermissionBackendException {
        if (permissions == null) {
            return;
        }
        Object backend = permissions.get("backend");
        if (backend != null) {
            if (!(backend instanceof String s) || s.isBlank()) {
                throw new PermissionBackendException("config.yml: permissions.backend must be a non-empty string");
            }
        }
        Object basedir = permissions.get("basedir");
        if (basedir != null && !(basedir instanceof String)) {
            throw new PermissionBackendException("config.yml: permissions.basedir must be a string path");
        }
        Object backends = permissions.get("backends");
        if (backends != null && !(backends instanceof Map)) {
            throw new PermissionBackendException("config.yml: permissions.backends must be a map of backend sections");
        }
    }
}
