---
sidebar_position: 2
---

# Group inheritance

Parents let groups share permissions and options without duplicating lists.

## Set parents

```java
PermissionGroup member = manager.getGroup("member");
PermissionGroup newcomer = manager.getGroup("newcomer");

member.setParents(List.of(newcomer));
member.setParents(List.of(newcomer), "survival"); // world-scoped

List<PermissionGroup> parents = member.getParents();
List<PermissionGroup> ownParents = member.getOwnParents();
List<PermissionGroup> children = newcomer.getChildGroups();
List<PermissionGroup> descendants = newcomer.getDescendantGroups();
```

```java
// Child / descendant checks
boolean direct = member.isChildOf("newcomer", false);
boolean anywhere = elite.isChildOf("newcomer", true);
```

## Staff group example

```java
PermissionManager manager = PermissionsEx.getPermissionManager();

PermissionGroup helper = manager.getGroup("helper");
helper.setWeight(50);
helper.setPrefix("&b[Helper] ", null);
helper.addPermission("essentials.kick");
helper.addPermission("essentials.mute");
helper.addPermission("-essentials.ban");
if (helper.isVirtual()) {
    helper.save();
}

PermissionGroup mod = manager.getGroup("moderator");
mod.setWeight(60);
mod.setPrefix("&9[Mod] ", null);
mod.setParents(List.of(helper));
mod.addPermission("essentials.ban");
mod.addPermission("essentials.banip");
if (mod.isVirtual()) {
    mod.save();
}
```

See also [Groups](groups) and [Ladders](ladders).
