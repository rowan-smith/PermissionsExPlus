---
title: Architecture
description: Module stack, dependency direction, and design rules.
slug: /developers/architecture
---
## Module stack

Grouped by concern (physical directories; matches root `pom.xml` reactor order):

```
legacy-api/
  permissionsex-legacy-api    Classic ru.tehkode.permissions types + utils + Bukkit events
  permissionsex-legacy-stub   Compile-only PermissionsEx static entry points
  permissionsex-legacy-compat Regression tests (MockBukkit + optional classic plugin JARs)

api/
  permissionsex-core-api      Engine ↔ API SPI (bus dispatches, permission holder types)
  permissionsex-api           PermissionsExApi + managers for modern hook plugins
  permissionsex-api-bungee    PermissionsEx.getApi() + proxy service registry

common/
  permissionsex-platform-api  Runtime bridge (PlatformAdapter, scheduler, logging, identity)
  permissionsex-core          Engine (manager, backends, commands, hierarchy) — THE ONLY permission logic

platform/
  permissionsex-spigot        Bukkit/Paper runtime (live today)
  permissionsex-bungee        Bungee/Waterfall proxy runtime (live today)
  permissionsex-paper         Paper-specific runtime enhancements
  permissionsex-velocity      Velocity proxy runtime
  permissionsex-sponge        Sponge runtime

bootstrap/
  permissionsex-bootstrap     Universal shaded jar (all platform descriptors)
```

Dependency direction: **platform** → **common** → **legacy-api** / **api** → **core-api**; **bootstrap** merges platform jars; **plugin** modules depend on **legacy-api** (+ **legacy-stub**) or **api** only.

## Design rules

| Rule | Meaning |
|------|---------|
| **Single engine** | Only `permissionsex-core` contains permission logic (evaluation, hierarchy, timed expiry, backends, caching). |
| **Platform thinness** | Platform modules are translation layers: adapters, lifecycle, event bridging, service registration — never permission logic. |
| **API separation** | `api/` = plugin-facing contracts; `core-api/` = engine-facing SPI; `platform-api/` = runtime abstraction between engine and host. |
| **Legacy freeze** | `ru.tehkode.permissions.*` is frozen — compatibility fixes only. |
| **Context rule** | `String world` = canonical subject scope; `PermissionContext` = extended metadata; platforms never interpret permissions. |

## Namespace policy

| Package | Role |
|---------|------|
| `ru.tehkode.permissions.*` | Stable legacy API for hook plugins (interfaces, events, backend aliases) |
| `dev.rono.permissions.core.*` | Implementation (manager, backends, commands) |
| `dev.rono.permissions.api.*` | Modern minimal integration SPI |
| `dev.rono.permissions.api.runtime.*` | Platform bridge types (`permissionsex-platform-api`) |
| `dev.rono.permissions.spigot.*` / `bungee.*` / `paper.*` / … | Platform-specific wiring |

New implementation code belongs under `dev.rono`. Public contracts consumed by third-party plugins remain under `ru.tehkode` as thin types delegating to core where needed.

**Hook plugin API documentation:** [Hook Plugin API](/developers/api) (modern + legacy reference and roadmap). Architectural invariants: [API Invariants](/developers/api/invariants).

## Runtime architecture flow

### Modern API flow

```
Plugin → PermissionsEx.getApi() → PermissionsExApi → UserManager / GroupManager
  → permissionsex-core (DefaultPermissionManager, GroupHierarchyEngine, TimedExpiryCoordinator)
```

### Platform interaction flow

```
permissionsex-core → PlatformRuntime
  ├── PlatformAdapter      (identity / realms)
  ├── PlatformEventBus     (native listener publication)
  └── PlatformScheduler    (host thread scheduling)
        ↓
permissionsex-spigot / bungee / velocity / sponge → Server API
```

### Legacy flow

```
Legacy plugin → PermissionsEx.getPermissionManager() → PermissionManager (adapter)
  → same permissionsex-core engine
```

## Permission resolution

1. `DefaultPermissionManager.getUser()` resolves UUID-first, then name / offline records.
2. `HierarchyTraverser` walks entity parents depth-first (world → world inheritance → global).
3. `AbstractPermissionEntity.getPermissionsInternal()` merges own + timed permissions from hierarchy.
4. `getMatchingExpression()` applies first regex match in list order.
5. `RegExpMatcher` compiles patterns with a bounded Guava cache.

### Spigot runtime

Two paths coexist:

- **Direct API**: `PermissionManager.has()` / `PermissionUser.has()`
- **Superperms bridge**: `SuperpermsListener` materializes PEX data into synthetic Bukkit permissions; `PermissiblePEX` regex-matches on `hasPermission()` with a per-player cache

