#!/usr/bin/env bash
# Boots a real Minecraft server or proxy in Docker with PermissionsExPlus installed
# and drives it from its console. Fails on any plugin startup error or command
# mismatch.
#
# usage: scripts/smoke-test.sh <type> <version> <java-tag> <plugin.jar> [adapter.jar ...]
#   type       PAPER or SPIGOT (itzg/minecraft-server TYPE), SPONGE7, SPONGE8, SPONGE9,
#              VELOCITY or BUNGEECORD (itzg/mc-proxy TYPE)
#   version    Minecraft version; for Sponge the full build (e.g. 1.12.2-7.4.7,
#              1.16.5-8.2.1-RC2708, 1.18.2-9.0.0-RC1306); for proxies "latest" or a
#              Velocity version such as 3.4.0-SNAPSHOT
#   java-tag   image tag: java8, java17, java21, ...
#
# With the command adapter installed, the original PEX command syntax is tested;
# otherwise the modern PermissionsExPlus commands are.
#
# examples:
#   scripts/smoke-test.sh PAPER 1.8.8 java8 PermissionsExPlus/SpigotLegacy/target/PermissionsExPlus-SpigotLegacy-*.jar
#   scripts/smoke-test.sh PAPER 1.12.2 java8 <legacy.jar> <api-adapter.jar> <command-adapter.jar>
#   scripts/smoke-test.sh SPONGE7 1.12.2-7.4.7 java8 PermissionsExPlus/Sponge7/target/PermissionsExPlus-Sponge7-*.jar
#   scripts/smoke-test.sh VELOCITY latest java17 PermissionsExPlus/Velocity/target/PermissionsExPlus-Velocity-*.jar
set -euo pipefail

type="$1"; version="$2"; java="$3"; shift 3
name="pex-smoke-$(echo "$type-$version" | tr '.A-Z' '-a-z')-$$"
work="$(mktemp -d)"
legacy_commands=false

cleanup() {
    docker rm -f -v "$name" >/dev/null 2>&1 || true
    rm -rf "$work" 2>/dev/null || true
}
trap cleanup EXIT

# Where the image keeps its data, and its staging directory for plugins: files placed
# in /plugins (or /mods) are copied into the data directory at startup and owned by
# the server user, which files copied straight into the data directory would not be.
case "$type" in
    VELOCITY | BUNGEECORD) image="itzg/mc-proxy:$java"; root=/server; staging=/plugins; proxy=true ;;
    SPONGE*) image="itzg/minecraft-server:$java"; root=/data; staging=/mods; proxy=false ;;
    *) image="itzg/minecraft-server:$java"; root=/data; staging=/plugins; proxy=false ;;
esac

server=( -e TYPE="$type" -e VERSION="$version" )
memory=1500M
case "$type" in
    SPONGE7)
        server=( -e TYPE=SPONGEVANILLA -e VERSION="${version%%-*}" -e SPONGEVERSION="$version" ) ;;
    SPONGE8 | SPONGE9)
        # The image cannot resolve Sponge 8+ release-candidate builds; fetch the official jar.
        mkdir -p "$work/data"
        curl -sfL -o "$work/data/spongevanilla.jar" \
            "https://repo.spongepowered.org/repository/maven-releases/org/spongepowered/spongevanilla/$version/spongevanilla-$version-universal.jar"
        server=( -e TYPE=CUSTOM -e CUSTOM_SERVER=/data/spongevanilla.jar )
        # SpongeVanilla 8+ remaps the Minecraft jar on first boot and stalls below ~2 GB of heap.
        memory=3G ;;
    VELOCITY)
        server=( -e TYPE=VELOCITY )
        [ "$version" != latest ] && server+=( -e VELOCITY_VERSION="$version" ) ;;
    BUNGEECORD)
        server=( -e TYPE=BUNGEECORD ) ;;
esac

mkdir -p "$work/staging"
for jar in "$@"; do
    cp "$jar" "$work/staging/"
    case "$(basename "$jar")" in PermissionsExCommandAdapter*) legacy_commands=true ;; esac
done

