# PermissionsExPlus bootstrap (merged jar)

Maven module **`permissionsex-bootstrap`** (`dev.rono.permissions:permissionsex-bootstrap`) emits a single installable jar:

**`bootstrap/target/PermissionsExPlus-{version}.jar`**

Maven still uses a normal module `artifactId` for the reactor; only the **on-disk jar name** is simplified as above (`build.finalName`).

## How routing works

- **Paper / Spigot** reads **`plugin.yml`** and loads **`ru.tehkode.permissions.spigot.bukkit.SpigotPermissionsExPlugin`**.
- **BungeeCord / Waterfall** reads **`bungee.yml`** and loads **`dev.rono.permissions.bungee.BungeePermissionsExPlugin`**.

Both descriptors are unpacked from the shaded Spigot and Bungee artifacts into this jar **without overwriting each other**. No Java “router” plugin is needed: **each loader only parses its own file** and ignores the other.

The **`ru.tehkode.permissions.bukkit.PermissionsEx`** type on the classpath is still the **static façade** (entry helpers) packaged in **`permissionsex-legacy-api`**; only the **`JavaPlugin` main class name** moved to **`PermissionsExPlugin`** to avoid duplicate type definitions across modules.

## Install

Copy **`PermissionsExPlus-{version}.jar`** into **`plugins/`** on backend servers **and/or** the proxy.

**Plugins folder must not contain a second PermissionsExPlus build.** Remove platform-only shaded jars before restart, including:

| Remove (examples) |
|---|
| `permissionsex-spigot-*.jar` |
| `permissionsex-bungee-*.jar` |
| Legacy: `PermissionsExPlus-spigot-*.jar`, `PermissionsExPlus-bungee-*.jar`, `PermissionsExPlus-bootstrap-*-universal.jar`, `ru.tehkode:permissionsex-*` |

Keep a **single** `PermissionsExPlus-*.jar` per server process.

## Build

From repo root:

```bash
mvn -pl bootstrap -am package
```

`spigot` and `bungee` must succeed first so **`permissionsex-spigot`** / **`permissionsex-bungee`** jars exist.
