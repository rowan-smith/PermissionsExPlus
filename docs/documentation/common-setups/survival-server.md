---
sidebar_position: 1
---

# Survival Server

A standard survival setup with four groups: default players, donors, moderators, and admins. This covers most SMP and vanilla+ servers.

## Groups

| Group | Weight | Prefix | Purpose |
|-------|--------|--------|---------|
| `default` | 0 | - | All new players |
| `donor` | 10 | `[&aVIP&r] ` | Players who support the server |
| `moderator` | 50 | `[&9Mod&r] ` | Staff who handle reports and chat |
| `admin` | 100 | `[&cAdmin&r] ` | Full access, server management |

## Commands

```bash
/pex group default create
/pex group default weight 0
/pex group default add essentials.home
/pex group default add essentials.tpaccept
/pex group default add essentials.tpahere

/pex group donor create
/pex group donor weight 10
/pex group donor parents set default
/pex group donor add essentials.fly
/pex group donor add essentials.back
/pex group donor set option prefix '[&aVIP&r] '

/pex group moderator create
/pex group moderator weight 50
/pex group moderator parents set donor
/pex group moderator add essentials.ban
/pex group moderator add essentials.kick
/pex group moderator add essentials.mute
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
      - essentials.home
      - essentials.tpaccept
      - essentials.tpahere
      - essentials.spawn

  donor:
    inheritance:
      - default
    permissions:
      - essentials.fly
      - essentials.back
      - essentials.feed
      - essentials.heal
    options:
      weight: 10
      prefix: "[&aVIP&r] "

  moderator:
    inheritance:
      - donor
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

- **default**: basic survival commands (home, teleport accept, spawn)
- **donor**: flight, back on death, feed/heal
- **moderator**: ban, kick, mute, teleport, gamemode
- **admin**: everything via wildcard
