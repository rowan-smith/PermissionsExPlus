---
title: Overview
description: Integrate your plugin with PermissionsExPlus.
slug: /developers
---

PermissionsExPlus exposes APIs for companion plugins that need to read or modify permissions at runtime.

## Quick start

1. Add Maven dependency (`provided` scope — do not shade PEX into your jar)
2. Check `PermissionsEx.isAvailable()` in `onEnable`
3. Use `PermissionsEx.getApi()` for new code

```xml
<dependency>
  <groupId>dev.rono.permissions</groupId>
  <artifactId>permissionsex-api</artifactId>
  <version>%%site.version%%</version>
  <scope>provided</scope>
</dependency>
```

## Documentation layers

| Layer | What it covers |
|-------|----------------|
| **[API Cookbook](/developers/cookbook/)** | Practical recipes — check perms, add groups, events |
| **[Javadoc](/developers/reference/)** | Full class and method reference for every version |
| **[Sample plugins](https://github.com/%%site.repo%%/tree/main/plugin)** | Working example jars in the repo |

The website is intentionally thin — detailed API signatures live in Javadoc. The cookbook shows you *how* to do common tasks with links to the relevant classes.

## Two APIs

| API | Package | When |
|-----|---------|------|
| **Modern** | `dev.rono.permissions.api.*` | New plugins |
| **Legacy** | `ru.tehkode.permissions.*` | Existing PEX 1.23.x plugins |

Both talk to the same runtime manager on game servers.

## Legacy dependencies

For classic `PermissionsEx.getUser()` static calls, also add:

```xml
<dependency>
  <groupId>dev.rono.permissions</groupId>
  <artifactId>permissionsex-legacy-stub</artifactId>
  <version>%%site.version%%</version>
  <scope>provided</scope>
</dependency>
```

## Next steps

- [API Cookbook](/developers/cookbook/) — copy-paste recipes
- [Javadoc](/developers/reference/) — browse all versions
- [Contributing](/developers/contributing/) — build from source
