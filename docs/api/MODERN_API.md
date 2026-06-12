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

Optional Bukkit helpers: artifact `permissionsex-api-bukkit` (`PexBukkitPermissions.on(player).hasPermission("node")`).

Sample plugin: [`plugin/permissionsex-example-plugin/`](../../plugin/permissionsex-example-plugin/)

---

## Entry point

```java
PermissionsExApi api = PermissionsEx.getApi();
PermissionManager manager = api.getPermissionManager();
```

| Method | Role |
|--------|------|
| `PermissionsEx.getApi()` | Modern `PermissionsExApi` (managers + holder-based permissions) |
| `PermissionsEx.getPermissionManager()` | **Deprecated** — `getApi().getPermissionManager()` |
| `PermissionsExApi.getPermissionManager()` | Classic `ru.tehkode.permissions.PermissionManager` |
| `PermissionsExApi.getPermissionService()` | Holder-based `dev.rono.permissions.api.permission.PermissionService` |

**Bungee/Waterfall:** `dev.rono.permissions.bungee.PermissionsEx.getApi()`

`PermissionsExApi` provides `getUserManager()`, `getGroupManager()`, `getWorldManager()`, `getLadderManager()`, and `getPermissionService()` with explicit `find` / `get` / `create` / `exists` lifecycle (no hidden creation in `getX()`).

---

## Quick start

```java
import dev.rono.permissions.api.PermissionsExApi;
import dev.rono.permissions.api.user.User;
import dev.rono.permissions.bukkit.PexBukkitPermissions;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.bukkit.PermissionsEx;
import org.bukkit.entity.Player;

if (!PermissionsEx.isAvailable()) {
    getLogger().warning("PermissionsEx not loaded");
    return;
}
PermissionsExApi api = PermissionsEx.getApi();
User user = api.getUserManager().getUser(player.getUniqueId());

// Holder-based permission check
if (api.getPermissionService().hasPermission(user.asHolder(), "my.plugin.use")) {
    ...
}

// Classic manager (deprecated methods on PermissionManager)
PermissionManager manager = api.getPermissionManager();
if (PexBukkitPermissions.on(player).hasPermission("my.plugin.use")) {
    ((dev.rono.permissions.api.service.PexPermissionService) manager)
            .world(player.getWorld().getName())
            .user(player.getUniqueId())
            .addTimedPermission("my.plugin.temp", 3600);
    manager.getUser(player).save();
}
```

---

## Flat API (canonical entry)

All operations are methods on `PexPermissionService`:

```java
// Global checks (PexWorlds.GLOBAL — applies to all worlds unless overridden)
pex.user(uuid).hasPermission("node");
pex.user("Steve").inGroup("vip", null, true);

// Per-world checks
pex.world(world).user(uuid).hasPermission("node");
pex.world(world).user(uuid).inGroup("vip", true);

// Optional persisted lookup
pex.findUser("Steve").get().hasPermission("node");
pex.findUser(uuid).optional().map(u -> u.hasPermission("node")).orElse(false);

// Registry
pex.users().count();
pex.groups().count();
pex.worlds().count();
pex.group("vip").members(null, true);

// Backend
pex.backend().getActive();
pex.backend().activate("file");
pex.backend().exportData();

// Maintenance
pex.reload();
pex.session().start();
pex.events();
```

### `PexPermissionService`

| Method | Returns | Role |
|--------|---------|------|
| `user(uuid\|name)` | `PexUser` | Materialize subject (creates virtual if absent) |
| `findUser(uuid\|name)` | `PexFoundUser` | Optional persisted lookup |
| `group(name)` | `PexGroup` | Materialize group |
| `findGroup(name)` | `PexFoundGroup` | Optional persisted lookup |
| `world(name)` / `global()` | `PexWorldScope` | World-bound subject chains |
| `findWorld(name)` | `Optional<PexWorldScope>` | When realm is registered |
| `users()` | `PexUsersScope` | PexUser registry |
| `groups()` | `PexGroupsScope` | PexGroup registry |
| `worlds()` | `PexWorldsScope` | Registered realms |
| `backend()` | `PexBackendScope` | Backend admin |
| `session()` | `PexSessionScope` | Batch edit sessions |
| `events()` | `PexPermissionEventBus` | Notifications |
| `isDebug()` | `boolean` | Debug flag |
| `reload()` / `reloadAsync()` | — | Reload backend |

### `PexWorldScope`

| Method | Description |
|--------|-------------|
| `user(uuid\|name)` | `PexUserWorldContext` in this world |
| `findUser(uuid\|name)` | `Optional<PexUserWorldContext>` |
| `group(name)` | `PexGroupWorldContext` in this world |
| `findGroup(name)` | `Optional<PexGroupWorldContext>` |
| `defaultGroups()` / `inheritance()` / `rankLadder(ladder)` | World config |

