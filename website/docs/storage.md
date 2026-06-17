---
title: Storage
description: Where PermissionsExPlus stores config and permission data.
slug: /storage
---

PermissionsExPlus stores its data in a folder on your server. By default this is:

```text
plugins/PermissionsEx/
├── config.yml
├── permissions.mv.db          # H2 database (default h2 backend)
└── permissions.yml.migrated   # present after YAML auto-migration
```

## config.yml

Plugin settings — which backend to use, command syntax, debug mode, and general behaviour. See [Configuration](/configuration/).

## Permission data (h2 backend)

By default, groups, users, and permissions live in an **H2 file database** at `plugins/PermissionsEx/permissions.mv.db` (configured via `permissions.backends.h2.database`). Manage data with `/pex` commands — you do not edit the database file by hand.

### Inspecting the H2 database (DataGrip / JDBC tools)

Current PermissionsExPlus builds embed **H2 2.3.232** at the standard `org.h2` package so external tools can open `permissions.mv.db`.

1. **Stop the Minecraft server** — H2 file databases must not be opened while the server is running.
2. Copy `plugins/PermissionsEx/` to a local folder (avoid cloud-synced paths such as iCloud while inspecting).
3. In DataGrip, add an H2 data source:
   - **Driver:** H2 **2.3.232** (download from [h2database.com](https://h2database.com/) if DataGrip’s bundled version differs)
   - **Driver class:** `org.h2.Driver`
   - **URL:** `jdbc:h2:file:/path/to/permissions;MODE=MySQL;DATABASE_TO_LOWER=TRUE;ACCESS_MODE_DATA=r`
   - **User / password:** leave empty

Use the database **base name** (`permissions`), not `permissions.mv.db`. `ACCESS_MODE_DATA=r` opens read-only so inspection cannot corrupt live data.

Prefer `/pex backend export` for backups and audits — it does not require external tools.

> **Upgrading from older builds:** databases created before H2 was unshaded store internal metadata under `ru.tehkode.libs.org.h2`. Those files cannot be opened with standard `org.h2` drivers. Export with `/pex backend export` on the old jar, remove `permissions.mv.db`, upgrade, then import the YAML via a `file` backend alias.

### YAML auto-migration

If `permissions.yml` is present on first startup with the default **`h2`** backend, PEX imports it into H2 automatically and renames the original file to **`permissions.yml.migrated`**. This is a one-way migration; ongoing changes are stored in the database.

## Backends

PEX can store data in different ways:

| Backend | Best for |
|---------|----------|
| **h2** (default) | Single servers — embedded H2 file database |
| **sql** | Networks sharing MySQL, PostgreSQL, or SQLite |
| **memory** | Testing only — data is lost on restart |
| **file** | YAML import only via `/pex import file` — not for day-to-day storage |

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
/pex import file
```

(Modern command framework: `/pex backend import file`.)

Export a YAML snapshot of the active backend (modern framework):

```text
/pex backend export
```

See [Import & Export](/guides/import-export) for backup workflows, YAML migration, and moving data between H2 and SQL.

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
