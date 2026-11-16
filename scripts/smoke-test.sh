#!/usr/bin/env bash
# Boots a real Minecraft server in Docker with PermissionsExPlus installed and
# drives it over RCON. Fails on any plugin startup error or command mismatch.
#
# usage: scripts/smoke-test.sh <type> <version> <java-tag> <plugin.jar> [adapter.jar ...]
#   type       PAPER or SPIGOT (itzg/minecraft-server TYPE), SPONGE7 or SPONGE8
#   version    Minecraft version; for SPONGE7/SPONGE8 the full Sponge build,
#              e.g. 1.12.2-7.4.7 or 1.16.5-8.2.1-RC2708
#   java-tag   itzg/minecraft-server image tag: java8, java17, java21, ...
#
# With the command adapter installed, the original PEX command syntax is tested;
# otherwise the modern PermissionsExPlus commands are.
#
# examples:
#   scripts/smoke-test.sh PAPER 1.8.8 java8 PermissionsExPlus/SpigotLegacy/target/PermissionsExPlus-SpigotLegacy-*.jar
#   scripts/smoke-test.sh PAPER 1.12.2 java8 <legacy.jar> <api-adapter.jar> <command-adapter.jar>
#   scripts/smoke-test.sh SPONGE7 1.12.2-7.4.7 java8 PermissionsExPlus/Sponge7/target/PermissionsExPlus-Sponge7-*.jar
set -euo pipefail

type="$1"; version="$2"; java="$3"; shift 3
name="pex-smoke-${version//./-}-$$"
data="$(mktemp -d)"
legacy_commands=false

# Sponge loads plugins from mods/; Bukkit servers from plugins/.
case "$type" in
    SPONGE*) plugin_dir="$data/mods" ;;
    *) plugin_dir="$data/plugins" ;;
esac

mkdir -p "$plugin_dir"
for jar in "$@"; do
    cp "$jar" "$plugin_dir/"
    case "$(basename "$jar")" in PermissionsExCommandAdapter*) legacy_commands=true ;; esac
done

cleanup() {
    docker rm -f "$name" >/dev/null 2>&1 || true
    rm -rf "$data" 2>/dev/null || true
}
trap cleanup EXIT

case "$type" in
    SPONGE7)
        server=( -e TYPE=SPONGEVANILLA -e VERSION="${version%%-*}" -e SPONGEVERSION="$version" )
        ;;
    SPONGE8)
        # The image cannot resolve Sponge 8 release-candidate builds; fetch the official jar.
        curl -sfL -o "$data/spongevanilla.jar" \
            "https://repo.spongepowered.org/repository/maven-releases/org/spongepowered/spongevanilla/$version/spongevanilla-$version-universal.jar"
        server=( -e TYPE=CUSTOM -e CUSTOM_SERVER=/data/spongevanilla.jar )
        ;;
    *)
        server=( -e TYPE="$type" -e VERSION="$version" )
        ;;
esac

# SpongeVanilla 8 remaps the Minecraft jar on first boot and stalls below ~2 GB of heap.
case "$type" in SPONGE8) memory=3G ;; *) memory=1500M ;; esac

start_server() {
    docker run -d --name "$name" \
        -e EULA=TRUE "${server[@]}" -e ONLINE_MODE=FALSE -e MEMORY="$memory" \
        -e ENABLE_RCON=true -e RCON_PASSWORD=pex \
        -v "$data:/data" "itzg/minecraft-server:$java" >/dev/null
}

start_server
restarted=false

echo "Waiting for $type $version ($java) to start..."
for _ in $(seq 1 600); do  # first boots download and patch the server: allow 20 minutes
    if docker logs "$name" 2>&1 | grep -qE 'Done \([0-9.]+s\)!'; then
        break
    fi
    if [ "$(docker inspect -f '{{.State.Running}}' "$name")" != "true" ]; then
        # SpongeVanilla 1.12 downloads its launcher libraries on first boot but builds its
        # classpath before they exist, so the very first start always fails once.
        if [ "$type" = SPONGE7 ] && [ "$restarted" = false ] && docker logs "$name" 2>&1 | grep -q 'launchwrapper/Launch'; then
            restarted=true
            docker rm -f "$name" >/dev/null
            start_server
            continue
        fi
        docker logs "$name" 2>&1 | tail -50
        echo "FAIL: server exited during startup"
        exit 1
    fi
    sleep 2
done

log() { docker logs "$name" 2>&1; }

# Bukkit logs "[PermissionsExPlus] API ready"; Sponge logs "[permissionsexplus]: API ready".
if ! log | grep -qiE 'permissionsexplus\]:? API ready'; then
    log | tail -60
    echo "FAIL: PermissionsExPlus did not report 'API ready'"
    exit 1
fi

failures=0

rcon() {
    docker exec "$name" rcon-cli "$1" 2>&1 | sed 's/\x1b\[[0-9;]*m//g; s/§.//g'
}

expect() { # <command> <regex>
    local output
    output="$(rcon "$1")"
    if printf '%s' "$output" | grep -qE "$2"; then
        echo "ok   /$1"
    else
        echo "FAIL /$1 -> expected /$2/, got:"
        printf '%s\n' "$output" | sed 's/^/       /'
        failures=$((failures + 1))
    fi
}

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
