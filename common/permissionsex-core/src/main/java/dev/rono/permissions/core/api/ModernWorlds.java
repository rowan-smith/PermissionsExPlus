package dev.rono.permissions.core.api;

import dev.rono.permissions.api.world.Worlds;

public final class ModernWorlds {

    private ModernWorlds() {}

    public static String toLegacy(String world) {
        return Worlds.normalize(world);
    }
}
