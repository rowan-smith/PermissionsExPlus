---
title: User Commands
description: Manage player permissions and group membership with /pex user commands.
slug: /commands/users
---

Manage individual players. Replace `<user>` with a **player name** or **UUID**.

PEX registers **`modern`** (default) or **`classic`** command trees. This page documents **modern syntax first**; classic equivalents are in [Command mapping — users](/commands/command-mapping#user-permissions).

**Context flags (modern):** append `--world <world>` or `--server <name>` instead of a trailing world argument.

---

## `/pex users list`

**Syntax:** `/pex users list`

Lists every user record stored in the backend.

```text
/pex users list
```

---

## `/pex user <user>` / `info`

**Syntax:** `/pex user <user>` · `/pex user <user> info`

Shows a summary: groups, prefix, suffix, and key options.

```text
/pex user Steve
/pex user Steve info
/pex user 069a79f4-44e9-4726-a5be-fca90e38aaf5
```

---

## Permissions

### List

**Syntax:** `/pex user <user> permissions list [--world <world>]`

```text
/pex user Steve permissions list
/pex user Steve permissions list --world world_nether
```

Classic: `/pex user Steve list [world]`

### Add / remove

**Syntax:**

```text
/pex user <user> permissions add <permission> [--world <world>]
/pex user <user> permissions remove <permission> [--world <world>]
```

```text
/pex user Steve permissions add essentials.home
/pex user Steve permissions add essentials.fly --world world_nether
/pex user Steve permissions add -essentials.ban
/pex user Steve permissions remove essentials.home
```

Negation: prefix with `-` to explicitly deny. Removing a direct permission does not affect permissions inherited from groups.

Classic: `/pex user Steve add|remove <permission> [world]`

### Check / trace

**Syntax:**

```text
/pex user <user> permissions check <permission> [--world <world>]
/pex user <user> permissions trace <permission> [--world <world>]
```

```text
/pex user Steve permissions check essentials.fly
/pex user Steve permissions check essentials.fly --world world_nether
/pex user Steve permissions trace essentials.fly
```

`trace` is **modern only** — shows how PEX resolved the node.

Classic check: `/pex user Steve check <permission> [world]`

### Timed permissions

**Syntax:**

```text
/pex user <user> permissions timed list [--world <world>]
/pex user <user> permissions timed add <permission> <duration> [--world <world>]
/pex user <user> permissions timed remove <permission> [--world <world>]
```

| Duration | Meaning |
|----------|---------|
| `30s` | 30 seconds |
| `5m` | 5 minutes |
| `2h` | 2 hours |
| `7d` | 7 days |

```text
/pex user Steve permissions timed add essentials.fly 7d
/pex user Steve permissions timed add essentials.god 30m --world world_nether
/pex user Steve permissions timed remove essentials.fly
```

Classic: `/pex user Steve timed add|remove <permission> … [world]`

---

## Groups

### List / add / remove / set

**Syntax:**

```text
/pex user <user> groups list [--world <world>]
/pex user <user> groups add <group> [--world <world>]
/pex user <user> groups remove <group> [--world <world>]
/pex user <user> groups set <group> [--world <world>]
```

```text
/pex user Steve groups list
/pex user Steve groups add vip
/pex user Steve groups add vip --world world_nether
/pex user Steve groups set admin
/pex user Steve groups remove vip
```

`groups set` **replaces** all memberships with a single group — the most common way to assign a rank.

Classic: `/pex user Steve group list|add|remove|set … [world]`

### Timed membership

**Syntax:**

```text
/pex user <user> groups timed list [--world <world>]
/pex user <user> groups timed add <group> <duration> [--world <world>]
/pex user <user> groups timed remove <group> [--world <world>]
```

```text
/pex user Steve groups timed add trial 7d
/pex user Steve groups timed remove trial
```

Classic: `/pex user Steve group add <group> [world] [lifetime]`

---

## Options (prefix, suffix, meta)

**Syntax:**

```text
/pex user <user> options list [--world <world>]
/pex user <user> options get <option> [--world <world>]
/pex user <user> options set <option> <value> [--world <world>]
/pex user <user> options unset <option> [--world <world>]
```

```text
/pex user Steve options list
/pex user Steve options set nickname "Big S"
/pex user Steve options get nickname
/pex user Steve options set prefix "&b[Builder]"
/pex user Steve options set suffix "&7"
/pex user Steve options unset nickname
```

Classic prefix/suffix shortcuts: `/pex user Steve prefix|suffix [value] [world]` · options: `/pex user Steve set|get <option> …`

See [Prefix & Meta](/concepts/meta/).

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

## Classic-only commands

These commands are available when `command-framework: classic`:

| Command | Purpose |
|---------|---------|
| `/pex user <user> toggle debug` | Per-player permission debug logging |
| `/pex user <user> superperms` | Bukkit superperms attachment state (Spigot/Paper) |

Use `/pex debug on` (modern) for server-wide debug instead of per-user toggle.
