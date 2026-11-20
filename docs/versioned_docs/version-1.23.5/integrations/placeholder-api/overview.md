---
sidebar_position: 2
---

# Placeholder API

PermissionsExPlus integrates with [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) to expose core user and group information to any PlaceholderAPI-enabled plugin. The expansion is registered automatically when PlaceholderAPI is present on the server.

## Features

- Registers a PermissionsExPlus expansion for PlaceholderAPI
- Exposes user and group placeholders for chat, scoreboards, tab lists, and other integrations
- Supports world-specific context, arbitrary user queries, and option/permission lookups

## Installation

1. Install [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) on your server.
2. Install PermissionsExPlus (PlaceholderAPI support is built-in).
3. Restart the server.

No additional configuration is required; the expansion will be available to any PlaceholderAPI-compatible plugin.


This section contains dedicated pages for the most commonly used placeholders:

- [User Placeholders](user-placeholders): identity, groups, options, permissions
- [Group Placeholders](group-placeholders): direct and effective group metadata and permission checks

See the subpages for full lists and examples.

:::caution
Performance: avoid expensive placeholders (for example, full permission or member lists) in high-frequency contexts such as scoreboards or tab list updates. Prefer counts or cached values.
:::

:::note
Unknown placeholders return `null`: PlaceholderAPI returns `null` for unsupported placeholders (not an empty string). Include fallback logic in templates where appropriate.
:::

:::tip
Registration: the expansion registers on startup when PlaceholderAPI is present. If the expansion is not available after installation, restart the server (do not rely on `/reload`).
:::