---
title: Common Setups
description: Copy-paste permission setups for common Minecraft server types.
slug: /guides/recipes
---

Ready-made permission structures. Adjust group names and nodes to match your plugins.

---

## Basic survival server

```text
/pex group default create
/pex group default weight 0
/pex group default add modifyworld
/pex set default group default true

/pex group member create default
/pex group member weight 10
/pex group member add essentials.sethome
/pex group member add essentials.spawn
/pex group member add essentials.tpa
```

New players land in `default`. Promote manually: `/pex user <name> group set member`

---

## VIP rank

```text
/pex group vip create default
/pex group vip weight 20
/pex group vip prefix &6[VIP]
/pex group vip add essentials.fly
/pex group vip add essentials.hat
/pex group vip add essentials.feed
/pex group vip add essentials.sethome.multiple

/pex user Steve group add vip
```

Temporary VIP:

```text
/pex user Steve group add vip 30d
```

---

## Staff hierarchy

Uses [inheritance](/concepts/inheritance/) and [rank ladders](/commands/ranks/).

```text
/pex group helper create default
/pex group helper weight 40
/pex group helper prefix &a[Helper]
/pex group helper rank 1 staff
/pex group helper add essentials.tp
/pex group helper add essentials.tp.others

/pex group moderator create helper
/pex group moderator weight 60
/pex group moderator prefix &9[Mod]
/pex group moderator rank 2 staff
/pex group moderator add essentials.kick
/pex group moderator add essentials.mute
/pex group moderator add essentials.ban

/pex group admin create moderator
/pex group admin weight 100
/pex group admin prefix &c[Admin]
/pex group admin rank 3 staff
/pex group admin add permissions.*
/pex group admin add '*'

/pex ladder staff promote NewStaff
```

---

## Creative + survival worlds

Uses [world context](/concepts/context/).

```text
/pex world world_creative inherit world

/pex group creative create default
/pex group creative add worldedit.*
/pex group creative add gamemode.creative world_creative

/pex set default group default true world
/pex set default group creative true world_creative
```

---

## Network proxy (Bungee + backend)

1. Install `PermissionsExPlus-%%site.version%%.jar` on **every** backend and the proxy
2. Use a shared **`sql`** backend (recommended) — not separate H2 files per server
3. Set up groups on one server, then share the database or [import data](/guides/import-export) into the SQL backend

```yaml
# config.yml — SQL backend for shared data
permissions:
  backend: sql
  backends:
    sql:
      type: sql
      url: jdbc:mysql://localhost:3306/permissions
      user: pex
      password: secret
```

---

## EssentialsX permission map

| Group | Typical nodes |
|-------|---------------|
| default | `modifyworld`, `essentials.help`, `essentials.list` |
| member | `essentials.sethome`, `essentials.spawn`, `essentials.tpa` |
| vip | `essentials.fly`, `essentials.hat`, `essentials.feed` |
| mod | `essentials.kick`, `essentials.mute`, `essentials.tp` |
| admin | `permissions.*`, `*` |

Plugin node names vary by EssentialsX version — check your plugin's wiki for exact nodes.

---

## After any setup

```text
/pex hierarchy
/pex user <yourname> check essentials.fly
/pex reload
```

See [Troubleshooting](/guides/troubleshooting/) if something does not work.
