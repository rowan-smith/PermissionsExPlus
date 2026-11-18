---
sidebar_position: 3
---

# User Commands

Commands for managing individual users.

## Basic Management

- `/pex user <user> list`: List user details.
- `/pex user <user> check <permission>`: Check if a user has a permission.

## Permissions

- `/pex user <user> permission add <permission> [--world <world>] [--server <server>]`: Add a permission.
- `/pex user <user> permission remove <permission> [--world <world>] [--server <server>]`: Remove a permission.

## Groups

- `/pex user <user> groups add <group> [duration]`: Add user to a group. With a duration
  (seconds, or units such as `7d`, `1h30m`), the membership expires by itself, like a
  PermissionsEx timed group. Adding again replaces the duration.
- `/pex user <user> groups remove <group>`: Remove user from a group.
- `/pex user <user> groups set <group>`: Make `<group>` the user's only group.

## Options

- `/pex user <user> option set <key> <value>`: Set an option.
- `/pex user <user> option remove <key>`: Remove an option.
