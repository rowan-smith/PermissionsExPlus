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

Optional Bukkit helpers: artifact `permissionsex-api-bukkit` (`BukkitPermissions.on(player).has("node")`).

Sample plugin: [`example-plugin/`](../../example-plugin/)

---

## Quick start

```java
import dev.rono.permissions.api.service.PermissionService;
import dev.rono.permissions.api.subject.User;
import org.bukkit.plugin.RegisteredServiceProvider;

RegisteredServiceProvider<PermissionService> reg =
        getServer().getServicesManager().getRegistration(PermissionService.class);
if (reg == null) {
    getLogger().warning("PermissionsEx not loaded");
    return;
}
PermissionService pex = reg.getProvider();

// Bukkit (no service param on checks)
if (BukkitPermissions.on(player).has("my.plugin.use")) {
    pex.query().world(player.getWorld().getName())
            .user(player.getUniqueId())
            .addTimedPermission("my.plugin.temp", 3600);
    pex.user(player.getUniqueId()).save();
}

// Or via query only
if (pex.query().world(player.getWorld().getName()).user(player.getUniqueId()).has("my.plugin.use")) {
    ...
}
```

---

## Query API (canonical entry)

Everything flows from {@code pex.query()}:

```java
// Checks (world-first)
pex.query().world(world).user(uuid).inGroup("vip", true);
pex.query().world(world).findUser(uuid).map(u -> u.has("node")).orElse(false);

// Registry
pex.query().users().count();
pex.query().groups().count();
pex.query().groups().resolve("vip").inWorld(world).members(true);

// Backend
pex.query().backend().info();
pex.query().backend().activate("file");
pex.query().backend().exportData();

// Maintenance
pex.query().reload();
pex.query().editSession();
pex.query().events();
```

### `PermissionQuery`

| Method | Returns | Role |
|--------|---------|------|
| `world(name)` / `global()` | `WorldScope` | World-bound subject chains |
| `findWorld(name)` | `Optional<WorldScope>` | When realm is registered |
| `users()` | `UsersScope` | User registry |
| `groups()` | `GroupsScope` | Group registry |
| `backend()` | `BackendScope` | Backend admin |
| `events()` | `PermissionEventBus` | Notifications |
| `worlds()` | `Collection<String>` | Known realms |
| `isDebug()` | `boolean` | Debug flag |
| `reload()` / `reloadAsync()` | — | Reload backend |
| `editSession()` | `PermissionEditSession` | Batch edits |

### `WorldScope`

| Method | Description |
|--------|-------------|
| `user(uuid\|name)` | `UserWorldContext` in this world |
| `findUser(uuid\|name)` | `Optional<UserWorldContext>` |
| `group(name)` | `GroupWorldContext` in this world |
| `findGroup(name)` | `Optional<GroupWorldContext>` |
| `defaultGroups()` / `inheritance()` / `rankLadder(ladder)` | World config |

### `UsersScope` / `GroupsScope`

| Method | Description |
|--------|-------------|
| `count()` | Registered subjects in backend |
| `identifiers()` / `names()` | All ids / group names |
| `resolve(...)` | Materialize subject, then `.inWorld(w)` |
| `find(...)` | Optional subject, then `.inWorld(w)` |
| `delete(...)` | Remove from backend |

### `BackendScope`

| Method | Description |
|--------|-------------|
| `info()` / `type()` / `simpleName()` | Active backend |
| `activate(alias)` | Switch backend |
| `createHandle(alias)` | Non-active handle |
| `importFrom(alias)` | Copy from configured backend |
| `exportData()` / `importData(doc, mode)` | YAML import/export |

Direct {@code user(uuid)}, {@code group(name)} remain for low-level access. Legacy top-level {@code userCount()}, {@code backend()}, etc. are deprecated — use {@code query()} instead.

---

## World model

| Modern constant | Meaning |
|-----------------|---------|
| `Worlds.GLOBAL` | `null` — global namespace |
| Empty string `""` | Normalized to global when passed to API methods |

Helpers in `dev.rono.permissions.api.world.Worlds`: `normalize`, `isGlobal`, `mapKey`, `fromMapKey`.

---

## `PermissionService`

Primary entry: **`query()`**. Direct subject methods remain for implementors and escape hatches.

### Deprecated (use `query()`)

| Deprecated | Replacement |
|------------|-------------|
| `userCount()` / `groupCount()` | `query().users().count()` / `query().groups().count()` |
| `backend()` | `query().backend().info()` |
| `setActiveBackend(alias)` | `query().backend().activate(alias)` |
| `exportData()` / `importData(...)` | `query().backend().exportData()` / `importData(...)` |

### Introspection (via `query()`)

| Method | Description |
|--------|-------------|
| `query().backend().info()` | `BackendInfo(type, simpleName, diagnosticLabel)` |
| `query().users().count()` / `query().groups().count()` | Registered subjects |
| `query().worlds()` | Known realm/world names |
| `query().isDebug()` | Debug mode flag |

### World inheritance

| Method | Description |
|--------|-------------|
| `worldInheritance(world)` | Parent worlds that inherit into `world` |
| `setWorldInheritance(world, parents)` | Set inheritance parents |
| `worldInheritanceMap()` | All mappings (`Worlds.GLOBAL` key = global) |
| `defaultGroups(world)` | Default groups for a world |
| `rankLadder(ladderName)` | `Map<rank, Group>` on a promotion ladder |
| `events()` | Modern `PermissionEventBus` for entity/system notifications |

### Backend administration

| Method | Description |
|--------|-------------|
| `setActiveBackend(alias)` | Switch active backend (same as legacy `setBackend`) |
| `createBackendHandle(alias)` | Non-active `BackendHandle` for inspection/transfer |
| `importFromBackend(alias)` | Copy configured backend into active store |
| `exportData()` | Export active backend as YAML document |
| `importData(document, ImportMode)` | Import YAML (`MERGE` or `REPLACE`) |

### Users

| Method | Description |
|--------|-------------|
| `findUser(identifier)` / `findUser(uuid)` | Optional lookup — **only persisted** users |
| `user(identifier)` / `user(uuid)` | Resolve or **materialize** (classic `getUser`) |
| `userIdentifiers()` | All identifiers in backend |
| `deleteUser(identifier)` | Remove from backend and cache |

Use `user(...).has(permission[, world])` and `user(...).inGroup(name[, world, inherit])` for subject checks — not service-level helpers.

### Groups

| Method | Description |
|--------|-------------|
| `findGroup(name)` | Optional — **only persisted** groups |
| `group(name)` | Resolve or materialize group |
| `groupNames()` | All group names |
| `deleteGroup(name)` | Remove from backend and cache |

### Maintenance

| Method | Description |
|--------|-------------|
| `reload()` | Reload backend; throws `PermissionsExException` on failure |
| `reloadAsync()` | Reload on PEX executor; returns `CompletableFuture<Void>` |
| `openEditSession()` | Batch edit session — track subjects, call `save()` once |

Source: `api/src/main/java/dev/rono/permissions/api/service/PermissionService.java`

---

## Events (`PermissionEventBus`)

Subscribe via `pex.query().events()` (or `pex.events()`):

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
try (var session = pex.query().editSession()) {
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

if (BukkitPermissions.on(player).has("my.node")) { ... }
BukkitPermissions.on(player).context().inGroup("vip");
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
| `has(permission[, world])` | Effective check on this subject |
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

Obtain via `subject.inWorld("world_nether")` or `user.global()`.

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
| `PermissionQuery` / `WorldScope` / `UsersScope` / `GroupsScope` / `BackendScope` | Query API (`pex.query()...`) |
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
