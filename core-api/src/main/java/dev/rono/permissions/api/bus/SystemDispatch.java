package dev.rono.permissions.api.bus;

import java.util.UUID;

public record SystemDispatch(UUID sourceId, SystemMutation mutation) implements PermissionDispatch {}
