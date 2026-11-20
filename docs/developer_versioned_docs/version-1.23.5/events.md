---
sidebar_position: 6
---

# Events

PermissionsExPlus fires Bukkit events when permissions data or system state changes. Listen with a normal `@EventHandler`.

## Entity events

`PermissionEntityEvent` is fired for user and group changes.

```java
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import ru.tehkode.permissions.PermissionEntity;
import ru.tehkode.permissions.events.PermissionEntityEvent;

public class PexListener implements Listener {

    @EventHandler
    public void onEntity(PermissionEntityEvent event) {
        PermissionEntity entity = event.getEntity();
        PermissionEntityEvent.Action action = event.getAction();

        getLogger().info(event.getType() + " " + event.getEntityIdentifier()
                + " action=" + action);

        switch (action) {
            case PERMISSIONS_CHANGED:
            case INHERITANCE_CHANGED:
            case RANK_CHANGED:
                // refresh your plugin's caches
                break;
            default:
                break;
        }
    }
}
```

### Actions

| Action | When |
|--------|------|
| `PERMISSIONS_CHANGED` | Permissions list or timed permissions changed |
| `OPTIONS_CHANGED` | An option was set/cleared |
| `INHERITANCE_CHANGED` | Parents / group membership changed |
| `INFO_CHANGED` | Prefix, suffix, or similar info changed |
| `RANK_CHANGED` | Rank, ladder, or promote/demote |
| `DEFAULTGROUP_CHANGED` | Default-group flag changed |
| `WEIGHT_CHANGED` | Group weight changed |
| `SAVED` | Entity saved to backend |
| `REMOVED` | Entity removed from backend |

:::note
`TIMEDPERMISSION_EXPIRED` exists on the enum but is not fired today. Timed expiry updates permissions and fires `PERMISSIONS_CHANGED` instead.
:::

### Useful getters

```java
event.getAction();
event.getEntity();              // may reload from manager if needed
event.getEntityIdentifier();
event.getType();                // USER or GROUP
event.getSourceUUID();          // who triggered the change, when known
```

## System events

`PermissionSystemEvent` covers manager-wide changes.

```java
import ru.tehkode.permissions.events.PermissionSystemEvent;

@EventHandler
public void onSystem(PermissionSystemEvent event) {
    switch (event.getAction()) {
        case BACKEND_CHANGED:
        case RELOADED:
            // drop caches that depend on PEX data
            break;
        case WORLDINHERITANCE_CHANGED:
            break;
        case DEBUGMODE_TOGGLE:
            break;
        case REINJECT_PERMISSIBLES:
            break;
        default:
            break;
    }
}
```

### Actions

| Action | When |
|--------|------|
| `BACKEND_CHANGED` | Active backend switched |
| `RELOADED` | Manager reset / reload |
| `WORLDINHERITANCE_CHANGED` | World inheritance updated |
| `DEBUGMODE_TOGGLE` | Debug mode toggled |
| `REINJECT_PERMISSIBLES` | Superperms injectables refreshed |

## Registering

```java
@Override
public void onEnable() {
    if (!PermissionsEx.isAvailable()) {
        return;
    }
    getServer().getPluginManager().registerEvents(new PexListener(), this);
}
```
