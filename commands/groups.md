---
layout: default
title: Group Commands
permalink: /commands/groups/
description: Create and manage permission groups with /pex group commands.
---

Groups bundle permissions together. Players inherit a group's permissions when they belong to it. See [Inheritance]({{ site.baseurl }}/concepts/inheritance/) for parent chains.

---

## `/pex groups list`

**Syntax:** `/pex groups list [world]`

Lists all defined groups. Add a world for world-scoped listing.

```text
/pex groups list
/pex groups list world_nether
```

---

## `/pex group <group>`

**Syntax:** `/pex group <group>`

Shows group details: parents, weight, prefix, member count.

```text
/pex group admin
/pex group vip
```

---

## `/pex group <group> create`

**Syntax:** `/pex group <group> create [parents...]`

Creates a new group. Optionally specify parent groups for [inheritance]({{ site.baseurl }}/concepts/inheritance/).

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

> You cannot delete the [default group]({{ site.baseurl }}/faq/default-groups/) while it is still marked as default.

---

## `/pex group <group> list`

**Syntax:** `/pex group <group> list [world]`

Lists all permissions assigned to the group (not inherited — use `/pex hierarchy` for the full tree).

```text
/pex group admin list
/pex group vip list world_nether
```

---

## `/pex group <group> add`

**Syntax:** `/pex group <group> add <permission> [world]`

Grants a permission to the group. All members (and child groups via inheritance) receive it.

```text
/pex group vip add essentials.fly
/pex group admin add permissions.*
/pex group admin add '*'
/pex group builder add worldedit.* world_creative
/pex group muted add -chat.use
```

---

## `/pex group <group> remove`

**Syntax:** `/pex group <group> remove <permission> [world]`

Removes a permission from the group's direct list.

```text
/pex group vip remove essentials.fly
```

---

## `/pex group <group> timed add` / `timed remove`

**Syntax:** `/pex group <group> timed add <permission> <lifetime> [world]`

Temporary group-level permission. Expires for the group definition (affects all members).

```text
/pex group event timed add modifyworld.* 4h
/pex group event timed remove modifyworld.*
```

---

## `/pex group <group> weight`

**Syntax:** `/pex group <group> weight [value]`

Get or set the group's [weight]({{ site.baseurl }}/concepts/weight/) (priority for prefix resolution).

```text
/pex group admin weight
/pex group admin weight 100
/pex group vip weight 10
/pex group default weight 0
```

---

## `/pex group <group> prefix` / `suffix`

**Syntax:** `/pex group <group> prefix [newprefix] [world]`

Set or read the group's chat prefix.

```text
/pex group admin prefix &c[Admin]
/pex group vip prefix &6[VIP]
/pex group mod prefix &7[Mod] &f
```

See [Prefix & Meta]({{ site.baseurl }}/concepts/meta/).

---

## `/pex group <group> set`

**Syntax:** `/pex group <group> set <option> <value> [world]`

Set a custom option on the group.

```text
/pex group staff set display "Staff Team"
```

---

## `/pex group <group> users`

**Syntax:** `/pex group <group> users`

Lists all users who belong to this group.

```text
/pex group admin users
/pex group vip users
```

---

## `/pex group <group> parents`

**Syntax:** `/pex group <group> parents [subcommand] [world]`

Manage [inheritance]({{ site.baseurl }}/concepts/inheritance/) parents.

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

## `/pex group <group> rank`

**Syntax:** `/pex group <group> rank [rank] [ladder]`

Assign a rank number on a [rank ladder]({{ site.baseurl }}/commands/ranks/).

```text
/pex group trainee rank 1 staff
/pex group helper rank 2 staff
/pex group moderator rank 3 staff
/pex group admin rank 4 staff
```

---

## Full setup example

```text
/pex group default create
/pex group default weight 0
/pex group default add modifyworld
/pex set default group default true

/pex group member create default
/pex group member weight 10
/pex group member add essentials.sethome

/pex group admin create member
/pex group admin weight 100
/pex group admin add permissions.*
/pex group admin add '*'
/pex group admin prefix &c[Admin]

/pex user Steve group set admin
```