### `PexUsersScope` / `PexGroupsScope` / `PexWorldsScope`

| Scope | Method | Description |
|-------|--------|-------------|
| `PexUsersScope` | `count()` / `identifiers()` / `delete(id)` | PexUser registry |
| `PexGroupsScope` | `count()` / `names()` / `delete(name)` | PexGroup registry |
| `PexWorldsScope` | `count()` / `names()` | Registered realms |

### `PexFoundUser` / `PexFoundGroup`

| Method | Description |
|--------|-------------|
| `get()` | Persisted subject; throws if absent |
| `optional()` | `Optional<PexUser>` / `Optional<PexGroup>` |
| `inWorld(w)` / `global()` | Optional world context |

### `PexBackendScope`

| Method | Description |
|--------|-------------|
| `getActive()` / `type()` / `simpleName()` | Active backend |
| `isActive()` / `isActive(alias)` | Whether a backend is active |
| `activate(alias)` | Switch backend |
| `createHandle(alias)` | Non-active handle |
| `importFrom(alias)` | Copy from configured backend |
| `exportData()` / `importData(doc, mode)` | YAML import/export |

Runtime implementors use {@link dev.rono.permissions.api.service.PexPermissionServiceBridge}; plugins use `PexPermissionService` only.

---

## World model

| Modern constant | Meaning |
|-----------------|---------|
| `PexWorlds.GLOBAL` | `null` — global namespace |
| Empty string `""` | Normalized to global when passed to API methods |

`pex.user(id).hasPermission("node")` checks the **global** namespace — effective across all worlds unless a per-world override exists.

Helpers in `dev.rono.permissions.api.world.PexWorlds`: `normalize`, `isGlobal`, `mapKey`, `fromMapKey`.

---

## Events (`PexPermissionEventBus`)

Subscribe via `pex.events()`:

```java
var sub = pex.events().subscribe(new PexPermissionEventListener() {
    @Override
    public void onEntity(PexEntityDispatch dispatch) {
        // entityIdentifier, entityType, mutation
    }
});
pex.events().unsubscribe(sub);
```

Uses types from `permissionsex-core-api`: `PexEntityDispatch`, `PexSystemDispatch`, `PexEntityMutation`, `PexSystemMutation`. On Spigot, the platform still publishes legacy Bukkit events in parallel.

---

## Batch edits (`PexPermissionEditSession`)

```java
try (var session = pex.session().start()) {
    session.editUser("Steve", user -> user.addPermission("foo.bar", null));
    session.editGroup("vip", group -> group.setPrefix("&6", null));
    session.save();
}
```

---

## Bukkit helpers (`permissionsex-api-bukkit`)

```xml
<dependency>
  <groupId>dev.rono.permissions</groupId>
  <artifactId>permissionsex-api-bukkit</artifactId>
  <version>1.23.5</version>
  <scope>provided</scope>
</dependency>
```

```java
import dev.rono.permissions.bukkit.PexBukkitPermissions;

if (PexBukkitPermissions.on(player).hasPermission("my.node")) { ... }
PexBukkitPermissions.on(player).context().inGroup("vip");
PexBukkitPermissions.on(player).hasPermissionGlobal("my.global.node");
```

---

## Proxy registration (Bungee/Waterfall)

```java
import dev.rono.permissions.bungee.PermissionsEx;
import dev.rono.permissions.api.PermissionsExApi;

PermissionsExApi api = PermissionsEx.getApi();
```

Maven artifact: `permissionsex-api-bungee` (optional; includes `ProxyPermissionServices` for advanced use).

---

## `PexPermissionSubject`

Shared by `PexUser` and `PexGroup`.

### Metadata

| Method | Description |
|--------|-------------|
| `type()` | `PexSubjectType.USER` or `GROUP` |
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
| `configuredWorlds()` | PexWorlds with any subject data |

### Timed permissions

| Method | Description |
|--------|-------------|
| `addTimedPermission(permission, [world,] seconds)` | Temporary grant |
| `removeTimedPermission(permission[, world])` | Remove timed node |
| `timedPermissions([world])` | Active timed permission names |
| `timedPermissionEntries([world])` | `PexTimedPermissionEntry(permission, world, remainingSeconds)` |
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
| `inWorld(world)` | `PexSubjectWorldContext` — world-scoped facade |
| `global()` | Same as `inWorld(PexWorlds.GLOBAL)` |

### Persistence

| Method | Description |
|--------|-------------|
| `save()` | Persist to backend |
| `delete()` | Remove from backend and cache |

---

## `PexUser`

