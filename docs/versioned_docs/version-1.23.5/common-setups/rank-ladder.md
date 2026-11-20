---
sidebar_position: 4
---

# Staff Rank Ladder

A staff progression system with promotion and demotion ranks. Use `/promote <player>` and `/demote <player>` to move staff members up and down the ladder.

## Groups

| Group | Rank | Weight | Prefix | Purpose |
|-------|------|--------|--------|---------|
| `owner` | 100 | 100 | `[&4Owner&r] ` | Server owner, full access |
| `admin` | 200 | 80 | `[&cAdmin&r] ` | Server administration |
| `srmod` | 300 | 70 | `[&5Sr.Mod&r] ` | Senior moderator, extra responsibilities |
| `moderator` | 400 | 60 | `[&9Mod&r] ` | Full moderator |
| `helper` | 500 | 50 | `[&bHelper&r] ` | Trial staff, basic tools |
| `default` | - | 0 | - | All players (not on ladder) |

Lower rank numbers are higher on the ladder (promote moves toward smaller numbers).

## Commands

```bash
/pex group helper create
/pex group helper rank 500 staff
/pex group helper weight 50
/pex group helper add essentials.tp
/pex group helper add essentials.tphere
/pex group helper add essentials.mute
/pex group helper add essentials.kick
/pex group helper add essentials.warn
/pex group helper add essentials.vanish
/pex group helper set option prefix '[&bHelper&r] '

/pex group moderator create
/pex group moderator rank 400 staff
/pex group moderator weight 60
/pex group moderator parents set helper
/pex group moderator add essentials.ban
/pex group moderator add essentials.banip
/pex group moderator add essentials.unban
/pex group moderator add essentials.gamemode
/pex group moderator add essentials.invsee
/pex group moderator add essentials.clearinventory
/pex group moderator set option prefix '[&9Mod&r] '

/pex group srmod create
/pex group srmod rank 300 staff
/pex group srmod weight 70
/pex group srmod parents set moderator
/pex group srmod add essentials.broadcast
/pex group srmod add essentials.warp
/pex group srmod add essentials.setwarp
/pex group srmod add essentials.kickall
/pex group srmod add worldedit.*
/pex group srmod set option prefix '[&5Sr.Mod&r] '

/pex group admin create
/pex group admin rank 200 staff
/pex group admin weight 80
/pex group admin parents set srmod
/pex group admin add essentials.ban
/pex group admin add essentials.gamemode
/pex group admin add essentials.tp
/pex group admin add worldguard.*
/pex group admin add essentials.heal
/pex group admin add essentials.feed
/pex group admin set option prefix '[&cAdmin&r] '

/pex group owner create
/pex group owner rank 100 staff
/pex group owner weight 100
/pex group owner parents set admin
/pex group owner add '*'
/pex group owner set option prefix '[&4Owner&r] '
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
      - essentials.home
      - essentials.sethome

  helper:
    permissions:
      - essentials.tp
      - essentials.tphere
      - essentials.mute
      - essentials.unmute
      - essentials.kick
      - essentials.warn
      - essentials.vanish
      - essentials.handbook
    options:
      weight: 50
      prefix: "[&bHelper&r] "
      rank: 500
      rank-ladder: staff

  moderator:
    inheritance:
      - helper
    permissions:
      - essentials.ban
      - essentials.banip
      - essentials.unban
      - essentials.gamemode
      - essentials.invsee
      - essentials.clearinventory
      - essentials.speed
      - essentials.fly
    options:
      weight: 60
      prefix: "[&9Mod&r] "
      rank: 400
      rank-ladder: staff

  srmod:
    inheritance:
      - moderator
    permissions:
      - essentials.broadcast
      - essentials.warp
      - essentials.setwarp
      - essentials.kickall
      - worldedit.selection.*
      - worldedit.set.*
      - worldedit.copy
      - worldedit.paste
      - worldedit.undo
      - worldedit.redo
    options:
      weight: 70
      prefix: "[&5Sr.Mod&r] "
      rank: 300
      rank-ladder: staff

  admin:
    inheritance:
      - srmod
    permissions:
      - worldguard.*
      - worldedit.*
      - essentials.heal
      - essentials.feed
      - essentials.kill
      - essentials.broadcast
      - essentials.setwarp
      - essentials.warp
    options:
      weight: 80
      prefix: "[&cAdmin&r] "
      rank: 200
      rank-ladder: staff

  owner:
    inheritance:
      - admin
    permissions:
      - "*"
    options:
      weight: 100
      prefix: "[&4Owner&r] "
      rank: 100
      rank-ladder: staff
```

## Using the Ladder

```bash
/pex promote Steve staff     # helper → moderator
/pex promote Steve staff     # moderator → srmod
/pex promote Steve staff     # srmod → admin
/pex promote Steve staff     # admin → owner
/pex demote Steve staff      # owner → admin
```

The `staff` argument is the ladder name. Each promote/demote moves the player one step up or down.

## What Each Rank Gets

| Rank | Key Permissions |
|------|-----------------|
| **Helper** | Teleport, mute, kick, warn, vanish |
| **Moderator** | + ban, banip, unban, gamemode, inventory inspect, speed, fly |
| **Sr.Mod** | + broadcast, warp management, WorldEdit basics |
| **Admin** | + WorldGuard, full WorldEdit, heal, feed, kill |
| **Owner** | Everything via wildcard |

## Weight vs Rank

:::note
Weight controls prefix priority and permission resolution. Rank controls position on the promotion ladder. A group can have high weight but low rank, or vice versa.
:::

In this setup:
- Weight increases with each rank so higher staff prefixes show in chat
- Rank determines promotion order, owner has the highest rank so they can't be promoted further

## Promoting New Staff

When a new staff member joins:

```bash
/pex user <name> group add helper
```

When they earn a promotion:

```bash
/pex promote <name> staff
```

When they need to be demoted:

```bash
/pex demote <name> staff
```

:::tip
Use `/pex user <name> list` to check which groups a staff member is in before promoting or demoting.
:::
