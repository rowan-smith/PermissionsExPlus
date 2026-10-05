# Developer Overview

PermissionsExPlus provides a powerful and flexible API for interacting with the permissions engine.

## Maven Dependency

```xml
<dependency>
    <groupId>dev.rono.permissions</groupId>
    <artifactId>PermissionsExPlusApi</artifactId>
    <version>2.0.0-SNAPSHOT</version>
    <scope>provided</scope>
</dependency>
```

## Core Components

- `PexApi`: The main entry point.
- `UserManager`, `GroupManager`, `LadderManager`: Managers for each entity type.
- `QueryOptions`: Define how permissions should be resolved (contexts, etc.).
- `Modifier`: Atomic mutation builder.
- `ContextKeys`: Type-safe context key constants (`WORLD`, `SERVER`, `GAMEMODE`, `PROXY`).

## Quick Example

```java
var api = PexProvider.get();

// Check a permission in a specific world
api.users().find(uuid).thenAccept(optionalUser -> {
    optionalUser.ifPresent(user -> {
        boolean allowed = api.resolvers().permissions()
                .hasPermission(user, "myplugin.use", ContextKeys.WORLD, "nether");
    });
});

// Grant a world-scoped permission
api.users().modify(uuid, modifier -> modifier
        .allowPermission("myplugin.fly", ContextKeys.WORLD, "creative"));
```
