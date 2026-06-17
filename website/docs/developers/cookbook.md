---
title: API Cookbook
description: Practical API recipes for PermissionsExPlus plugin developers.
slug: /developers/cookbook
---

Short, copy-paste recipes for common integration tasks. For full method signatures, see [Javadoc](/developers/reference/) and the [Modern API reference](/developers/api/modern).

**Concept primer:** A **realm** is a stored permission namespace (Bukkit world name, proxy backend id, or admin-registered key). **Context** (`PermissionContext`) is the scope used when checking or mutating permissions. Day-to-day plugin code uses context; realm inheritance administration uses `RealmManager`.

---

## Setup

### Spigot / Paper

```java
import ru.tehkode.permissions.bukkit.PermissionsEx;

if (!PermissionsEx.isAvailable()) {
    getLogger().warning("PEX not loaded");
    return;
}
var api = PermissionsEx.getApi();
```

### BungeeCord / Waterfall

```java
import dev.rono.permissions.bungee.PermissionsEx;

if (!PermissionsEx.isAvailable()) {
    getLogger().warning("PEX not loaded");
    return;
}
var api = PermissionsEx.getApi();
```

### Maven (`provided` scope)

```xml
<dependency>
  <groupId>dev.rono.permissions</groupId>
  <artifactId>permissionsex-api</artifactId>
  <version>%%site.version%%</version>
  <scope>provided</scope>
</dependency>
```

On proxies, add `permissionsex-api-bungee` if you need proxy-specific types.

---

## Realms vs context

| Task | API | Example |
|------|-----|---------|
| Check or grant in a scope | `PermissionContext` + `user.inContext(...)` | Fly only in `world_nether` |
| Register realm / set inheritance | `api.getRealmManager()` + `Realm` | `world_nether` inherits `world` |
| Global (all realms) | `PermissionContext.global()` or `user.global()` | `essentials.home` everywhere |

On **game servers**, world names are the usual realm ids. On **proxies**, backend server names are stored as realm ids — scope with `PermissionContext.server("lobby")` in plugin code.

---

## Check a permission

### Global check

```java
var user = api.getUserManager().getUser(player.getUniqueId());
if (user.has("myplugin.use")) {
    // allowed in global scope (parameterless = global)
}
```

### Check in the player's current world

```java
import dev.rono.permissions.api.permission.PermissionContext;

var world = player.getWorld().getName();
if (user.inContext(PermissionContext.world(world)).has("myplugin.nether-only")) {
    // allowed in this world only
}
```

### Holder check with full context (Spigot)

Use when you need world, server, region, and gamemode in one evaluation:

```java
var ctx = PermissionContext.of(
        player.getWorld().getName(),
        getServer().getName(),
        "spawn",
        player.getGameMode().name());
boolean allowed = api.getPermissionManager()
        .hasPermission(user.asHolder(), "myplugin.use", ctx);
```

