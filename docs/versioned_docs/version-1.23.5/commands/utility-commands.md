---
sidebar_position: 6
---

# Utility Commands

## `/pex`

Display the plugin version. Running `/pex` with no arguments shows the version banner.

## `/pex reload`

Reload the permissions environment from storage.

**Permission:** `permissions.manage.reload`

## `/pex report`

Generate an issue template URL to report a bug.

**Permission:** `permissions.manage.reportbug`

## `/pex config <node> [value]`

Print or set a configuration node.

**Permission:** `permissions.manage.config`

**Arguments:**
- `<node>`: configuration path (e.g. `permissions.basedir`)
- `[value]`: new value (omit to view current)

## `/pex backend`

Print the currently active storage backend.

**Permission:** `permissions.manage.backend`

## `/pex backend <backend>`

Switch to a different permission backend at runtime.

**Permission:** `permissions.manage.backend`

:::caution
Changing backends at runtime discards any unsaved changes in the current backend. Use with caution.
:::

**Arguments:**
- `<backend>`: backend name (tab-completes: `file`, `sql`, `h2`, `memory`, `multi`)

## `/pex hierarchy [world]`

Print the complete user/group inheritance hierarchy.

**Permission:** `permissions.manage.users`

## `/pex import <backend>`

Import data from another backend into the currently active backend.

:::caution
This can overwrite existing data in the active backend. Back up your data first.
:::

**Permission:** `permissions.dump`

**Arguments:**
- `<backend>`: backend to import from

## `/pex convert uuid [force]`

Bulk-convert user data from name-based to UUID-based storage.

:::caution
Only use `force` on offline-mode servers. UUIDs may change if a player's username changes.
:::

**Permission:** `permissions.convert`

**Arguments:**
- `[force]`: use `force` to convert on offline-mode servers (tab-completes)

## `/pex toggle debug`

Enable or disable global debug mode.

**Permission:** `permissions.debug`

## `/pex help [page] [count]`

Display the command help listing.

**Permission:** `permissions.manage`

**Arguments:**
- `[page]`: page number
- `[count]`: commands per page

## `/pex version`

Display the PermissionsExPlus version.

**Permission:** `permissions.manage`
