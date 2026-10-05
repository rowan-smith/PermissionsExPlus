---
sidebar_position: 2
---

# User Commands

## `/pex users list`

List all registered users.

**Permission:** `permissions.manage.users`

## `/pex user <user>`

List a user's permissions, groups, and options.

**Permission:** `permissions.manage.users.permissions.<user>`

**Arguments:**
- `<user>`: player name or UUID

## `/pex user <user> list [world]`

List a user's permissions in a specific world. If no world is specified, all permissions are shown.

**Permission:** `permissions.manage.users.permissions.<user>`

**Arguments:**
- `<user>`: player name or UUID
- `[world]`: world to filter by (tab-completes)

## `/pex user <user> superperms`

List the user's effective Bukkit SuperPerms, including permissions granted by all plugins.

**Permission:** `permissions.manage.users.permissions.<user>`

## `/pex user <user> prefix [newprefix] [world]`

Get or set a user's chat prefix.

**Permission:** `permissions.manage.users.prefix.<user>`

**Arguments:**
- `<user>`: player name or UUID
- `[newprefix]`: new prefix text (omit to view current)
- `[world]`: world to scope the prefix to

## `/pex user <user> suffix [newsuffix] [world]`

Get or set a user's chat suffix.

**Permission:** `permissions.manage.users.suffix.<user>`

## `/pex user <user> toggle debug`

Toggle debug mode for a specific user.

**Permission:** `permissions.manage.<user>`

## `/pex user <user> check <permission> [world]`

Check whether a user has a specific permission. Tab-complete shows the user's own permissions for this argument.

**Permission:** `permissions.manage.<user>`

**Arguments:**
- `<user>`: player name or UUID
- `<permission>`: permission node to check (tab-completes from user's own permissions)
- `[world]`: world to check in

## `/pex user <user> get <option> [world]`

Get the value of a user option.

**Permission:** `permissions.manage.<user>`

## `/pex user <user> delete`

Remove a user from the permissions database.

:::caution
This permanently removes the user's data. They will be recreated on next login with default permissions.
:::

**Permission:** `permissions.manage.users.<user>`

## `/pex user <user> add <permission> [world]`

Add a permission to a user. Tab-complete shows all server-registered permissions the user doesn't already have.

**Permission:** `permissions.manage.users.permissions.<user>`

**Arguments:**
- `<user>`: player name or UUID
- `<permission>`: permission node to add (tab-completes from server permissions excluding owned)
- `[world]`: world to scope the permission to

## `/pex user <user> remove <permission> [world]`

Remove a permission from a user. Tab-complete shows the user's own permissions.

**Permission:** `permissions.manage.users.permissions.<user>`

**Arguments:**
- `<user>`: player name or UUID
- `<permission>`: permission node to remove (tab-completes from user's own permissions)
- `[world]`: world to remove from

## `/pex user <user> swap <permission> <targetPermission> [world]`

Swap the position of two permissions in a user's permission list.

:::note
Permission order matters for priority. Use this to reorder when permissions conflict.
:::

**Permission:** `permissions.manage.users.permissions.<user>`

**Arguments:**
- `<user>`: player name or UUID
- `<permission>`: source permission or index number
- `<targetPermission>`: target permission or index number
- `[world]`: world

## `/pex user <user> timed add <permission> [lifetime] [world]`

Add a timed permission that automatically expires after the specified lifetime.

**Permission:** `permissions.manage.users.permissions.timed.<user>`

**Arguments:**
- `<user>`: player name or UUID
- `<permission>`: permission node
- `[lifetime]`: duration (e.g. `10m`, `1h`, `1d`)
- `[world]`: world

## `/pex user <user> timed remove <permission> [world]`

Remove a timed permission.

**Permission:** `permissions.manage.users.permissions.timed.<user>`

## `/pex user <user> set <option> <value> [world]`

Set a user option. Common options include `prefix`, `suffix`, and custom options for chat plugins.

**Permission:** `permissions.manage.users.permissions.<user>`

**Arguments:**
- `<user>`: player name or UUID
- `<option>`: option name
- `<value>`: option value
- `[world]`: world

## `/pex user <user> group list [world]`

List all groups a user is a member of.

**Permission:** `permissions.manage.membership.<user>`

## `/pex user <user> group add <group> [world] [lifetime]`

Add a user to a group. An optional lifetime can be specified for temporary membership.

**Permission:** `permissions.manage.membership.<group>`

**Arguments:**
- `<user>`: player name or UUID
- `<group>`: group name (tab-completes)
- `[world]`: world
- `[lifetime]`: membership duration (e.g. `10m`, `1h`, `1d`)

## `/pex user <user> group set <group> [world]`

Replace a user's current groups with a single group (or comma-separated list).

**Permission:** `permissions.manage.membership.<group>`

## `/pex user <user> group remove <group> [world]`

Remove a user from a group.

**Permission:** `permissions.manage.membership.<group>`

## `/pex users cleanup <group> [threshold]`

Remove users who haven't logged in within the threshold (default 30 days) and are members of the specified group.

**Permission:** `permissions.manage.users.cleanup`

**Arguments:**
- `<group>`: group to filter by
- `[threshold]`: days since last login (default: 30)
