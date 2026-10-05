# Configuration Overview

PermissionsExPlus stores its main configuration in `config.yml` located in `plugins/PermissionsEx/`. The default configuration is created automatically on first run, you only need to edit it if you want to change backends or adjust default settings.

## Configuration Files

| File | Purpose |
|------|---------|
| `config.yml` | Core plugin settings: backend, debug mode, operator bypass, and logging |
| `permissions.yml` | User and group data: permissions, inheritance, prefixes, and options |

## Guides

- [Configuration File](config-file): core settings like storage backend, debug mode, operator bypass, and logging. Edit directly or use `/pex config` to modify values at runtime.

- [Storage Backends](storage): choose between file, H2, SQL, memory, or multi-backend storage. Includes connection settings for SQL databases and instructions for switching backends at runtime.

- [Permissions File](permissions): the data structure for users and groups. Covers permissions, inheritance, prefixes, suffixes, world scoping, and rank assignments.

- [Permission Nodes](../concepts-guides/permission-nodes): dot-separated identifiers that control what players can do. Each plugin defines its own nodes.

- [Negation](../concepts-guides/negation): deny specific permissions with the `-` prefix, even from wildcards or inheritance.

- [Wildcards](../concepts-guides/wildcards): match multiple permission nodes at once with `*`. Covers matching behavior and combining with negation.

- [Timed Permissions](../concepts-guides/timed-permissions): grant permissions that automatically expire after a set duration.

- [Default Groups](../concepts-guides/default-groups): automatically assign new players to specific groups on first join.

- [PlaceholderAPI](../integrations/placeholder-api/overview): placeholder expansion for chat plugins, scoreboards, and other PlaceholderAPI-supported integrations.
