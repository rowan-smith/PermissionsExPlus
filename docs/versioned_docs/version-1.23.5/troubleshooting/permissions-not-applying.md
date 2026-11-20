---
sidebar_position: 1
---

# Permissions Not Applying

If a player reports that permissions aren't working as expected, work through these steps in order.

## 1. Reload the plugin

Run `/pex reload` to reload all data from storage. This picks up any manual edits to `permissions.yml` or `config.yml`.

```bash
/pex reload
```

## 2. Check the hierarchy

Run `/pex hierarchy` to verify the current inheritance structure. This prints the full group tree and shows which groups inherit from which.

```bash
/pex hierarchy
```

Look for:
- Missing inheritance links (a group not inheriting from the parent you expected)
- Groups listed in the wrong order

## 3. Check for permission conflicts

Use `/pex user <name> list` to see the effective permissions for a player. This shows the final resolved list after all groups, inheritance, and negation are applied.

```bash
/pex user Steve list
```

If a permission is missing, check whether:
- It's granted by a group the user is actually in
- It's being negated by a `-` prefix somewhere
- A higher-priority group is overriding it

## 4. Verify the user and group exist

Confirm the user and group are registered in the system:

```bash
/pex users list
/pex groups list
```

If the user doesn't appear, they may not have been added to a group yet. If the group doesn't exist, create it:

```bash
/pex group <name> create
```

## 5. Check world scoping

If the permission only fails in certain worlds, it may be world-scoped. Check world-specific permissions:

```bash
/pex user <name> list <world>
/pex group <group> list <world>
```

World-specific permissions are additive, they don't replace global permissions, they add to them. A permission granted globally should work in all worlds unless explicitly negated.

## 6. Enable debug mode

If nothing above helps, enable debug mode to see detailed permission resolution logs:

```bash
/pex toggle debug
```

This prints step-by-step permission checks to the console. Look for lines showing which nodes were checked and whether they were granted or denied.

:::caution
Debug mode is verbose, disable it when you're done investigating with `/pex toggle debug` again.
:::
