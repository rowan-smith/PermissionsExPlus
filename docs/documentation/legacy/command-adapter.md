# Command Adapter

The `PermissionsExCommandAdapter` restores the original command structure of PermissionsEx 1.x.

## Purpose

For users who prefer the original PEX command syntax, this adapter provides a familiar interface while using the PermissionsExPlus engine under the hood.

## Usage

When enabled, it intercepts legacy commands and routes them through the modern API manager. This ensures that even legacy commands result in atomic, persistent changes consistent with the new model.

## PermissionsEx 1.23.6 features

- **Tab completion** for every `/pex` command: subcommands, users, groups (only the ones a user
  can be added to or removed from), ladders, worlds, permissions and options.
- **Formatted output.** Command output supports legacy `&` colour codes and
  [MiniMessage](https://docs.advntr.dev/minimessage/format.html) tags, and expands PlaceholderAPI
  placeholders for players when PlaceholderAPI is installed. This works on every supported server
  version.
- **Importing PermissionsEx data.** `/pex import permissionsex` imports an original PermissionsEx
  installation; see [Migrating from PermissionsEx](../guides/migrating-from-pex-1.md).
