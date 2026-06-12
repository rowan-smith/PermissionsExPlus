# Platform compatibility (1.8.8 – 1.26.1)

PermissionsExPlus targets **Minecraft `1.8.8` through `1.26.1`** on Spigot/Paper and compatible forks.

## JVM requirements

| Server era | Typical Minecraft | Minimum JVM for **this build** |
|------------|-------------------|--------------------------------|
| Legacy     | 1.8.8 – 1.16.x    | **Java 21** (current artifact bytecode) |
| Modern     | 1.17+             | **Java 21** |

The plugin JAR is compiled with **Java 21**. Hosts must run a **Java 21+** runtime even when the Minecraft version is older. This matches current Paper/Spigot toolchain expectations for maintained forks.

## Bukkit loader

`plugin.yml` does **not** set `api-version`, so Bukkit attempts to load the plugin on pre-1.13 servers. Unsupported combinations log a warning from `ServerVersions` and continue with reflection-based shims where possible.

## Proxy

BungeeCord / Waterfall module targets the current Bungee API (`1.21-R0.3-SNAPSHOT` in the parent POM).

## Legacy hook plugins

Compile against:

- `permissionsex-legacy-api` — types, events, utils
- `permissionsex-legacy-stub` — `PermissionsEx` static entry points only

API reference: [api/LEGACY_API.md](api/LEGACY_API.md). New plugins: [api/MODERN_API.md](api/MODERN_API.md).

Runtime uses the live `ru.tehkode.permissions.bukkit.PermissionsEx` class from the deployed plugin jar.

## Verification

- Unit tests: `mvn test`
- Hook smoke test: `permissionsex-legacy-compat` module (MockBukkit + example hook plugin)
- Manual matrix: [REAL_SERVER_MATRIX.md](testing/REAL_SERVER_MATRIX.md)
