---
apply: always
---

## Module boundary rules
- Keep api free of platform-specific implementation details.
- Keep core platform-agnostic where possible.
- Put Bukkit/Spigot/Paper API usage in the spigot module unless the existing project structure explicitly does otherwise.
- Do not move platform-specific code into api or shared core logic without approval.
- Prefer adapting platform events, commands, and schedulers in spigot and delegating business logic into core services.

## 20. Fill-in project section
Replace the placeholders below with repo-specific details:

- This project is: a multi-module Java Minecraft server plugin built with Maven
- Main feature folders/modules (nested under concern groups):
    - `api/permissionsex-core-api`, `api/permissionsex-api`, `api/permissionsex-api-bukkit`: public interfaces, shared contracts, exposed types, extension points
    - `platform/permissionsex-core`: shared business logic, services, managers, configuration handling, persistence abstractions, reusable utilities
    - `platform/permissionsex-spigot`: Spigot/Paper/Bukkit platform bootstrap, command registration, listener registration, platform adapters, scheduler integration