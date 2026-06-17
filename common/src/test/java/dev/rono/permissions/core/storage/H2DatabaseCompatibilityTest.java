package dev.rono.permissions.core.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class H2DatabaseCompatibilityTest {

    @TempDir
    Path tempDir;

    @Test
    void detectsShadedH2MarkerInMvDbFile() throws Exception {
        Path base = tempDir.resolve("permissions");
        Files.writeString(
                tempDir.resolve("permissions.mv.db"),
                "header ru.tehkode.libs.org.h2.mvstore.type.ByteArrayDataType trailer",
                StandardCharsets.US_ASCII);
        assertTrue(H2DatabaseCompatibility.appearsShadedH2Database(base));
    }

    @Test
    void ignoresStandardH2DatabaseFiles() throws Exception {
        Path base = tempDir.resolve("permissions");
        Files.writeString(
                tempDir.resolve("permissions.mv.db"),
                "header org.h2.mvstore.type.ByteArrayDataType trailer",
                StandardCharsets.US_ASCII);
        assertFalse(H2DatabaseCompatibility.appearsShadedH2Database(base));
    }

    @Test
    void returnsFalseWhenMvDbMissing() {
        assertFalse(H2DatabaseCompatibility.appearsShadedH2Database(tempDir.resolve("permissions")));
    }
}
