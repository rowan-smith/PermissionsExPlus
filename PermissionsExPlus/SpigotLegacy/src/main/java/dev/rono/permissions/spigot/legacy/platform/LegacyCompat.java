package dev.rono.permissions.spigot.legacy.platform;

import java.lang.reflect.Method;
import org.bukkit.entity.Player;

/**
 * Bukkit API that only exists on part of the 1.8.8 - 1.17 range, looked up once
 * at runtime so this build loads on every version it supports.
 */
public final class LegacyCompat {
    /** {@code Player#updateCommands()}, added in 1.13; absent on 1.8 - 1.12. */
    private static final Method UPDATE_COMMANDS = find(Player.class, "updateCommands");

    private LegacyCompat() {
        throw new AssertionError();
    }

    /**
     * Resends the client's command tree so command visibility and tab completion
     * follow permission changes. Clients before 1.13 have no command tree, so
     * there is nothing to resend on those servers.
     */
    public static void updateCommands(Player player) {
        if (UPDATE_COMMANDS == null) {
            return;
        }

        try {
            UPDATE_COMMANDS.invoke(player);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            // Best effort: a stale client command tree only affects tab completion.
        }
    }

    private static Method find(Class<?> type, String name) {
        try {
            return type.getMethod(name);
        } catch (NoSuchMethodException | SecurityException absent) {
            return null;
        }
    }
}
