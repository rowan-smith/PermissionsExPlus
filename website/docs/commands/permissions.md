---
title: Permission Commands
description: How to assign, remove, and check permission nodes in PermissionsExPlus.
slug: /commands/permissions
---

Permission **nodes** are strings like `essentials.fly` that plugins check. This page covers assigning them — see [How Permissions Work](/concepts/permissions/) for resolution rules.

PEX registers **`modern`** (default) or **`classic`** command trees. Examples below use **modern** syntax; classic equivalents are in [Command mapping](/commands/command-mapping).

**Context flags (modern):** use `--world <world>` instead of a trailing world argument.

---

## Assign to a group (recommended)

```text
/pex group <group> permissions add <permission> [--world <world>]
```

Grants a node to every member of the group.

```text
/pex group default permissions add modifyworld
/pex group vip permissions add essentials.fly
/pex group vip permissions add essentials.hat
/pex group admin permissions add permissions.*
/pex group admin permissions add '*'
/pex group builder permissions add worldedit.* --world world_creative
```

**Why groups?** One command updates every member. Easier to audit and maintain.

Classic: `/pex group <group> add <permission> [world]`

---

## Assign to a user (exceptions)

```text
/pex user <user> permissions add <permission> [--world <world>]
```

Direct assignment overrides or supplements group permissions.

```text
/pex user Steve permissions add essentials.home
/pex user Steve permissions add -essentials.ban
/pex user Alex permissions add essentials.fly --world world_nether
```

Use for per-player exceptions, not your main permission structure.

Classic: `/pex user <user> add <permission> [world]`

---

## Remove permissions

```text
/pex group <group> permissions remove <permission> [--world <world>]
/pex user <user> permissions remove <permission> [--world <world>]
```

```text
/pex group vip permissions remove essentials.hat
/pex user Steve permissions remove essentials.home
```

Removing from a group affects all members. Removing from a user only affects that player.

---

## Temporary permissions

```text
/pex user <user> permissions timed add <permission> <duration> [--world <world>]
/pex group <group> permissions timed add <permission> <duration> [--world <world>]
```

| Unit | Example |
|------|---------|
| Seconds | `30s` |
| Minutes | `15m` |
| Hours | `2h` |
| Days | `7d`, `30d` |

```text
/pex user Steve permissions timed add essentials.fly 7d
/pex user Trial permissions timed add essentials.fly 1h
/pex group weekend permissions timed add essentials.kit 2d
```

Remove early:

```text
/pex user Steve permissions timed remove essentials.fly
```

---

## Check permissions

```text
/pex user <user> permissions check <permission> [--world <world>]
/pex user <user> permissions list [--world <world>]
/pex group <group> permissions list [--world <world>]
/pex user <user> permissions trace <permission> [--world <world>]
/pex hierarchy [--world <world>]
```

```text
/pex user Steve permissions check essentials.fly
/pex user Steve permissions check essentials.fly --world world_nether
/pex user Steve permissions list
/pex user Steve permissions trace essentials.fly
/pex hierarchy --world world_nether
```

`trace` is **modern only**.

---

## Swap permissions (classic only)

```text
/pex group <group> swap <permission> <targetPermission> [world]
```

Replaces one node with another in a single step. Not available in the modern framework — use remove + add instead.

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
/pex group default permissions add modifyworld
/pex group default permissions add essentials.help
/pex group default permissions add essentials.list
```

**VIP package:**
```text
/pex group vip permissions add essentials.fly
/pex group vip permissions add essentials.hat
/pex group vip permissions add essentials.feed
/pex group vip permissions add essentials.sethome.multiple
```

**Moderator:**
```text
/pex group mod permissions add essentials.kick
/pex group mod permissions add essentials.mute
/pex group mod permissions add essentials.tp
```

**Admin:**
```text
/pex group admin permissions add permissions.*
/pex group admin permissions add '*'
```

---

## World-scoped permissions

Modern: append `--world <world>`. Classic: trailing world argument. See [Context](/concepts/context/).

```text
/pex group vip permissions add essentials.fly
/pex group vip permissions add essentials.godmode --world world_nether
```

---

## Special node

| Node | Effect |
|------|--------|
| `permissionsex.disabled` | Disables regex matching for that player |

```text
/pex user Griefer permissions add permissionsex.disabled
```

Classic: `/pex user Griefer add permissionsex.disabled`
