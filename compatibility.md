---
layout: page
title: Compatibility
permalink: /compatibility/
---

PermissionsExPlus targets **Minecraft 1.8.8 through 1.26.1** on Spigot/Paper and compatible forks.

## JVM requirements

| Server era | Typical Minecraft | Minimum JVM for this build |
|------------|-------------------|----------------------------|
| Legacy | 1.8.8 – 1.16.x | **Java 21** (current artifact bytecode) |
| Modern | 1.17+ | **Java 21** |

The plugin JAR is compiled with **Java 21**. Hosts must run a **Java 21+** runtime even when the Minecraft version is older.

**Caveat:** "1.8.8 – 1.26.1" means the plugin loads and is intended to work across that range on a Java 21+ host. Hosts still on Java 8 need a separate legacy bytecode build (not yet provided). Real-world soak testing on your target versions is recommended before production.

## Platform support

| Platform | Status | Notes |
|----------|--------|-------|
| Spigot / Paper | Supported | Primary target; superperms bridge on game servers |
| BungeeCord / Waterfall | Supported | Proxy permission bridge; no Bukkit events |
| Velocity | Supported | Permission provider via `PermissionsSetupEvent` |
| Sponge | Supported | Shared proxy bootstrap path |

## Bukkit loader

`plugin.yml` does **not** set `api-version`, so Bukkit attempts to load the plugin on pre-1.13 servers. Unsupported combinations log a warning from `ServerVersions` and continue with reflection-based shims where possible.

## Proxy behavior

On Bungee/Waterfall/Velocity:

- `PermissionsExApi` is available via proxy-specific entry points
- Legacy Bukkit events (`ru.tehkode.permissions.events.*`) are **not** fired on proxies
- Config, backends, and API registration share the proxy bootstrap path

## Legacy hook plugins

Pre-1.23.5 PEX hook JARs should run **without recompiling** if they only used the classic public API (`ru.tehkode.permissions.*`).

Compile against:

- `permissionsex-legacy-api` — types, events, utils
- `permissionsex-legacy-stub` — `PermissionsEx` static entry points (optional)

API reference: [Legacy API]({{ site.baseurl }}/legacy-api/). New plugins: [Modern API]({{ site.baseurl }}/modern-api/).

## Verification

| Level | Command |
|-------|---------|
| Unit tests | `mvn test` |
| Hook smoke test | `permissionsex-legacy-compat` module (MockBukkit) |
| Manual matrix | See repo `docs/testing/REAL_SERVER_MATRIX.md` |

## Current status ({{ site.version }})

| Area | State |
|------|--------|
| Build | `mvn test` passes on the full reactor |
| Spigot/Paper | Compiles against 1.21.x API |
| Bungee | Compiles and tests against BungeeCord API |
| Legacy hook plugins | `ru.tehkode.*` contract restored to baseline `628215f` |
| Release | **{{ site.version }}** |

MockBukkit full-server tests skip automatically when the test Paper API does not match the compile-time Spigot API. Unit and backend tests still run.
