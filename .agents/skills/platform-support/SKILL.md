---
name: platform-support
description: Use when adding or changing Spigot, Paper, Bukkit, Sponge, Velocity or BungeeCord integrations and supported versions.
---

# Platform Support

1. Identify the exact module, Minecraft/server versions and Java runtime target.
2. Check the module POM and platform API before adding calls or dependencies.
3. Keep platform-specific lifecycle, threading and command wiring in its adapter.
4. Keep permission logic in Core and public contracts in API.
5. Check shaded dependencies, class loading and plugin enable/disable behaviour.
6. Test the oldest affected version and a recent supported version on real servers.

Do not assume Spigot and Paper expose identical APIs or internals.
