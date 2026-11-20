# Public API Rules

- This module defines the stable, platform-independent contract for all implementations.
- Do not import Bukkit, Paper, Sponge, proxy APIs, Core or legacy adapter types.
- Do not expose implementation objects, internal lifecycle hooks or legacy-only methods.
- Preserve public method descriptors, return types, exceptions and documented behaviour.
- Keep Java 8 compatibility under the current module configuration.
- Use clear Javadoc for public contracts, nullability, threading and asynchronous completion.
- Add contract tests for changed API behaviour; check binary compatibility before release.
