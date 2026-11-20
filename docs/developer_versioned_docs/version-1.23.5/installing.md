---
sidebar_position: 1
---

# Installing

Add PermissionsExPlus to your plugin build as a **provided** / `compileOnly` dependency. The API ships inside the server plugin jar; do not shade or relocate it into your plugin.

## Maven (JitPack)

```markup
<repositories>
  <repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
  </repository>
</repositories>

<dependencies>
  <dependency>
    <groupId>com.github.rowan-smith</groupId>
    <artifactId>PermissionsExPlus</artifactId>
    <version>STABLE-1.23.5</version>
    <scope>provided</scope>
  </dependency>
</dependencies>
```

Use a [release tag](https://github.com/rowan-smith/PermissionsExPlus/releases) such as `STABLE-1.23.5`, or a commit hash. Check [jitpack.io/#rowan-smith/PermissionsExPlus](https://jitpack.io/#rowan-smith/PermissionsExPlus) for build status.

Project POM coordinates (if you publish/install the artifact yourself):

| Field | Value |
|-------|-------|
| Group ID | `ru.tehkode` |
| Artifact ID | `PermissionsEx` |
| Version | matches the release (e.g. `1.23.5`) |

## Gradle (Kotlin DSL)

```kotlin
repositories {
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("com.github.rowan-smith:PermissionsExPlus:STABLE-1.23.5")
}
```

## Gradle (Groovy)

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    compileOnly 'com.github.rowan-smith:PermissionsExPlus:STABLE-1.23.5'
}
```

## Local jar

For a one-off build against a downloaded release jar:

```markup
<dependency>
  <groupId>ru.tehkode</groupId>
  <artifactId>PermissionsEx</artifactId>
  <version>1.23.5</version>
  <scope>system</scope>
  <systemPath>${project.basedir}/libs/PermissionsExPlus.jar</systemPath>
</dependency>
```

Prefer JitPack or a Maven repository over `system` scope when you can.

## Package imports

```text
ru.tehkode.permissions.PermissionManager
ru.tehkode.permissions.PermissionUser
ru.tehkode.permissions.PermissionGroup
ru.tehkode.permissions.PermissionEntity
ru.tehkode.permissions.bukkit.PermissionsEx
ru.tehkode.permissions.events.PermissionEntityEvent
ru.tehkode.permissions.exceptions.RankingException
ru.tehkode.permissions.exceptions.PermissionsNotAvailable
```

Next: [Depending on PermissionsExPlus](depending).