Bus dispatches (`EntityDispatch`, `SystemDispatch`) are translated to legacy `ru.tehkode.permissions.events.*` Bukkit events on game servers via `SpigotEventPublisher`.

### Bungee runtime

`BungeePexPermissionBridge` handles `PermissionCheckEvent` using direct expression matching. No Bukkit events are published on proxy. Config, backends, and API registration are shared with Velocity/Sponge via `ProxyPlatformInitializer`.

### Velocity runtime

`PermissionsSetupEvent` supplies a permission provider; `ProxyPlatformInitializer` registers `PermissionsExApi` through `ProxyPermissionServices` (same static registry as Bungee).

### Sponge runtime

Engine wiring reuses `ProxyPlatformInitializer` for config/backends/API registry. Platform adapter maps worlds as permission realms.

## Backends

| Alias | Implementation | Registered in |
|-------|----------------|---------------|
| `file` | `YamlFileBackend` (core), wrapped as `ru.tehkode...FileBackend` / `BungeeFileBackend` | Spigot / Bungee plugin constructors |
| `memory` | Platform memory backends | Spigot / Bungee |
| `sql` | `SQLBackend` | `CorePermissionBackendRegistrar` |
| `multi` | `MultiBackend` | `CorePermissionBackendRegistrar` |

YAML file I/O is unified in `dev.rono.permissions.core.backends.file` (SnakeYAML). Platform wrappers only supply classpath-stable legacy type names.

## Performance

- **Group membership index**: `GroupMembershipIndex` backs `getUsers(group)` without scanning every user.
- **SQL name caches**: entity identifiers and display names cached with `AtomicReference<ImmutableSet>`.
- **User entity caches**: per-user permission / option / prefix caches in `DefaultPermissionUser`.
- **Selective superperms updates**: permission/metadata changes clear `PermissiblePEX` regex cache without full permissible rebuild when injected.

## Commands

Cloud Command Framework (shared annotations in core) with platform command managers:

- Spigot: `StrippingBukkitCommandManager` + Brigadier when available
- Bungee: `StrippingBungeeCommandManager`

Business logic lives in `CoreCommandService`.

## Reload contract

`/pex reload` → `DefaultPermissionManager.reset()` clears in-memory users/groups, marks group index dirty, reloads backend, fires `RELOADED` system dispatch / Bukkit event on game servers.

## Legacy isolation policy

Classic hook-plugin compatibility is confined to **`permissionsex-legacy-api`** and thin platform façades. New work belongs under **`dev.rono.*`**.

| Layer | Package / module | Role |
|-------|------------------|------|
| **Compile contract** | `permissionsex-legacy-api` → `ru.tehkode.permissions.*` | Frozen public types for third-party plugins (`PermissionManager`, events, `NativeInterface`, config interfaces, `ru.tehkode.utils.*`). |
| **Compile stub** | `permissionsex-legacy-stub` → `ru.tehkode.permissions.bukkit.PermissionsEx` | Static entry points for hook plugins only; **not** on the Spigot module compile classpath. |
| **Runtime plugin entry** | `permissionsex-spigot` → `ru.tehkode.permissions.bukkit.PermissionsEx` | Live `JavaPlugin` subclass registered in `plugin.yml`. |
| **Legacy bridges** | `dev.rono.permissions.core.legacy.*` | Adapters from modern config/runtime to classic types (e.g. `LegacyPermissionsExConfigAdapter`). Not part of the hook-plugin compile surface. |
| **Modern internals** | `dev.rono.permissions.core.InternalPermissionManager` | Runtime hooks removed from the legacy `PermissionManager` interface (`PlatformAdapter`, bus publish, scheduling, `getBasedir`, …). |
| **Bukkit events** | Published only from `permissionsex-spigot` | `SpigotEventPublisher` translates `dev.rono.permissions.api.bus.*` dispatches into `ru.tehkode.permissions.events.*`. Core does not depend on event publication. |
| **Backend aliases** | `ru.tehkode.permissions.spigot.backends.*` (Spigot), `dev.rono.permissions.bungee.backends.*` (Bungee) | Classpath-stable names delegating to `dev.rono.permissions.core.backends.*`. |

**Baseline:** legacy `PermissionManager` / events / `NativeInterface` match commit **`628215f`** (plus `shouldSaveDefaultGroup`). Guarded by `LegacyApiContractTest` in `permissionsex-legacy-api`.

**Rule of thumb:** if a feature is new, add it to `dev.rono.permissions.api` or `InternalPermissionManager` — never expand `ru.tehkode.permissions.PermissionManager`.
