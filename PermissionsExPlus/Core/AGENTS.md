# Core Rules

- Core implements the public API and owns permission resolution, inheritance, storage, caches and mutations.
- Do not depend on Bukkit, Paper, Sponge, Velocity, BungeeCord or legacy adapters.
- Keep implementation-only hooks out of `PermissionsExPlusApi`.
- Preserve permission precedence, contextual evaluation and explicit deny semantics.
- Keep snapshots immutable and mutations atomic from callers' perspective.
- Complete mutation futures only after required persistence and cache updates.
- Keep invalidation and event ordering deterministic; test concurrency and failure paths.
- Check Java 8 compatibility for Core code and runtime dependencies.
