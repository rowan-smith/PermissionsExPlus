---
layout: default
title: Permission Commands
permalink: /commands/permissions/
description: How to assign, remove, and check permission nodes in PermissionsExPlus.
---

Permission **nodes** are strings like `essentials.fly` that plugins check. This page covers assigning them — see [How Permissions Work]({{ site.baseurl }}/concepts/permissions/) for resolution rules.

---

## Assign to a group (recommended)

```text
/pex group <group> add <permission> [world]
```

Grants a node to every member of the group.

```text
/pex group default add modifyworld
/pex group vip add essentials.fly
/pex group vip add essentials.hat
/pex group admin add permissions.*
/pex group admin add '*'
/pex group builder add worldedit.* world_creative
```

**Why groups?** One command updates every member. Easier to audit and maintain.

---

## Assign to a user (exceptions)

```text
/pex user <user> add <permission> [world]
```

Direct assignment overrides or supplements group permissions.

```text
/pex user Steve add essentials.home
/pex user Steve add -essentials.ban
/pex user Alex add essentials.fly world_nether
```

Use for per-player exceptions, not your main permission structure.

---

## Remove permissions

```text
/pex group <group> remove <permission> [world]
/pex user <user> remove <permission> [world]
```

```text
/pex group vip remove essentials.hat
/pex user Steve remove essentials.home
```

Removing from a group affects all members. Removing from a user only affects that player.

---

## Temporary permissions

```text
/pex user <user> timed add <permission> <lifetime> [world]
/pex group <group> timed add <permission> <lifetime> [world]
```

| Unit | Example |
|------|---------|
| Seconds | `30s` |
| Minutes | `15m` |
| Hours | `2h` |
| Days | `7d`, `30d` |

```text
/pex user Steve timed add essentials.fly 7d
/pex user Trial timed add essentials.fly 1h
/pex group weekend timed add essentials.kit 2d
```

Remove early:

```text
/pex user Steve timed remove essentials.fly
```

---

## Check permissions

```text
/pex user <user> check <permission> [world]
/pex user <user> list [world]
/pex group <group> list [world]
```

```text
/pex user Steve check essentials.fly
/pex user Steve check essentials.fly world_nether
/pex user Steve list
/pex hierarchy
```

---

## Swap permissions

```text
/pex user <user> swap <permission> <targetPermission> [world]
/pex group <group> swap <permission> <targetPermission> [world]
```

Replaces one node with another in a single step.

```text
/pex group vip swap essentials.fly essentials.fly.unlimited
```

---

## Node syntax reference

| Pattern | Meaning | Example |
|---------|---------|---------|
| `plugin.node` | Exact match | `essentials.home` |
| `plugin.*` | All nodes under prefix | `essentials.*` |
| `*` | Full admin access | `*` |
| `-plugin.node` | Explicit deny | `-essentials.ban` |
| Regex | Pattern match | `(?i)essentials\.fly.*` |

**Negation** (`-`) always wins over a grant at the same level. Put denies below grants in the list.

---

## Common permission sets

**Survival default:**
```text
/pex group default add modifyworld
/pex group default add essentials.help
/pex group default add essentials.list
```

**VIP package:**
```text
/pex group vip add essentials.fly
/pex group vip add essentials.hat
/pex group vip add essentials.feed
/pex group vip add essentials.sethome.multiple
```

**Moderator:**
```text
/pex group mod add essentials.kick
/pex group mod add essentials.mute
/pex group mod add essentials.tp
```

**Admin:**
```text
/pex group admin add permissions.*
/pex group admin add '*'
```

---

## World-scoped permissions

Add the world name as the last argument. See [Context]({{ site.baseurl }}/concepts/context/).

```text
/pex group vip add essentials.fly
/pex group vip add essentials.godmode world_nether
```

---

## Special node

| Node | Effect |
|------|--------|
| `permissionsex.disabled` | Disables regex matching for that player |

```text
/pex user Griefer add permissionsex.disabled
```
