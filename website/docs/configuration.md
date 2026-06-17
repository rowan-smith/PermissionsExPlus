---
title: Configuration
description: Configure PermissionsExPlus with config.yml and storage backends.
slug: /configuration
---

## config.yml

Place in `plugins/PermissionsEx/config.yml`:

```yaml
permissions:
  command-framework: modern
  debug: false
  allowOps: false
  createUserRecords: true
  backend: h2
  basedir: plugins/PermissionsEx
  backends:
    h2:
      type: h2
      database: permissions
      migration-source: permissions.yml
```

### Common options

| Option | What it does |
|--------|--------------|
| `command-framework` | Command syntax tree: `modern` (default) or `classic` / `legacy` — see [Command framework](#command-framework) |
| `debug` | Extra logging for troubleshooting |
| `allowOps` | Whether server ops bypass PEX |
| `createUserRecords` | Auto-create a user entry when someone joins |
| `backend` | Active storage backend (`h2`, `sql`, `memory`; legacy `file` is normalized to `h2`) |
| `basedir` | Folder for config and data files |

Change a setting in-game:

```text
/pex config permissions.debug true
/pex reload
```

## Command framework

Config key: `permissions.command-framework`

| Value | Syntax | Notes |
|-------|--------|-------|
| `modern` (default) | Cloud-based `/pex` subcommands with structured flags | e.g. `/pex user Steve permissions add essentials.home` |
| `classic`, `legacy`, or `old` | Original PEX command tree | e.g. `/pex user Steve add essentials.home` |

Both frameworks use the [Cloud Command Framework](https://github.com/incendo/cloud-minecraft) under the hood; only the registered command tree differs.

Check your active setting:

```text
/pex config permissions.command-framework
```

Switch to classic syntax (requires restart or reload that re-registers commands):

```yaml
permissions:
  command-framework: classic
```

See [General commands — command framework](/commands/general/#command-framework) for side-by-side examples.

## Storage backends

### h2 (default)

Embedded H2 database at `{basedir}/{database}.mv.db` — by default `plugins/PermissionsEx/permissions.mv.db`.

| Option | What it does |
|--------|--------------|
| `database` | Base filename without extension (default: `permissions`) |
| `migration-source` | YAML file to import on first startup if the database is empty (default: `permissions.yml`) |

On first startup, if `migration-source` exists and the database is empty, PEX imports the YAML and renames it to `permissions.yml.migrated`.

### sql

Shared database for proxy networks. Configure under `permissions.backends.<alias>` with `type: sql` and JDBC settings. See [Common Setups — network proxy](/guides/recipes/#network-proxy-bungee--backend).

### memory

In-memory store for testing. Data is lost on restart.

### file (import only)

YAML import backend for pulling data from `permissions.yml` via `/pex import file` (classic) or `/pex backend import file` (modern). Setting `backend: file` in config is automatically normalized to `h2` with the YAML path preserved as `migration-source`.

## Permission data

Groups and users are stored in the active backend (H2 by default). Example structure — useful when reading migrated YAML or writing import files:

```yaml
schema-version: 1

groups:
  default:
    default: true
    permissions:
      - modifyworld
  moderator:
    inheritance:
      - default
    prefix: '&7[Mod] '
    permissions:
      - essentials.kick
  admin:
    inheritance:
      - moderator
    prefix: '&c[Admin] '
    permissions:
      - '*'

users:
  069a79f4-44e9-4726-a5be-fca90e38aaf5:
    group:
      - admin
```

### Permission syntax

| Syntax | Meaning |
|--------|---------|
| `*` | All permissions |
| `-node.name` | Explicitly deny a permission |
| `permissions.*` | All PEX admin commands |

### Reload after config edits

```text
/pex reload
```

## Example: starter server

Create groups and assign a player (modern syntax):

```text
/pex group default create
/pex group default permissions add modifyworld
/pex set default group default true
/pex group vip create
/pex group vip parents add default
/pex group vip prefix &6[VIP]
/pex group vip permissions add essentials.fly
/pex user Steve groups set vip
```

Classic syntax equivalent:

```text
/pex user Steve group add vip
```

## Example files

Full starter files are in the repository [`examples/`](https://github.com/rowan-smith/PermissionsExPlus/tree/main/examples) directory:

- [`config.yml`](https://github.com/rowan-smith/PermissionsExPlus/blob/main/examples/config.yml) — plugin settings (update `backend: h2` for current defaults)
- [`permissions.yml`](https://github.com/rowan-smith/PermissionsExPlus/blob/main/examples/permissions.yml) — sample groups/users for YAML migration or import
