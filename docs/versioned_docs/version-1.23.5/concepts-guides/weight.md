---
sidebar_position: 5
---

# Weight

Weight determines the priority order of groups. Higher weight groups take precedence over lower weight groups when determining effective permissions.

## How Weight Works

When a user is a member of multiple groups, PEX evaluates permissions in order of group weight. Higher weight means higher priority.

Weight also affects:
- Which prefix/suffix is shown (highest weight group wins)
- Permission resolution order

## Setting Weight

### Via command

```bash
/pex group admin weight 100
/pex group moderator weight 50
/pex group default weight 0
```

### Via permissions.yml

```yaml
groups:
  admin:
    options:
      weight: 100
  moderator:
    options:
      weight: 50
  default:
    options:
      weight: 0
```

## Viewing Weight

```bash
/pex group admin weight
```

## Weight vs Rank

Weight and rank serve different purposes:

- **Weight**: determines general priority between groups (which prefix to show, permission resolution order)
- **Rank**: determines position on a promotion ladder (used by `/promote` and `/demote`)

:::note
A group can have a high weight (priority) but sit at the bottom of a ladder (high rank *number*), or vice versa. They are independent settings. Lower rank numbers are higher on the ladder.
:::

## Default Weight

:::note
If not specified, groups default to a weight of `0`. Higher numbers = higher priority. Negative weights are allowed.
:::

## Example

```bash
/pex group vip weight 50
/pex group premium weight 75
/pex group admin weight 100
```

When a user is in both `vip` (50) and `premium` (75), the `premium` prefix will be shown because it has the higher weight.
