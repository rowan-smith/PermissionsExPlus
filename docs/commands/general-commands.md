---
sidebar_position: 1.5
---

# General Commands

General administrative and utility commands.

## Plugin Management

- `/pex reload`: Reload the plugin configuration, translations, and backends.
- `/pex version`: Show version information and active adapters.
- `/pex debug`: Toggle debug mode for permission resolution logging.
- `/pex help`: Show available commands.

## Backend Management

- `/pex backend`: Show current backend status.
- `/pex backend list`: List all loaded backends.
- `/pex backend info`: Show detailed backend information.
- `/pex backend switch <backend>`: Switch the active backend.

## Importing

- `/pex import permissionsex [folder]`: Import data from an original PermissionsEx
  installation. `folder` defaults to the `PermissionsEx` folder beside this plugin's
  own. See [Migrating from PermissionsEx](../guides/migrating-from-pex-1.md).
