---
name: platform-support
description: Use when adding or changing Spigot, Paper, Bukkit, Sponge, Velocity or BungeeCord integrations and supported versions, or when a new Minecraft version or release line comes out.
---

# Platform Support

1. Identify the exact module, Minecraft/server versions and Java runtime target.
2. Check the module POM and platform API before adding calls or dependencies.
3. Keep platform-specific lifecycle, threading and command wiring in its adapter.
4. Keep permission logic in Core and public contracts in API.
5. Check shaded dependencies, class loading and plugin enable/disable behaviour.
6. Test the oldest affected version and a recent supported version on real servers.

Do not assume Spigot and Paper expose identical APIs or internals.

## Supported versions

Support exactly these Minecraft versions, and nothing between them:

- **One version per finished release line: its last.** For 1.x that is 1.7.10, 1.8.8,
  1.9.4, 1.10.2, 1.11.2, 1.12.2, 1.13.2, 1.14.4, 1.15.2, 1.16.5, 1.17.1, 1.18.2, 1.19.4,
  1.20.6 and 1.21.11. No other 1.x versions (not 1.21.4, not 1.20.4).
- **Every version of the current release line**, currently 26.x: 26.1 (its latest patch,
  26.1.2), 26.2 and 26.3.
- **When a new line starts** (27.x): support every 27.x version, keep only the last version
  of the previous line (26.3), and drop the rest of it (26.1, 26.2).

The same rule applies on every platform:

- **Bukkit, Spigot, Paper:** CraftBukkit/Crucible for 1.7.10; Spigot and Paper for the rest.
- **Sponge:** one SpongeVanilla build per supported Minecraft version that Sponge released,
  using its newest build even when that is a release candidate, pre-release or snapshot.
  Today: 1.12.2 (API 7), 1.16.5 (API 8), 1.18.2 (API 9), 1.19.4 (API 10), 1.20.6 (API 11),
  the newest 1.21 build (1.21.11, API 18), and 26.1.2, 26.2 and 26.3.

A supported version is one with a real-server smoke test in `.github/workflows/build.yml`.
Versions outside the list are neither tested nor promised. When the list changes, update
these together:

1. the smoke matrix in `.github/workflows/build.yml`;
2. the `minecraft.versions` property of each affected module's POM, which names its release
   jar `PermissionsExPlus-<Platform>-<first>-<last>.jar` (for example
   `PermissionsExPlus-Spigot-1.21.11-26.3.jar`; a single version has no range);
3. the requirements page in `docs/documentation/getting-started/requirements.md`;
4. this list.
