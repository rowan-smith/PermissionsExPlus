package ru.tehkode.permissions.backends.file;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * Compatibility stub for the legacy file-backend config type.
 *
 * <p>
 * Retained so plugins referencing {@link FileBackend#permissions} keep a binary-
 * compatible field type. Persistence is owned by PermissionsExPlus; load/save
 * are no-ops.
 * </p>
 */
public class FileConfig extends YamlConfiguration {
    private final List<String> lowerCaseSections;
    private final File file, tempFile, oldFile;
    private final Object lock;
    private boolean saveSuppressed;

    public FileConfig(File file) {
        this(file, new Object());
    }

    public FileConfig(File file, Object lock, String... lowerCaseSections) {
        super();
        this.lock = lock;
        this.lowerCaseSections = Arrays.asList(lowerCaseSections);
        this.options().pathSeparator(FileBackend.PATH_SEPARATOR);
        this.file = file;
        this.tempFile = new File(file.getPath() + ".tmp");
        this.oldFile = new File(file.getPath() + ".old");
    }

    public File getFile() {
        return file;
    }

    public void load() throws IOException, InvalidConfigurationException {
        // No-op: PermissionsExPlus owns persistence.
    }

    public void save() throws IOException {
        // No-op: PermissionsExPlus owns persistence.
    }

    public boolean isSaveSuppressed() {
        return saveSuppressed;
    }

    void setSaveSuppressed(boolean saveSuppressed) {
        this.saveSuppressed = saveSuppressed;
    }

    Object lock() {
        return lock;
    }

    File tempFile() {
        return tempFile;
    }

    File oldFile() {
        return oldFile;
    }

    List<String> lowerCaseSections() {
        return Collections.unmodifiableList(lowerCaseSections);
    }
}
