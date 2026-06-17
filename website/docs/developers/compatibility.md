---
title: Platform Compatibility
description: Minecraft, JVM, proxy, and legacy hook plugin compatibility for PermissionsExPlus %%site.version%%.
slug: /developers/compatibility
---
PermissionsExPlus **%%site.version%%** (PermissionsExPlus 3.0) targets **Minecraft `1.8.8` through `1.26.1`** on Spigot/Paper and compatible forks.

Upgrading from **PermissionsEx 1.23.4**? See [Migrating from PermissionsEx 1.23.4](/faq/migrate-from-v1). PermissionsExPlus 3.0 is a new major line with full backwards compatibility for data, commands, and hook plugins.

## PermissionsEx 1.23.4 (upstream baseline)

Verified against git tag **`STABLE-1.23.4`** (upstream release `1.23.4`, January 2016):

| Area | PermissionsEx 1.23.4 |
|------|----------------------|
| Platforms | **Bukkit / Spigot / Paper only** — single-module plugin (`plugin.yml` → `ru.tehkode.permissions.bukkit.PermissionsEx`) |
| Proxy | **Not supported** — no BungeeCord, Waterfall, Velocity, or Sponge artifacts in upstream |
| Minecraft | Built against Bukkit **`1.8.8-R0.1-SNAPSHOT`**; typical deployments were **1.8.x era** servers |
| Java | Compiled for **Java 7** bytecode |
| Storage backends | `file` (YAML), `memory`, `sql` — no embedded H2 `h2` default |
| Commands | Classic `/pex` tree only |
| Hook API | `ru.tehkode.permissions.*` only |

Proxy and multi-platform support are **new in PermissionsExPlus 3.0**.

## JVM requirements

| Server era | Typical Minecraft | Minimum JVM for **this build** |
|------------|-------------------|--------------------------------|
| Legacy     | 1.8.8 – 1.16.x    | **Java 21** (current artifact bytecode) |
| Modern     | 1.17+             | **Java 21** |

The plugin JAR is compiled with **Java 21**. Hosts must run a **Java 21+** runtime even when the Minecraft version is older. This matches current Paper/Spigot toolchain expectations for maintained forks.

## Bukkit loader

`plugin.yml` does **not** set `api-version`, so Bukkit attempts to load the plugin on pre-1.13 servers. Unsupported combinations log a warning from `ServerVersions` and continue with reflection-based shims where possible.

## Proxy

BungeeCord / Waterfall module targets the current Bungee API (`1.21-R0.3-SNAPSHOT` in the parent POM).

## Legacy hook plugins

Compile against:

- `permissionsex-legacy-api` — types, events, utils
- `permissionsex-legacy-stub` — `PermissionsEx` static entry points only

API reference: [api/LEGACY_API.md](/developers/api/legacy). New plugins: [api/MODERN_API.md](/developers/api/modern).

Runtime uses the live `ru.tehkode.permissions.bukkit.PermissionsEx` class from the deployed plugin jar.

## Verification

- Unit tests: `mvn test`
- Hook smoke test: `permissionsex-legacy-compat` module (MockBukkit + example hook plugin)
- Manual matrix: [REAL_SERVER_MATRIX.md](/developers/testing-matrix)