# The data directory is a container volume (fast on every OS, unlike bind mounts on
# Windows/macOS); files are copied into the created container before the first start.
docker create --name "$name" -i -v "$root" \
    -e EULA=TRUE "${server[@]}" -e ONLINE_MODE=FALSE -e MEMORY="$memory" \
    -e ENABLE_RCON=true -e RCON_PASSWORD=pex \
    "$image" >/dev/null
docker cp "$work/staging/." "$name:$staging/"
if [ -d "$work/data" ]; then
    docker cp "$work/data/." "$name:$root/"
fi
docker start "$name" >/dev/null
restarted=false

echo "Waiting for $type $version ($java) to start..."
ready='Done \([0-9.]+s\)!'
[ "$proxy" = true ] && ready='Done \([0-9.]+s\)!|Listening on'
for _ in $(seq 1 600); do  # first boots download and patch the server: allow 20 minutes
    if docker logs "$name" 2>&1 | grep -qE "$ready"; then
        break
    fi
    if [ "$(docker inspect -f '{{.State.Running}}' "$name")" != "true" ]; then
        # SpongeVanilla 1.12 downloads its launcher libraries on first boot but builds its
        # classpath before they exist, so the very first start always fails once.
        if [ "$type" = SPONGE7 ] && [ "$restarted" = false ] && docker logs "$name" 2>&1 | grep -q 'launchwrapper/Launch'; then
            restarted=true
            docker start "$name" >/dev/null
            continue
        fi
        docker logs "$name" 2>&1 | tail -50
        echo "FAIL: server exited during startup"
        exit 1
    fi
    sleep 2
done

log() { docker logs "$name" 2>&1; }

# Bukkit logs "[PermissionsExPlus] API ready"; Sponge "[permissionsexplus]: API ready".
if ! log | grep -qiE 'permissionsexplus\]:? API ready'; then
    log | tail -60
    echo "FAIL: PermissionsExPlus did not report 'API ready'"
    exit 1
fi

failures=0

# Servers have RCON built in; the proxy image adds an RCON plugin (which needs Java 21+).
# 127.0.0.1 explicitly: the proxy RCON plugins listen on IPv4 only. Output is joined
# onto one line with single spaces because BungeeCord's RCON plugin returns each text
# component on its own line.
console() {
    docker exec "$name" rcon-cli --host 127.0.0.1 --port 25575 --password pex "$1" 2>&1 \
        | sed 's/\x1b\[[0-9;]*m//g; s/§.//g' | tr '\n' ' ' | tr -s ' '
}

expect() { # <command> <regex>
    local output
    output="$(console "$1" || true)"
    if printf '%s' "$output" | grep -qE "$2"; then
        echo "ok   /$1"
    else
        echo "FAIL /$1 -> expected /$2/, got:"
        printf '%s\n' "$output" | sed 's/^/       /'
        failures=$((failures + 1))
    fi
}

# Proxies register the modern command as /pex too, but Velocity and Bungee reserve
# a few names; the root is the same on every platform.
if [ "$legacy_commands" = true ]; then
    expect "pex group smoke create" "created|Group smoke"
    expect "pex group smoke add smoke.node" "smoke.node"
    expect "pex reload" "reload"
    # The command adapter must still own /pex after a reload.
    expect "pex group smoke add smoke.after.reload" "smoke.after.reload"
    expect "pex group smoke list" "smoke.after.reload"
else
    expect "pex group smoke create" "Created group smoke"
    expect "pex group smoke permissions add smoke.node" "Added permission smoke.node"
    expect "pex group smoke permissions check smoke.node" "ALLOWED"
    expect "pex reload" "reloaded"
    expect "pex group smoke permissions list" "smoke.node"
fi

# Any stack trace from PermissionsExPlus or the adapters is a failure.
if log | grep -E '^\s+at (dev\.rono|ru\.tehkode)\.' >/dev/null; then
    log | grep -B5 -A15 -E 'Exception|Error' | grep -vE 'Failed to read library list' | head -80
    echo "FAIL: stack trace from PermissionsExPlus in server log"
    failures=$((failures + 1))
fi

if [ "$failures" -gt 0 ]; then
    echo "FAILED: $failures check(s) on $type $version"
    exit 1
fi

echo "PASSED: $type $version ($java)"
