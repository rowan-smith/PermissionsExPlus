---
title: Group Commands
description: Create and manage permission groups with /pex group commands.
slug: /commands/groups
---

Groups bundle permissions together. Players inherit a group's permissions when they belong to it. See [Inheritance](/concepts/inheritance/) for parent chains.

PEX registers **`modern`** (default) or **`classic`** command trees. This page documents **modern syntax first**; classic equivalents are in [Command mapping — groups](/commands/command-mapping#group-permissions).

**Context flags (modern):** append `--world <world>` or `--server <name>` where applicable.

---

## `/pex groups list`

**Syntax:** `/pex groups list [--world <world>]`

Lists all defined groups. Add `--world` for world-scoped listing.

```text
/pex groups list
/pex groups list --world world_nether
```

Classic: `/pex groups list [world]`

---

## `/pex group <group>` / `info`

**Syntax:** `/pex group <group>` · `/pex group <group> info`

Shows group details: parents, weight, prefix, member count.

```text
/pex group admin
/pex group vip info
```

---

## `/pex group <group> create`

**Syntax:** `/pex group <group> create [parents...]`

Creates a new group. Optionally specify parent groups for [inheritance](/concepts/inheritance/).

```text
/pex group vip create
/pex group vip create default
/pex group admin create vip
/pex group builder create default creative
```

Multiple parents: `/pex group special create default vip`

---

## `/pex group <group> delete`

**Syntax:** `/pex group <group> delete`

Deletes the group. Users in this group are **not** deleted but lose this membership.

```text
/pex group trial delete
```

> You cannot delete the [default group](/faq/default-groups/) while it is still marked as default.

---

## Permissions

**Syntax:**

```text
/pex group <group> permissions list [--world <world>]
/pex group <group> permissions add <permission> [--world <world>]
/pex group <group> permissions remove <permission> [--world <world>]
/pex group <group> permissions check <permission> [--world <world>]
/pex group <group> permissions trace <permission> [--world <world>]
/pex group <group> permissions timed list [--world <world>]
/pex group <group> permissions timed add <permission> <duration> [--world <world>]
/pex group <group> permissions timed remove <permission> [--world <world>]
```

```text
/pex group vip permissions add essentials.fly
/pex group admin permissions add permissions.*
/pex group builder permissions add worldedit.* --world world_creative
/pex group vip permissions remove essentials.fly
/pex group vip permissions check essentials.fly
/pex group event permissions timed add modifyworld.* 4h
```

Classic: `/pex group <group> list|add|remove|timed … [world]`

---

## Options (weight, prefix, suffix)

**Syntax:**

```text
/pex group <group> options list [--world <world>]
/pex group <group> options get <option> [--world <world>]
/pex group <group> options set <option> <value> [--world <world>]
/pex group <group> options unset <option> [--world <world>]
```

```text
/pex group admin options set weight 100
/pex group vip options set weight 10
/pex group admin options set prefix "&c[Admin]"
/pex group vip options set prefix "&6[VIP]"
/pex group staff options set display "Staff Team"
```

Classic: `/pex group <group> weight|prefix|suffix|set … [world]`

See [Prefix & Meta](/concepts/meta/) and [Weight](/concepts/weight/).

---

## Members

**Syntax:**

```text
/pex group <group> members list
/pex group <group> members add <user> [--world <world>]
/pex group <group> members remove <user> [--world <world>]
```

```text
/pex group admin members list
/pex group admin members add Steve
/pex group admin members remove Steve
```

Classic: `/pex group <group> users` · `/pex group <group> user add <user>`

---

## `/pex group <group> parents`

**Syntax:** `/pex group <group> parents [subcommand] [world]`

Manage [inheritance](/concepts/inheritance/) parents.

| Subcommand | Syntax | Action |
|------------|--------|--------|
| (none) | `parents` | Show current parents |
| `list` | `parents list` | List parents |
| `add` | `parents add <parents...>` | Add parent(s) |
| `set` | `parents set <parents...>` | Replace all parents |
| `remove` | `parents remove <parents...>` | Remove parent(s) |

```text
/pex group vip parents
/pex group vip parents list
/pex group vip parents add default
/pex group admin parents set vip
/pex group builder parents remove creative
```

---

## Rank ladders

Modern framework manages ladder membership via [Rank commands](/commands/ranks):

```text
/pex ladder staff groups add trainee
/pex ladder staff groups move helper 2
/pex ladder staff promote Steve
```

Classic shortcut: `/pex group <group> rank [rank] [ladder]`

---

## Full setup example (modern)

```text
/pex group default create
/pex group default options set weight 0
/pex group default permissions add modifyworld

/pex group member create default
/pex group member options set weight 10
/pex group member permissions add essentials.sethome

/pex group admin create member
/pex group admin options set weight 100
/pex group admin permissions add permissions.*
/pex group admin permissions add '*'
/pex group admin options set prefix "&c[Admin]"

/pex user Steve groups set admin
```