Extends `PexPermissionSubject`.

| Method | Description |
|--------|-------------|
| `uniqueId()` | `Optional<UUID>` when identifier is UUID-shaped |
| `groups([world,] inherit)` | PexGroup membership list |
| `inGroup(name[, world, inherit])` | Membership test |
| `addGroup(name[, world])` | Add to group |
| `addGroup(name, [world,] lifetimeSeconds)` | Timed membership |
| `removeGroup(name[, world])` | Remove from group |
| `timedGroupMemberships([world])` | `PexTimedGroupMembership(group, world, remainingSeconds)` |
| `allTimedGroupMemberships()` | Across all worlds |
| `groupMembershipRemainingSeconds(group[, world])` | Seconds until timed membership expires |
| `promote(ladder)` / `promote(promoter, ladder)` | Rank ladder promotion; throws `PexRankingException` |
| `demote(ladder)` / `demote(demoter, ladder)` | Rank ladder demotion |
| `isRanked(ladder)` / `rank(ladder)` | Rank metadata |
| `inWorld(world)` / `global()` | Returns `PexUserWorldContext` |

---

## `PexGroup`

Extends `PexPermissionSubject`.

| Method | Description |
|--------|-------------|
| `weight()` / `setWeight(int)` | Sort order |
| `isDefault([world])` / `setDefault(bool[, world])` | Default group flag |
| `parents([world])` | Direct parent groups |
| `parentTree([world])` | Expanded ancestor groups |
| `addParent` / `removeParent` / `setParents` | Inheritance CRUD |
| `isChildOf(name[, world, inherit])` | Hierarchy test |
| `rank()` / `rankLadder()` / `setRank(rank, ladder)` | Promotion ladder |
| `memberIdentifiers([world])` | PexUser ids with direct membership |
| `members([world,] inherit)` | `List<PexUser>` in this group (`inherit=true` includes descendant groups) |
| `children([world,] inherit)` | Direct or all descendant child groups |
| `descendants([world])` | All descendant groups (`children(world, true)`) |
| `activeMembers([inherit])` | Online members |
| `inWorld(world)` / `global()` | Returns `PexGroupWorldContext` |

---

## World context interfaces

Ergonomic world-scoped views (same operations without repeating `world` parameter):

| Type | Extends | Extra operations |
|------|---------|------------------|
| `PexSubjectWorldContext` | — | Permissions, timed perms, prefix/suffix, options |
| `PexUserWorldContext` | `PexSubjectWorldContext` | Groups, timed membership |
| `PexGroupWorldContext` | `PexSubjectWorldContext` | Parents, default, hierarchy, members, children, descendants |

Obtain via `subject.inWorld("world_nether")`, `user.global()`, or `pex.world("world_nether").user(uuid)`.

---

## Supporting types

| Type | Role |
|------|------|
| `PexBackendInfo` | Record: backend alias, implementation class name, label |
| `PexTimedPermissionEntry` | Record: permission, world, remainingSeconds |
| `PexTimedGroupMembership` | Record: groupName, world, remainingSeconds |
| `PexPermissionsExException` | Checked exception for reload/backend failures |
| `PexRankingException` | Promotion/demotion failures |
| `PexBackendHandle` | Non-active backend for copy/apply |
| `PexImportMode` | `MERGE` / `REPLACE` for `importData` |
| `PexPermissionEventBus` / `PexPermissionEventListener` | Modern event subscription |
| `PexPermissionEditSession` | Batch edit helper |
| `PexWorldScope` / `PexUsersScope` / `PexGroupsScope` / `PexWorldsScope` / `PexBackendScope` / `PexSessionScope` | Flat API scopes |
| `PexFoundUser` / `PexFoundGroup` | Optional persisted lookups |
| `PexPlayerScope` | Bukkit `PexBukkitPermissions.on(player)` |
| `PexSubjectType` | `USER`, `GROUP` |

---

## Advanced SPI (`permissionsex-core-api`)

Not required for typical hook plugins. Used by platform modules and deep integration:

| Type | Role |
|------|------|
| `PlatformAdapter` | Host bridge (UUID/name, realms, event publish) |
| `PexPermissionDispatch` | `PexEntityDispatch` / `PexSystemDispatch` notifications |
| `PexEntityMutation` / `PexSystemMutation` | Change kinds |
| `SchedulerBridge` | Sync/async scheduling |
| `ContextResolver` | `realmFor(UUID)` |

On Spigot, internal dispatches are translated to legacy Bukkit events. See [LEGACY_API.md — Events](LEGACY_API.md#events).

---

## Planned additions

See [FUTURE.md](FUTURE.md) for remaining gaps (diagnostics, config snapshot, matcher access, cache control).
