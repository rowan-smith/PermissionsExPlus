---
sidebar_position: 2
---

# Getting Started

## Installation

1. Download the latest `PermissionsExPlus.jar` from the [releases page](https://github.com/rowan-smith/PermissionsExPlus/releases).
2. Place the jar file in your server's `plugins/` directory.
3. Restart your server (or reload if your plugin manager supports it).
4. Configure groups, users, and permissions using commands or the configuration file.

## First Steps

### Grant yourself access

:::tip
Replace `<yourname>` with your actual Minecraft username.
:::

```bash
/pex user <yourname> group add admin
```

### Create a group

```bash
/pex group admin create
/pex group admin add '*'
/pex group admin set option prefix '[&cAdmin&r] '
```

:::caution
The `'*'` wildcard grants every permission. Only use it on trusted groups like admin.
:::

### Assign a user to a group

```bash
/pex user Steve group set admin
```

### Check permissions

```bash
/pex user Steve check essentials.home
```

## Basic Workflow

1. **Create groups** with `/pex group <name> create`
2. **Set permissions** on groups with `/pex group <name> add <permission>`
3. **Assign users** to groups with `/pex user <name> group set <group>`
4. **Set prefixes/suffixes** for chat formatting
5. **Build inheritance** with `/pex group <name> parents set <parentGroups>`

## Next Steps

- [Common Setups](../common-setups/): ready-made configurations for survival, creative, network, and rank ladder servers
