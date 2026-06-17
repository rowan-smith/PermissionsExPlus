package dev.rono.permissions.api.permission;

/**
 * A single resolved permission entry after applying inheritance, context, and priority rules.
 */
public interface ResolvedPermissionView {

    String permission();

    boolean value();

    int priority();

    /** Context key from storage; {@code null} means global. */
    String contextKey();

    /** Origin: {@code user}, {@code group}, or {@code inheritance}. */
    String source();
}
