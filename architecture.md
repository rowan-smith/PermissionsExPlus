---
layout: page
title: Architecture
permalink: /architecture/
---

PermissionsExPlus is organized as a Maven multi-module reactor. A single engine (`permissionsex-core`) owns all permission logic; platform modules are thin translation layers.

## Module stack

```
legacy-api/
  permissionsex-legacy-api    Classic ru.tehkode.permissions types + utils + Bukkit events
  permissionsex-legacy-stub   Compile-only PermissionsEx static entry points
  permissionsex-legacy-compat Regression tests (MockBukkit + optional classic plugin JARs)

api/
  permissionsex-core-api      Engine ↔ API SPI (bus dispatches, permission holder types)
  permissionsex-api           PermissionsExApi + managers for modern hook plugins

common/
  permissionsex-platform-api  Runtime bridge (PlatformAdapter, scheduler, logging, identity)
  permissionsex-core          Engine (manager, backends, commands, hierarchy)

platform/
  permissionsex-spigot        Bukkit/Paper runtime
  permissionsex-bungee        Bungee/Waterfall proxy runtime
  permissionsex-paper         Paper-specific enhancements
  permissionsex-velocity      Velocity proxy runtime
  permissionsex-sponge        Sponge runtime

bootstrap/
  permissionsex-bootstrap     Universal shaded jar (all platform descriptors)

plugin/
  permissionsex-example-plugin          Modern API sample
  permissionsex-example-legacy-plugin   Legacy API sample
```

Dependency direction: **platform → common → legacy-api / api → core-api**; **bootstrap** merges platform jars.

## Design rules

| Rule | Meaning |
|------|---------|
| **Single engine** | Only `permissionsex-core` contains permission logic |
| **Platform thinness** | Platform modules translate APIs — never evaluate permissions |
| **API separation** | `api/` = plugin contracts; `core-api/` = engine SPI; `platform-api/` = host bridge |
| **Legacy freeze** | `ru.tehkode.permissions.*` is frozen — compatibility fixes only |
| **Context rule** | `String world` = canonical scope; `PermissionContext` = extended metadata |

## Namespace policy

| Package | Role | Hook plugins? |
|---------|------|---------------|
| `ru.tehkode.permissions.*` | Classic API (frozen) | Yes — via `permissionsex-legacy-api` |
| `ru.tehkode.permissions.bukkit.PermissionsEx` | Static entry points | Yes — via `permissionsex-legacy-stub` |
| `dev.rono.permissions.api.*` | Modern integration SPI | Yes — via `permissionsex-api` |
| `dev.rono.permissions.core.*` | Implementation internals | No |

## Runtime flows

### Modern API

```
Plugin → PermissionsEx.getApi() → PermissionsExApi → UserManager / GroupManager
  → permissionsex-core (DefaultPermissionManager, GroupHierarchyEngine, TimedExpiryCoordinator)
```

### Legacy API

```
Legacy plugin → PermissionsEx.getPermissionManager() → PermissionManager (adapter)
  → same permissionsex-core engine
```

### Platform bridge

```
permissionsex-core → PlatformRuntime
  ├── PlatformAdapter      (identity / realms)
  ├── PlatformEventBus     (native listener publication)
  └── PlatformScheduler    (host thread scheduling)
        ↓
permissionsex-spigot / bungee / velocity / sponge → Server API
```

## Permission resolution

1. `DefaultPermissionManager.getUser()` resolves UUID-first, then name/offline records
2. `HierarchyTraverser` walks entity parents depth-first (world → world inheritance → global)
3. `AbstractPermissionEntity.getPermissionsInternal()` merges own + timed permissions
4. `getMatchingExpression()` applies first regex match in list order
5. `RegExpMatcher` compiles patterns with a bounded Guava cache

### Spigot superperms bridge

Two paths coexist on game servers:

- **Direct API**: `PermissionManager.has()` / `PermissionUser.has()`
- **Superperms bridge**: `SuperpermsListener` materializes PEX data into synthetic Bukkit permissions

Bus dispatches are translated to legacy `ru.tehkode.permissions.events.*` Bukkit events via `SpigotEventPublisher`.

## Backends

| Alias | Implementation |
|-------|----------------|
| `file` | `YamlFileBackend` |
| `memory` | Platform memory backends |
| `sql` | `SQLBackend` |
| `multi` | `MultiBackend` |

## Commands

Cloud Command Framework with platform command managers. Business logic lives in `CoreCommandService`.

## Reload contract

`/pex reload` → `DefaultPermissionManager.reset()` clears in-memory state, reloads backend, fires `RELOADED` system dispatch.

## Related

- [Modern API]({{ site.baseurl }}/modern-api/)
- [Legacy API]({{ site.baseurl }}/legacy-api/)
- [API Invariants]({{ site.baseurl }}/api-invariants/)
- [Integrations]({{ site.baseurl }}/integrations/)
