---
title: Default Groups
description: How default groups work in PermissionsExPlus.
slug: /faq/default-groups
---

Every player needs a group. The **default group** is assigned automatically to new players.

## Set the default group

```text
/pex default group
/pex set default group default true
```

Example data structure (also valid in a `permissions.yml` import file):

```yaml
groups:
  default:
    default: true
    permissions:
      - modifyworld
```

Only one group should have `default: true` per context (global or per-world).

## Per-world default

```text
/pex set default group default true world_nether
```

## What new players get

When `createUserRecords: true` in config (the default), PEX creates a user entry on first join and assigns the default group.

## Common setup

```yaml
groups:
  default:
    default: true
    permissions:
      - modifyworld
  member:
    inheritance: [default]
    permissions:
      - essentials.sethome
```

Promote players by moving them to other groups:

```text
/pex user Steve group set member
```

## FAQ

**Player has no permissions?**
Check they are in a group: `/pex user <name> group list`

**Wrong default group?**
Run `/pex default group` and verify the default group flag with `/pex group <name> info`.
