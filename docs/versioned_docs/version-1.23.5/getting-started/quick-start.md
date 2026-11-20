---
sidebar_position: 3
---

# Quick Start

This guide creates a basic PermissionsExPlus setup containing:

- A `default` group for new players
- An `admin` group with full permissions
- Group inheritance
- An administrator prefix
- Your initial administrator membership

:::tip
Run these initial commands from the server console.

Replace `<yourname>` with your Minecraft username and `<permission.node>` with a permission provided by one of your installed plugins.
:::

## 1. Create the Default Group

Create a group named `default`:

```bash
/pex group default create
```

Mark it as a default group:

```bash
/pex set default group default true
```

Players joining for the first time will now be assigned to the `default` group.

## 2. Add Basic Permissions

Add the permissions that ordinary players should receive:

```bash
/pex group default add <permission.node>
```

For example, when using a plugin that defines `essentials.spawn`:

```bash
/pex group default add essentials.spawn
```

Repeat the command for each permission:

```bash
/pex group default add essentials.help
/pex group default add essentials.spawn
/pex group default add essentials.home
```

:::note
PermissionsExPlus does not create permission nodes for other plugins. Check each plugin's documentation to find the nodes it provides.
:::

## 3. Create the Admin Group

Create an administrator group:

```bash
/pex group admin create
```

Make `admin` inherit the permissions assigned to `default`:

```bash
/pex group admin parents set default
```

Grant the group access to every registered permission:

```bash
/pex group admin add '*'
```

:::caution
The `'*'` wildcard grants every permission, including permissions added by plugins in the future. Only assign it to fully trusted groups.
:::

## 4. Set an Admin Prefix

Set a prefix for members of the `admin` group:

```bash
/pex group admin prefix '&c[Admin]&r '
```

:::note
PermissionsExPlus stores and exposes prefixes, but a compatible chat or display plugin is responsible for showing them.

See the [Vault](../integrations/vault) and [PlaceholderAPI](../integrations/placeholder-api/overview) integration pages.
:::

## 5. Add Yourself to the Admin Group

Add your account to the new group:

```bash
/pex user <yourname> group add admin
```

For example:

```bash
/pex user Steve group add admin
```

Using `group add` preserves any other groups already assigned to the user.

## 6. Verify the Setup

List your groups:

```bash
/pex user <yourname> group list
```

Check whether a permission is being granted:

```bash
/pex user <yourname> check permissions.manage
```

You can also inspect the admin group:

```bash
/pex group admin
```

The group should contain:

- The `'*'` permission
- `default` as a parent
- The configured administrator prefix

## Resulting Structure

Your basic hierarchy now looks like this:

```text
admin
└── default
```

The `default` group contains normal player permissions. The `admin` group inherits those permissions and additionally receives every permission through `'*'`.

## Common Next Commands

### Add another permission

```bash
/pex group default add <permission.node>
```

### Remove a permission

```bash
/pex group default remove <permission.node>
```

### Create another group

```bash
/pex group member create
```

### Make a group inherit from another group

```bash
/pex group member parents set default
```

### Assign a player to a group

```bash
/pex user <user> group add <group>
```

### Replace a player's current groups

```bash
/pex user <user> group set <group>
```

:::caution
`group set` replaces the user's existing group assignments. Use `group add` when the user should retain their current groups.
:::

## Next Steps

- Read [Permission Nodes](../concepts-guides/permission-nodes) to understand how plugin permissions work.
- Read [Inheritance](../concepts-guides/inheritance) to build more advanced group hierarchies.
- Read [Prefix & Suffix](../concepts-guides/prefix-suffix) to configure player formatting.
- Browse the [Group Commands](../commands/group-commands) and [User Commands](../commands/user-commands) references.
- Choose a ready-made configuration from [Common Setups](../common-setups).
