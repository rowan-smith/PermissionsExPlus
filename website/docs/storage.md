---
title: Storage
description: Where PermissionsExPlus stores config and permission data.
slug: /storage
---

PermissionsExPlus stores its data in a folder on your server. By default this is:

```text
plugins/PermissionsEx/
├── config.yml
├── permissions.mv.db          # H2 database (default local backend)
└── permissions.yml.migrated   # present after YAML auto-migration
```

## config.yml

Plugin settings — which backend to use, command syntax, debug mode, and general behaviour. See [Configuration](/configuration/).

## Permission data (local backend)

By default, groups, users, and permissions live in an **H2 file database** at `plugins/PermissionsEx/permissions.mv.db` (configured via `permissions.backends.local.database`). Manage data with `/pex` commands — you do not edit the database file by hand.

### YAML auto-migration

If `permissions.yml` is present on first startup with the default **`local`** backend, PEX imports it into H2 automatically and renames the original file to **`permissions.yml.migrated`**. This is a one-way migration; ongoing changes are stored in the database.

## Backends

PEX can store data in different ways:

| Backend | Best for |
|---------|----------|
| **local** (default) | Single servers — embedded H2 file database |
| **sql** | Networks sharing MySQL, PostgreSQL, or SQLite |
| **memory** | Testing only — data is lost on restart |
| **file** / **yaml-import** | Legacy YAML import only — **deprecated**; not for day-to-day storage |

Check your active backend:

```text
/pex backend
```

Switch backends:

```text
/pex backend sql
```

Import data from another configured backend:

```text
/pex import yaml-import
```

(Modern command framework: `/pex backend import yaml-import`.)

## UUID storage

Modern servers should use player UUIDs instead of usernames. Convert existing data:

```text
/pex convert uuid
```

## Reloading

After editing `config.yml` by hand, reload PEX:

```text
/pex reload
```

In-game `/pex` changes are saved immediately. Reload is mainly for config file edits.
