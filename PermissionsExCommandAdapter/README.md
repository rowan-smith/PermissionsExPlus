# PermissionsEx Command Adapter

## Legacy command compatibility shim

Provides the legacy `/pex`, `/promote`, and `/demote` command formats while
delegating every lookup and mutation to PermissionsExPlus. It publishes no
`ru.tehkode.permissions` API contracts; consumers needing the legacy binary API
must use `PermissionsExApiAdapter`.

## Behaviour

- On enable, clears the PermissionsExPlus Cloud command tree via
  `api.commands().clear()`.
- Registers the original PEX `@Command` handlers through a local
  `CommandsManager`.
- All handlers call into `PexImplProvider` / `PexApi` (users, groups, ladders,
  resolvers).

## Hard dependency

- PermissionsExPlus
