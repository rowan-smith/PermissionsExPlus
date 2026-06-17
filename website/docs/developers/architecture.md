---
title: Architecture
description: Module stack, dependency direction, and design rules.
slug: /developers/architecture
---
## Module stack

Flat layout at the repository root (matches root `pom.xml` reactor order; same shape as LuckPerms, ViaVersion, and Maintenance):

```
api-core/           Engine ↔ API SPI (bus dispatches, permission holder types)
legacy-api/         Classic ru.tehkode.permissions types + utils + Bukkit events
api/                PermissionsExApi + managers for modern hook plugins
legacy-stub/        Compile-only PermissionsEx static entry points
platform-api/       Runtime bridge (PlatformAdapter, scheduler, logging, identity)
common/             Engine (manager, backends, commands, hierarchy) — THE ONLY permission logic
proxy-common/       Shared proxy bootstrap (config, backends, API registry)
bukkit/             Bukkit/Paper runtime (live today)
bungee/             Bungee/Waterfall proxy runtime (live today)
velocity/           Velocity proxy runtime
sponge/             Sponge runtime
universal/          Universal shaded jar (all platform descriptors)
example-plugin/     Modern hook plugin sample
example-legacy-plugin/ Legacy hook plugin sample
legacy-compat/      Regression tests (MockBukkit + optional classic plugin JARs)
```

Dependency direction: **bukkit** / **bungee** / **velocity** / **sponge** → **common** → **legacy-api** / **api** → **api-core**; **universal** merges platform jars; example modules depend on **legacy-api** (+ **legacy-stub**) or **api** only.

## Design rules

| Rule | Meaning |
|------|---------|
| **Single engine** | Only `common` (`permissionsex-core`) contains permission logic (evaluation, hierarchy, timed expiry, backends, caching). |
| **Platform thinness** | Platform modules are translation layers: adapters, lifecycle, event bridging, service registration — never permission logic. |
| **API separation** | `api/` = plugin-facing contracts; `api-core` = engine-facing SPI; `platform-api` = runtime abstraction between engine and host. |
| **Legacy freeze** | `ru.tehkode.permissions.*` is frozen — compatibility fixes only. |
| **Context rule** | `String world` = canonical subject scope; `PermissionContext` = extended metadata; platforms never interpret permissions. |

## Namespace policy

| Package | Role |
|---------|------|
| `ru.tehkode.permissions.*` | Stable legacy API for hook plugins (interfaces, events, backend aliases) |
| `dev.rono.permissions.core.*` | Implementation (manager, backends, commands) |
| `dev.rono.permissions.api.*` | Modern minimal integration SPI |
| `dev.rono.permissions.api.runtime.*` | Platform bridge types (`platform-api`) |
| `dev.rono.permissions.spigot.*` / `bungee.*` / `paper.*` / … | Platform-specific wiring |

New implementation code belongs under `dev.rono`. Public contracts consumed by third-party plugins remain under `ru.tehkode` as thin types delegating to core where needed.

**Hook plugin API documentation:** [Hook Plugin API](/developers/api) (modern + legacy reference and roadmap). Architectural invariants: [API Invariants](/developers/api/invariants).

## Runtime architecture flow

### Modern API flow

```
Plugin → PermissionsEx.getApi() → PermissionsExApi → UserManager / GroupManager
  → common (DefaultPermissionManager, GroupHierarchyEngine, TimedExpiryCoordinator)
```

### Platform interaction flow

```
common → PlatformRuntime
  ├── PlatformAdapter      (identity / realms)
  ├── PlatformEventBus     (native listener publication)
  └── PlatformScheduler    (host thread scheduling)
        ↓
bukkit / bungee / velocity / sponge → Server API
```

### Legacy flow

