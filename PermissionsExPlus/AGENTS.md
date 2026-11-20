# Platform Implementation Rules

- Platform modules integrate the shared Core with their server runtime.
- Keep permission decisions, persistence and shared business logic in Core.
- Translate platform identities, lifecycle, scheduling, events and commands at the boundary.
- Treat Spigot and Paper as separate implementations; do not assume Paper APIs on Spigot.
- Do not make legacy adapters a dependency of non-Bukkit platforms.
- Respect each module's Maven Java release and target platform API.
- Test plugin enable/disable, command registration and player permission checks on relevant servers.
