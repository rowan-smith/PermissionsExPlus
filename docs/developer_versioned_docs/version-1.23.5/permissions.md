---
sidebar_position: 1
---

# Permissions

Users and groups share permission methods on `PermissionEntity`. Negation uses a leading `-` on the node; there is no separate deny method.

## Allow a permission

```java
PermissionGroup group = manager.getGroup("member");
group.addPermission("essentials.home");
group.addPermission("essentials.sethome", "survival"); // world-scoped

PermissionUser user = manager.getUser(player);
user.addPermission("myplugin.special");
user.addPermission("myplugin.vip.kit", "lobby");
```

Nodes added later sit at the **front** of the list (higher priority when resolving conflicts).

## Negate (deny) a permission

Prefix the node with `-`:

```java
PermissionGroup moderator = manager.getGroup("moderator");
moderator.addPermission("essentials.*");
moderator.addPermission("-essentials.ban");
moderator.addPermission("-essentials.banip");

user.addPermission("-essentials.fly");
user.addPermission("-myplugin.admin", "creative");
```

See [Negation](/docs/concepts-guides/negation) for inheritance and wildcards.

## Remove a permission

```java
group.removePermission("essentials.home");
group.removePermission("essentials.home", "survival");

user.removePermission("myplugin.special"); // all worlds
user.removePermission("-essentials.fly");  // remove the deny node itself
```

Removing matches the **exact** stored string. Clearing a negation means removing `-essentials.ban`, not `essentials.ban`.

## Replace the full list

```java
group.setPermissions(List.of(
    "essentials.spawn",
    "essentials.tpaccept",
    "-essentials.troll"
));

user.setPermissions(List.of("myplugin.beta"), "creative");
```

## Read permissions

```java
List<String> own = group.getOwnPermissions(null);     // direct only
List<String> effective = group.getPermissions(null);  // may include inheritance
Map<String, List<String>> all = group.getAllPermissions();

List<String> userEffective = user.getPermissions(player.getWorld().getName());
List<String> userOwn = user.getOwnPermissions(null);
```

## Timed permissions

```java
// lifeTime in seconds; world may be null for global
user.addTimedPermission("essentials.fly", null, 600);
user.addTimedPermission("myplugin.temp", "event", 3600);

List<String> timed = user.getTimedPermissions(null);
int remaining = user.getTimedPermissionLifetime("essentials.fly", null);

user.removeTimedPermission("essentials.fly", null);
```

Groups support the same timed APIs via `PermissionEntity`.

Next: [Permission checks](permission-checks).
