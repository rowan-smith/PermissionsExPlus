package dev.rono.permissions.api.bus;

/**
 * Immutable permission-domain notification produced by the engine and delivered to the active {@link dev.rono.permissions.api.runtime.PlatformAdapter}.
 */
public sealed interface PermissionDispatch permits EntityDispatch, SystemDispatch {}
