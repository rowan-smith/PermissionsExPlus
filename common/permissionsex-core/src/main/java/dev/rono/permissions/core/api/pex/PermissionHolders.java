package dev.rono.permissions.core.api.pex;

import dev.rono.permissions.api.permission.HolderType;
import dev.rono.permissions.api.permission.PermissionHolder;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

final class UserPermissionHolder implements PermissionHolder {

    private final UUID id;

    UserPermissionHolder(UUID id) {
        this.id = id;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public HolderType getType() {
        return HolderType.USER;
    }
}

final class GroupPermissionHolder implements PermissionHolder {

    private final String name;
    private final UUID id;

    GroupPermissionHolder(String name) {
        this.name = name;
        this.id = UUID.nameUUIDFromBytes(("group:" + name).getBytes(StandardCharsets.UTF_8));
    }

    String groupName() {
        return name;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public HolderType getType() {
        return HolderType.GROUP;
    }
}

final class WorldPermissionHolder implements PermissionHolder {

    private final String name;
    private final UUID id;

    WorldPermissionHolder(String name) {
        this.name = name;
        this.id = UUID.nameUUIDFromBytes(("world:" + name).getBytes(StandardCharsets.UTF_8));
    }

    String worldName() {
        return name;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public HolderType getType() {
        return HolderType.WORLD;
    }
}

final class LadderPermissionHolder implements PermissionHolder {

    private final String name;
    private final UUID id;

    LadderPermissionHolder(String name) {
        this.name = name;
        this.id = UUID.nameUUIDFromBytes(("ladder:" + name).getBytes(StandardCharsets.UTF_8));
    }

    String ladderName() {
        return name;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public HolderType getType() {
        return HolderType.LADDER;
    }
}
