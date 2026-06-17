---
title: Command Mapping
description: Modern vs classic /pex command syntax side by side.
slug: /commands/command-mapping
---

PEX registers one of two command trees at startup, controlled by `permissions.command-framework` in `config.yml`:

| Value | Framework |
|-------|-----------|
| `modern` (default) | Structured subcommands (`permissions`, `groups`, `options`, …) |
| `classic`, `legacy`, `old` | Original PEX syntax |

Check your active framework:

```text
/pex config permissions.command-framework
```

(Classic framework only — on modern servers, read `config.yml` directly or use `/pex version`.)

Switch frameworks in `config.yml` and restart or `/pex reload`:

```yaml
permissions:
  command-framework: classic   # or modern
```

---

## Context: worlds and servers

| Framework | World scope |
|-----------|-------------|
| **Classic** | Trailing argument: `/pex user Steve add essentials.fly world_nether` |
| **Modern** | Flag: `/pex user Steve permissions add essentials.fly --world world_nether` |

On proxy installs, modern commands also accept `--server <name>` where applicable.

---

## User permissions

| Task | Modern | Classic |
|------|--------|---------|
| List permissions | `/pex user Steve permissions list` | `/pex user Steve list` |
| Add permission | `/pex user Steve permissions add essentials.home` | `/pex user Steve add essentials.home` |
| Remove permission | `/pex user Steve permissions remove essentials.home` | `/pex user Steve remove essentials.home` |
| Check permission | `/pex user Steve permissions check essentials.fly` | `/pex user Steve check essentials.fly` |
| Trace resolution | `/pex user Steve permissions trace essentials.fly` | — |
| Timed add | `/pex user Steve permissions timed add essentials.fly 7d` | `/pex user Steve timed add essentials.fly 7d` |
| Timed remove | `/pex user Steve permissions timed remove essentials.fly` | `/pex user Steve timed remove essentials.fly` |
| World-scoped add | `… add essentials.fly --world world_nether` | `/pex user Steve add essentials.fly world_nether` |

---

## User groups

| Task | Modern | Classic |
|------|--------|---------|
| List groups | `/pex user Steve groups list` | `/pex user Steve group list` |
| Add to group | `/pex user Steve groups add vip` | `/pex user Steve group add vip` |
| Set primary group | `/pex user Steve groups set admin` | `/pex user Steve group set admin` |
| Remove from group | `/pex user Steve groups remove vip` | `/pex user Steve group remove vip` |
| Timed membership | `/pex user Steve groups timed add vip 30d` | `/pex user Steve group add vip world 30d` |

---

## User meta (prefix, suffix, options)

| Task | Modern | Classic |
|------|--------|---------|
| Set option | `/pex user Steve options set nickname "Big S"` | `/pex user Steve set nickname "Big S"` |
| Get option | `/pex user Steve options get nickname` | `/pex user Steve get nickname` |
| Set prefix | `/pex user Steve options set prefix "&b[Builder]"` | `/pex user Steve prefix &b[Builder]` |
| Set suffix | `/pex user Steve options set suffix "&7"` | `/pex user Steve suffix &7` |

---

## Group permissions

| Task | Modern | Classic |
|------|--------|---------|
| List permissions | `/pex group vip permissions list` | `/pex group vip list` |
| Add permission | `/pex group vip permissions add essentials.fly` | `/pex group vip add essentials.fly` |
| Remove permission | `/pex group vip permissions remove essentials.fly` | `/pex group vip remove essentials.fly` |
| Check permission | `/pex group vip permissions check essentials.fly` | — |
| Timed add | `/pex group vip permissions timed add essentials.fly 7d` | `/pex group vip timed add essentials.fly 7d` |
| Swap nodes | — | `/pex group vip swap old.node new.node` |

---

## Group structure

| Task | Modern | Classic |
|------|--------|---------|
| Create group | `/pex group vip create` | `/pex group vip create` |
| Create with parents | `/pex group admin create vip` | `/pex group admin create vip` |
| Delete group | `/pex group trial delete` | `/pex group trial delete` |
| List parents | `/pex group vip parents list` | `/pex group vip parents list` |
| Add parents | `/pex group vip parents add default` | `/pex group vip parents add default` |
| Set parents | `/pex group admin parents set vip` | `/pex group admin parents set vip` |
| List members | `/pex group admin members list` | `/pex group admin users` |
| Add member | `/pex group admin members add Steve` | `/pex group admin user add Steve` |
| Set weight | `/pex group admin options set weight 100` | `/pex group admin weight 100` |
| Set prefix | `/pex group admin options set prefix "&c[Admin]"` | `/pex group admin prefix &c[Admin]` |
| Set rank | `/pex ladder staff groups add admin` (see [Ranks](/commands/ranks)) | `/pex group admin rank 4 staff` |

---

## Backends & data transfer

| Task | Modern | Classic |
|------|--------|---------|
| Show active backend | `/pex backend` or `/pex backend info` | `/pex backend` |
| List backends | `/pex backend list` | — |
| Switch backend | `/pex backend switch sql` | `/pex backend sql` |
| Import data | `/pex backend import file` | `/pex import file` |
| Export data | `/pex backend export` | — |

See [Import & Export](/guides/import-export) for full workflows.

---

## Server-wide commands

| Task | Modern | Classic |
|------|--------|---------|
| Reload | `/pex reload` | `/pex reload` |
| Version | `/pex version` | `/pex version` |
| Debug on | `/pex debug on` | `/pex toggle debug` |
| Debug off | `/pex debug off` | `/pex toggle debug` |
| Debug status | `/pex debug` | — |
| Config read/write | — | `/pex config permissions.debug` |
| UUID conversion | — | `/pex convert uuid` |
| UUID conversion (force) | — | `/pex convert uuid force` |
| Hierarchy | `/pex hierarchy` | `/pex hierarchy` |
| World hierarchy | `/pex hierarchy --world world_nether` | `/pex hierarchy world_nether` |
| Report | `/pex report` | `/pex report` |

---

## Ranks & ladders

| Task | Modern | Classic |
|------|--------|---------|
| Promote | `/pex promote Steve staff` | `/pex promote Steve staff` |
| Demote | `/pex demote Steve staff` | `/pex demote Steve staff` |
| List ladders | `/pex ladders` | — |
| Ladder info | `/pex ladder staff info` | — |
| Add group to ladder | `/pex ladder staff groups add helper` | `/pex group helper rank 2 staff` |

Shortcut commands (both frameworks): `/promote Steve` · `/demote Steve`

---

## Which framework should I use?

| Choose **modern** if… | Choose **classic** if… |
|------------------------|-------------------------|
| You are setting up a new server | You are migrating from original PEX and know the old syntax |
| You want structured subcommands and `--world` flags | You rely on `/pex config` or `/pex convert uuid` in-game |
| You need `/pex backend export` | Your staff already memorized classic commands |

Both frameworks operate on the same data. You can switch at any time via `config.yml`.

---

## Related

- [General commands](/commands/general) — reload, backends, debug
- [Commands index](/commands) — searchable command list
- [Configuration — command framework](/configuration#command-framework)
- [Import & Export](/guides/import-export)
