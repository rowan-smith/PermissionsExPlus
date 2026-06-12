package dev.rono.permissions.core.api;

import dev.rono.permissions.api.world.Worlds;

final class ModernWorlds {
    private ModernWorlds() {}

    static String toLegacy(String world) {
        return Worlds.normalize(world);
    }
}
