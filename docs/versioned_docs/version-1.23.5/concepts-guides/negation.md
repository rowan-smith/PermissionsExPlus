---
sidebar_position: 2
---

# Negation

Negation lets you deny a specific permission even if it would otherwise be granted by a wildcard or inheritance. Prefix any permission node with `-` to deny it.

## Syntax

```yaml
permissions:
  - essentials.*            # Grant all essentials permissions
  - -essentials.troll       # But deny this one
```

:::note
The `-` prefix overrides positive grants. This works at any level, users, groups, and world-specific sections.
:::

## How It Works

When a player checks for a permission, PermissionsExPlus evaluates all their nodes (from direct assignments, group inheritance, and world-specific overrides). Negated nodes take precedence over positive ones.

```yaml
groups:
  moderator:
    permissions:
      - essentials.*
      - -essentials.ban    # Moderators can't ban
      - -essentials.banip  # Or ban by IP
```

## Common Use Cases

**Remove a single node from a wildcard:**

```yaml
permissions:
  - worldedit.*
  - -worldedit.gen            # Allow all WorldEdit except terrain generation
```

**Override an inherited permission:**

```yaml
groups:
  default:
    permissions:
      - essentials.fly

  vip:
    inheritance:
      - default
    permissions:
      - -essentials.fly      # VIPs don't get fly from default
      - essentials.fly.*     # Instead, give them full fly control
```

**Deny in a specific world only:**

```yaml
worlds:
  creative:
    permissions:
      - -essentials.tp           # No teleporting in creative
```
