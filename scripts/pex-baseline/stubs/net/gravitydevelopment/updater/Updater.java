package net.gravitydevelopment.updater;

import java.io.File;
import org.bukkit.plugin.Plugin;

/** Compile-only stand-in for Gravity's Updater, which is no longer published. */
public class Updater {
    public enum UpdateType {
        DEFAULT, NO_VERSION_CHECK, NO_DOWNLOAD
    }

    public enum UpdateResult {
        SUCCESS, NO_UPDATE, DISABLED, FAIL_DOWNLOAD, FAIL_DBO, FAIL_NOVERSION, FAIL_BADID, FAIL_APIKEY, UPDATE_AVAILABLE
    }

    public Updater(Plugin plugin, int id, File file, UpdateType type, boolean announce) {
    }

    public boolean shouldUpdate(String localVersion, String remoteVersion) {
        throw new UnsupportedOperationException();
    }

    public UpdateResult getResult() {
        throw new UnsupportedOperationException();
    }

    public String getLatestName() {
        throw new UnsupportedOperationException();
    }
}
