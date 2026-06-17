package dev.rono.permissions.api.bus;

/**
 * Immutable permission-domain notification produced by the engine and delivered to the active platform {@code PlatformEventBus}.
 */
public sealed interface PermissionDispatch permits EntityDispatch, SystemDispatch {}
