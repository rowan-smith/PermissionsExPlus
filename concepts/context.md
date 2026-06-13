---
layout: default
title: Context & Worlds
permalink: /concepts/context/
description: World context and scoped permissions in PermissionsExPlus.
---

**Context** is the scope a permission applies to. On a single game server, context usually means a **world name**. On a proxy network, it can also mean a **backend server**.

## Global vs world-scoped

| Scope | When to use | Example |
|-------|-------------|---------|
| **Global** (no world) | Permission applies everywhere | `essentials.home` |
| **World-scoped** | Permission only in one world | `essentials.fly` in `world_nether` only |

```text
/pex group vip add essentials.fly
/pex group vip add essentials.fly world_nether
```

The first grants fly everywhere. The second grants fly **only** in the Nether.

## World inheritance

Child worlds can inherit a parent's permissions:

```text
/pex world world_nether inherit world
/pex world world_the_end inherit world
```

Now `world_nether` starts with everything from `world`, plus any nether-specific overrides.

```text
/pex group builder add worldedit.* world
/pex group builder add worldedit.navigator world_nether
```

## Checking in context

Always specify the world when testing world-scoped permissions:

```text
/pex user Steve check essentials.fly
/pex user Steve check essentials.fly world_nether
/pex user Steve list world_nether
```

A player might have fly in the Nether but not in the overworld — always check the right context.

## Default group per world

Different worlds can have different default groups:

```text
/pex set default group default true
/pex set default group survival true world_survival
```

New players in `world_survival` join the `survival` group automatically.

## Proxy networks

On BungeeCord or Velocity, permissions can be scoped per backend server. Game-server plugins typically use world context; proxy setups may use server-level context.

For most single-server owners, **world** is the only context you need.

## In permissions.yml

```yaml
groups:
  vip:
    permissions:
      - essentials.fly
    worlds:
      world_nether:
        permissions:
          - essentials.godmode
```

## Related

- [World commands]({{ site.baseurl }}/commands/worlds/)
- [API Cookbook — world context]({{ site.baseurl }}/developers/cookbook/#world-scoped-permissions)
