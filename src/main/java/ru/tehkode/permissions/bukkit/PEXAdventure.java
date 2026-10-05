/*
 * PermissionsEx - Permissions plugin for Bukkit
 * Copyright (C) 2011 t3hk0d3 http://www.tehkode.ru
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package ru.tehkode.permissions.bukkit;

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
 * Compatibility layer for Kyori Adventure text components.
 * Provides utilities for sending colored messages using Adventure API.
 */
public final class PEXAdventure {
	private static BukkitAudiences audiences;

	private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();
	private static final LegacyComponentSerializer LEGACY_SECTION = LegacyComponentSerializer.legacySection();
	private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

	private PEXAdventure() {
		throw new RuntimeException();
	}

	/**
	 * Initialize Adventure audiences for the plugin.
	 *
	 * @param plugin The plugin instance
	 */
	public static void init(Plugin plugin) {
		audiences = BukkitAudiences.create(plugin);
	}

	/**
	 * Shutdown Adventure audiences.
	 */
	public static void shutdown() {
		if (audiences != null) {
			audiences.close();
		}
	}

	/**
	 * Check if Adventure is available.
	 *
	 * @return true if Adventure is available, false otherwise
	 */
	public static boolean isAvailable() {
		return audiences != null;
	}

	/**
	 * Check if PlaceholderAPI plugin is available.
	 *
	 * @return true if PlaceholderAPI is loaded
	 */
	private static boolean isPlaceholderApiAvailable() {
		return Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
	}

	public static void sendMessage(CommandSender sender, String message) {
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
