---
title: Requirements
description: Java, Minecraft, and platform requirements for PermissionsExPlus.
slug: /requirements
---

## Server software

| Platform | Supported |
|----------|-----------|
| Spigot / Paper | Yes |
| BungeeCord / Waterfall | Yes |
| Velocity | Yes |
| Sponge | Yes |

Install the universal **`PermissionsExPlus-%%site.version%%.jar`** on each server process (backend and proxy).

## Java

**Java 21 or newer** is required to run PermissionsExPlus %%site.version%%.

Check your version:

```text
java -version
```

Most modern Paper/Spigot hosts ship with Java 21+. If your host runs an older JVM, upgrade before installing PEX.

## Minecraft versions

Target range: **1.8.8 through 1.26.1**.

PEX is tested against current Spigot/Paper APIs. Always verify on your exact server version before going live.

## Disk space

Minimal. Default file storage uses two small YAML files in `plugins/PermissionsEx/`:

- `config.yml` — settings
- `permissions.yml` — groups and users

SQL backends need a MySQL/MariaDB database (optional, for large networks).

## Permissions to run PEX commands

Players need `permissions.*` (or `*`) to use admin commands. The simplest approach:

```text
/pex group admin add permissions.*
```

## One jar per server

Each server process must have **only one** PEX jar. Remove old copies before restarting:

- `PermissionsEx.jar`, `PermissionsExPlus-spigot-*.jar`
- `permissionsex-spigot-*.jar`, `permissionsex-bungee-*.jar`

Keep a single `PermissionsExPlus-%%site.version%%.jar`.
