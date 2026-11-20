---
sidebar_position: 4
---

# Group Commands

Commands for managing permission groups.

## Basic Management

- `/pex group <group> create`: Create a new group.
- `/pex group <group> delete`: Delete a group.
- `/pex group <group> list`: List group details.

## Permissions

- `/pex group <group> permission add <permission> [--world <world>] [--server <server>]`: Add a permission.
- `/pex group <group> permission remove <permission> [--world <world>] [--server <server>]`: Remove a permission.

## Inheritance

- `/pex group <group> parent add <parent>`: Add a parent group.
- `/pex group <group> parent remove <parent>`: Remove a parent group.

## Options

- `/pex group <group> option set <key> <value>`: Set an option.
- `/pex group <group> option remove <key>`: Remove an option.
