---
layout: page
title: Commands
permalink: /commands/
---

All commands use the `/pex` prefix unless noted. World arguments are optional and scope permissions to a specific world.

## Main command

```text
/pex
```

## General commands

| Command | Description |
|---------|-------------|
| `/pex` | Display help |
| `/pex reload` | Reload environment |
| `/pex report` | Report an issue with PEX |
| `/pex config <node> [value]` | Print or set a config node |
| `/pex backend` | Print currently used backend |
| `/pex backend <backend>` | Change permission backend on the fly |
| `/pex hierarchy [world]` | Print complete user/group hierarchy |
| `/pex import <backend>` | Import data from another backend |
| `/pex convert uuid` | Bulk convert user data to UUID-based storage |
| `/pex toggle debug` | Enable or disable debug mode |
| `/pex help [page] [count]` | Show command help |

## User commands

| Command | Description |
|---------|-------------|
| `/pex users list` | List all users |
| `/pex user <user>` | Show user info |
| `/pex user <user> list [world]` | List user permissions |
| `/pex user <user> superperms` | Show superperms attachment |
| `/pex user <user> prefix [newprefix] [world]` | Get or set prefix |
| `/pex user <user> suffix [newsuffix] [world]` | Get or set suffix |
| `/pex user <user> toggle debug` | Toggle debug for user |
| `/pex user <user> check <permission> [world]` | Check a permission |
| `/pex user <user> get <option> [world]` | Get an option value |
| `/pex user <user> delete` | Delete user record |
| `/pex user <user> add <permission> [world]` | Add permission |
| `/pex user <user> remove <permission> [world]` | Remove permission |
| `/pex user <user> swap <permission> <targetPermission> [world]` | Swap permissions |
| `/pex user <user> timed add <permission> [lifetime] [world]` | Add timed permission |
| `/pex user <user> timed remove <permission> [world]` | Remove timed permission |
| `/pex user <user> set <option> <value> [world]` | Set option |
| `/pex user <user> group list [world]` | List user groups |
| `/pex user <user> group add <group> [world] [lifetime]` | Add group membership |
| `/pex user <user> group set <group> [world]` | Set primary group |
| `/pex user <user> group remove <group> [world]` | Remove group membership |
| `/pex users cleanup <group> [threshold]` | Clean up inactive users |

## Group commands

| Command | Description |
|---------|-------------|
| `/pex groups list [world]` | List all groups |
| `/pex group <group>` | Show group info |
| `/pex group <group> list [world]` | List group permissions |
| `/pex group <group> create [parents]` | Create group |
| `/pex group <group> delete` | Delete group |
| `/pex group <group> add <permission> [world]` | Add permission |
| `/pex group <group> remove <permission> [world]` | Remove permission |
| `/pex group <group> swap <permission> <targetPermission> [world]` | Swap permissions |
| `/pex group <group> set <option> <value> [world]` | Set option |
| `/pex group <group> weight [weight]` | Get or set sort weight |
| `/pex group <group> prefix [newprefix] [world]` | Get or set prefix |
| `/pex group <group> suffix [newsuffix] [world]` | Get or set suffix |
| `/pex group <group> toggle debug` | Toggle debug for group |
| `/pex group <group> timed add <permission> [lifetime] [world]` | Add timed permission |
| `/pex group <group> timed remove <permission> [world]` | Remove timed permission |
| `/pex group <group> users` | List group members |

## Parent and rank commands

| Command | Description |
|---------|-------------|
| `/pex group <group> parents [world]` | Show parent groups |
| `/pex group <group> parents list [world]` | List parent groups |
| `/pex group <group> parents set <parents> [world]` | Set parent groups |
| `/pex group <group> parents add <parents> [world]` | Add parent groups |
| `/pex group <group> parents remove <parents> [world]` | Remove parent groups |
| `/pex default group [world]` | Show default group |
| `/pex set default group <group> <value> [world]` | Set default group |
| `/pex group <group> rank [rank] [ladder]` | Get or set rank |
| `/pex promote <user> [ladder]` | Promote user on ladder |
| `/pex demote <user> [ladder]` | Demote user on ladder |

## World commands

| Command | Description |
|---------|-------------|
| `/pex worlds` | List worlds |
| `/pex world <world>` | Show world info |
| `/pex world <world> inherit <parentWorlds>` | Set world inheritance |

## Standalone commands

These commands work without the `/pex` prefix:

```text
/promote <user>   — Promotes a user to the next group on the default ladder
/demote <user>    — Demotes a user to the previous group on the default ladder
```

## Permission nodes

| Node | Effect |
|------|--------|
| `permissionsex.disabled` | Disables regex-based permission matching for players who should not have it applied |

## Common workflows

### Create an admin group

```text
/pex group admin create
/pex group admin add '*'
/pex user Steve group set admin
```

### Add a timed permission

```text
/pex user Alex timed add essentials.fly 1d
```

### Set up group inheritance

```text
/pex group moderator create default
/pex group admin create moderator
/pex group admin prefix &c[Admin]
```

### Promote along a rank ladder

```text
/pex group trainee rank 1 staff
/pex group moderator rank 2 staff
/pex group admin rank 3 staff
/pex promote Steve staff
```

See [Examples]({{ site.baseurl }}/examples/) for configuration-based setups.
