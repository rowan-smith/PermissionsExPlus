---
layout: page
title: Examples
permalink: /examples/
---

## Example command session

```text
/pex group admin create
/pex group admin add '*'
/pex user Steve group set admin
/pex user Alex add essentials.home
/pex group moderator prefix [Mod]
/pex promote Steve
```

## Starter `config.yml`

Place in `plugins/PermissionsEx/config.yml`:

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
```

### Config options

| Node | Default | Description |
|------|---------|-------------|
| `debug` | `false` | Enable verbose debug logging |
| `allowOps` | `false` | Grant ops automatic superuser access |
| `user-add-groups-last` | `false` | Append groups instead of prepending |
| `log-players` | `true` | Log player join/leave for permission tracking |
| `createUserRecords` | `true` | Auto-create user records on join |
| `save-default-group` | `true` | Persist default group assignments |
| `backend` | `file` | Active backend alias (`file`, `sql`, `multi`, `memory`) |
| `basedir` | `plugins/PermissionsEx` | Data directory |
| `backends.file.file` | `permissions.yml` | Permissions data file for file backend |

## Starter `permissions.yml`

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
      - essentials.mute
  admin:
    inheritance:
      - moderator
    prefix: '&c[Admin] '
    permissions:
      - permissions.*
      - '*'

users:
  069a79f4-44e9-4726-a5be-fca90e38aaf5:
    group:
      - admin
    options:
      name: Admin
```

## Multi-world setup

Use world-specific permission nodes or world inheritance:

```text
/pex world world_nether inherit world
/pex group vip add essentials.fly world_nether
```

In YAML, world-scoped data is stored under world keys in the permissions file.

## Rank ladder example

```text
/pex group trainee rank 1 staff
/pex group helper rank 2 staff
/pex group moderator rank 3 staff
/pex group admin rank 4 staff
/pex promote NewPlayer staff
```

## Timed permissions

```text
/pex user Steve timed add essentials.fly 7d
/pex user Steve group add vip 30d
```

Lifetime formats follow classic PEX conventions (e.g. `30s`, `5m`, `2h`, `7d`).

## Backend switching

```text
/pex backend
/pex backend sql
/pex import file
```

Available backends depend on your `config.yml` `backends` section. The `file` backend is the default and requires no external database.

## Hook plugin examples

Sample companion plugins ship with the repository:

| Module | API | Description |
|--------|-----|-------------|
| `permissionsex-example-plugin` | Modern | Uses `PermissionsEx.getApi()` and managers |
| `permissionsex-example-legacy-plugin` | Legacy | Uses `PermissionsEx.getPermissionManager()` and events |

See [Integrations]({{ site.baseurl }}/integrations/) and the [Modern API]({{ site.baseurl }}/modern-api/) / [Legacy API]({{ site.baseurl }}/legacy-api/) references for code samples.
