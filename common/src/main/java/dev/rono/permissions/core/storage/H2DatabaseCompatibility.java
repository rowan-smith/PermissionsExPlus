package dev.rono.permissions.core.storage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Detects legacy H2 databases created when the engine was relocated inside the plugin jar.
 */
public final class H2DatabaseCompatibility {

    private static final byte[] SHADED_H2_MARKER =
            "ru.tehkode.libs.org.h2".getBytes(StandardCharsets.US_ASCII);

    private H2DatabaseCompatibility() {}

    public static boolean appearsShadedH2Database(Path databaseBasePath) {
        Path mvDb = databaseBasePath.getParent() == null
                ? Path.of(databaseBasePath.getFileName() + ".mv.db")
                : databaseBasePath.getParent().resolve(databaseBasePath.getFileName() + ".mv.db");
        if (!Files.isRegularFile(mvDb)) {
            return false;
        }
        try (InputStream in = Files.newInputStream(mvDb)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                if (containsMarker(buffer, read)) {
                    return true;
                }
            }
        } catch (IOException ignored) {
        }
        return false;
    }

    public static String shadedDatabaseMigrationHint(String databaseBasePath) {
        return "The H2 database at '" + databaseBasePath
                + "' was created by an older PermissionsExPlus build that relocated H2 inside the plugin jar."
                + " Export your data with the previous jar (/pex backend export), stop the server,"
                + " rename or remove permissions.mv.db, upgrade to this build, start the server,"
                + " and import the YAML via a file backend alias."
                + " For read-only inspection without upgrading, use DataGrip with the old PermissionsExPlus jar"
                + " as the driver (class ru.tehkode.libs.org.h2.Driver) or export via /pex backend export.";
    }

    private static boolean containsMarker(byte[] buffer, int length) {
        if (length < SHADED_H2_MARKER.length) {
            return false;
        }
        outer:
        for (int i = 0; i <= length - SHADED_H2_MARKER.length; i++) {
            for (int j = 0; j < SHADED_H2_MARKER.length; j++) {
                if (buffer[i + j] != SHADED_H2_MARKER[j]) {
                    continue outer;
                }
            }
            return true;
        }
        return false;
    }
}
