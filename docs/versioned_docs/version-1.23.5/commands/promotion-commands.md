---
sidebar_position: 5
---

# Promotion Commands

## Rank System

PermissionsExPlus supports a promotion/demotion system where groups can be assigned ranks on ladders. A user can be promoted to a higher-ranked group or demoted to a lower-ranked group on the same ladder.

:::note
Promotion and demotion only work within the same ladder. A user in the `default` ladder cannot be promoted into the `premium` ladder.
:::

## `/pex group <group> rank [rank] [ladder]`

Get or set a group's rank on a ladder.

**Permission:** `permissions.groups.rank.<group>`

**Arguments:**
- `<group>`: group name (tab-completes)
- `[rank]`: rank number (lower number = higher rank/position)
- `[ladder]`: ladder name (tab-completes: `default`)

Rank determines promotion order within a ladder. Groups with lower rank numbers are higher in the hierarchy. Promoting a user moves them to the next group with a lower rank number.

## `/pex promote <user> [ladder]`

Promote a user to the next group on the specified ladder.

**Permission:** `permissions.user.promote.<ladder>`

**Arguments:**
- `<user>`: player name or UUID (tab-completes)
- `[ladder]`: ladder name (tab-completes: `default`)

## `/pex demote <user> [ladder]`

Demote a user to the previous group on the specified ladder.

**Permission:** `permissions.user.demote.<ladder>`

**Arguments:**
- `<user>`: player name or UUID (tab-completes)
- `[ladder]`: ladder name

## Standalone Commands

For convenience, standalone command aliases are registered:

### `/promote <user> [ladder]`

**Permission:** `permissions.user.rank.promote`

### `/demote <user> [ladder]`

**Permission:** `permissions.user.rank.demote`

## Example Setup

```bash
/pex group default  rank 500  default
/pex group member   rank 400  default
/pex group vip      rank 300  default
/pex group mod      rank 200  default
/pex group admin    rank 100  default

/pex user Steve group add default
/pex promote Steve default   // Promotes Steve to member
/pex promote Steve default   // Promotes Steve to vip
/pex demote Steve default    // Demotes Steve back to member
```