**Javadoc:** [User](pathname:///apidocs/%%site.version%%/dev/rono/permissions/api/user/User.html) · [PermissionContext](pathname:///apidocs/%%site.version%%/dev/rono/permissions/api/permission/PermissionContext.html)

---

## Global vs realm-scoped permissions

Global grants apply everywhere unless overridden per realm:

```java
user.global().addPermission("essentials.home");
user.inContext(PermissionContext.world("world_nether")).addPermission("essentials.god");
user.save();

assertTrue(user.has("essentials.home"));                              // global
assertTrue(user.inContext(PermissionContext.world("world_nether")).has("essentials.god"));
assertFalse(user.inContext(PermissionContext.world("world")).has("essentials.god"));
```

Empty world strings normalize to global:

```java
// PermissionContext.world("") and PermissionContext.world(null) both mean global
user.inContext(PermissionContext.world("")).addPermission("everywhere");
user.save();
assertTrue(user.has("everywhere"));
```

---

## World-scoped permissions

Grant, check, and remove in a single world:

```java
import dev.rono.permissions.api.permission.PermissionContext;

var ctx = PermissionContext.world("world_nether");
var scoped = user.inContext(ctx);

scoped.addPermission("myplugin.visited");
if (scoped.has("myplugin.nether-only")) {
    // ...
}
scoped.removePermission("myplugin.visited");
user.save();
```

Per-world prefix (chat plugins):

```java
user.inContext(PermissionContext.world("creative"))
        .setPrefix("&b[Creative] ");
user.save();
```

Per-world group membership:

```java
user.inContext(PermissionContext.world("minigames")).addGroup("arena");
user.save();

if (user.inContext(PermissionContext.world("minigames")).inGroup("arena")) {
    // player is in arena group only while data is scoped to minigames realm
}
```

**Commands equivalent:**

```text
/pex user Steve add myplugin.visited world_nether
/pex user Steve check myplugin.nether-only world_nether
```

See also [Context & Worlds](/concepts/context/).

---

## Server-scoped permissions (proxy)

On BungeeCord/Waterfall, backend server names are permission namespaces. Use `PermissionContext.server`:

```java
import dev.rono.permissions.api.permission.PermissionContext;

var lobby = PermissionContext.server("lobby");
var scoped = user.inContext(lobby);

if (scoped.has("bungee.command.server")) {
    // allowed on lobby backend
}

scoped.addPermission("proxy.welcome");
user.save();
```

Check which server a player is connected to (proxy):

```java
// Player permission checks on the proxy auto-scope via PlatformAdapter.onlineRealm()
if (user.has("network.staff")) {
    // effective in the player's current backend context
}
```

**Commands equivalent (proxy):**

```text
/pex server lobby user Steve add network.vip
/pex server lobby user Steve check network.vip
```

---

## Combined context (world + server + region + gamemode)

Build rich scopes for minigames, regions, or multi-world networks:

```java
var ctx = PermissionContext.of("arena", "survival-1", "red-base", "ADVENTURE");
// keys: world=arena, server=survival-1, region=red-base, gamemode=ADVENTURE

if (user.inContext(ctx).has("minigame.capture")) {
    // ...
}
```

Custom attribute map:

```java
var attrs = Map.of(
        PermissionContext.WORLD, "event",
        PermissionContext.STATE, "tournament");
var ctx = PermissionContext.of(attrs);
user.inContext(ctx).addPermission("event.join");
user.save();
```

Event/minigame state only:

```java
user.inContext(PermissionContext.withState("world", "boss-fight"))
        .addPermission("boss.damage-boost");
user.save();
```

---

## Realm registry (admin)

Register realms and inspect the registry. Realms are backend namespaces — they may exist before a Bukkit world is loaded.

```java
import dev.rono.permissions.api.realm.RealmNotFoundException;
import dev.rono.permissions.api.realm.RealmAlreadyExistsException;

var realms = api.getRealmManager();

// find does not create; get throws if missing
if (realms.findRealm("world_nether").isEmpty()) {
    realms.createRealm("world_nether");
}

try {
    var nether = realms.getRealm("world_nether");
    getLogger().info("Realm: " + nether.getName());
} catch (RealmNotFoundException e) {
    getLogger().warning("Realm not registered: " + e.getName());
}

for (String name : realms.listRealmNames()) {
    getLogger().fine("Registered realm: " + name);
}

assertThrows(RealmAlreadyExistsException.class, () -> realms.createRealm("world_nether"));
```

Count and filter:

```java
int total = realms.count();
long netherCount = realms.count(r -> r.getName().equals("world_nether"));
```

---

## Realm inheritance (admin)

Configure which realms inherit from others. This affects **effective** permission resolution order for that namespace.

```java
var realms = api.getRealmManager();
if (!realms.exists("world_nether")) {
    realms.createRealm("world_nether");
}
var nether = realms.getRealm("world_nether");

nether.addParent("world");
// nether.parents() -> ["world"]
// nether.parentTree() -> all ancestors in traversal order

nether.setParents(List.of("world", "shared"));  // replace entire chain
nether.removeParent("shared");
```

**Commands equivalent:**

```text
/pex world world_nether inherit world
/pex world world_nether inherit list
```

Realm inheritance is separate from group parent trees. Use `RealmManager` for world/server namespace graphs; use `Group.addParent` for group hierarchy.

---

## Realms helper (`Realms`)

Normalize global vs named realms when bridging maps and API arguments:

```java
import dev.rono.permissions.api.realm.Realms;

Realms.isGlobal(null);           // true — global namespace
Realms.isGlobal("survival");       // false
Realms.normalize("  arena  ");    // "arena"
Realms.normalize("");             // null (GLOBAL)

// Map keys cannot be null; global is stored as ""
String key = Realms.mapKey(null);       // ""
String realm = Realms.fromMapKey("");   // null (global)
```

Use when reading `permissionsByRealm()` / `configuredRealms()` results.

---

## Inspect permissions across realms

List direct and effective permissions per stored realm:

```java
Map<String, List<String>> direct = user.permissionsByRealm();
Map<String, List<String>> effective = user.effectivePermissionsByRealm();
Set<String> configured = user.configuredRealms();

for (String realm : configured) {
    getLogger().info(realm + " direct: " + direct.get(realm));
}
```

Global realm is represented as `null` in API maps; use `Realms.fromMapKey` when iterating map keys from legacy storage views.

---

## Add a permission to a user

```java
var user = api.getUserManager().getUser(player.getUniqueId());
user.addPermission("myplugin.reward");
user.save();
```

Negated permission:

```java
user.addPermission("-myplugin.banned-feature");
user.save();
```

Always call `save()` after mutations.

---

## Add a permission to a group

```java
var group = api.getGroupManager().getGroup("vip");
group.addPermission("essentials.fly");
group.save();
```

Per-world group permission:

```java
group.inContext(PermissionContext.world("creative"))
        .addPermission("worldedit.*");
group.save();
```

---

## Create a group

```java
var group = api.getGroupManager().createGroup("vip");
group.addParent("default");
group.addPermission("essentials.fly");
group.setPrefix("&6[VIP] ");
group.save();
```

---

## Add a user to a group

```java
var user = api.getUserManager().getUser(player.getUniqueId());
user.addGroup("vip");
user.save();
```

Replace all groups (remove existing memberships, then add):

```java
for (String group : user.groups()) {
    user.removeGroup(group);
}
user.addGroup("admin");
user.save();
```

Timed group membership (global):

```java
user.addGroup("trial", PermissionContext.global(), 604800); // 7 days
user.save();
```

Timed group membership in one world:

```java
user.addGroup("arena", PermissionContext.world("minigames"), 3600);
user.save();

if (user.hasTimedGroupMembership("arena", PermissionContext.world("minigames"))) {
    long seconds = user.groupMembershipRemainingSeconds(
            "arena", PermissionContext.world("minigames"));
}
```

Remove timed membership explicitly:

```java
user.removeTimedGroup("trial", PermissionContext.global());
user.save();
```

---

## Timed permission

Global timed grant:

```java
user.addTimedPermission("essentials.fly", 604800); // 7 days in seconds
user.save();
```

Timed grant in a world:

```java
user.inContext(PermissionContext.world("event"))
        .addTimedPermission("event.boost", 3600);
user.save();

for (var entry : user.timedPermissionEntries(PermissionContext.world("event"))) {
    getLogger().info(entry.permission() + " expires in " + entry.remainingSeconds() + "s");
}
```

---

## User lifecycle (find vs get)

`find` never materializes missing records; `get` requires persistence:

```java
import dev.rono.permissions.api.user.UserNotFoundException;

var uuid = player.getUniqueId();
if (api.getUserManager().findUser(uuid).isEmpty()) {
    var created = api.getUserManager().createUser(uuid);
    created.save();
}

try {
    var user = api.getUserManager().getUser(uuid);
} catch (UserNotFoundException e) {
    getLogger().warning("No persisted user: " + e.getIdentifier());
}
```

---

## Group hierarchy

```java
var parent = api.getGroupManager().getGroup("staff");
var junior = api.getGroupManager().getGroup("helper");
junior.addParent(parent.getName());
junior.save();

assertTrue(junior.isChildOf("staff"));
assertFalse(parent.children().isEmpty());
assertTrue(parent.parentTree().isEmpty()); // parent has no parents
```

Per-world group parents:

```java
junior.inContext(PermissionContext.world("survival")).addParent("survival-mod");
junior.save();
```

---

## Promote / demote

```java
import dev.rono.permissions.api.RankingException;

try {
    api.getLadderManager().promote(user, "staff");
} catch (RankingException e) {
    getLogger().warning("Cannot promote: " + e.getMessage());
}

int rank = api.getLadderManager().rank(user, "staff");
if (api.getLadderManager().isRanked(user, "staff")) {
    getLogger().info("Staff rank: " + rank);
}
```

---

## Listen for changes (Spigot)

Legacy Bukkit events — still the recommended way on game servers:

```java
import ru.tehkode.permissions.events.PermissionEntityEvent;
import org.bukkit.event.EventHandler;

@EventHandler
public void onChange(PermissionEntityEvent event) {
    getLogger().info("Changed: " + event.getIdentifier());
}
```

Modern event bus:

```java
import dev.rono.permissions.api.event.PermissionEventListener;
import dev.rono.permissions.api.bus.EntityDispatch;

var sub = api.getEventBus().subscribe(new PermissionEventListener() {
    @Override
    public void onEntity(EntityDispatch dispatch) {
        getLogger().info("Dispatch: " + dispatch);
    }
});
// later: api.getEventBus().unsubscribe(sub);
```

---

## Legacy API (existing plugins)

```java
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

PermissionUser user = PermissionsEx.getUser(player);
boolean ok = PermissionsEx.getPermissionManager()
        .has(player, "myplugin.use", player.getWorld().getName());
```

The modern API no longer exposes `WorldManager` / `World` types — use `RealmManager` and `PermissionContext` in new code.

---

## Which API?

| Situation | API |
|-----------|-----|
| New plugin | Modern — `PermissionsEx.getApi()` |
| World-scoped checks | `PermissionContext.world(name)` + `user.inContext(...)` |
| Proxy server scope | `PermissionContext.server(id)` + `user.inContext(...)` |
| Realm inheritance admin | `api.getRealmManager()` + `Realm` |
| PermissionsEx 1.23.4 hook plugin | Legacy — usually works without changes |
| Bukkit events on Spigot | Legacy events |
| Bungee/Waterfall proxy | `dev.rono.permissions.bungee.PermissionsEx.getApi()` |

Full class reference: [Javadoc](/developers/reference/) · [Modern API](/developers/api/modern) · [Context concepts](/concepts/context/)

Sample plugins: [`example-plugin/`](https://github.com/%%site.repo%%/tree/main/example-plugin) (modern) · [`example-legacy-plugin/`](https://github.com/%%site.repo%%/tree/main/example-legacy-plugin) (legacy)
