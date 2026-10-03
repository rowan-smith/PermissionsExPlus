# PermissionsEx Api Adapter

## Binary Compatability Shim

* Provides a 100% binary api compatability shim for the original PermissionsEx API.
* This adapter allows for seamless integration with plugins that rely on the PermissionsEx API, ensuring compatibility with existing configurations and permissions systems.

### How it works

We delegate all API calls to the PermissionsExPlus API equivilents.

### Differences

* We have stripped out:
  * Configuration (handled by PermissionsExPlus)
  * Commands (original commands are handled by PermissionsExCommandAdapter)
  * Backends (handled by PermissionsExPlus)

* Configuration is handled by the PermissionsExPlus API.
  * This will include a `/pex backend import permissionsex` command or delegate in future.

## Implementation
- The api adapter imports `PermissionsExPlus/Core` directly so it can
  use `PexImplProvider.get()` essentially using an **unsupported** api.

## Hard Dependencies

- PermissionExPlus
