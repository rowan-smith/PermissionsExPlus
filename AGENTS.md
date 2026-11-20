# PermissionsExPlus — Repository Rules

## Architecture
- `PermissionsExPlusApi` is the stable, platform-independent public contract.
- `PermissionsExPlus/Core` implements that contract and owns permission behaviour.
- Platform modules adapt Core to server-specific lifecycles, APIs and threading.
- Legacy API and command adapters preserve original PermissionsEx compatibility on Spigot/Paper only.
- Dependencies must point toward API/Core, never from API/Core into a platform or legacy adapter.
- Legacy adapters may use implementation-only hooks where necessary; do not add those hooks to the public API.

## Compatibility
- Preserve existing public binary and behavioural contracts unless explicitly approved.
- Check each module's actual Java release and supported server versions before using new APIs or dependencies.
- Keep storage formats, configuration and migration behaviour backwards-compatible.

## Style
- Follow `config/eclipse-java-formatter.xml` and the existing Spotless Maven configuration.
- Prefer clear names, small focused methods and explicit behaviour over clever abstractions.
- Do not reformat or refactor unrelated code.

## Verification
- Add regression tests for bug fixes and changed behaviour.
- Run relevant Maven tests and checks; use real-server tests when platform behaviour changes.
- Report what was tested and what could not be verified.
