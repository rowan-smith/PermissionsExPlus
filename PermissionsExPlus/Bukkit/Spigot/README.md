# PermissionsExPlus Spigot adapter

This module adapts PermissionsExPlus Core to Spigot/Bukkit. `SpigotPlugin` starts Core, registers the player listener, exposes the API provider, and stops Core during plugin disable.

On join, a player is loaded from durable storage asynchronously and then placed in the Core read cache. On quit, the player is evicted from that cache. The adapter uses Bukkit's scheduler for Core async writes, expiry checks, and main-thread callbacks.

Server configuration is created in the plugin directory as `config.yml` and `database.yml`; local storage is placed in `data/`.
