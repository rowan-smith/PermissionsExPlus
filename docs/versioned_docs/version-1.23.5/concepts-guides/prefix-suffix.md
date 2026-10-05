---
sidebar_position: 9
---

# Prefix & Suffix

Prefixes and suffixes are chat formatting strings that appear before and after a player's name. They can be set per-group and per-user, and can be world-scoped.

## How They Work

- **Prefix**: text displayed before the player's name in chat
- **Suffix**: text displayed after the player's name in chat

:::note
If a user has a prefix set, it overrides the group prefix. If no user prefix is set, the highest-weight group's prefix is used.
:::

- Color codes using `&` are supported (e.g. `&c`, `&a`, `&l`, etc.)

## Setting Prefixes

### For a group

```bash
/pex group admin prefix '&c[Admin] &r'
/pex group admin prefix '&c[Admin] &r' world_nether
```

### For a user

```bash
/pex user Steve prefix '&e[VIP] &r'
```

## Setting Suffixes

```bash
/pex group admin suffix ' &c[Admin]'
/pex user Steve suffix ' &e[VIP]'
```

## Viewing Prefixes/Suffixes

Omit the value to view the current setting:

```bash
/pex group admin prefix
/pex user Steve prefix
```

## Color Codes

You can use standard Minecraft color codes:

| Code | Color | Code | Color |
|------|-------|------|-------|
| `&0` | Black | `&a` | Green |
| `&1` | Dark Blue | `&b` | Aqua |
| `&2` | Dark Green | `&c` | Red |
| `&3` | Dark Aqua | `&d` | Light Purple |
| `&4` | Dark Red | `&e` | Yellow |
| `&5` | Dark Purple | `&f` | White |
| `&6` | Gold | `&k` | Magic |
| `&7` | Gray | `&l` | Bold |
| `&8` | Dark Gray | `&m` | Strikethrough |
| `&9` | Blue | `&n` | Underline |
| `&r` | Reset | `&o` | Italic |

## Integration with Chat Plugins

PermissionsExPlus provides the group prefix/suffix information through its API. Chat plugins like:

- **EssentialsX Chat**
- **Vault** (via Vault's chat API)

Will automatically pick up the configured prefixes and suffixes.

### PlaceholderAPI

See [PlaceholderAPI](../integrations/placeholder-api/overview) for the full list of available placeholders.

## World-Scoped Prefixes

Set a prefix that only applies in a specific world:

```bash
/pex group admin prefix '&c[Nether Admin] &r' world_nether
```

In other worlds, the common (non-world-scoped) prefix will be used.
