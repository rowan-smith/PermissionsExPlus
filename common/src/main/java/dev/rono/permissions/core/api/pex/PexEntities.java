package dev.rono.permissions.core.api.pex;

import dev.rono.permissions.api.ladder.Ladder;
import dev.rono.permissions.api.permission.PermissionHolder;

final class LadderImpl implements Ladder {

    private final String name;
    private final PermissionHolder holder;

    LadderImpl(String name) {
        this.name = name;
        this.holder = new LadderPermissionHolder(name);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public PermissionHolder asHolder() {
        return holder;
    }
}
