---
title: Weight
description: Group weight and priority in PermissionsExPlus.
slug: /concepts/weight
---

**Weight** is a number that sets a group's **priority**. Higher weight = more important when PEX needs to pick between conflicting values.

## What weight affects

| Feature | How weight is used |
|---------|-------------------|
| **Chat prefix/suffix** | Higher-weight group's prefix displays when a player is in multiple groups |
| **Permission ordering** | Influences which group's permissions are considered first in edge cases |

Weight does **not** grant more permissions. A group with weight 100 has the same permission power as weight 0 — weight only affects **priority**, not access.

## Set weight

```text
/pex group default weight 0
/pex group vip weight 10
/pex group moderator weight 50
/pex group admin weight 100
```

View current weight:

```text
/pex group admin weight
```

## Prefix example

Steve is in both `vip` (weight 10, prefix `&6[VIP]`) and `admin` (weight 100, prefix `&c[Admin]`).

Chat plugins show **`&c[Admin]`** because admin has higher weight.

```text
/pex group vip weight 10
/pex group vip prefix &6[VIP]
/pex group admin weight 100
/pex group admin prefix &c[Admin]
/pex user Steve group add vip
/pex user Steve group add admin
```

## Recommended values

| Group type | Suggested weight |
|------------|------------------|
| `default` | 0 |
| Regular ranks (member, vip) | 1 – 20 |
| Staff (helper, mod) | 30 – 60 |
| Senior staff (admin, owner) | 70 – 100 |

Leave gaps between tiers so you can insert new groups later.

## Example data structure

Representative YAML layout (used for import/migration; default storage is H2):

```yaml
groups:
  default:
    weight: 0
  vip:
    weight: 10
    prefix: '&6[VIP] '
  admin:
    weight: 100
    prefix: '&c[Admin] '
```

## Related

- [Prefix & Meta](/concepts/meta/)
- [Group commands](/commands/groups/) — `weight` subcommand
