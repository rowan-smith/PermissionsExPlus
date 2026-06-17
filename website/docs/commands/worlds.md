---
title: World Commands
description: Multi-world permissions and world inheritance commands.
slug: /commands/worlds
---

World commands manage [context](/concepts/context/) — scoping permissions to specific worlds.

**Modern framework (default):** scope user and group commands with **`--world <world>`** flags instead of trailing world arguments. World inheritance and default-group setup remain **classic-only** commands (see below).

---

## Modern: world-scoped permissions

Any modern user or group command accepts optional context flags:

```text
/pex group vip permissions add essentials.fly --world world_nether
/pex user Steve permissions check essentials.fly --world world_nether
/pex user Steve permissions list --world world_nether
/pex group builder parents add creative --world world_creative
/pex hierarchy --world world_nether
```

On proxy installs, use `--server <name>` where applicable.

See [Command mapping — context](/commands/command-mapping#context-worlds-and-servers).

---

## Classic: `/pex worlds`

**Syntax:** `/pex worlds`

Lists all known worlds/realms. **Classic framework only.**

```text
/pex worlds
```

---

## Classic: `/pex world <world>`

**Syntax:** `/pex world <world>`

Shows world info: inheritance parents, default group. **Classic framework only.**

```text
/pex world world
/pex world world_nether
/pex world world_the_end
```

---

## Classic: `/pex world <world> inherit`

**Syntax:** `/pex world <world> inherit <parentWorlds...>`

Sets which worlds this world inherits permissions from. Child worlds start with the parent's permissions plus their own overrides. **Classic framework only.**

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

## Classic: default group per world

```text
/pex default group [world]
/pex set default group <group> <true|false> [world]
```

```text
/pex set default group default true
/pex set default group survival true world_survival
/pex default group world_survival
```

New players joining that world are assigned the default group. See [Default Groups](/faq/default-groups/).

---

## Example: survival + creative

```text
/pex world world_creative inherit world

/pex group survival permissions add essentials.sethome
/pex group creative permissions add worldedit.*
/pex group creative permissions add gamemode.creative --world world_creative

/pex set default group survival true world
/pex set default group creative true world_creative
```

Players in the overworld get survival permissions. In the creative world they get creative tools too.

---

## Example: nether-only perks

```text
/pex world world_nether inherit world
/pex group nether_vip permissions add essentials.godmode --world world_nether
/pex group nether_vip permissions add essentials.fly --world world_nether
/pex user Steve groups add nether_vip --world world_nether
```

Steve gets godmode and fly only in the Nether.

---

## Inspect world setup

```text
/pex hierarchy --world world_nether
/pex world world_nether
/pex user Steve permissions list --world world_nether
/pex user Steve permissions check essentials.fly --world world_nether
```

(`/pex world …` requires classic framework; hierarchy and user commands work on modern with `--world`.)
