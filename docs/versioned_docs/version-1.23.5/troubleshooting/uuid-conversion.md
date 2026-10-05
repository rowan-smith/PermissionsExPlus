---
sidebar_position: 4
---

# UUID Conversion Issues

PermissionsExPlus uses UUIDs to identify players. If you're running an offline-mode server or migrated from a name-based setup, you may need to bulk-convert user data.

## When to convert

You need UUID conversion if:

- You switched from name-based to UUID-based storage
- You moved from an offline-mode to online-mode setup (or vice versa)
- User data shows old usernames instead of UUIDs

## Running the conversion

```bash
/pex convert uuid
```

This scans all user data and converts name-based entries to UUID-based entries.

## Offline-mode servers

:::caution
Only use `force` on offline-mode servers if you understand the risks. UUIDs may change if a player's username changes.
:::

On offline-mode servers, UUIDs aren't provided by Mojang. Run:

```bash
/pex convert uuid force
```

This generates UUIDs from usernames using an offline-mode algorithm. Be aware that:

- If a player changes their username, they'll get a different UUID
- Their old permissions data won't carry over to the new UUID
- This is a limitation of offline-mode, not a PermissionsExPlus bug

## Verifying the conversion

After converting, check that users still have their permissions:

```bash
/pex users list
/pex user <name> list
```

If any users lost their permissions, re-add them to the appropriate groups:

```bash
/pex user <name> group add <group>
```
