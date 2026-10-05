# API Cookbook

Short, copy-paste recipes for common integration tasks. For full method signatures, see [Javadoc](javadoc.md) and the [API Reference](api-reference/).

## Setup

### Spigot / Paper

```java
import dev.rono.permissions.api.PexProvider;

if (!PexProvider.available()) {
    getLogger().warning("PEX not loaded");
    return;
}
var api = PexProvider.get();
```

### Maven (provided scope)

```xml
<dependency>
    <groupId>dev.rono.permissions</groupId>
    <artifactId>PermissionsExPlusApi</artifactId>
    <version>2.0.0-SNAPSHOT</version>
    <scope>provided</scope>
</dependency>
```

## Context & Query Options

Permissions are resolved within a context scope. `QueryOptions` defines the contexts and policy flags for resolution.

### Global context (no scope)

```java
var query = QueryOptions.global();
```

### World-scoped context

```java
var query = QueryOptions.builder()
        .contexts(ContextKeys.WORLD, "world_nether")
        .build();
```

### Combined context (world + custom)

```java
var contexts = ContextSet.builder()
        .add(ContextKeys.WORLD, "arena")
        .add("region", "red-base")
        .add(ContextKeys.GAMEMODE, "ADVENTURE")
        .build();
```

## Check a Permission

### Global check

```java
api.users().find(uuid).thenAccept(optionalUser -> {
    optionalUser.ifPresent(user -> {
        boolean allowed = api.resolvers().permissions()
                .hasPermission(user, "myplugin.use", QueryOptions.global());
        if (allowed) {
            // Do something
        }
    });
});
```

### World-scoped check

```java
api.users().find(uuid).thenAccept(optionalUser -> {
    optionalUser.ifPresent(user -> {
        boolean allowed = api.resolvers().permissions()
                .hasPermission(user, "myplugin.nether-only", ContextKeys.WORLD, "world_nether");
        if (allowed) {
            // Allowed in this world only
        }
    });
});
```

### Permission resolution with explanation

```java
api.users().find(uuid).thenAccept(optionalUser -> {
    optionalUser.ifPresent(user -> {
        var resolution = api.resolvers().permissions()
                .explain(user, "myplugin.use", QueryOptions.global());
        
        getLogger().info("Result: " + resolution.result());
        resolution.winningNode().ifPresent(node -> {
            getLogger().info("Winning node: " + node.permission());
        });
    });
});
```

## Mutations

Mutations use atomic modifiers and complete after persistence and cache replacement.

### Add a permission to a user

```java
api.users().modify(uuid, modifier -> modifier
        .allowPermission("myplugin.reward"));
```

### Deny a permission

```java
api.users().modify(uuid, modifier -> modifier
        .denyPermission("myplugin.banned-feature"));
```

### Add a world-scoped permission

```java
api.users().modify(uuid, modifier -> modifier
        .allowPermission("myplugin.nether-only", ContextKeys.WORLD, "world_nether"));
```

### Remove a permission

```java
api.users().modify(uuid, modifier -> modifier
        .removePermission("myplugin.reward"));
```

### Add a permission to a group

```java
api.groups().modify("vip", modifier -> modifier
        .allowPermission("essentials.fly"));
```

### World-scoped group permission

```java
api.groups().modify("vip", modifier -> modifier
        .allowPermission("worldedit.*", ContextKeys.WORLD, "creative"));
```

## Prefix & Suffix

### Set a user prefix

```java
api.users().modify(uuid, modifier -> modifier
        .setPrefix("&a[Admin] "));
```

### Set a world-scoped prefix

```java
api.users().modify(uuid, modifier -> modifier
        .setPrefix("&b[Creative] ", ContextKeys.WORLD, "creative"));
```

### Set a suffix

```java
api.users().modify(uuid, modifier -> modifier
        .setSuffix("&r"));
```

## Group Management

### Create a group

```java
api.groups().modify("vip", modifier -> modifier
        .allowPermission("essentials.fly")
        .setPrefix("&6[VIP] ")
        .setSuffix("&r"));
```

### Add a user to a group

```java
api.users().modify(uuid, modifier -> modifier
        .addGroup("vip"));
```

### Add user to group with context

```java
api.users().modify(uuid, modifier -> modifier
        .addGroup("arena", ContextKeys.WORLD, "minigames"));
```

### Remove user from group

```java
api.users().modify(uuid, modifier -> modifier
        .removeGroup("vip"));
```

### Replace all groups

```java
api.users().modify(uuid, modifier -> modifier
        .clearGroups()
        .addGroup("admin"));
```

### Timed group membership

```java
import java.time.Duration;

api.users().modify(uuid, modifier -> modifier
        .addTemporaryGroup("trial", Duration.ofDays(7)));
```

### Group hierarchy (parent groups)

```java
api.groups().modify("helper", modifier -> modifier
        .addParent("staff"));
```

### World-scoped parent group

```java
api.groups().modify("helper", modifier -> modifier
        .addParent("survival-mod", ContextKeys.WORLD, "survival"));
```

### Remove parent group

```java
api.groups().modify("helper", modifier -> modifier
        .removeParent("staff"));
```

### Check if user is in a group

```java
api.users().find(uuid).thenAccept(optionalUser -> {
    optionalUser.ifPresent(user -> {
        boolean inGroup = user.hasDirectGroup("vip");
        if (inGroup) {
            // User is in the vip group
        }
    });
});
```

