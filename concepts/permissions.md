---
layout: default
title: How Permissions Work
permalink: /concepts/permissions/
description: How PermissionsExPlus checks and resolves permissions.
---

When a plugin asks "does this player have `essentials.fly`?", PEX walks through a resolution chain and returns yes or no.

## The resolution order

PEX checks permissions in this order (first match wins for negations):

1. **User's direct permissions** — nodes assigned directly to the player
2. **User's group permissions** — from every group the player belongs to
3. **Inherited group permissions** — from parent groups up the [inheritance]({{ site.baseurl }}/concepts/inheritance/) chain
4. **World-scoped overrides** — if checking in a specific [context]({{ site.baseurl }}/concepts/context/)

Within each level, permissions are evaluated **top to bottom** in the list. A negation (`-node`) overrides a grant above it.

## Permission node syntax

| Syntax | Meaning | Example |
|--------|---------|---------|
| `plugin.command` | Exact node | `essentials.home` |
| `plugin.*` | Wildcard subtree | `essentials.*` grants all essentials nodes |
| `*` | Everything | Admin access |
| `-plugin.command` | Explicit deny | `-essentials.ban` removes ban even if `*` is granted |

## Example resolution

Player **Steve** is in group `vip`. Group `vip` inherits `default`.

```
default:  modifyworld
vip:      essentials.fly, essentials.hat
steve:    -essentials.hat        (direct user permission)
```

When checking `essentials.hat`:
- Steve's direct `-essentials.hat` **denies** it, even though `vip` grants it.

When checking `essentials.fly`:
- Not on Steve directly → check `vip` → **granted**.

## Checking permissions

In-game:

```text
/pex user Steve check essentials.fly
/pex user Steve check essentials.fly world_nether
/pex user Steve list
```

## Regex permissions

PEX supports regex patterns in permission lists. Players with the node `permissionsex.disabled` skip regex matching.

## Best practice

Put shared permissions on **groups**, not individual users. Use direct user permissions only for exceptions.

```text
/pex group vip add essentials.fly       # good — applies to all VIPs
/pex user Steve add essentials.fly      # ok — but only for Steve
```

See [Permission commands]({{ site.baseurl }}/commands/permissions/) for how to assign nodes.
