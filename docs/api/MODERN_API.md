# Modern API reference

Package root: `dev.rono.permissions.api`  
Maven artifact: `permissionsex-api`

```xml
<dependency>
  <groupId>dev.rono.permissions</groupId>
  <artifactId>permissionsex-api</artifactId>
  <version>1.23.5</version>
  <scope>provided</scope>
</dependency>
```

Runtime: on **Spigot/Paper**, resolve the API via **`PermissionsEx.getApi()`**. On **Bungee/Waterfall**, use **`dev.rono.permissions.bungee.PermissionsEx.getApi()`**.

Add **`permissionsex-legacy-stub`** when calling `ru.tehkode.permissions.bukkit.PermissionsEx` static helpers from a hook plugin.

Sample plugin: [`plugin/permissionsex-example-plugin/`](../../plugin/permissionsex-example-plugin/)

---

## Entry point

```java
var api = PermissionsEx.getApi();
var manager = api.getPermissionManager();
```

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
        Map.of("world", player.getWorld().getName()))) {
    ...
}

// Per-world subject operations
user.inWorld(player.getWorld().getName()).addPermission("my.plugin.temp");
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

Subject operations (`hasPermission`, groups, meta, timed grants) are on `User` / `Group` returned by managers.

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

`user.hasPermission("node")` checks the **global** namespace. Use `user.inWorld(world).hasPermission("node")` or `hasPermission(holder, node, Map.of("world", world))` for per-world checks.

Helpers in `dev.rono.permissions.api.world.Worlds`: `normalize`, `isGlobal`, `mapKey`, `fromMapKey`.

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
| `hasPermission(permission)` | Effective check in global namespace |
| `has(permission[, world])` | Effective check (alias; world defaults to global) |
| `permissions([world])` | **Direct** assignments (not inherited) |
| `effectivePermissions([world])` | Merged after inheritance |
| `addPermission` / `removePermission` / `setPermissions` | Direct CRUD |
| `permissionsByWorld()` | Map of world → direct permissions |
| `effectivePermissionsByWorld()` | Map of world → effective permissions |
| `configuredWorlds()` | Worlds with any subject data |

### Timed permissions

| Method | Description |
|--------|-------------|
| `addTimedPermission(permission, [world,] seconds)` | Temporary grant |
| `removeTimedPermission(permission[, world])` | Remove timed node |
| `timedPermissions([world])` | Active timed permission names |
| `timedPermissionEntries([world])` | `TimedPermissionEntry(permission, world, remainingSeconds)` |
| `allTimedPermissionEntries()` | Across all configured worlds |
| `timedPermissionRemainingSeconds(permission[, world])` | Seconds until expiry; `0` if absent |
| `hasTimedPermission(permission[, world])` | Whether timed node is active |

### Meta (prefix / suffix / options)

| Method | Description |
|--------|-------------|
| `prefix` / `suffix` / `setPrefix` / `setSuffix` | Chat meta per world |
| `option(key[, world])` / `setOption` / `options` | Arbitrary key/value meta |

### World views

| Method | Description |
|--------|-------------|
| `inWorld(world)` | `SubjectWorldContext` — world-scoped facade |
| `global()` | Same as `inWorld(Worlds.GLOBAL)` |

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
| `groups([world,] inherit)` | Group membership list |
| `inGroup(name[, world, inherit])` | Membership test |
| `addGroup(name[, world])` | Add to group |
| `addGroup(name, [world,] lifetimeSeconds)` | Timed membership |
| `removeGroup(name[, world])` | Remove from group |
| `timedGroupMemberships([world])` | `TimedGroupMembership(group, world, remainingSeconds)` |
| `allTimedGroupMemberships()` | Across all worlds |
| `groupMembershipRemainingSeconds(group[, world])` | Seconds until timed membership expires |
| `promote(ladder)` / `promote(promoter, ladder)` | Rank ladder promotion; throws `RankingException` |
| `demote(ladder)` / `demote(demoter, ladder)` | Rank ladder demotion |
| `isRanked(ladder)` / `rank(ladder)` | Rank metadata |
| `inWorld(world)` / `global()` | Returns `UserWorldContext` |

---

## `Group`

Extends `PermissionSubject`.

| Method | Description |
|--------|-------------|
| `weight()` / `setWeight(int)` | Sort order |
| `isDefault([world])` / `setDefault(bool[, world])` | Default group flag |
| `parents([world])` | Direct parent groups |
| `parentTree([world])` | Expanded ancestor groups |
| `addParent` / `removeParent` / `setParents` | Inheritance CRUD |
| `isChildOf(name[, world, inherit])` | Hierarchy test |
| `rank()` / `rankLadder()` / `setRank(rank, ladder)` | Promotion ladder |
| `memberIdentifiers([world])` | User ids with direct membership |
| `members([world,] inherit)` | `List<User>` in this group (`inherit=true` includes descendant groups) |
| `children([world,] inherit)` | Direct or all descendant child groups |
| `descendants([world])` | All descendant groups (`children(world, true)`) |
| `activeMembers([inherit])` | Online members |
| `inWorld(world)` / `global()` | Returns `GroupWorldContext` |

---

## World context interfaces

Ergonomic world-scoped views (same operations without repeating `world` parameter):

| Type | Extends | Extra operations |
|------|---------|------------------|
| `SubjectWorldContext` | — | Permissions, timed perms, prefix/suffix, options |
| `UserWorldContext` | `SubjectWorldContext` | Groups, timed membership |
| `GroupWorldContext` | `SubjectWorldContext` | Parents, default, hierarchy, members, children, descendants |

Obtain via `subject.inWorld("world_nether")`, `user.global()`, or `pex.world("world_nether").user(uuid)`.

---

## Supporting types

| Type | Role |
|------|------|
| `BackendInfo` | Record: backend alias, implementation class name, label |
| `TimedPermissionEntry` | Record: permission, world, remainingSeconds |
| `TimedGroupMembership` | Record: groupName, world, remainingSeconds |
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
| `SchedulerBridge` | Sync/async scheduling |
| `ContextResolver` | `realmFor(UUID)` |

On Spigot, internal dispatches are translated to legacy Bukkit events. See [LEGACY_API.md — Events](LEGACY_API.md#events).

---

## Planned additions

See [FUTURE.md](FUTURE.md) for remaining gaps (diagnostics, config snapshot, matcher access, cache control).
