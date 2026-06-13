---
layout: default
title: User Commands
permalink: /commands/users/
description: Manage player permissions and group membership with /pex user commands.
---

Manage individual players. Replace `<user>` with a **player name** or **UUID**.

---

## `/pex users list`

**Syntax:** `/pex users list`

Lists every user record stored in the backend.

```text
/pex users list
```

---

## `/pex user <user>`

**Syntax:** `/pex user <user>`

Shows a summary: groups, prefix, suffix, and key options.

```text
/pex user Steve
/pex user 069a79f4-44e9-4726-a5be-fca90e38aaf5
```

---

## `/pex user <user> list`

**Syntax:** `/pex user <user> list [world]`

Lists all effective permissions for the user. Add a world name for world-scoped view.

```text
/pex user Steve list
/pex user Steve list world_nether
```

---

## `/pex user <user> check`

**Syntax:** `/pex user <user> check <permission> [world]`

Tests whether the user has a specific permission node. Returns yes/no with the matching expression.

```text
/pex user Steve check essentials.fly
/pex user Steve check essentials.fly world_nether
/pex user Steve check permissions.user
```

---

## `/pex user <user> add`

**Syntax:** `/pex user <user> add <permission> [world]`

Grants a permission directly to the user (not via a group). Use for one-off exceptions.

```text
/pex user Steve add essentials.home
/pex user Steve add essentials.fly world_nether
/pex user Steve add -essentials.ban
```

Negation: prefix with `-` to explicitly deny.

---

## `/pex user <user> remove`

**Syntax:** `/pex user <user> remove <permission> [world]`

Removes a directly-assigned permission.

```text
/pex user Steve remove essentials.home
/pex user Steve remove essentials.fly world_nether
```

Does not remove permissions inherited from groups.

---

## `/pex user <user> timed add`

**Syntax:** `/pex user <user> timed add <permission> <lifetime> [world]`

Grants a permission that **expires automatically**.

| Lifetime | Meaning |
|----------|---------|
| `30s` | 30 seconds |
| `5m` | 5 minutes |
| `2h` | 2 hours |
| `7d` | 7 days |

```text
/pex user Steve timed add essentials.fly 7d
/pex user Steve timed add essentials.god 30m world_nether
```

---

## `/pex user <user> timed remove`

**Syntax:** `/pex user <user> timed remove <permission> [world]`

Removes an active timed permission before it expires.

```text
/pex user Steve timed remove essentials.fly
```

---

## `/pex user <user> group list`

**Syntax:** `/pex user <user> group list [world]`

Shows all groups the user belongs to.

```text
/pex user Steve group list
/pex user Steve group list world_nether
```

---

## `/pex user <user> group add`

**Syntax:** `/pex user <user> group add <group> [world] [lifetime]`

Adds the user to a group. Optional lifetime makes membership temporary.

```text
/pex user Steve group add vip
/pex user Steve group add vip world_nether
/pex user Steve group add trial 7d
```

---

## `/pex user <user> group set`

**Syntax:** `/pex user <user> group set <group> [world]`

**Replaces** all group memberships with a single group. The most common way to "rank" a player.

```text
/pex user Steve group set admin
/pex user Steve group set default
/pex user NewPlayer group set member
```

---

## `/pex user <user> group remove`

**Syntax:** `/pex user <user> group remove <group> [world]`

Removes the user from one group without affecting other memberships.

```text
/pex user Steve group remove vip
```

---

## `/pex user <user> prefix` / `suffix`

**Syntax:** `/pex user <user> prefix [newprefix] [world]`

Get or set the user's chat prefix. Omit `newprefix` to read the current value.

```text
/pex user Steve prefix
/pex user Steve prefix &b[Builder]
/pex user Steve prefix &6[VIP] world_nether
```

Suffix works the same: `/pex user <user> suffix [newsuffix] [world]`

See [Prefix & Meta]({{ site.baseurl }}/concepts/meta/).

---

## `/pex user <user> set` / `get`

**Syntax:** `/pex user <user> set <option> <value> [world]`

Set or read custom option key-value pairs.

```text
/pex user Steve set nickname "Big S"
/pex user Steve get nickname
```

---

## `/pex user <user> delete`

**Syntax:** `/pex user <user> delete`

Permanently deletes the user's record. They will be re-created on next join if `createUserRecords` is enabled.

```text
/pex user Steve delete
```

---

## `/pex users cleanup`

**Syntax:** `/pex users cleanup <group> [threshold]`

Removes user records in a group that have been inactive for the threshold (days).

```text
/pex users cleanup trial 30
```

---

## `/pex user <user> toggle debug`

**Syntax:** `/pex user <user> toggle debug`

Enables permission-resolution debug logging for one player only.

```text
/pex user Steve toggle debug
```

---

## `/pex user <user> superperms`

**Syntax:** `/pex user <user> superperms`

Shows the Bukkit superperms attachment state (Spigot/Paper only). Useful for [troubleshooting]({{ site.baseurl }}/guides/troubleshooting/).

```text
/pex user Steve superperms
```
