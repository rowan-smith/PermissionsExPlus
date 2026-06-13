---
title: Modern API Reference
description: dev.rono.permissions.api reference for new companion plugins.
slug: /developers/api/modern
---
Package root: `dev.rono.permissions.api`  
Maven artifact: `permissionsex-api`

```xml
<dependency>
  <groupId>dev.rono.permissions</groupId>
  <artifactId>permissionsex-api</artifactId>
  <version>%%site.version%%</version>
  <scope>provided</scope>
</dependency>
```

Runtime: on **Spigot/Paper**, resolve the API via **`PermissionsEx.getApi()`**. On **Bungee/Waterfall**, use **`dev.rono.permissions.bungee.PermissionsEx.getApi()`**.

Add **`permissionsex-legacy-stub`** when calling `ru.tehkode.permissions.bukkit.PermissionsEx` static helpers from a hook plugin.

Sample plugin: [`plugin/permissionsex-example-plugin/`](https://github.com/%%site.repo%%/tree/main/plugin/permissionsex-example-plugin/)

---

## Documentation

Every public type under `dev.rono.permissions.api.*` is documented with Javadoc. Generate HTML reference from the repository root:

```bash
# Recommended — unified docs for core-api + permissionsex-api
mvn -pl api javadoc:aggregate -am

# Single-module docs (builds dependencies via -am)
mvn -pl api/permissionsex-api javadoc:javadoc -am
```

Output: `api/target/reports/apidocs/` (aggregate) or `api/permissionsex-api/target/reports/apidocs/` (single module).

Package overviews live in `package-info.java` files. Architectural rules: [API_INVARIANTS.md](/developers/api/invariants).

---

## Entry point

```java
var api = PermissionsEx.getApi();  // primary API — see API_INVARIANTS.md
var manager = api.getPermissionManager();  // legacy bridge + holder operations
```

**Primary API:** `PermissionsExApi` via `PermissionsEx.getApi()`. Subject operations use managers; `PermissionManager` is the legacy/holder bridge. See [API_INVARIANTS.md](/developers/api/invariants).

| Method | Role |
|--------|------|
| `PermissionsEx.getApi()` | Modern `PermissionsExApi` (managers + holder-based permissions) |
| `PermissionsEx.getPermissionManager()` | **Deprecated** — `getApi().getPermissionManager()` |
| `PermissionsExApi.getPermissionManager()` | Classic + holder-based operations on `ru.tehkode.permissions.PermissionManager` |

**Bungee/Waterfall:** `dev.rono.permissions.bungee.PermissionsEx.getApi()`

`PermissionsExApi` provides `getUserManager()`, `getGroupManager()`, `getWorldManager()`, and `getLadderManager()` with explicit `find` / `get` / `create` / `exists` lifecycle (no hidden creation in `getX()`). Each manager also exposes `count()` (total stored/registry entries) and `count(Predicate)` (filtered count). Holder-based permission edits use `getPermissionManager().addPermission(holder, …)` / `hasPermission(holder, …)`.

---

## Quick start

```java
import dev.rono.permissions.api.permission.PermissionContext;
import ru.tehkode.permissions.bukkit.PermissionsEx;

if (!PermissionsEx.isAvailable()) {
    getLogger().warning("PermissionsEx not loaded");
    return;
}
var api = PermissionsEx.getApi();
var user = api.getUserManager().getUser(player.getUniqueId());
var manager = api.getPermissionManager();
if (manager.hasPermission(
        user.asHolder(),
        "my.plugin.use",
        PermissionContext.of(
                player.getWorld().getName(),
                getServer().getName(),
                "spawn",
                player.getGameMode().name()))) {
    ...
}

// Per-realm subject operations
user.inContext(PermissionContext.world(player.getWorld().getName())).addPermission("my.plugin.temp");
user.save();
```

---

## Managers

| Manager | `find` | `get` | `create` | `count()` |
|---------|--------|-------|----------|-----------|
| `UserManager` | no materialize | requires persisted | explicit create | backend users |
| `GroupManager` | no materialize | requires persisted | explicit create | backend groups |
| `WorldManager` | registered realms | requires exists | explicit create | worlds |
| `LadderManager` | known ladders | requires exists | explicit create | ladders |

`LadderManager` also exposes `promote(user, ladder)`, `demote(user, ladder)`, `isRanked(user, ladder)`, and `rank(user, ladder)`.

Subject operations (`has`, groups, meta, timed grants) are on `User` / `Group` returned by managers.

Holder-based checks and edits use `api.getPermissionManager()` (`hasPermission(holder, node, context)`, `addPermission`, etc.).

### Events

```java
var sub = api.getEventBus().subscribe(dispatch -> { ... });
api.getEventBus().unsubscribe(sub);
```

---

## World model

| Constant | Meaning |
|----------|---------|
| `Worlds.GLOBAL` | `null` — global namespace |
| Empty string `""` | Normalized to global |

`user.has("node")` checks the **global** namespace. Use `user.inContext(PermissionContext.world(name)).has("node")` or `hasPermission(holder, node, context)` for scoped checks.

### Permission context (platform-neutral)

Structured scope for holder checks and the modern subject API uses `PermissionContext` (`dev.rono.permissions.api.permission.PermissionContext`):

| Key | Role |
|-----|------|
| `world` | Loaded world / realm (game servers) |
| `server` | Backend or logical server id (proxies, Sponge) |
| `dimension` | Sponge dimension (optional) |
| `region`, `gamemode`, `state` | Optional plugin metadata |

```java
import dev.rono.permissions.api.permission.PermissionContext;

var context = PermissionContext.of("survival", "lobby-1", "spawn", "creative");
manager.hasPermission(holder, "my.node", context);

user.inContext(PermissionContext.server("lobby")).addPermission("proxy.admin");
```

Each platform supplies a `ContextResolver` via `PlatformAdapter#getContextResolver()` for inheritance ordering (for example Bukkit: `world → server → global`; Velocity: `server → global`; Sponge: `dimension → world → server → global`).

All scoped operations on `User` / `Group` take `PermissionContext`. Parameterless overloads use `PermissionContext.global()`.

---

## Events (`PermissionEventBus`)

Subscribe via `api.getEventBus()`:

```java
var sub = api.getEventBus().subscribe(new PermissionEventListener() {
    @Override
    public void onEntity(EntityDispatch dispatch) {
        // entityIdentifier, entityType, mutation
    }
});
api.getEventBus().unsubscribe(sub);
```

Uses types from `permissionsex-core-api`: `EntityDispatch`, `SystemDispatch`, `EntityMutation`, `SystemMutation`. On Spigot, the platform still publishes legacy Bukkit events in parallel.

---

## Proxy registration (Bungee/Waterfall)

```java
import dev.rono.permissions.bungee.PermissionsEx;
import dev.rono.permissions.api.PermissionsExApi;

PermissionsExApi api = PermissionsEx.getApi();
```

Maven artifact: `permissionsex-api-bungee` (optional; includes `ProxyPermissionServices` for advanced use).

### Server context on proxies

Backend server names are permission namespaces (stored as `world` keys in the backend). Commands use `pex server` / `pex servers` instead of `pex world` / `pex worlds`. In proxy plugins, scope with `PermissionContext.server(id)`:

```java
user.inContext(PermissionContext.server("lobby")).addPermission("bungee.command.server");
if (user.inContext(PermissionContext.server("lobby")).has("proxy.admin")) { ... }
```

---

## `PermissionSubject`

Shared by `User` and `Group`.

### Metadata

| Method | Description |
|--------|-------------|
| `type()` | `SubjectType.USER` or `GROUP` |
| `identifier()` / `name()` | Backend id and display name |
| `virtual()` | In-memory-only, not yet persisted |

### Permissions

| Method | Description |
|--------|-------------|
| `has(permission[, context])` | Effective check (context defaults to global) |
| `permissions([context])` | **Direct** assignments (not inherited) |
| `effectivePermissions([context])` | Merged after inheritance |
| `addPermission` / `removePermission` / `setPermissions` | Direct CRUD |
| `permissionsByRealm()` | Map of realm → direct permissions |
| `effectivePermissionsByRealm()` | Map of realm → effective permissions |
| `configuredRealms()` | Realms with any subject data |

### Timed permissions

| Method | Description |
|--------|-------------|
| `addTimedPermission(permission, [context,] seconds)` | Temporary grant |
| `removeTimedPermission(permission[, context])` | Remove timed node |
| `timedPermissions([context])` | Active timed permission names |
| `timedPermissionEntries([context])` | `TimedPermissionEntry(permission, context, remainingSeconds)` |
| `allTimedPermissionEntries()` | Across all configured realms |
| `timedPermissionRemainingSeconds(permission[, context])` | Seconds until expiry; `0` if absent |
| `hasTimedPermission(permission[, context])` | Whether timed node is active |

### Meta (prefix / suffix / options)

| Method | Description |
|--------|-------------|
| `prefix` / `suffix` / `setPrefix` / `setSuffix` | Chat meta per context |
| `option(key[, context])` / `setOption` / `options` | Arbitrary key/value meta |

### Context views

| Method | Description |
|--------|-------------|
| `inContext(context)` | `SubjectContext` — context-scoped facade |
| `global()` | Same as `inContext(PermissionContext.global())` |

Player permission checks on the proxy auto-scope to the connected backend via `PlatformAdapter.onlineRealm()`.

### Persistence

| Method | Description |
|--------|-------------|
| `save()` | Persist to backend |
| `delete()` | Remove from backend and cache |

---

## `User`

Extends `PermissionSubject`.

| Method | Description |
|--------|-------------|
| `uniqueId()` | `Optional<UUID>` when identifier is UUID-shaped |
| `groups([context,] inherit)` | Group membership list |
| `inGroup(name[, context, inherit])` | Membership test |
| `addGroup(name[, context])` | Add to group |
| `addGroup(name, [context,] lifetimeSeconds)` | Timed membership |
| `removeGroup(name[, context])` | Remove from group |
| `timedGroupMemberships([context])` | `TimedGroupMembership(group, context, remainingSeconds)` |
| `allTimedGroupMemberships()` | Across all realms |
| `groupMembershipRemainingSeconds(group[, context])` | Seconds until timed membership expires |
| `inContext(context)` / `global()` | Returns `UserContext` |

Rank-ladder promotion/demotion is on `LadderManager` (`promote(user, ladder)`, `demote(user, ladder)`, `rank(user, ladder)`).

---

## `Group`

Extends `PermissionSubject`.

| Method | Description |
|--------|-------------|
| `weight()` / `setWeight(int)` | Sort order |
| `isDefault([context])` / `setDefault(bool[, context])` | Default group flag |
| `parents([context])` | Direct parent groups |
| `parentTree([context])` | Expanded ancestor groups |
| `addParent` / `removeParent` / `setParents` | Inheritance CRUD |
| `isChildOf(name[, context, inherit])` | Hierarchy test |
| `rank()` / `rankLadder()` / `setRank(rank, ladder)` | Promotion ladder |
| `memberIdentifiers([context])` | User ids with direct membership |
| `members([context,] inherit)` | `List<User>` in this group (`inherit=true` includes descendant groups) |
| `children([context,] inherit)` | Direct or all descendant child groups |
| `descendants([context])` | All descendant groups (`children(context, true)`) |
| `activeMembers([inherit])` | Online members |
| `inContext(context)` / `global()` | Returns `GroupContext` |

---

## Context interfaces

Ergonomic context-scoped views (same operations without repeating the `PermissionContext` parameter):

| Type | Extends | Extra operations |
|------|---------|------------------|
| `SubjectContext` | — | Permissions, timed perms, prefix/suffix, options |
| `UserContext` | `SubjectContext` | Groups, timed membership |
| `GroupContext` | `SubjectContext` | Parents, default, hierarchy, members, children, descendants |

Obtain via `subject.inContext(PermissionContext.world("world_nether"))`, `user.global()`, or `pex.world("world_nether").user(uuid)`.

---

## Supporting types

| Type | Role |
|------|------|
| `BackendInfo` | Record: backend alias, implementation class name, label |
| `TimedPermissionEntry` | Record: permission, context, remainingSeconds |
| `TimedGroupMembership` | Record: groupName, context, remainingSeconds |
| `PermissionContext` | Standard context map keys and builders |
| `PermissionsExException` | Checked exception for reload/backend failures |
| `RankingException` | Promotion/demotion failures |
| `BackendHandle` | Non-active backend for copy/apply |
| `ImportMode` | `MERGE` / `REPLACE` for `importData` |
| `PermissionEventBus` / `PermissionEventListener` | Modern event subscription |
| `PermissionEventBus` / `PermissionEventListener` | Modern event subscription via `PermissionsExApi.getEventBus()` |
| `FoundUser` / `FoundGroup` | Optional persisted lookups |
| `SubjectType` | `USER`, `GROUP` |

---

## Advanced SPI (`permissionsex-core-api`)

Not required for typical hook plugins. Used by platform modules and deep integration:

| Type | Role |
|------|------|
| `PlatformAdapter` | Host bridge (UUID/name, realms, event publish) |
| `PermissionDispatch` | `EntityDispatch` / `SystemDispatch` notifications |
| `EntityMutation` / `SystemMutation` | Change kinds |
| `PlatformScheduler` | Sync/async scheduling |
| `ContextResolver` | `realmFor(UUID)` |

On Spigot, internal dispatches are translated to legacy Bukkit events. See [Legacy API — Events](/developers/api/legacy#events).

---

## Planned additions

See [FUTURE.md](/developers/api/future) for remaining gaps (diagnostics, config snapshot, matcher access, cache control).
