---
title: Troubleshooting
description: Fix common PermissionsExPlus issues.
slug: /guides/troubleshooting
---

## Plugin won't load

| Symptom | Fix |
|---------|-----|
| "Unsupported Java version" | Upgrade to **Java 21+** — see [Requirements](/requirements/) |
| "Error loading plugin" in console | Remove duplicate PEX jars from `plugins/` |
| Plugin missing from `/plugins` | Check the jar is named `PermissionsExPlus-*.jar` and not corrupted |

---

## Commands don't work

| Symptom | Fix |
|---------|-----|
| "Unknown command" | PEX not loaded — check console for errors on startup |
| "You don't have permission" | Grant yourself access: `/pex group admin add permissions.*` (from console) |
| Changes not visible | Run `/pex reload` after manual config edits |

Run from console (bypasses permission checks):

```text
pex group admin add permissions.*
pex user YourName group set admin
```

---

## Player has wrong permissions

**Step 1 — Check what PEX thinks:**

```text
/pex user Steve check essentials.fly
/pex user Steve list
/pex user Steve group list
/pex hierarchy
```

**Step 2 — Common causes:**

| Cause | Fix |
|-------|-----|
| Player not in any group | `/pex user Steve group set default` |
| Permission on wrong world | Add world: `/pex user Steve check essentials.fly world_nether` |
| Negation overrides grant | Look for `-node` in `/pex user Steve list` |
| Another plugin overriding | Check if LuckPerms or another perm plugin is also installed |
| Stale superperms cache | `/pex reload` or restart server |

**Step 3 — Debug logging:**

```text
/pex toggle debug
/pex user Steve toggle debug
```

Watch the console while the player triggers the permission check.

---

## Prefix not showing in chat

| Cause | Fix |
|-------|-----|
| Chat plugin not reading Vault/PEX | Ensure chat plugin supports Vault or PEX directly |
| Wrong group weight | Set higher [weight](/concepts/weight/) on the group whose prefix should show |
| No prefix set | `/pex group admin prefix &c[Admin]` |
| Colour codes wrong | Use `&` not `§` in commands |

```text
/pex group admin weight 100
/pex group admin prefix &c[Admin]
/pex user Steve group list
```

---

## Data not saving

| Cause | Fix |
|-------|-----|
| File permissions | Check server can write to `plugins/PermissionsEx/` (H2 needs write access for `permissions.mv.db`) |
| SQL connection failed | Check database credentials in `config.yml` |
| Wrong backend active | `/pex backend` — should show `h2` or your SQL alias |
| YAML not imported | Ensure `permissions.yml` exists before first startup with `backend: h2`, or use `/pex import file` |

```text
/pex backend
/pex config permissions.backend
/pex reload
```

---

## Migration issues

See [Migration FAQ](/faq/migration/).

After importing from another plugin, verify:

```text
/pex groups list
/pex users list
/pex hierarchy
/pex convert uuid
```

---

## Generate a report

```text
/pex report
```

Include this output when opening a [GitHub issue](https://github.com/%%site.repo%%/issues).

---

## Still stuck?

1. Check [GitHub Issues](https://github.com/%%site.repo%%/issues) for known problems
2. Search for your Minecraft + PEX version combination
3. Open a new issue with `/pex report` output, server type, and Java version
