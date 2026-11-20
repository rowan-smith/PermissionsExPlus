# Legacy PermissionsEx API Adapter

- Preserve the original PermissionsEx public packages, signatures and observable behaviour.
- This adapter is for Spigot/Paper compatibility, not a new cross-platform API.
- Delegate to the new API where possible; use Core implementation hooks only for legacy-specific needs.
- Never expand `PermissionsExPlusApi` solely to satisfy legacy internals.
- Preserve legacy return values, exceptions, mutability expectations and side effects where supported.
- Compare binary signatures with the original PEX JAR and add behavioural regression tests.
- Do not silently remove unsupported legacy behaviour; document intentional gaps.
