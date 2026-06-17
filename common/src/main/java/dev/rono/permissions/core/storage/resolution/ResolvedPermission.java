package dev.rono.permissions.core.storage.resolution;

public final class ResolvedPermission {

    private final String permission;
    private final boolean value;
    private final int priority;
    private final String contextKey;
    private final String source;

    public ResolvedPermission(String permission,
                              boolean value,
                              int priority,
                              String contextKey,
                              String source) {
        this.permission = permission;
        this.value = value;
        this.priority = priority;
        this.contextKey = contextKey;
        this.source = source;
    }

    public String getPermission() { return permission; }
    public boolean isValue() { return value; }
    public int getPriority() { return priority; }
    public String getContextKey() { return contextKey; }
    public String getSource() { return source; }
}
