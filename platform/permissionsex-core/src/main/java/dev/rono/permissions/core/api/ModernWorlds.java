package dev.rono.permissions.core.api;

import dev.rono.permissions.api.world.PexWorlds;

final class ModernWorlds {
    private ModernWorlds() {}

    static String toLegacy(String world) {
        return PexWorlds.normalize(world);
    }
}
