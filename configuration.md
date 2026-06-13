---
layout: page
title: Configuration
permalink: /configuration/
---

PermissionsExPlus stores data in `plugins/PermissionsEx/` by default (configurable via `basedir`).

## File layout

```
plugins/PermissionsEx/
├── config.yml          # Plugin settings and backend configuration
└── permissions.yml     # Groups, users, and permissions (file backend)
```

## `config.yml` reference

```yaml
permissions:
  debug: false
  allowOps: false
  user-add-groups-last: false
  log-players: true
  createUserRecords: true
  save-default-group: true
  backend: file
  basedir: plugins/PermissionsEx
  informplayers:
    changes: false
  backends:
    file:
      type: file
      file: permissions.yml
    sql:
      type: sql
      # driver, url, user, password — see SQL backend docs in repo
    multi:
      type: multi
      backends:
        - file
        - sql
```

## Backends

| Alias | Type | Description |
|-------|------|-------------|
| `file` | YAML | Default. Stores groups/users in `permissions.yml` |
| `memory` | In-memory | Ephemeral — useful for testing |
| `sql` | Database | MySQL/MariaDB/SQLite via JDBC |
| `multi` | Composite | Routes reads/writes across multiple backends |

Switch backends at runtime:

```text
/pex backend <alias>
```

Reload after config changes:

```text
/pex reload
```

## `permissions.yml` schema

```yaml
schema-version: 1

groups:
  <group-name>:
    default: true|false
    weight: <integer>
    prefix: '<chat prefix>'
    suffix: '<chat suffix>'
    inheritance:
      - <parent-group>
    permissions:
      - <permission.node>
    worlds:
      <world-name>:
        permissions:
          - <world-scoped.node>

users:
  <uuid-or-name>:
    group:
      - <group-name>
    permissions:
      - <permission.node>
    options:
      name: <display-name>
```

### Permission syntax

| Pattern | Meaning |
|---------|---------|
| `*` | Wildcard — grants all permissions |
| `-node.name` | Negation — explicitly denies a node |
| `regex` patterns | Regex matching (disable per-player with `permissionsex.disabled`) |

### World scoping

Permissions and group memberships can be scoped per world. World inheritance (`/pex world <world> inherit <parents>`) lets child worlds inherit parent world permissions.

## UUID storage

Modern servers should use UUID keys in `permissions.yml`:

```text
/pex convert uuid
```

This bulk-converts name-based records to UUID-based storage.

## Validation

PEX validates YAML on load. Invalid config produces clear error messages in the server log. See `PexYamlValidator` in the source for validation rules.

## Related

- [Examples]({{ site.baseurl }}/examples/) — copy-paste starter configs
- [Commands]({{ site.baseurl }}/commands/) — manage permissions in-game
- [Compatibility]({{ site.baseurl }}/compatibility/) — platform requirements
