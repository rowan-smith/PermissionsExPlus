---
title: Legacy API Reference
description: Frozen ru.tehkode.permissions API for classic hook plugins.
slug: /developers/api/legacy
---
Package root: `ru.tehkode.permissions`  
Maven artifacts:

| Artifact | Purpose |
|----------|---------|
| `permissionsex-legacy-api` | Types, interfaces, events, utils (**required**) |
| `permissionsex-legacy-stub` | Compile-only `PermissionsEx` static helpers (**optional**) |

**Status:** Frozen public contract matching upstream **PermissionsEx 1.23.4** (baseline commit **`628215f`**). Guarded by `LegacyApiContractTest` in `permissionsex-legacy-api`. Do not add methods to `ru.tehkode.*` — use the [modern API](/developers/api/modern) for new features. Legacy methods deprecated in PermissionsExPlus 3.0 use `@Deprecated(since = "3.0.0")`.

Sample plugin: [`example-legacy-plugin/`](https://github.com/%%site.repo%%/tree/main/example-legacy-plugin/)

---

## Quick start

```java
import org.bukkit.entity.Player;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

@Override
public void onEnable() {
    if (!PermissionsEx.isAvailable()) {
        getLogger().warning("PEX not loaded");
        return;
    }
    PermissionManager mgr = PermissionsEx.getPermissionManager();
    getLogger().info("Backend: " + mgr.getBackend().getClass().getSimpleName());
}

void onJoin(Player player) {
    PermissionUser user = PermissionsEx.getUser(player);
    PermissionManager mgr = PermissionsEx.getPermissionManager();
    boolean allowed = mgr.has(player, "my.node", player.getWorld().getName());
    String[] groups = user.getGroupsNames();
}
```

**Without the stub:** resolve `PermissionManager` from `ServicesManager` only (no `PermissionsEx.*` static calls).

---

## Maven dependencies

```xml
<dependency>
  <groupId>dev.rono.permissions</groupId>
  <artifactId>permissionsex-legacy-api</artifactId>
  <version>%%site.version%%</version>
  <scope>provided</scope>
</dependency>
<!-- Optional: only if you call PermissionsEx.getUser / getPermissionManager -->
<dependency>
  <groupId>dev.rono.permissions</groupId>
  <artifactId>permissionsex-legacy-stub</artifactId>
  <version>%%site.version%%</version>
  <scope>provided</scope>
</dependency>
<dependency>
  <groupId>org.spigotmc</groupId>
  <artifactId>spigot-api</artifactId>
  <scope>provided</scope>
</dependency>
```

---

## `PermissionsEx` (stub)

Compile-only static façade. Runtime class is the live `JavaPlugin` in `bukkit`.

| Method | Description |
|--------|-------------|
| `isAvailable()` | Plugin enabled and `PermissionManager` registered |
| `getPlugin()` | Bukkit plugin instance |
| `getPermissionManager()` | Registered manager; throws `PermissionsNotAvailable` |
| `getUser(Player)` / `getUser(String)` | Shortcut to manager |

---

## `PermissionManager`

Registered on Bukkit `ServicesManager` as `PermissionManager.class` on game servers.

### Permission checks

| Method | Description |
|--------|-------------|
| `has(Player, permission)` | Check in player's current world |
| `has(Player, permission, world)` | Check in named world |
| `has(String playerName, permission, world)` | Offline/name resolution |
| `has(UUID playerId, permission, world)` | UUID-based check |

### Users

| Method | Description |
|--------|-------------|
| `getUser(String)` / `getUser(UUID)` / `getUser(Player)` | Resolve or materialize user |
| `cacheUser(ident, fallbackName)` | Pre-cache during async login |
| `getUsers()` | All registered users |
| `getActiveUsers()` | In-memory cached users |
| `getUserIdentifiers()` / `getUserNames()` | Backend identifiers |
| `getUsers(group[, world[, inherit]])` | Users in group |
| `resetUser(name\|Player)` | Drop in-memory user |
| `clearUserCache(name\|UUID\|Player)` | Clear resolved permission cache |

### Groups

| Method | Description |
|--------|-------------|
| `getGroup(name)` | Resolve or materialize group |
| `getGroupList()` | All groups |
| `getGroups(parent[, world[, inherit]])` | Child/descendant groups |
| `getDefaultGroups(world)` | Default groups for world |
| `resetGroup(name)` | Drop in-memory group |
| `getGroupNames()` | **Deprecated** — use backend via manager patterns |

### Worlds & ladders

| Method | Description |
|--------|-------------|
| `getWorldInheritance(world)` | Parent worlds for inheritance |
| `setWorldInheritance(world, parents)` | Configure inheritance |
| `getRankLadder(ladderName)` | `Map<rank, PermissionGroup>` |

### Backend & engine

| Method | Description |
|--------|-------------|
| `getBackend()` | Active `PermissionBackend` |
| `setBackend(name)` / `createBackend(name)` | Switch/create backend |
| `reset()` / `reset(callEvent)` | Reload data |
| `end()` | Shutdown |
| `getPermissionMatcher()` / `setPermissionMatcher(m)` | Regex matcher |
| `getLogger()` / `getExecutor()` | Engine logger and scheduler |
| `setDebug` / `isDebug` | Debug mode |
| `shouldCreateUserRecords()` / `shouldSaveDefaultGroup()` | Config-driven behaviour |
| `getConfiguration()` | `PermissionsExConfig` |
| `initTimer()` | Timed-permission scheduler |
| `TRANSIENT_PERMISSION` | Constant `0` — non-persisted timed permission |

Constant `TRANSIENT_PERMISSION = 0` means timed permission is not stored to backend.

---

## `PermissionEntity`

Base type for users and groups (`PermissionUser`, `PermissionGroup`).

### Identity

| Method | Description |
|--------|-------------|
| `getIdentifier()` / `getName()` | Id and display name |
| `getType()` | `USER` or `GROUP` |
| `isVirtual()` | Not yet persisted |
| `getPermissionManager()` | Owning manager |
| `getWorlds()` | Worlds with data |

### Permissions

| Method | Description |
|--------|-------------|
| `has(permission[, world])` | Effective check |
| `getPermissions([world])` | Effective list |
| `getOwnPermissions(world)` | Direct assignments only |
| `getAllPermissions()` | Map world → permissions |
| `addPermission` / `removePermission` / `setPermissions` | CRUD |
| `getTimedPermissions(world)` | Timed permission names |
| `getTimedPermissionLifetime(perm, world)` | Remaining seconds |
| `addTimedPermission(perm, world, seconds)` | Grant timed |
| `removeTimedPermission(perm, world)` | Remove timed |

Non-inheritable permissions use prefix `#` (`PermissionEntity.NON_INHERITABLE_PREFIX`).

### Prefix / suffix / options

| Method | Description |
|--------|-------------|
| `getPrefix` / `getSuffix` / `setPrefix` / `setSuffix` | Chat meta |
| `getOwnPrefix` / `getOwnSuffix` | Direct meta only |
| `getOption` / `setOption` / `getOptions` | Options map |
| `getOwnOption` / typed getters | Direct options |
| `getAllOptions()` | All worlds |

### Inheritance (parents)

| Method | Description |
|--------|-------------|
| `getParents([world])` | Resolved parent groups |
| `getOwnParents([world])` | Direct parents |
| `getParentIdentifiers([world])` | Parent names |
| `getOwnParentIdentifiers([world])` | Direct parent names |
| `setParents` / `setParentsIdentifier` | Set parent list |

### Diagnostics

| Method | Description |
|--------|-------------|
| `getMatchingExpression(permission, world)` | Winning expression |
| `explainExpression(expression)` | Whether expression grants |
| `isMatches` / `isDebug` / `setDebug` | Matcher/debug helpers |

### Persistence

| Method | Description |
|--------|-------------|
| `save()` | Persist |
| `remove()` | Delete from backend |
| `clearCache()` / `initialize()` | Internal cache lifecycle |

---

## `PermissionUser`

Extends `PermissionEntity`.

| Method | Description |
|--------|-------------|
| `addGroup(name\|group[, world[, lifetime]])` | Join group; optional timed |
| `removeGroup(name\|group[, world])` | Leave group |
| `inGroup(name\|group[, world[, inherit]])` | Membership test |
| `promote` / `demote(promoter, ladder)` | Rank ladder; throws `RankingException` |
| `isRanked` / `getRank` / `getRankLadderGroup` / `getRankLadders` | Rank metadata |
| `updateTimedGroups()` | Process expired timed groups |

Deprecated array helpers: `getGroupsNames`, `getGroups`, `setGroups`, … — prefer `getParentIdentifiers` / `setParentsIdentifier`.

---

## `PermissionGroup`

Extends `PermissionEntity`, `Comparable<PermissionGroup>`.

| Method | Description |
|--------|-------------|
| `getWeight()` / `setWeight(int)` | Sort weight |
| `isDefault(world)` / `setDefault(bool, world)` | Default group |
| `addParent` / `removeParent` | Inheritance |
| `isChildOf(group\|name[, world[, inherit]])` | Hierarchy |
| `getChildGroups` / `getDescendantGroups` | Downward traversal |
| `getUsers([world])` / `getActiveUsers([inherit])` | Members |
| `getRank()` / `setRank` / `getRankLadder()` / `setRankLadder` | Promotion ladder |

---

## Events

Spigot/Paper only. Subscribe with `@EventHandler` or `PluginManager.registerEvents`.

### `PermissionEntityEvent`

Fired when a user or group changes.

| Field / method | Description |
|----------------|-------------|
| `getEntity()` | `PermissionUser` or `PermissionGroup` (lazy reload) |
| `getEntityIdentifier()` | Stable id |
| `getType()` | `USER` or `GROUP` |
| `getAction()` | See enum below |

**Actions:** `PERMISSIONS_CHANGED`, `OPTIONS_CHANGED`, `INHERITANCE_CHANGED`, `INFO_CHANGED`, `TIMEDPERMISSION_EXPIRED`, `RANK_CHANGED`, `DEFAULTGROUP_CHANGED`, `WEIGHT_CHANGED`, `SAVED`, `REMOVED`

### `PermissionSystemEvent`

Engine-level changes.

**Actions:** `BACKEND_CHANGED`, `RELOADED`, `WORLDINHERITANCE_CHANGED`, `DEFAULTGROUP_CHANGED`, `DEBUGMODE_TOGGLE`, `REINJECT_PERMISSIBLES`

Both extend `PermissionEvent` (carries source server UUID).

---

## `NativeInterface`

Classic host SPI (5 methods). Spigot implementation delegates to `PlatformAdapter`.

| Method | Description |
|--------|-------------|
| `UUIDToName(UUID)` | Online name |
| `nameToUUID(String)` | Resolve UUID |
| `isOnline(UUID)` | Connection state |
| `getServerUUID()` | Logical server id |
| `callEvent(PermissionEvent)` | Publish Bukkit event |

---

## `PermissionsExConfig`

Read-only config contract (`getConfiguration()` on manager):

`useNetEvents`, `isDebug`, `allowOps`, `userAddGroupsLast`, `getDefaultBackend`, `shouldLogPlayers`, `createUserRecords`, `saveDefaultGroup`, updater flags, `getServerTags`, `getBasedir`, `getBackendConfig`, `save`.

---

## Utilities (`ru.tehkode.utils`)

Shipped in `permissionsex-legacy-api`:

| Class | Role |
|-------|------|
| `DateUtils` | Parse time intervals (`1d`, `30m`, …) for timed permissions |
| `StringUtils` | String helpers |
| `Interval` | Time interval value type |
| `FieldReplacer` | Reflection helper (internal-style) |

---

## Backend interfaces

For plugins that implement custom backends (rare):

- `ru.tehkode.permissions.backends.PermissionBackend`
- `PermissionsUserData` / `PermissionsGroupData`
- `PEXBackendConfiguration`

Most hook plugins should **not** depend on backend types — use `PermissionManager` / `PermissionsExApi` only.

---

## Migration to modern API

| Legacy | Modern |
|--------|--------|
| `PermissionsEx.getPermissionManager()` | `PermissionsEx.getApi()` |
| `getUser(...)` | `service.user(...)` |
| `PermissionUser.addPermission` | `user.addPermission(...)` or `user.inContext(PermissionContext.world(w)).addPermission(...)` |
| `getWorldInheritance` | `service.worldInheritance(...)` |
| `PermissionEntityEvent` | Still use legacy events on Spigot until [modern event bus](/developers/api/future) exists |

See [API index](/developers/api) and [FUTURE.md](/developers/api/future).