```
Legacy plugin → PermissionsEx.getPermissionManager() → PermissionManager (adapter)
  → same common engine
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
| `h2` (default) | `LocalSqlBackend` — H2 file store + one-time YAML migration | `CorePermissionBackendRegistrar` |
| `file` | `YamlFileBackend` — YAML import via `/pex import file` | `CorePermissionBackendRegistrar` |
| `memory` | Platform memory backends | Spigot / Bungee plugin constructors |
| `sql` | `SQLBackend` | `CorePermissionBackendRegistrar` |
| `multi` | `MultiBackend` | `CorePermissionBackendRegistrar` |

Default data path: `{basedir}/{database}.mv.db` (typically `plugins/PermissionsEx/permissions.mv.db`). On first startup, `YamlToSqlMigrator` imports `migration-source` (default `permissions.yml`) and renames it to `permissions.yml.migrated`.

YAML file I/O for import lives in `dev.rono.permissions.core.backends.file` (SnakeYAML). Platform wrappers (`FileBackend`, `BungeeFileBackend`) supply classpath-stable legacy type names where still needed for tests and import.

## Performance

- **Group membership index**: `GroupMembershipIndex` backs `getUsers(group)` without scanning every user.
- **SQL name caches**: entity identifiers and display names cached with `AtomicReference<ImmutableSet>`.
- **User entity caches**: per-user permission / option / prefix caches in `DefaultPermissionUser`.
- **Selective superperms updates**: permission/metadata changes clear `PermissiblePEX` regex cache without full permissible rebuild when injected.

## Commands

Cloud Command Framework (shared annotations in core) with platform command managers:

- Spigot: `StrippingBukkitCommandManager` + Brigadier when available
- Bungee: `StrippingBungeeCommandManager`

Two command trees are registered based on `permissions.command-framework`:

- **Modern** (default): structured subcommands in `dev.rono.permissions.core.commands.cloud.modern`
- **Classic**: legacy syntax in `dev.rono.permissions.core.commands.cloud.classic`

Business logic lives in `CoreCommandService`.

## Reload contract

`/pex reload` → `DefaultPermissionManager.reset()` clears in-memory users/groups, marks group index dirty, reloads backend, fires `RELOADED` system dispatch / Bukkit event on game servers.

## Legacy isolation policy

Classic hook-plugin compatibility is confined to **`legacy-api`** and thin platform façades. New work belongs under **`dev.rono.*`**.

| Layer | Package / module | Role |
|-------|------------------|------|
| **Compile contract** | `legacy-api` → `ru.tehkode.permissions.*` | Frozen public types for third-party plugins (`PermissionManager`, events, `NativeInterface`, config interfaces, `ru.tehkode.utils.*`). |
| **Compile stub** | `legacy-stub` → `ru.tehkode.permissions.bukkit.PermissionsEx` | Static entry points for hook plugins only; **not** on the Bukkit module compile classpath. |
| **Runtime plugin entry** | `bukkit` → `ru.tehkode.permissions.bukkit.PermissionsEx` | Live `JavaPlugin` subclass registered in `plugin.yml`. |
| **Legacy bridges** | `dev.rono.permissions.core.legacy.*` | Adapters from modern config/runtime to classic types (e.g. `LegacyPermissionsExConfigAdapter`). Not part of the hook-plugin compile surface. |
| **Modern internals** | `dev.rono.permissions.core.InternalPermissionManager` | Runtime hooks removed from the legacy `PermissionManager` interface (`PlatformAdapter`, bus publish, scheduling, `getBasedir`, …). |
| **Bukkit events** | Published only from `bukkit` | `SpigotEventPublisher` translates `dev.rono.permissions.api.bus.*` dispatches into `ru.tehkode.permissions.events.*`. Core does not depend on event publication. |
| **Backend aliases** | `ru.tehkode.permissions.spigot.backends.*` (Bukkit), `dev.rono.permissions.bungee.backends.*` (Bungee) | Classpath-stable names delegating to `dev.rono.permissions.core.backends.*`. |

**Baseline:** legacy `PermissionManager` / events / `NativeInterface` match commit **`628215f`** (plus `shouldSaveDefaultGroup`). Guarded by `LegacyApiContractTest` in `legacy-api`.

**Rule of thumb:** if a feature is new, add it to `dev.rono.permissions.api` or `InternalPermissionManager` — never expand `ru.tehkode.permissions.PermissionManager`.
