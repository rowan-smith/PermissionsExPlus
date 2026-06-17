package dev.rono.permissions.api.runtime;

import java.util.UUID;

/**
 * Unified online player identity for permission checks and context resolution.
 */
public interface PlatformPlayer {

    UUID uuid();

    String name();

    String displayName();

    boolean isOnline();

    boolean isOperator();
}
