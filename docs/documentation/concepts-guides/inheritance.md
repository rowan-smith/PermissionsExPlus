---
sidebar_position: 4
---

# Inheritance

PermissionsExPlus supports multi-level group inheritance. A group can inherit permissions, options, prefixes, and suffixes from multiple parent groups.

## How Inheritance Works

When a user or group inherits from another group, they gain all permissions, options, and metadata from that group. Inheritance chains can be as deep as needed.

```text
Admin
  └─ Mod
       └─ VIP
            └─ Default
```

In this example:
- `Admin` inherits from `Mod` (and transitively from `VIP` and `Default`)
- `Mod` inherits from `VIP` (and transitively from `Default`)
- `VIP` inherits from `Default`

## Setting Inheritance

### Via commands

```bash
/pex group moderator parents set default
/pex group admin parents set moderator,vip
```

### Via permissions.yml

```yaml
groups:
  admin:
    inheritance:
      - moderator
      - vip
  moderator:
    inheritance:
      - default
  vip:
    inheritance:
      - default
  default:
    permissions:
      - essentials.help
```

## World-Specific Inheritance

Inheritance can be different per world:

```bash
/pex group admin parents set moderator world_nether
```

This means `admin` inherits from `moderator` only in `world_nether`, while using the normal inheritance in other worlds.

## Viewing Inheritance

```bash
/pex group admin parents
```

This shows the direct parents of the `admin` group.

```bash
/pex group admin
```

This shows the full inheritance tree, including inherited permissions.

## Inheritance Order

Permissions are evaluated in order:

1. User's own permissions
2. Group permissions (in group order)
3. Parent group permissions

:::note
For conflicts, the more specific permission takes precedence. If permissions conflict at the same level, the first match wins.
:::

## Non-Inheritable Permissions

:::note
Prefix a permission with `#` to make it non-inheritable:
:::

```bash
/pex group admin add #some.secret.permission
```

This permission will only apply to members of `admin` directly, not to groups that inherit from `admin`.
