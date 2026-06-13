package dev.rono.permissions.core.backends.file;

import org.junit.jupiter.api.Test;
import ru.tehkode.permissions.exceptions.PermissionBackendException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PexYamlValidatorTest {

    @Test
    void acceptsMinimalValidDocument() {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("groups", Map.of("default", Map.of("permissions", List.of("permissions.*"))));
        assertDoesNotThrow(() -> PexYamlValidator.validateRoot(root));
    }

    @Test
    void rejectsInvalidUsersSectionType() {
        Map<String, Object> root = Map.of("users", "not-a-map");
        assertThrows(PermissionBackendException.class, () -> PexYamlValidator.validateRoot(root));
    }

    @Test
    void rejectsInvalidPermissionListEntry() {
        Map<String, Object> root = Map.of(
                "groups",
                Map.of("default", Map.of("permissions", List.of(42))));
        assertThrows(PermissionBackendException.class, () -> PexYamlValidator.validateRoot(root));
    }
}
