---
sidebar_position: 3
---

# Group Commands

## `/pex groups list [world]`

List all registered groups.

**Permission:** `permissions.manage.groups.list`

**Arguments:**
- `[world]`: filter by world (tab-completes)

## `/pex group <group>`

List a group's permissions, options, and inheritance.

**Permission:** `permissions.manage.groups.permissions.<group>`

**Arguments:**
- `<group>`: group name (tab-completes)

## `/pex group <group> list [world]`

List a group's permissions for a specific world.

**Permission:** `permissions.manage.groups.permissions.<group>`

## `/pex group <group> create [parents]`

Create a new group with optional parent groups (comma-separated).

**Permission:** `permissions.manage.groups.create.<group>`

**Arguments:**
- `<group>`: group name
- `[parents]`: comma-separated parent group names (tab-completes)

## `/pex group <group> delete`

Delete a group.

:::caution
Users in this group will lose its permissions. Reassign them to another group first if needed.
:::

**Permission:** `permissions.manage.groups.remove.<group>`

## `/pex group <group> add <permission> [world]`

Add a permission to a group. Tab-complete shows all server-registered permissions the group doesn't already have.

**Permission:** `permissions.manage.groups.permissions.<group>`

**Arguments:**
- `<group>`: group name (tab-completes)
- `<permission>`: permission node
- `[world]`: world

## `/pex group <group> remove <permission> [world]`

Remove a permission from a group. Tab-complete shows the group's own permissions.

**Permission:** `permissions.manage.groups.permissions.<group>`

## `/pex group <group> swap <permission> <targetPermission> [world]`

Swap the position of two permissions in a group's permission list.

**Permission:** `permissions.manage.groups.permissions.<group>`

## `/pex group <group> set <option> <value> [world]`

Set a group option.

**Permission:** `permissions.manage.groups.permissions.<group>`

**Arguments:**
- `<group>`: group name (tab-completes)
- `<option>`: option name
- `<value>`: option value
- `[world]`: world

## `/pex group <group> weight [weight]`

Display or set a group's weight. Higher weight groups take priority.

**Permission:** `permissions.manage.groups.weight.<group>`

## `/pex group <group> prefix [newprefix] [world]`

Get or set a group's chat prefix.

**Permission:** `permissions.manage.groups.prefix.<group>`

## `/pex group <group> suffix [newsuffix] [world]`

Get or set a group's chat suffix.

**Permission:** `permissions.manage.groups.suffix.<group>`

## `/pex group <group> toggle debug`

Toggle debug mode for a group.

**Permission:** `permissions.manage.groups.debug.<group>`

## `/pex group <group> timed add <permission> [lifetime] [world]`

Add a timed permission to a group.

**Permission:** `permissions.manage.groups.permissions.timed.<group>`

## `/pex group <group> timed remove <permission> [world]`

Remove a timed permission from a group.

**Permission:** `permissions.manage.groups.permissions.timed.<group>`

## `/pex group <group> users`

List all users who are members of this group.

**Permission:** `permissions.manage.membership.<group>`

## `/pex group <group> user add <user> [world]`

Add a user to this group (single or comma-separated list).

**Permission:** `permissions.manage.membership.<group>`

## `/pex group <group> user remove <user> [world]`

Remove a user from this group.

**Permission:** `permissions.manage.membership.<group>`

## Parent Commands

### `/pex group <group> parents [world]`

List parent groups for this group.

**Permission:** `permissions.manage.groups.inheritance.<group>`

### `/pex group <group> parents list [world]`

Alias for listing parents.

### `/pex group <group> parents set <parents> [world]`

Set the parent groups (comma-separated). Tab-complete shows all groups.

**Permission:** `permissions.manage.groups.inheritance.<group>`

**Arguments:**
- `<group>`: group name (tab-completes)
- `<parents>`: comma-separated parent group names (tab-completes all groups)
- `[world]`: world

### `/pex group <group> parents add <parents> [world]`

Add parent groups. Tab-complete shows groups that aren't already parents.

### `/pex group <group> parents remove <parents> [world]`

Remove parent groups. Tab-complete shows the group's current parents.

## Default Group Commands

### `/pex default group [world]`

Display the default group(s) for a world.

**Permission:** `permissions.manage.groups.inheritance`

### `/pex set default group <group> <value> [world]`

Set or unset a group as the default for a world.

**Permission:** `permissions.manage.groups.inheritance`

**Arguments:**
- `<group>`: group name (tab-completes)
- `<value>`: `true` or `false` (tab-completes)
- `[world]`: world
