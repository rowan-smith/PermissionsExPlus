---
title: Universal Bootstrap Jar
description: Merged jar routing, install, and build instructions.
slug: /developers/bootstrap
---
Maven module **`permissionsex-bootstrap`** (`dev.rono.permissions:permissionsex-bootstrap`) emits a single installable jar:

**`bootstrap/target/PermissionsExPlus-{version}.jar`**

Maven still uses a normal module `artifactId` for the reactor; only the **on-disk jar name** is simplified as above (`build.finalName`).

## How routing works

Each platform loader reads **only its own descriptor** from the merged jar. No Java “router” plugin is required.

| Platform | Descriptor | Main class |
|----------|------------|------------|
| **Spigot / CraftBukkit** | `plugin.yml` | `ru.tehkode.permissions.bukkit.PermissionsEx` |
| **Paper** | `paper-plugin.yml` | `dev.rono.permissions.paper.PaperPermissionsExPlugin` |
| **BungeeCord / Waterfall** | `bungee.yml` | `dev.rono.permissions.bungee.BungeePermissionsExPlugin` |
| **Velocity** | `velocity-plugin.json` | `dev.rono.permissions.velocity.VelocityPermissionsExPlugin` |
| **Sponge** | `META-INF/sponge_plugins.json` | `dev.rono.permissions.sponge.SpongePermissionsExPlugin` |

Descriptors are unpacked from the shaded platform artifacts into this jar **without overwriting each other**.

The **`ru.tehkode.permissions.bukkit.PermissionsEx`** type on the classpath is still the **static façade** (entry helpers) packaged in **`permissionsex-legacy-api`**; only the **`JavaPlugin` main class name** moved to **`PermissionsExPlugin`** on Spigot to avoid duplicate type definitions across modules. Paper uses its own entry class that extends the Spigot plugin.

## Install

Copy **`PermissionsExPlus-{version}.jar`** into **`plugins/`** on backend servers, proxies, or Sponge `mods/` / `plugins/` (per your Sponge loader layout).

**Each server process must contain only one PermissionsExPlus build.** Remove platform-only shaded jars before restart, including:

| Remove (examples) |
|---|
| `permissionsex-spigot-*.jar` |
| `permissionsex-bungee-*.jar` |
| `permissionsex-paper-*.jar` |
| `permissionsex-velocity-*.jar` |
| `permissionsex-sponge-*.jar` |
| Legacy: `PermissionsExPlus-spigot-*.jar`, `PermissionsExPlus-bungee-*.jar`, `PermissionsExPlus-bootstrap-*-universal.jar`, `ru.tehkode:permissionsex-*` |

Keep a **single** `PermissionsExPlus-*.jar` per server process.

## Build

From repo root:

```bash
mvn -pl bootstrap -am package
```

All five platform modules must succeed first so their jars exist for the merge step.
