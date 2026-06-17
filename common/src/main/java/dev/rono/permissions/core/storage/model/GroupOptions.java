package dev.rono.permissions.core.storage.model;

public final class GroupOptions {

    private final String prefix;
    private final String suffix;

    public GroupOptions(String prefix, String suffix) {
        this.prefix = prefix;
        this.suffix = suffix;
    }

    public String getPrefix() { return prefix; }
    public String getSuffix() { return suffix; }
}
