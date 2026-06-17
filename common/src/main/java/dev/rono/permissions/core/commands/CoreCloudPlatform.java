package dev.rono.permissions.core.commands;

/**
 * Where Cloud commands execute: gameplay servers use {@link #GAME_SERVER}; proxies use proxy-specific
 * literals (e.g. {@code pex server}) via {@link #PROXY}.
 */
public enum CoreCloudPlatform {
    GAME_SERVER,
    PROXY
}
