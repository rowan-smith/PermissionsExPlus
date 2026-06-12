package dev.rono.permissions.api.backend;

/**
 * Active permission backend snapshot.
 *
 * @param type            configured backend alias (e.g. {@code file}, {@code sql})
 * @param simpleName      runtime implementation simple class name
 * @param diagnosticLabel human-readable label for logs
 */
public record PexBackendInfo(String type, String simpleName, String diagnosticLabel) {}
