---
layout: default
title: Configuration
permalink: /configuration/
description: Configure PermissionsExPlus with config.yml and permissions.yml.
---

## config.yml

Place in `plugins/PermissionsEx/config.yml`:

```yaml
permissions:
  debug: false
  allowOps: false
  createUserRecords: true
  backend: file
  basedir: plugins/PermissionsEx
  backends:
    file:
      type: file
      file: permissions.yml
```

### Common options

| Option | What it does |
|--------|--------------|
| `debug` | Extra logging for troubleshooting |
| `allowOps` | Whether server ops bypass PEX |
| `createUserRecords` | Auto-create a user entry when someone joins |
| `backend` | Active storage backend (`file`, `sql`, `memory`) |
| `basedir` | Folder for config and data files |

Change a setting in-game:

```text
/pex config permissions.debug true
/pex reload
```

## permissions.yml

This file holds your groups and users:

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

### Reload after edits

```text
/pex reload
```

## Example: starter server

```yaml
groups:
  default:
    default: true
    permissions:
      - modifyworld
  vip:
    inheritance: [default]
    prefix: '&6[VIP] '
    permissions:
      - essentials.fly
```

Then assign a player:

```text
/pex user Steve group add vip
```
