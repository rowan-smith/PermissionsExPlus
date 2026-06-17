package dev.rono.permissions.api.runtime;

import dev.rono.permissions.api.bus.PermissionDispatch;

/**
 * Event bus that intentionally ignores all dispatches (proxy runtimes without legacy Bukkit listeners).
 */
public enum NoOpPlatformEventBus implements PlatformEventBus {
    INSTANCE;

    @Override
    public void publish(PermissionDispatch dispatch) {
        // Proxy runtimes intentionally do not emulate Bukkit event listeners.
    }
}