### Check if user is in a group (world-scoped)

```java
api.users().find(uuid).thenAccept(optionalUser -> {
    optionalUser.ifPresent(user -> {
        boolean inGroup = user.hasDirectGroup("arena", ContextKeys.WORLD, "minigames");
        if (inGroup) {
            // User is in arena group in minigames world
        }
    });
});
```

### Check if group has a parent

```java
api.groups().find("helper").thenAccept(optionalGroup -> {
    optionalGroup.ifPresent(group -> {
        boolean hasParent = group.hasDirectParent("staff");
        if (hasParent) {
            // Group inherits from staff
        }
    });
});
```

## Timed Permissions

### Add a timed permission

```java
import java.time.Duration;

api.users().modify(uuid, modifier -> modifier
        .allowTimedPermission("essentials.fly", Duration.ofDays(7)));
```

### Add a world-scoped timed permission

```java
import java.time.Duration;

api.users().modify(uuid, modifier -> modifier
        .allowTimedPermission("event.boost", ContextKeys.WORLD, "event", Duration.ofHours(1)));
```

## User Lifecycle

### Find a user (never creates)

```java
api.users().find(uuid).thenAccept(optionalUser -> {
    if (optionalUser.isPresent()) {
        var user = optionalUser.get();
        // User exists
    } else {
        // User not found
    }
});
```

### Get or create a user

```java
api.users().find(uuid).thenAccept(optionalUser -> {
    optionalUser.ifPresentOrElse(
        user -> {
            // User exists, use it
        },
        () -> {
            // Create new user
            api.users().modify(uuid, modifier -> {
                // Initialize user
            });
        }
    );
});
```

### Get user groups

```java
api.users().find(uuid).thenAccept(optionalUser -> {
    optionalUser.ifPresent(user -> {
        var groups = user.groups();
        for (var group : groups) {
            getLogger().info("Group: " + group.group());
        }
    });
});
```

### Get user name

```java
api.users().find(uuid).thenAccept(optionalUser -> {
    optionalUser.ifPresent(user -> {
        String name = user.name();
        getLogger().info("User: " + name);
    });
});
```

### Get user UUID

```java
api.users().find(uuid).thenAccept(optionalUser -> {
    optionalUser.ifPresent(user -> {
        UUID userUuid = user.uniqueId();
        getLogger().info("UUID: " + userUuid);
    });
});
```

### Set group weight

```java
api.groups().modify("vip", modifier -> modifier
        .setWeight(100));
```

### Get group parents

```java
api.groups().find("helper").thenAccept(optionalGroup -> {
    optionalGroup.ifPresent(group -> {
        var parents = group.parents();
        for (var parent : parents) {
            getLogger().info("Parent: " + parent.group());
        }
    });
});
```

### Get group name and weight

```java
api.groups().find("vip").thenAccept(optionalGroup -> {
    optionalGroup.ifPresent(group -> {
        getLogger().info("Group: " + group.name());
        group.weight().ifPresent(weight -> {
            getLogger().info("Weight: " + weight);
        });
    });
});
```

## Permission Resolution

### Check effective permissions

```java
api.users().find(uuid).thenAccept(optionalUser -> {
    optionalUser.ifPresent(user -> {
        var resolved = api.resolvers().resolve(user, QueryOptions.global());
        
        // Get all effective permissions
        var permissions = resolved.permissions();
        getLogger().info("User permissions: " + permissions);
    });
});
```

### Check inherited permissions

```java
var query = QueryOptions.builder()
        .includeInheritance(true)
        .build();

var resolved = api.resolvers().resolve(user, query);
```

## Context Registration

### Register a custom context type

```java
var contexts = api.contexts();

var registration = contexts.registry().registerContextType(
    "flying", 
    () -> List.of("false", "true")
);
```

### Register a context calculator

```java
var calculatorRegistration = contexts.registerCalculator((uuid, consumer) -> {
    // Calculate context value for this user
    boolean isFlying = getPlayer(uuid).isFlying();
    consumer.accept("flying", Boolean.toString(isFlying));
});
```

### Get contexts for a user

```java
var userContexts = contexts.contexts(uuid);
```

### Get query options for a user

```java
var queryOptions = contexts.queryOptions(uuid);
```

## Event Listening

### Subscribe to modification events

```java
import dev.rono.permissions.api.event.ModificationEvent;

var subscription = api.events().subscribe(ModificationEvent.class, event -> {
    getLogger().info("Entity modified: " + event.previous() + " -> " + event.current());
});

// Later, unsubscribe
subscription.close();
```

## Legacy API

### Using PermissionsEx 1.x API

```java
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

var pm = PermissionsEx.getPermissionManager();
PermissionUser user = pm.getUser(player);
boolean ok = pm.has(player, "myplugin.use", player.getWorld().getName());
```

## Which API?

| Situation | API |
|-----------|-----|
| New plugin | Modern — `PexProvider.get()` |
| World-scoped checks | `QueryOptions` with `ContextKeys` |
| Legacy plugin compatibility | Legacy — `PermissionsEx.getUser()` |
| Custom context resolution | `ContextManager` and calculators |

## Examples

See the `example-plugin/` and `example-legacy-plugin/` directories for complete working examples.

---

**Full class reference:** [Javadoc](javadoc.md) · [API Reference](api-reference/) · [Overview](overview.md)