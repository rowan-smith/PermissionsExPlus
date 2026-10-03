# PermissionsEx Command Adapter

## Legacy command compatibility shim

Provides the legacy `/pex`, `/promote`, and `/demote` command formats while delegating
every lookup and mutation to `PermissionsExPlusApi`. It intentionally publishes no
`ru.tehkode.permissions` API contracts; consumers needing the legacy binary API must
use `PermissionsExApiAdapter`.

## Implementation
- The command adapter imports `PermissionsExPlus/Core` directly so it can
  use `PexImplProvider.get()` to enable and disable existing command framework,
  essentially we use an **unsupported** api to use this.
- `api.commands().clear()` is used to clear the existing command set and
  `api.commands().register()` is used to register the new commands.

## Hard dependency
- PermissionsExPlus
