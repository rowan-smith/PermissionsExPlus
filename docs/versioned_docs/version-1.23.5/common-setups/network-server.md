---
sidebar_position: 3
---

# Network Server

A network setup for BungeeCord or Velocity proxies with multiple game modes (lobby, survival, creative, minigames). Permissions are shared across all nodes, with world-specific overrides for each game mode.

## Groups

| Group | Weight | Prefix | Purpose |
|-------|--------|--------|---------|
| `default` | 0 | - | All players across the network |
| `vip` | 10 | `[&6VIP&r] ` | Donors with per-game perks |
| `moderator` | 50 | `[&9Mod&r] ` | Network-wide staff |
| `admin` | 100 | `[&cAdmin&r] ` | Server owners and developers |

## Commands

```bash
/pex group default create
/pex group default weight 0
/pex group default add essentials.spawn
/pex group default add essentials.tpaccept
/pex group default add essentials.balance
/pex group default add multiverse.core.list.worlds

/pex group vip create
/pex group vip weight 10
/pex group vip parents set default
/pex group vip add essentials.fly
/pex group vip add essentials.back
/pex group vip add essentials.feed
/pex group vip set option prefix '[&6VIP&r] '

/pex group moderator create
/pex group moderator weight 50
/pex group moderator parents set vip
/pex group moderator add essentials.ban
/pex group moderator add essentials.kick
/pex group moderator add essentials.teleport
/pex group moderator add essentials.warp
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
      - multiverse.core.list.worlds
      - multiverse.core.tp
    worlds:
      lobby:
        permissions:
          - essentials setwarp
          - essentials.warp
      survival:
        permissions:
          - essentials.home
          - essentials.sethome
          - essentials.tpa
          - essentials.tpahere
      creative:
        permissions:
          - essentials.fly
          - essentials.gamemode
      minigames:
        permissions:
          - essentials.fly

  vip:
    inheritance:
      - default
    permissions:
      - essentials.fly
      - essentials.back
      - essentials.feed
      - essentials.heal
      - essentials.kit
    options:
      weight: 10
      prefix: "[&6VIP&r] "
    worlds:
      survival:
        permissions:
          - essentials.tp
          - essentials.tphere
      creative:
        permissions:
          - worldedit.selection.*
          - worldedit.set.*

  moderator:
    inheritance:
      - vip
    permissions:
      - essentials.ban
      - essentials.banip
      - essentials.unban
      - essentials.kick
      - essentials.mute
      - essentials.teleport
      - essentials.tp
      - essentials.tphere
      - essentials.warp
      - essentials.gamemode
      - essentials.invsee
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

- **default**: lobby warps, survival homes, creative flight, minigame fly
- **vip**: flight, back on death, feed/heal, plus survival TP and basic WorldEdit in creative
- **moderator**: ban, kick, mute, teleport, gamemode, inventory inspect across all modes
- **admin**: everything via wildcard

:::note
Replace world names (`lobby`, `survival`, `creative`, `minigames`) with your actual world names. If using Multiverse, these match the world names shown in `/mv list`.
:::
