---
layout: default
title: Rank Commands
permalink: /commands/ranks/
description: Promote and demote players on rank ladders in PermissionsExPlus.
---

**Rank ladders** move players between groups in a defined order — ideal for staff progression, RPG ranks, or automated promotions.

---

## How ladders work

1. Assign each group a **rank number** on a named **ladder**
2. Lower number = lower rank
3. `/pex promote` moves the player to the **next higher** group on the ladder
4. `/pex demote` moves them **down**

```
Ladder "staff":  trainee (1) → helper (2) → moderator (3) → admin (4)
```

---

## `/pex group <group> rank`

**Syntax:** `/pex group <group> rank [rank] [ladder]`

Assign or view a group's position on a ladder.

| Argument | Description |
|----------|-------------|
| `rank` | Integer position (1 = lowest) |
| `ladder` | Ladder name (any string) |

```text
/pex group trainee rank 1 staff
/pex group helper rank 2 staff
/pex group moderator rank 3 staff
/pex group admin rank 4 staff

/pex group trainee rank staff
```

You can have multiple ladders:

```text
/pex group wood rank 1 gathering
/pex group stone rank 2 gathering
/pex group iron rank 3 gathering
```

---

## `/pex promote`

**Syntax:** `/pex promote <user> [ladder]`

Promotes the user to the next group on the ladder.

```text
/pex promote Steve staff
/pex promote NewMod staff
```

If `ladder` is omitted, uses the default ladder.

**Shortcut** (no `/pex` prefix):

```text
/promote Steve
/promote Steve staff
```

---

## `/pex demote`

**Syntax:** `/pex demote <user> [ladder]`

Demotes the user to the previous group on the ladder.

```text
/pex demote Steve staff
```

**Shortcut:**

```text
/demote Steve
/demote Steve staff
```

---

## Full staff ladder setup

```text
/pex group trainee create default
/pex group trainee rank 1 staff
/pex group trainee weight 20
/pex group trainee prefix &7[Trainee]
/pex group trainee add essentials.help

/pex group helper create trainee
/pex group helper rank 2 staff
/pex group helper weight 40
/pex group helper prefix &a[Helper]
/pex group helper add essentials.tp

/pex group moderator create helper
/pex group moderator rank 3 staff
/pex group moderator weight 60
/pex group moderator prefix &9[Mod]
/pex group moderator add essentials.kick
/pex group moderator add essentials.mute

/pex group admin create moderator
/pex group admin rank 4 staff
/pex group admin weight 100
/pex group admin prefix &c[Admin]
/pex group admin add permissions.*
/pex group admin add '*'

/pex user NewHire group set trainee
/pex promote NewHire staff
```

---

## Notes

- A player must already be on a group that has a rank on the ladder
- Promote fails if the player is already at the highest rank
- Demote fails at the lowest rank
- Rank changes **replace** the player's group on the ladder (via group set internally)

## Related

- [Group commands]({{ site.baseurl }}/commands/groups/) — `rank` subcommand
- [Common Setups]({{ site.baseurl }}/guides/recipes/) — staff hierarchy recipe
- [Inheritance]({{ site.baseurl }}/concepts/inheritance/) — groups still inherit permissions
