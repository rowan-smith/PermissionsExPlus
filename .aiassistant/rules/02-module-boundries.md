---
apply: always
---

## Module boundary rules
- Keep api free of platform-specific implementation details.
- Keep core platform-agnostic where possible.
- Put Bukkit/Spigot/Paper API usage in the bukkit module unless the existing project structure explicitly does otherwise.
- Do not move platform-specific code into api or shared core logic without approval.
- Prefer adapting platform events, commands, and schedulers in bukkit and delegating business logic into core services.

## 20. Fill-in project section
Replace the placeholders below with repo-specific details:

- This project is: a multi-module Java Minecraft server plugin built with Maven
- Main feature folders/modules (flat at repository root):
    - `api-core`, `api`: public interfaces, shared contracts, exposed types, extension points
    - `legacy-api`, `legacy-stub`, `legacy-compat`: frozen classic API, compile stub, regression tests
    - `platform-api`: runtime bridge between engine and host (`PlatformAdapter`, scheduler, logging)
    - `common`: shared business logic, services, managers, configuration handling, persistence abstractions, reusable utilities
    - `bukkit`: Bukkit/Paper platform bootstrap, command registration, listener registration, platform adapters, scheduler integration
    - `bungee`, `velocity`, `sponge`: platform-specific translation layers only — no permission logic
    - `proxy-common`: shared proxy bootstrap for Bungee, Velocity, and Sponge
    - `universal`: merged deployable jar for all platforms
    - `example-plugin`, `example-legacy-plugin`: hook plugin samples
