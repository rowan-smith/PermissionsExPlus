package ru.tehkode.permissions.support;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;

/**
 * Java 8 stand-ins for Files.writeString/readString (Java 11) used by tests.
 */
public final class TestFiles {
    private TestFiles() {}

    public static Path writeString(Path path, CharSequence text, OpenOption... options) throws IOException {
        return Files.write(path, text.toString().getBytes(StandardCharsets.UTF_8), options);
    }

    public static String readString(Path path) throws IOException {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
}
