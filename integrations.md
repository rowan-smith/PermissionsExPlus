---
layout: page
title: Integrations
permalink: /integrations/
---

PermissionsExPlus exposes **two compile surfaces** for companion plugins. Both talk to the same runtime manager on game servers.

| Surface | Maven artifact(s) | Package | Status |
|---------|-------------------|---------|--------|
| **Modern** | `permissionsex-api` | `dev.rono.permissions.api.*` | Active — new features land here |
| **Legacy (classic)** | `permissionsex-legacy-api`, optional `permissionsex-legacy-stub` | `ru.tehkode.permissions.*` | Frozen — baseline commit `628215f` |

## Which API should I use?

| Situation | Use |
|-----------|-----|
| New plugin | [Modern API]({{ site.baseurl }}/modern-api/) (`PermissionsEx.getApi()`) |
| Existing PEX 1.23.x hook plugin | [Legacy API]({{ site.baseurl }}/legacy-api/) — no recompile required for typical hooks |
| Static `PermissionsEx.getUser(...)` calls | Legacy API + `permissionsex-legacy-stub` |
| Permission change events on Spigot | Legacy Bukkit events (`ru.tehkode.permissions.events.*`) |
| Proxy (Bungee/Waterfall) | `PermissionsEx.getApi()` on proxy entry points |

## Setup

PEX is already on the server at runtime (`plugins/PermissionsExPlus-*.jar`). Your plugin only needs **compile-time** dependencies with `scope` **`provided`** — do **not** shade PEX into your jar.

Maven parent: **`dev.rono.permissions:PermissionsExPlus`** at version **`{{ site.version }}`**.

---

## Classic (legacy) API hook

For plugins originally written against PermissionsEx 1.23.x.

### Maven dependencies

```xml
<dependency>
  <groupId>dev.rono.permissions</groupId>
  <artifactId>permissionsex-legacy-api</artifactId>
  <version>{{ site.version }}</version>
  <scope>provided</scope>
</dependency>
<!-- Optional: only if you call PermissionsEx.getUser / getPermissionManager -->
<dependency>
  <groupId>dev.rono.permissions</groupId>
  <artifactId>permissionsex-legacy-stub</artifactId>
  <version>{{ site.version }}</version>
  <scope>provided</scope>
</dependency>
<dependency>
  <groupId>org.spigotmc</groupId>
  <artifactId>spigot-api</artifactId>
  <scope>provided</scope>
</dependency>
```

| Dependency | What it gives you |
|------------|-------------------|
| `permissionsex-legacy-api` | Types and contracts: `PermissionManager`, `PermissionUser`, events, `ru.tehkode.utils.*` |
| `permissionsex-legacy-stub` | Only the `PermissionsEx` static class for compile-time resolution |

### Minimal example

```java
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

@Override
public void onEnable() {
    if (!PermissionsEx.isAvailable()) {
        getLogger().warning("PEX not loaded");
        return;
    }
    PermissionManager mgr = PermissionsEx.getPermissionManager();
    getLogger().info("Backend: " + mgr.getBackend().getClass().getSimpleName());
}

void onJoin(Player player) {
    PermissionUser user = PermissionsEx.getUser(player);
    boolean allowed = PermissionsEx.getPermissionManager()
        .has(player, "my.node", player.getWorld().getName());
}
```

Full reference: [Legacy API]({{ site.baseurl }}/legacy-api/)

---

## Modern API hook

For new integrations that should not depend on the frozen `ru.tehkode.*` surface.

### Maven dependencies

```xml
<dependency>
  <groupId>dev.rono.permissions</groupId>
  <artifactId>permissionsex-api</artifactId>
  <version>{{ site.version }}</version>
  <scope>provided</scope>
</dependency>
<dependency>
  <groupId>org.spigotmc</groupId>
  <artifactId>spigot-api</artifactId>
  <scope>provided</scope>
</dependency>
```

### Runtime registration (Spigot/Paper)

```java
import dev.rono.permissions.api.service.PermissionService;
import ru.tehkode.permissions.bukkit.PermissionsEx;

@Override
public void onEnable() {
    if (!PermissionsEx.isAvailable()) {
        getLogger().warning("PermissionsEx is not available.");
        return;
    }
    var api = PermissionsEx.getApi();
    getLogger().info("PEX backend: " + api.backend().simpleName());
    getLogger().info("Users: " + api.users().count()
        + ", groups: " + api.groups().count());
}
```

On game servers, PEX registers **`PermissionsExApi`** on Bukkit **`ServicesManager`**. The same object also implements legacy **`PermissionManager`**.

### Minimal join handler

```java
public void onJoin(PlayerJoinEvent event) {
    var api = PermissionsEx.getApi();
    var user = api.getUserManager().getUser(event.getPlayer().getUniqueId());
    if (user.has("my.plugin.use")) {
        user.addPermission("joined.today");
        user.save();
    }
}
```

Full reference: [Modern API]({{ site.baseurl }}/modern-api/)

---

## Events

### Spigot (legacy Bukkit events)

Subscribe to `ru.tehkode.permissions.events.PermissionEntityEvent` and `PermissionSystemEvent`. These are published from bus dispatches via `SpigotEventPublisher`.

### Modern event bus

```java
var api = PermissionsEx.getApi();
api.events().subscribe(dispatch -> {
    // handle PermissionDispatch
});
```

---

## Sample plugins

| Module | API |
|--------|-----|
| `permissionsex-example-plugin` | Modern only |
| `permissionsex-example-legacy-plugin` | Legacy + stub |

Source: [github.com/{{ site.repo }}](https://github.com/{{ site.repo }}/tree/main/plugin)

## Related documentation

- [Modern API]({{ site.baseurl }}/modern-api/)
- [Legacy API]({{ site.baseurl }}/legacy-api/)
- [API Invariants]({{ site.baseurl }}/api-invariants/)
- [API Roadmap]({{ site.baseurl }}/api-roadmap/)
- [Developer Resources]({{ site.baseurl }}/developer-resources/)
