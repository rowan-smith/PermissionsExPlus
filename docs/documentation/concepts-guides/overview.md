---
sidebar_position: 6
---

# Concepts & Guides

Learn how to configure and manage your PermissionsExPlus setup.

- [Inheritance](inheritance): build group hierarchies with parent/child relationships. Groups can inherit permissions, options, prefixes, and suffixes from multiple parents, and inheritance can be scoped per-world. Includes details on non-inheritable permissions and conflict resolution.

- [Weight](weight): control which group takes priority when a user belongs to multiple groups. Higher weight means higher priority for permission resolution and prefix/suffix selection. Covers the difference between weight and rank, and how to set default values.

- [Prefix & Suffix](prefix-suffix): set chat formatting for groups and individual users. Supports Minecraft color codes, world-scoped prefixes, and works out of the box with EssentialsX Chat, Vault, and PlaceholderAPI.

- [Options](options): manage key-value metadata for chat plugins and custom features. Options like `prefix`, `suffix`, `color`, and `fly` can be set globally or per-world, and accessed via commands or the API.

- [World Permissions](world-permissions): scope permissions and options to specific worlds. World-specific permissions are additive to your global set, so you can grant extra abilities in one world without affecting others.

- [Ranks & Ladders](ranks-ladders): set up promotion and demotion systems with named ladders. Covers rank assignment (lower number = higher on the ladder), weight vs rank, multiple ladders, and how `/promote` and `/demote` work.

For plugin developers integrating with PermissionsExPlus, see the [Developer](/developer/next/overview) tab.
