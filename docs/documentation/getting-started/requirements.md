---
sidebar_position: 2
---

# Requirements

## Supported versions

PermissionsExPlus supports the **last version of every finished Minecraft release line**,
and **every version of the current one**:

| Platform | Supported Minecraft versions | Download |
|---|---|---|
| CraftBukkit / Crucible | 1.7.10 | `PermissionsExPlus-Bukkit17` |
| Spigot, Paper | 1.8.8, 1.9.4, 1.10.2, 1.11.2, 1.12.2, 1.13.2, 1.14.4, 1.15.2, 1.16.5, 1.17.1, 1.18.2, 1.19.4, 1.20.6 | `PermissionsExPlus-SpigotLegacy` |
| Spigot | 1.21.11, 26.1, 26.2, 26.3 | `PermissionsExPlus-Spigot` |
| Paper | 1.21.11, 26.1, 26.2, 26.3 | `PermissionsExPlus-Paper` |
| SpongeVanilla 7 | 1.12.2 | `PermissionsExPlus-Sponge7` |
| SpongeVanilla 8 | 1.16.5 | `PermissionsExPlus-Sponge8` |
| SpongeVanilla 9 and 10 | 1.18.2, 1.19.4 | `PermissionsExPlus-Sponge9` |
| SpongeVanilla 11 to 21 | 1.20.6, 1.21.11, 26.1, 26.2, 26.3 | `PermissionsExPlus-Sponge11` |
| Velocity, BungeeCord | Current releases | `PermissionsExPlus-Velocity`, `PermissionsExPlus-BungeeCord` |

Only these versions are tested, on real servers, for every change. Other versions of the
same release line (for example 1.20.4 or 1.21.4) may work, but are not supported: update to
the version listed.

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
