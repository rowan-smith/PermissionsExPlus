package dev.rono.permissions.core.config;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PexConfigDataLegacyBackendTest {

    @Test
    void normalizesActiveFileBackendToLocalWithMigrationSource() {
        LinkedHashMap<String, Object> fileSection = new LinkedHashMap<>();
        fileSection.put(PexConfigData.KEY_BACKEND_TYPE, "file");
        fileSection.put(PexConfigData.KEY_BACKEND_FILE_LEAF, "customperms.yml");

        Map<String, Object> root = new LinkedHashMap<>();
        root.put(PexConfigData.KEY_BACKEND, "file");
        LinkedHashMap<String, Object> backends = new LinkedHashMap<>();
        backends.put("file", fileSection);
        root.put(PexConfigData.KEY_BACKENDS, backends);

        PexConfigData data = PexConfigData.fromPermissionsMap(root, () -> ".", PexConfigFlavor.SPIGOT);

        assertEquals("local", data.backend());
        assertEquals("customperms.yml", data.storeRelative());
        assertEquals("customperms.yml", data.backends().get("local").get("migration-source"));
    }
}
