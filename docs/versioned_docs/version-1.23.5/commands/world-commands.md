---
sidebar_position: 4
---

# World Commands

## `/pex worlds`

Print all loaded worlds and their world inheritance.

**Permission:** `permissions.manage.worlds`

## `/pex world <world>`

Print inheritance information for a specific world.

**Permission:** `permissions.manage.worlds`

**Arguments:**
- `<world>`: world name (tab-completes)

## `/pex world <world> inherit <parentWorlds>`

Set which worlds this world inherits permissions from. Multiple worlds can be comma-separated.

**Permission:** `permissions.manage.worlds.inheritance`

**Arguments:**
- `<world>`: world name (tab-completes)
- `<parentWorlds>`: comma-separated world names (tab-completes)

## World Inheritance

World inheritance allows one world to inherit permissions and options from another world. This is useful for managing permissions across multiple worlds without duplicating configuration.

Example:

```bash
/pex world world_nether inherit world
/pex world world_the_end inherit world
```

This makes the Nether and End inherit permissions from the overworld.
