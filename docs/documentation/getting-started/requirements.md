---
sidebar_position: 2
---

# Requirements

## Supported versions

PermissionsExPlus supports the **last version of every finished Minecraft release line**,
and **every version of the current one**. Each release jar is named after its platform and
the first and last Minecraft version it supports:

| Platform | Supported Minecraft versions | Download |
|---|---|---|
| CraftBukkit / Crucible | 1.7.10 | `PermissionsExPlus-Bukkit-1.7.10.jar` |
| Spigot, Paper | 1.8.8, 1.9.4, 1.10.2, 1.11.2, 1.12.2, 1.13.2, 1.14.4, 1.15.2, 1.16.5, 1.17.1, 1.18.2, 1.19.4, 1.20.6 | `PermissionsExPlus-Spigot-1.8.8-1.20.6.jar` |
| Spigot | 1.21.11, 26.1, 26.2, 26.3 | `PermissionsExPlus-Spigot-1.21.11-26.3.jar` |
| Paper | 1.21.11, 26.1, 26.2, 26.3 | `PermissionsExPlus-Paper-1.21.11-26.3.jar` |
| SpongeVanilla | 1.12.2 | `PermissionsExPlus-Sponge-1.12.2.jar` |
| SpongeVanilla | 1.16.5 | `PermissionsExPlus-Sponge-1.16.5.jar` |
| SpongeVanilla | 1.17.1, 1.18.2, 1.19.4 | `PermissionsExPlus-Sponge-1.17.1-1.19.4.jar` |
| SpongeVanilla | 1.20.6, 1.21.11, 26.1, 26.2, 26.3 | `PermissionsExPlus-Sponge-1.20.6-26.3.jar` |
| Velocity | Current releases | `PermissionsExPlus-Velocity.jar` |
| BungeeCord | Current releases | `PermissionsExPlus-BungeeCord.jar` |

On Paper 1.8.8 to 1.20.6, use the Spigot jar. Sponge never released SpongeVanilla for 1.13
or 1.14, and its only 1.15.2 build does not start, so those versions are Bukkit-only.
SpongeVanilla's 1.8.9 - 1.11.2 builds (Sponge API 4 - 6, betas only) are not supported.

Only the versions listed are tested, on real servers, for every change. Versions inside a
jar's range that are not listed (for example 1.20.4 or 1.21.4) may work, but are not
supported: update to the version listed.

When the next release line (27.x) starts, every 27.x version is supported, 26.3 stays as the
last version of 26.x, and 26.1 and 26.2 are dropped.

## Java

Use the Java version your Minecraft version needs: Java 8 up to 1.16.5, Java 16 or 17 for
1.17.1, Java 17 for 1.18.2 and 1.19.4, Java 21 for 1.20.6 and 1.21.11, and Java 25 for 26.x.

## Other requirements

- **Memory**: Minimum 512MB RAM dedicated to the server.
- **Optional**:
  - Redis (for cross-server synchronization)
  - MySQL, PostgreSQL, or MariaDB (for external database storage)
