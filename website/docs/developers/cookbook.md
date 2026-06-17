---
title: API Cookbook
description: Practical API recipes for PermissionsExPlus plugin developers.
slug: /developers/cookbook
---

Short recipes for common integration tasks. For full method signatures, see [Javadoc](/developers/reference/).

---

## Setup

```java
import ru.tehkode.permissions.bukkit.PermissionsEx;

if (!PermissionsEx.isAvailable()) {
    getLogger().warning("PEX not loaded");
    return;
}
var api = PermissionsEx.getApi();
```

**Maven** (`provided` scope):

```xml
<dependency>
  <groupId>dev.rono.permissions</groupId>
  <artifactId>permissionsex-api</artifactId>
  <version>%%site.version%%</version>
  <scope>provided</scope>
</dependency>
```

---

## Check a permission

```java
var user = api.getUserManager().getUser(player.getUniqueId());
if (user.has("myplugin.use")) {
    // allowed
}
```

**Javadoc:** [User](pathname:///apidocs/%%site.version%%/dev/rono/permissions/api/subject/User.html) · [PermissionSubject.has()](pathname:///apidocs/%%site.version%%/dev/rono/permissions/api/subject/PermissionSubject.html)

---

## Add a permission to a user

```java
var user = api.getUserManager().getUser(player.getUniqueId());
user.addPermission("myplugin.reward");
user.save();
```

Always call `save()` after mutations.

**Javadoc:** [User.addPermission()](pathname:///apidocs/%%site.version%%/dev/rono/permissions/api/subject/PermissionSubject.html#addPermission(java.lang.String))

---

## Add a permission to a group

```java
var group = api.getGroupManager().getGroup("vip");
group.addPermission("essentials.fly");
group.save();
```

**Javadoc:** [GroupManager](pathname:///apidocs/%%site.version%%/dev/rono/permissions/api/service/GroupManager.html) · [Group](pathname:///apidocs/%%site.version%%/dev/rono/permissions/api/subject/Group.html)

---

## Create a group

```java
var group = api.getGroupManager().createGroup("vip");
group.addParent("default");
group.addPermission("essentials.fly");
group.setPrefix("&6[VIP] ");
group.save();
```

**Javadoc:** [GroupManager.createGroup()](pathname:///apidocs/%%site.version%%/dev/rono/permissions/api/service/GroupManager.html)

---

## Add a user to a group

```java
var user = api.getUserManager().getUser(player.getUniqueId());
user.addGroup("vip");
user.save();
```

Replace all groups:

```java
user.setGroups(java.util.List.of("admin"));
user.save();
```

**Javadoc:** [User.addGroup()](pathname:///apidocs/%%site.version%%/dev/rono/permissions/api/subject/User.html)

---

## Timed permission

```java
user.addTimedPermission("essentials.fly", 604800); // 7 days in seconds
user.save();
```

**Javadoc:** [PermissionSubject.addTimedPermission()](pathname:///apidocs/%%site.version%%/dev/rono/permissions/api/subject/PermissionSubject.html)

---

## World-scoped permissions

```java
import dev.rono.permissions.api.permission.PermissionContext;

var ctx = PermissionContext.of(player.getWorld().getName());
var scoped = user.inContext(ctx);
if (scoped.has("myplugin.nether-only")) {
    // ...
}
scoped.addPermission("myplugin.visited");
user.save();
```

**Javadoc:** [PermissionContext](pathname:///apidocs/%%site.version%%/dev/rono/permissions/api/permission/PermissionContext.html) · [SubjectContext](pathname:///apidocs/%%site.version%%/dev/rono/permissions/api/subject/SubjectContext.html)

---

## Promote / demote

```java
try {
    user.promote("staff");
} catch (RankingException e) {
    getLogger().warning("Cannot promote: " + e.getMessage());
}
```

**Javadoc:** [User.promote()](pathname:///apidocs/%%site.version%%/dev/rono/permissions/api/subject/User.html)

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
api.events().subscribe(dispatch -> {
    getLogger().info("Dispatch: " + dispatch);
});
```

**Javadoc:** [PermissionEntityEvent](pathname:///apidocs/%%site.version%%/ru/tehkode/permissions/events/PermissionEntityEvent.html) · [PermissionsExApi.events()](pathname:///apidocs/%%site.version%%/dev/rono/permissions/api/service/PermissionsExApi.html)

---

## Legacy API (existing plugins)

```java
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

PermissionUser user = PermissionsEx.getUser(player);
boolean ok = PermissionsEx.getPermissionManager()
    .has(player, "myplugin.use", player.getWorld().getName());
```

**Javadoc:** [PermissionManager](pathname:///apidocs/%%site.version%%/ru/tehkode/permissions/PermissionManager.html) · [PermissionUser](pathname:///apidocs/%%site.version%%/ru/tehkode/permissions/PermissionUser.html)

---

## Which API?

| Situation | API |
|-----------|-----|
| New plugin | Modern — `PermissionsEx.getApi()` |
| Existing PEX 1.x plugin | Legacy — usually works without changes |
| Bukkit events on Spigot | Legacy events |
| Bungee/Velocity proxy | `dev.rono.permissions.bungee.PermissionsEx.getApi()` |

Full class reference: [Javadoc](/developers/reference/)

Sample plugins: [GitHub /plugin](https://github.com/%%site.repo%%/tree/main/plugin)
