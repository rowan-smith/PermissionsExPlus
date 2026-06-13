package dev.rono.permissions.api.runtime;

import dev.rono.permissions.api.bus.PermissionDispatch;

/**
 * Bundles the three platform bridge contracts injected into the permission engine at bootstrap.
 *
 * @param adapter   identity and realm resolution
 * @param eventBus  native listener publication for bus dispatches
 * @param scheduler host thread scheduling
 */
public record PlatformRuntime(PlatformAdapter adapter, PlatformEventBus eventBus, PlatformScheduler scheduler) {

    public PlatformRuntime {
        if (adapter == null || eventBus == null || scheduler == null) {
            throw new NullPointerException("PlatformRuntime components must not be null");
        }
    }

    public static PlatformRuntime of(
            PlatformAdapter adapter,
            PlatformEventBus eventBus,
            PlatformScheduler scheduler) {
        return new PlatformRuntime(adapter, eventBus, scheduler);
    }

    /**
     * Convenience for tests and minimal hosts that only supply an adapter.
     */
    public static PlatformRuntime adapterOnly(PlatformAdapter adapter) {
        return new PlatformRuntime(adapter, NoOpPlatformEventBus.INSTANCE, DirectPlatformScheduler.INSTANCE);
    }
}
