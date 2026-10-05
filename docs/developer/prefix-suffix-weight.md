---
sidebar_position: 4
---

# Prefix, suffix & weight

Prefix/suffix are convenience wrappers around options. Weight controls priority when a player is in multiple groups.

## Prefix and suffix

```java
group.setPrefix("&6[VIP] ", null);
group.setSuffix("&r", null);

PermissionUser user = manager.getUser(player);
String prefix = user.getPrefix(player.getWorld().getName());
String suffix = user.getSuffix();
String ownPrefix = user.getOwnPrefix(null); // only if set on the user

group.setPrefix(null, null); // clear
```

World-scoped formatting:

```java
group.setPrefix("&b[Builder] ", "creative");
user.setPrefix("&c*", "event");
```

### Chat formatting helper

```java
public String formatName(Player player) {
    PermissionUser user = PermissionsEx.getPermissionManager().getUser(player);
    String world = player.getWorld().getName();
    String prefix = user.getPrefix(world);
    String suffix = user.getSuffix(world);
    if (prefix == null) prefix = "";
    if (suffix == null) suffix = "";
    return prefix + player.getName() + suffix;
}
```

## Weight

Higher weight wins for prefix/suffix selection and permission conflict resolution when a user is in multiple groups.

```java
PermissionGroup admin = manager.getGroup("admin");
admin.setWeight(100);

int weight = admin.getWeight();
```

`setWeight` stores the `weight` option and fires a `WEIGHT_CHANGED` entity event.

```java
manager.getGroup("default").setWeight(0);
manager.getGroup("member").setWeight(10);
manager.getGroup("vip").setWeight(20);
manager.getGroup("moderator").setWeight(50);
manager.getGroup("admin").setWeight(100);
```

Weight is independent of ladder **rank**. See [Weight](/docs/concepts-guides/weight) and [Options](options).
