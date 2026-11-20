---
sidebar_position: 2
---

# Creative Server

A creative server with a survival lobby and separate creative worlds. Players get basic permissions everywhere, but creative-specific commands only work in creative worlds.

## Groups

| Group | Weight | Prefix | Purpose |
|-------|--------|--------|---------|
| `default` | 0 | - | All players, lobby and survival |
| `builder` | 10 | `[&bBuilder&r] ` | Creative world builders |
| `moderator` | 50 | `[&9Mod&r] ` | Staff |
| `admin` | 100 | `[&cAdmin&r] ` | Full access |

## Commands

```bash
/pex group default create
/pex group default weight 0
/pex group default add essentials.spawn
/pex group default add essentials.tpaccept
/pex group default add essentials.balance

/pex group builder create
/pex group builder weight 10
/pex group builder parents set default
/pex group builder add essentials.fly
/pex group builder add worldedit.*
/pex group builder set option prefix '[&bBuilder&r] '

/pex group moderator create
/pex group moderator weight 50
/pex group moderator parents set builder
/pex group moderator add essentials.ban
/pex group moderator add essentials.kick
/pex group moderator add essentials.teleport
/pex group moderator set option prefix '[&9Mod&r] '

/pex group admin create
/pex group admin weight 100
/pex group admin parents set moderator
/pex group admin add '*'
/pex group admin set option prefix '[&cAdmin&r] '

/pex set default group default true
```

## permissions.yml

```yaml
groups:
  default:
    options:
      default: true
      weight: 0
    permissions:
      - essentials.spawn
      - essentials.tpaccept
      - essentials.balance
      - essentials.pay
    worlds:
      creative_1:
        permissions:
          - essentials.fly
          - essentials.gamemode
      creative_2:
        permissions:
          - essentials.fly
          - essentials.gamemode

  builder:
    inheritance:
      - default
    permissions:
      - worldedit.selection.*
      - worldedit.set.*
      - worldedit.copy
      - worldedit.paste
      - worldedit.undo
      - worldedit.redo
    options:
      weight: 10
      prefix: "[&bBuilder&r] "
    worlds:
      creative_1:
        permissions:
          - worldedit.*
      creative_2:
        permissions:
          - worldedit.*

  moderator:
    inheritance:
      - builder
    permissions:
      - essentials.ban
      - essentials.kick
      - essentials.teleport
      - essentials.tphere
      - essentials.warp
    options:
      weight: 50
      prefix: "[&9Mod&r] "

  admin:
    inheritance:
      - moderator
    permissions:
      - "*"
    options:
      weight: 100
      prefix: "[&cAdmin&r] "
```

## What This Gives You

- **default**: spawn, teleport accept, economy basics in lobby/survival; flight and gamemode in creative worlds
- **builder**: WorldEdit tools in creative worlds only
- **moderator**: staff tools everywhere
- **admin**: everything via wildcard

:::note
Creative worlds are defined by their world name in the `worlds` section. Replace `creative_1` and `creative_2` with your actual world names.
:::
