package dev.rono.permissions.compat.commands.util;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Sends command output through Adventure, as PermissionsEx 1.23.6 does: legacy
 * {@code &} and {@code §} colour codes and MiniMessage tags both work, and
 * PlaceholderAPI placeholders are expanded for players when it is installed.
 * Adventure is bundled (relocated) in this plugin, so this works on every
 * server version; until {@link #init} succeeds, messages are sent as legacy
 * text.
 */
public final class PEXAdventure {
    private static BukkitAudiences audiences;

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();
    private static final LegacyComponentSerializer LEGACY_SECTION = LegacyComponentSerializer.legacySection();
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private PEXAdventure() {
        throw new RuntimeException();
    }

    public static void init(Plugin plugin) {
        audiences = BukkitAudiences.create(plugin);
    }

    public static void shutdown() {
        if (audiences != null) {
            audiences.close();
            audiences = null;
        }
    }

    public static boolean isAvailable() {
        return audiences != null;
    }

    private static boolean isPlaceholderApiAvailable() {
        return Bukkit.getServer() != null && Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
    }

    public static void sendMessage(CommandSender sender, String message) {
        // Only touch PlaceholderAPI's classes when it is installed.
        if (sender instanceof Player && isPlaceholderApiAvailable()) {
            message = PlaceholderAPI.setPlaceholders((Player) sender, message);
        }

        String normalized = message.replace("§", "&");
        Component legacyParsed = LEGACY.deserialize(normalized);

        if (audiences == null) {
            sender.sendMessage(LEGACY_SECTION.serialize(legacyParsed));
            return;
        }

        String miniMessage = MINI_MESSAGE.serialize(legacyParsed);

        String hybridMessage = miniMessage.replace("\\<", "<");

        Component component = MINI_MESSAGE.deserialize(hybridMessage);
        audiences.sender(sender).sendMessage(component);
    }
}
