# PermissionsExPlus platform modules

This Maven parent contains the platform-neutral `Core` module and the plugin for each platform, grouped by the API it is built on:

- `Bukkit/` — `Common` (shared by every Bukkit-based plugin), `Bukkit17`, `SpigotLegacy`, `Spigot` and `Paper`;
- `Sponge/` — `Sponge7`, `Sponge8`, `Sponge9` and `Sponge11`;
- `Proxy/` — `Velocity` and `BungeeCord`.

Build a platform module to obtain a server-loadable plugin JAR.

See the repository [README](../README.md) for server setup and configuration.
