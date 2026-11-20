---
sidebar_position: 3
---

# Wildcards

Wildcards use `*` to match multiple permission nodes at once, reducing the length of permission lists.

## Syntax

```yaml
permissions:
  - essentials.*            # All essentials permissions
  - essentials.fly.*        # All fly-related sub-permissions
  - worldedit.selection.*   # All WorldEdit selection tools
  - *                       # All permissions (use with caution)
```

A `*` at the end of a node matches everything below that level. A bare `*` grants every permission in the game.

## How Matching Works

```text
essentials.*         → matches essentials.fly, essentials.tp, essentials.home, ...
essentials.fly.*     → matches essentials.fly, essentials.fly.others, ...
vault.*              → matches vault.economy.npc, vault.chat.prefix, ...
*                    → matches everything
```

## Combining with Negation

Wildcards pair well with negation. Grant everything, then deny specific nodes:

```yaml
groups:
  admin:
    permissions:
      - essentials.*
      - -essentials.troll      # Deny one specific node
      - -essentials.creative   # Deny another
```

This is cleaner than listing every individual permission.

:::caution
- A bare `*` gives a player every permission, including any future ones added by new plugins. Use it only for trusted staff.
- Wildcards are greedy. `essentials.*` includes `essentials.tp.others`, which lets a player teleport anyone. Review what a wildcard covers before assigning it.
:::
