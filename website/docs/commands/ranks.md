---
title: Rank Commands
description: Manage rank ladders and promote or demote players in PermissionsExPlus.
slug: /commands/ranks
---

**Rank ladders** move players between groups in a defined order — ideal for staff progression, RPG ranks, or automated promotions.

PEX registers **`modern`** (default) or **`classic`** command trees. This page documents **modern ladder commands first**; [classic shortcuts](#classic-promote-and-demote) remain available when `command-framework: classic`.

---

## How ladders work

1. Each group on a ladder has a **rank number** (lower = lower rank).
2. **`/pex ladder <ladder> promote <user>`** moves the player to the **next higher** group on that ladder.
3. **`/pex ladder <ladder> demote <user>`** moves them **down**.

```
Ladder "staff":  trainee (1) → helper (2) → moderator (3) → admin (4)
```

Hook plugins should use [`LadderManager`](/developers/api/modern#laddermanager) (`promote` / `demote`) — not top-level `/pex promote` commands.

---

## Modern: list ladders

**Syntax:** `/pex ladders` · `/pex ladder`

Lists every known rank ladder.

```text
/pex ladders
/pex ladder
```

---

## Modern: ladder info

**Syntax:** `/pex ladder <ladder>` · `/pex ladder <ladder> info`

Shows groups and ranks on a ladder.

```text
/pex ladder staff
/pex ladder staff info
```

---

## Modern: manage ladder groups

| Task | Command |
|------|---------|
| List groups on ladder | `/pex ladder <ladder> groups list` |
| Add group to ladder | `/pex ladder <ladder> groups add <group>` |
| Remove group from ladder | `/pex ladder <ladder> groups remove <group>` |
| Move group to rank | `/pex ladder <ladder> groups move <group> <rank>` |

```text
/pex ladder staff groups list
/pex ladder staff groups add helper
/pex ladder staff groups move helper 2
/pex ladder staff groups remove trainee
```

Adding a group assigns the next free rank automatically. Use **move** to reorder.

---

## Modern: promote and demote

**Syntax:**

```text
/pex ladder <ladder> promote <user>
/pex ladder <ladder> demote <user>
```

```text
/pex ladder staff promote Steve
/pex ladder staff demote Steve
```

Requirements:

- The player must already belong to a group on the ladder.
- Promote fails at the highest rank; demote fails at the lowest.

> **Modern framework does not register** `/pex promote`, `/pex demote`, `/promote`, or `/demote`. Use the ladder subcommands above. See [Command mapping — ranks](/commands/command-mapping#ranks--ladders).

---

## Full staff ladder setup (modern)

```text
/pex group trainee create default
/pex group trainee options set weight 20
/pex group trainee options set prefix "&7[Trainee]"
/pex group trainee permissions add essentials.help

/pex group helper create trainee
/pex group helper options set weight 40
/pex group helper options set prefix "&a[Helper]"
/pex group helper permissions add essentials.tp

/pex group moderator create helper
/pex group moderator options set weight 60
/pex group moderator options set prefix "&9[Mod]"
/pex group moderator permissions add essentials.kick

/pex group admin create moderator
/pex group admin options set weight 100
/pex group admin options set prefix "&c[Admin]"
/pex group admin permissions add permissions.*

/pex ladder staff groups add trainee
/pex ladder staff groups add helper
/pex ladder staff groups add moderator
/pex ladder staff groups add admin

/pex user NewHire groups set trainee
/pex ladder staff promote NewHire
```

---

## Classic: group rank

When using **`command-framework: classic`**, assign ranks with:

**Syntax:** `/pex group <group> rank [rank] [ladder]`

```text
/pex group trainee rank 1 staff
/pex group helper rank 2 staff
/pex group trainee rank staff
```

---

## Classic promote and demote

Available only with **`command-framework: classic`** (or `legacy` / `old`):

```text
/pex promote <user> [ladder]
/pex demote <user> [ladder]
/promote <user> [ladder]
/demote <user> [ladder]
```

If `ladder` is omitted, the **default** ladder is used.

```text
/pex promote Steve staff
/promote Steve staff
/pex demote Steve
```

---

## Related

- [Command mapping](/commands/command-mapping) — full modern ↔ classic table
- [Group commands](/commands/groups/) — create groups and options
- [Common Setups](/guides/recipes/) — staff hierarchy recipe
- [Modern API — LadderManager](/developers/api/modern#laddermanager) — programmatic promote/demote
