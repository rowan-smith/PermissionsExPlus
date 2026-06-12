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

Runtime: on **Spigot/Paper**, `PermissionService` is registered on Bukkit `ServicesManager`. On **Bungee/Waterfall**, use `dev.rono.permissions.bungee.ProxyPermissionServices.permissionService()`.

Optional Bukkit helpers: artifact `permissionsex-api-bukkit` (`BukkitPermissions.on(player).hasPermission("node")`).

Sample plugin: [`example-plugin/`](../../example-plugin/)

---

## Quick start

```java
import dev.rono.permissions.api.service.PermissionService;
import dev.rono.permissions.api.subject.User;
import dev.rono.permissions.bukkit.BukkitPermissions;
import org.bukkit.plugin.RegisteredServiceProvider;

RegisteredServiceProvider<PermissionService> reg =
        getServer().getServicesManager().getRegistration(PermissionService.class);
if (reg == null) {
    getLogger().warning("PermissionsEx not loaded");
    return;
}
PermissionService pex = reg.getProvider();

// Global permission (all worlds unless overridden per world)
if (pex.user(player.getUniqueId()).hasPermission("my.plugin.use")) {
    ...
}

// Player's current world (Bukkit helper)
if (BukkitPermissions.on(player).hasPermission("my.plugin.use")) {
    pex.world(player.getWorld().getName())
            .user(player.getUniqueId())
            .addTimedPermission("my.plugin.temp", 3600);
    pex.user(player.getUniqueId()).save();
}
```

---

## Flat API (canonical entry)

All operations are methods on `PermissionService`:

```java
// Global checks (Worlds.GLOBAL — applies to all worlds unless overridden)
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

### `PermissionService`

| Method | Returns | Role |
|--------|---------|------|
| `user(uuid\|name)` | `User` | Materialize subject (creates virtual if absent) |
| `findUser(uuid\|name)` | `FoundUser` | Optional persisted lookup |
| `group(name)` | `Group` | Materialize group |
| `findGroup(name)` | `FoundGroup` | Optional persisted lookup |
| `world(name)` / `global()` | `WorldScope` | World-bound subject chains |
| `findWorld(name)` | `Optional<WorldScope>` | When realm is registered |
| `users()` | `UsersScope` | User registry |
| `groups()` | `GroupsScope` | Group registry |
| `worlds()` | `WorldsScope` | Registered realms |
| `backend()` | `BackendScope` | Backend admin |
| `session()` | `SessionScope` | Batch edit sessions |
| `events()` | `PermissionEventBus` | Notifications |
| `isDebug()` | `boolean` | Debug flag |
| `reload()` / `reloadAsync()` | — | Reload backend |

### `WorldScope`

| Method | Description |
|--------|-------------|
| `user(uuid\|name)` | `UserWorldContext` in this world |
| `findUser(uuid\|name)` | `Optional<UserWorldContext>` |
| `group(name)` | `GroupWorldContext` in this world |
| `findGroup(name)` | `Optional<GroupWorldContext>` |
| `defaultGroups()` / `inheritance()` / `rankLadder(ladder)` | World config |

### `UsersScope` / `GroupsScope` / `WorldsScope`

| Scope | Method | Description |
|-------|--------|-------------|
| `UsersScope` | `count()` / `identifiers()` / `delete(id)` | User registry |
| `GroupsScope` | `count()` / `names()` / `delete(name)` | Group registry |
| `WorldsScope` | `count()` / `names()` | Registered realms |

### `FoundUser` / `FoundGroup`

| Method | Description |
|--------|-------------|
| `get()` | Persisted subject; throws if absent |
| `optional()` | `Optional<User>` / `Optional<Group>` |
| `inWorld(w)` / `global()` | Optional world context |

### `BackendScope`

| Method | Description |
|--------|-------------|
| `getActive()` / `type()` / `simpleName()` | Active backend |
| `isActive()` / `isActive(alias)` | Whether a backend is active |
| `activate(alias)` | Switch backend |
| `createHandle(alias)` | Non-active handle |
| `importFrom(alias)` | Copy from configured backend |
| `exportData()` / `importData(doc, mode)` | YAML import/export |

Runtime implementors use {@link dev.rono.permissions.api.service.PermissionServiceBridge}; plugins use `PermissionService` only.

---

## World model

| Modern constant | Meaning |
|-----------------|---------|
| `Worlds.GLOBAL` | `null` — global namespace |
| Empty string `""` | Normalized to global when passed to API methods |

`pex.user(id).hasPermission("node")` checks the **global** namespace — effective across all worlds unless a per-world override exists.

Helpers in `dev.rono.permissions.api.world.Worlds`: `normalize`, `isGlobal`, `mapKey`, `fromMapKey`.

---

## Events (`PermissionEventBus`)

Subscribe via `pex.events()`:

```java
var sub = pex.events().subscribe(new PermissionEventListener() {
    @Override
    public void onEntity(EntityDispatch dispatch) {
        // entityIdentifier, entityType, mutation
    }
});
pex.events().unsubscribe(sub);
```

Uses types from `permissionsex-core-api`: `EntityDispatch`, `SystemDispatch`, `EntityMutation`, `SystemMutation`. On Spigot, the platform still publishes legacy Bukkit events in parallel.

---

## Batch edits (`PermissionEditSession`)

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
import dev.rono.permissions.bukkit.BukkitPermissions;

if (BukkitPermissions.on(player).hasPermission("my.node")) { ... }
BukkitPermissions.on(player).context().inGroup("vip");
BukkitPermissions.on(player).hasPermissionGlobal("my.global.node");
```

---

## Proxy registration (Bungee/Waterfall)

```java
import dev.rono.permissions.bungee.ProxyPermissionServices;

PermissionService pex = ProxyPermissionServices.permissionService();
// or ProxyPermissionServices.get(PermissionService.class);
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
| `PermissionEditSession` | Batch edit helper |
| `WorldScope` / `UsersScope` / `GroupsScope` / `WorldsScope` / `BackendScope` / `SessionScope` | Flat API scopes |
| `FoundUser` / `FoundGroup` | Optional persisted lookups |
| `PlayerScope` | Bukkit `BukkitPermissions.on(player)` |
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
