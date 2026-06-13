#!/usr/bin/env bash
set -euo pipefail

REPO=/workspace
TAG="$1"
OUT_VERSION="$2"
WORKTREE="/tmp/pex-${OUT_VERSION}"
OUT_DIR="${REPO}/apidocs/${OUT_VERSION}"
LIBS=/tmp/javadoc-libs

prepare_libs() {
  mkdir -p "$LIBS"
  if [ ! -f "$LIBS/bukkit-1.8-R0.1-20150227.224714-194.jar" ]; then
    mvn -s /tmp/maven-settings.xml dependency:copy -Dartifact=org.bukkit:bukkit:1.8-R0.1-SNAPSHOT:jar -DoutputDirectory="$LIBS" -q
  fi
  for dep in \
    "com.google.guava:guava:17.0" \
    "commons-dbcp:commons-dbcp:1.4" \
    "commons-pool:commons-pool:1.5.4" \
    "org.yaml:snakeyaml:1.9" \
    "commons-lang:commons-lang:2.6" \
    "com.googlecode.json-simple:json-simple:1.1.1"; do
    artifact=$(echo "$dep" | tr ':' '/')
    name=$(basename "$artifact")
    if ! ls "$LIBS"/${name}*.jar >/dev/null 2>&1; then
      mvn -s /tmp/maven-settings.xml dependency:copy -Dartifact="${dep}:jar" -DoutputDirectory="$LIBS" -q
    fi
  done

  if [ ! -f "$LIBS/netevents-stub.jar" ]; then
    mkdir -p /tmp/netevents-stub/com/zachsthings/netevents
    echo 'package com.zachsthings.netevents; public class NetEvents {}' > /tmp/netevents-stub/com/zachsthings/netevents/NetEvents.java
    echo 'package com.zachsthings.netevents; public class NetEventsPlugin {}' > /tmp/netevents-stub/com/zachsthings/netevents/NetEventsPlugin.java
    javac -d /tmp/netevents-stub /tmp/netevents-stub/com/zachsthings/netevents/*.java
    jar cf "$LIBS/netevents-stub.jar" -C /tmp/netevents-stub .
  fi

  if [ ! -f "$LIBS/mojang-stub.jar" ]; then
    mkdir -p /tmp/mojang-stub/com/mojang/api/profiles
    cat > /tmp/mojang-stub/com/mojang/api/profiles/Profile.java << 'EOF'
package com.mojang.api.profiles;
public interface Profile { String getName(); }
EOF
    cat > /tmp/mojang-stub/com/mojang/api/profiles/ProfileRepository.java << 'EOF'
package com.mojang.api.profiles;
public interface ProfileRepository { Profile[] findProfilesByNames(String[] names); }
EOF
    cat > /tmp/mojang-stub/com/mojang/api/profiles/HttpProfileRepository.java << 'EOF'
package com.mojang.api.profiles;
public class HttpProfileRepository implements ProfileRepository {
    public Profile[] findProfilesByNames(String[] names) { return new Profile[0]; }
}
EOF
    javac -d /tmp/mojang-stub /tmp/mojang-stub/com/mojang/api/profiles/*.java
    jar cf "$LIBS/mojang-stub.jar" -C /tmp/mojang-stub .
  fi

  if [ ! -f "$LIBS/updater-stub.jar" ]; then
    mkdir -p /tmp/updater-stub/net/gravitydevelopment/updater
    echo 'package net.gravitydevelopment.updater; public class Updater { public Updater() {} }' > /tmp/updater-stub/net/gravitydevelopment/updater/Updater.java
    javac -d /tmp/updater-stub /tmp/updater-stub/net/gravitydevelopment/updater/Updater.java
    jar cf "$LIBS/updater-stub.jar" -C /tmp/updater-stub .
  fi
}

echo "Building Javadoc for ${OUT_VERSION} (${TAG})"
prepare_libs

git -C "$REPO" worktree remove "$WORKTREE" --force 2>/dev/null || true
git -C "$REPO" worktree add "$WORKTREE" "$TAG" 2>&1

CP=$(echo "$LIBS"/*.jar | tr ' ' ':')
rm -rf "$OUT_DIR"
mkdir -p "$OUT_DIR"

cd "$WORKTREE"
javadoc -d "$OUT_DIR" \
  -sourcepath src/main/java \
  -classpath "$CP" \
  -subpackages ru.tehkode \
  -Xdoclint:none -quiet

test -f "${OUT_DIR}/index.html"
echo "OK: ${OUT_VERSION} -> ${OUT_DIR}"

git -C "$REPO" worktree remove "$WORKTREE" --force
