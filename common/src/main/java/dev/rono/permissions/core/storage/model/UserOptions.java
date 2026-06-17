package dev.rono.permissions.core.storage.model;

public final class UserOptions {

    private final String prefix;
    private final String suffix;

    public UserOptions(String prefix, String suffix) {
        this.prefix = prefix;
        this.suffix = suffix;
    }

    public String getPrefix() { return prefix; }
    public String getSuffix() { return suffix; }
}
