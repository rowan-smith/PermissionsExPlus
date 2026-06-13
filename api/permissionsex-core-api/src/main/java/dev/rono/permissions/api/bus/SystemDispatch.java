package dev.rono.permissions.api.bus;

import java.util.UUID;

/**
 * Installation-wide system dispatch published on the permission event bus.
 *
 * @param sourceId logical server identifier that originated the dispatch
 * @param mutation kind of system-level change
 */
public record SystemDispatch(UUID sourceId, SystemMutation mutation) implements PermissionDispatch {}
