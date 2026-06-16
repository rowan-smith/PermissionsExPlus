---
title: Prefix & Meta
description: Chat prefixes, suffixes, and custom options in PermissionsExPlus.
slug: /concepts/meta
---

PEX stores **metadata** on users and groups that chat and other plugins can read.

## Prefix and suffix

Displayed in chat by plugins like EssentialsChat, Vault-compatible bridges, or TAB.

```text
/pex group admin prefix &c[Admin]
/pex group admin suffix &7»
/pex group vip prefix &6[VIP]
```

**Colour codes** use `&` format: `&c` red, `&6` gold, `&a` green, `&7` grey.

Set on a user to override the group:

```text
/pex user Steve prefix &b[Builder]
```

View without changing:

```text
/pex group admin prefix
/pex user Steve prefix
```

### Which prefix shows?

When a player is in multiple groups, [weight](/concepts/weight/) decides. The highest-weight group's prefix wins.

## Options

Arbitrary key-value pairs for other plugins or your own logic:

```text
/pex user Steve set name "Builder Steve"
/pex user Steve get name
/pex group vip set tag premium
```

Common uses: display names, custom tags, integration metadata.

## Example data structure

Representative YAML layout (used for import/migration; default storage is H2):

```yaml
groups:
  admin:
    prefix: '&c[Admin] '
    suffix: '&7» '
    options:
      staff: 'true'
users:
  uuid-here:
    options:
      name: Steve
```

## World-scoped meta

Prefixes can differ per world:

```text
/pex group vip prefix &6[VIP] world
/pex group vip prefix &5[Nether VIP] world_nether
```

## Related commands

| Command | Purpose |
|---------|---------|
| `/pex user <u> prefix [value] [world]` | User prefix |
| `/pex user <u> suffix [value] [world]` | User suffix |
| `/pex user <u> set <option> <value> [world]` | User option |
| `/pex group <g> prefix [value] [world]` | Group prefix |
| `/pex group <g> set <option> <value> [world]` | Group option |

See [User commands](/commands/users/) and [Group commands](/commands/groups/).
