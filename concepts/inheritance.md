---
layout: default
title: Inheritance
permalink: /concepts/inheritance/
description: How group inheritance works in PermissionsExPlus.
---

**Inheritance** lets groups reuse permissions from other groups. Instead of copying the same nodes into every group, you declare a parent and the child gets everything the parent has.

## How it works

```
default ──► member ──► vip ──► admin
```

- `member` inherits `default` → gets `modifyworld` + member's own nodes
- `vip` inherits `member` → gets default + member + vip nodes
- `admin` inherits `vip` → gets the full chain + admin nodes

A player in `admin` receives permissions from **all ancestors**, not just the direct parent.

## Set up inheritance

**At group creation** — pass parent names:

```text
/pex group member create default
/pex group vip create member
/pex group admin create vip
```

**After creation** — manage parents:

```text
/pex group vip parents add member
/pex group vip parents list
/pex group vip parents set member default
/pex group vip parents remove default
```

## In permissions.yml

```yaml
groups:
  default:
    default: true
    permissions:
      - modifyworld
  vip:
    inheritance:
      - default
    permissions:
      - essentials.fly
  admin:
    inheritance:
      - vip
    permissions:
      - permissions.*
```

## Multiple parents

A group can inherit from several parents:

```text
/pex group builder parents set default creative
```

The child gets permissions from **both** `default` and `creative`.

## World-scoped inheritance

Parents can differ per world:

```text
/pex group vip parents add default
/pex group vip parents add premium world_nether
```

See [Context & Worlds]({{ site.baseurl }}/concepts/context/).

## Common mistakes

| Mistake | Fix |
|---------|-----|
| Circular inheritance (A → B → A) | PEX rejects cycles — use a linear chain |
| Copying all permissions into every group | Use inheritance instead |
| Forgetting the `default` group | Set one group as [default]({{ site.baseurl }}/faq/default-groups/) |

## Inspect the tree

```text
/pex hierarchy
/pex hierarchy world_nether
/pex group admin parents
```

## Related commands

- [Group commands]({{ site.baseurl }}/commands/groups/) — `parents add/set/remove`
- [User commands]({{ site.baseurl }}/commands/users/) — `group add/set/remove`
