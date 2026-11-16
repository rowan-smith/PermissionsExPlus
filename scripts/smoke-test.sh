#!/usr/bin/env bash
# Boots a real Minecraft server in Docker with PermissionsExPlus installed and
# drives it over RCON. Fails on any plugin startup error or command mismatch.
#
# usage: scripts/smoke-test.sh <type> <minecraft-version> <java-tag> <plugin.jar> [adapter.jar ...]
#   type       PAPER or SPIGOT (itzg/minecraft-server TYPE)
#   java-tag   itzg/minecraft-server image tag: java8, java17, java21, ...
#
# With the command adapter installed, the original PEX command syntax is tested;
# otherwise the modern PermissionsExPlus commands are.
#
# examples:
#   scripts/smoke-test.sh PAPER 1.8.8 java8 PermissionsExPlus/SpigotLegacy/target/PermissionsExPlus-SpigotLegacy-*.jar
#   scripts/smoke-test.sh PAPER 1.12.2 java8 <legacy.jar> <api-adapter.jar> <command-adapter.jar>
set -euo pipefail

type="$1"; version="$2"; java="$3"; shift 3
name="pex-smoke-${version//./-}-$$"
data="$(mktemp -d)"
legacy_commands=false

mkdir -p "$data/plugins"
for jar in "$@"; do
    cp "$jar" "$data/plugins/"
    case "$(basename "$jar")" in PermissionsExCommandAdapter*) legacy_commands=true ;; esac
done

cleanup() {
    docker rm -f "$name" >/dev/null 2>&1 || true
    rm -rf "$data" 2>/dev/null || true
}
trap cleanup EXIT

docker run -d --name "$name" \
    -e EULA=TRUE -e TYPE="$type" -e VERSION="$version" -e ONLINE_MODE=FALSE -e MEMORY=1G \
    -e ENABLE_RCON=true -e RCON_PASSWORD=pex \
    -v "$data:/data" "itzg/minecraft-server:$java" >/dev/null

echo "Waiting for $type $version ($java) to start..."
for _ in $(seq 1 600); do  # first boot of old Paper patches the vanilla jar: allow 20 minutes
    if docker logs "$name" 2>&1 | grep -qE 'Done \([0-9.]+s\)!'; then
        break
    fi
    if [ "$(docker inspect -f '{{.State.Running}}' "$name")" != "true" ]; then
        docker logs "$name" 2>&1 | tail -50
        echo "FAIL: server exited during startup"
        exit 1
    fi
    sleep 2
done

log() { docker logs "$name" 2>&1; }

if ! log | grep -q 'PermissionsExPlus\] API ready'; then
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
