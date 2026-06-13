---
layout: default
title: World Commands
permalink: /commands/worlds/
description: Multi-world permissions and world inheritance commands.
---

World commands manage [context]({{ site.baseurl }}/concepts/context/) — scoping permissions to specific worlds.

---

## `/pex worlds`

**Syntax:** `/pex worlds`

Lists all known worlds/realms.

```text
/pex worlds
```

---

## `/pex world <world>`

**Syntax:** `/pex world <world>`

Shows world info: inheritance parents, default group.

```text
/pex world world
/pex world world_nether
/pex world world_the_end
```

---

## `/pex world <world> inherit`

**Syntax:** `/pex world <world> inherit <parentWorlds...>`

Sets which worlds this world inherits permissions from. Child worlds start with the parent's permissions plus their own overrides.

```text
/pex world world_nether inherit world
/pex world world_the_end inherit world
/pex world minigame inherit world creative
```

Multiple parents:

```text
/pex world special inherit world creative
```

**Effect:** A permission granted in `world` automatically applies in `world_nether` unless explicitly overridden.

---

## Default group per world

```text
/pex default group [world]
/pex set default group <group> <true|false> [world]
```

```text
/pex set default group default true
/pex set default group survival true world_survival
/pex default group world_survival
```

New players joining that world are assigned the default group. See [Default Groups]({{ site.baseurl }}/faq/default-groups/).

---

## World-scoped permission commands

Any user or group command accepts an optional world as the last argument:

```text
/pex group vip add essentials.fly world_nether
/pex user Steve check essentials.fly world_nether
/pex user Steve list world_nether
/pex group builder parents add creative world_creative
```

---

## Example: survival + creative

```text
/pex world world_creative inherit world

/pex group survival add essentials.sethome
/pex group creative add worldedit.*
/pex group creative add gamemode.creative world_creative

/pex set default group survival true world
/pex set default group creative true world_creative
```

Players in the overworld get survival permissions. In the creative world they get creative tools too.

---

## Example: nether-only perks

```text
/pex world world_nether inherit world
/pex group nether_vip add essentials.godmode world_nether
/pex group nether_vip add essentials.fly world_nether
/pex user Steve group add nether_vip world_nether
```

Steve gets godmode and fly only in the Nether.

---

## Inspect world setup

```text
/pex hierarchy world_nether
/pex world world_nether
/pex user Steve list world_nether
/pex user Steve check essentials.fly world_nether
```
