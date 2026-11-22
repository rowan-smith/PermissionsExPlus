#!/usr/bin/env bash
# Boots a real Minecraft server or proxy in Docker with PermissionsExPlus installed
# and drives it from its console. Fails on any plugin startup error or command
# mismatch.
#
# usage: scripts/smoke-test.sh <type> <version> <java-tag> <plugin.jar> [adapter.jar ...]
#   type       PAPER, SPIGOT (built with BuildTools) or CRUCIBLE (1.7.10), all
#              itzg/minecraft-server TYPEs; SPONGE7, SPONGE8, SPONGE9; VELOCITY or
#              BUNGEECORD (itzg/mc-proxy TYPE)
#   version    Minecraft version; for Sponge the full build (e.g. 1.12.2-7.4.7,
#              1.16.5-8.2.1-RC2708, 1.18.2-9.0.0-RC1306); for proxies "latest" or a
#              Velocity version such as 3.4.0-SNAPSHOT
#   java-tag   image tag: java8, java17, java21, ...
#
# With the command adapter installed, the original PEX command syntax is tested;
# otherwise the modern PermissionsExPlus commands are.
#
# environment:
#   PLAYER_TEST=1  also join with a headless player and check real permission checks
#   KEEP=1         leave the containers running afterwards, for debugging
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
legacy_probe=false

bot="$name-bot"
network="$name-net"

cleanup() {
    if [ "${KEEP:-0}" = 1 ]; then
        echo "KEEP=1: leaving containers $name and $bot running"
        return
    fi
    docker rm -f -v "$bot" "$name" >/dev/null 2>&1 || true
    docker network rm "$network" >/dev/null 2>&1 || true
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
    SPIGOT)
        # Build Spigot with SpigotMC's BuildTools rather than downloading a third-party mirror.
        server=( -e TYPE=SPIGOT -e VERSION="$version" -e BUILD_FROM_SOURCE=true ) ;;
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

# Original PermissionsEx data for the import check: a group, a timed membership for
# the player bot (offline-mode UUID of PexBot) and world inheritance.
mkdir -p "$work/data/pex-import"
cat > "$work/data/pex-import/permissions.yml" <<YAML
groups:
  imported:
    options:
      rank: '10'
    permissions:
    - imported.node
users:
  3533c1af-4de4-3cc4-b333-68d89b418277:
    group:
    - imported
    options:
      name: PexBot
      group-imported-until: '$(( $(date +%s) + 86400 ))'
worlds:
  world_nether:
    inheritance:
    - world
YAML

mkdir -p "$work/staging"
for jar in "$@"; do
    cp "$jar" "$work/staging/"
    case "$(basename "$jar")" in
        PermissionsExCommandAdapter*) legacy_commands=true ;;
        LegacyPexProbe*) legacy_probe=true ;;
    esac
done

# The data directory is a container volume (fast on every OS, unlike bind mounts on
# Windows/macOS); files are copied into the created container before the first start.
# MSYS_NO_PATHCONV: Git Bash on Windows would rewrite in-container paths such as
# CUSTOM_SERVER=/data/... into Windows paths. It has no effect elsewhere.
MSYS_NO_PATHCONV=1 docker create --name "$name" -i -v "$root" \
    -e EULA=TRUE "${server[@]}" -e ONLINE_MODE=FALSE -e MEMORY="$memory" \
    -e ENABLE_RCON=true -e RCON_PASSWORD=pex \
    "$image" >/dev/null
docker cp "$work/staging/." "$name:$staging/"
# Entry by entry: "docker cp dir/. dest" nests the directory on Docker for Windows.
for entry in "$work/data"/*; do
    docker cp "$entry" "$name:$root/"
done
docker start "$name" >/dev/null
restarted=false

echo "Waiting for $type $version ($java) to start..."
ready='Done \([0-9.]+s\)!'
[ "$proxy" = true ] && ready='Done \([0-9.]+s\)!|Listening on'
for _ in $(seq 1 600); do  # first boots download and patch the server: allow 20 minutes
    if docker logs "$name" 2>&1 | grep -E "$ready" >/dev/null; then
        break
    fi
    if [ "$(docker inspect -f '{{.State.Running}}' "$name")" != "true" ]; then
        # SpongeVanilla 1.12 downloads its launcher libraries on first boot but builds its
        # classpath before they exist, so the very first start always fails once.
        if [ "$type" = SPONGE7 ] && [ "$restarted" = false ] && docker logs "$name" 2>&1 | grep 'launchwrapper/Launch' >/dev/null; then
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

# Under pipefail, `log | grep -q` fails whenever grep stops reading before the log
# ends (SIGPIPE), so greps over the log read all of it: `grep ... >/dev/null`.
log() { docker logs "$name" 2>&1; }

# The plugin's own lines, which the end of a long startup log no longer shows.
plugin_log() {
    echo "--- PermissionsExPlus and adapter log lines ---"
    log | grep -aiE 'permissionsex|exception|caused by' | grep -vE 'Failed to read library list' | head -80
    echo "---"
}

# Bukkit logs "[PermissionsExPlus] API ready"; Sponge "[permissionsexplus]: API ready".
if ! log | grep -iE 'permissionsexplus\]:? API ready' >/dev/null; then
    log | tail -60
    plugin_log
    echo "FAIL: PermissionsExPlus did not report 'API ready'"
    exit 1
fi

failures=0

# Servers have RCON built in; the proxy image adds an RCON plugin (which needs Java 21+).
# 127.0.0.1 explicitly: the proxy RCON plugins listen on IPv4 only. Output is joined
# onto one line with single spaces because BungeeCord's RCON plugin returns each text
# component on its own line.
#
# Older servers sometimes drop a new RCON connection before it is authenticated
# ("Failed to connect to RCON server"). The command was never sent then, so it is
# safe to send it again.
console() {
    local output attempt
    for attempt in 1 2 3 4 5; do
        output="$(docker exec "$name" rcon-cli --host 127.0.0.1 --port 25575 --password pex "$1" 2>&1 \
            | sed 's/\x1b\[[0-9;]*m//g; s/§.//g' | tr '\n' ' ' | tr -s ' ')"
        case "$output" in
            *"Failed to connect to RCON"*) sleep 1 ;;
            *) break ;;
        esac
    done
    printf '%s' "$output"
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
    expect "pex user NoSuchUser groups" "User not found: NoSuchUser"

    # Import original PermissionsEx data. The import runs off the server thread, so its
    # report goes to the sender after RCON has already returned; check the result instead.
    expect "pex import permissionsex $root/pex-import" "Importing PermissionsEx data"
    sleep 5
    expect "pex group imported permissions list" "imported.node"
    expect "pex user PexBot groups" "imported .*expires in (23h|1d)"
    expect "pex ladder default" "imported"
    expect "pex world world_nether" "world"

    # World inheritance commands, saved to worlds.yml.
    expect "pex world world_the_end inherit world world_nether" "now inherits from world, world_nether"
    expect "pex reload" "reloaded"
    expect "pex worlds" "world_the_end inherits world, world_nether"
    expect "pex world world_the_end clear" "no longer inherits"
fi

# With scripts/legacy-plugins installed (Vault 1.7.3 and LegacyPexProbe, both built
# against the original PermissionsEx API), plugins written for PermissionsEx 1.23.4 must
# work against the API adapter, directly and through Vault's own PermissionsEx hooks.
if [ "$legacy_probe" = true ]; then
    expect "legacyprobe" "PROBE PASSED"
fi

# PLAYER_TEST=1: a headless player (scripts/player-bot) joins and checks that granting and
# revoking a node through PermissionsExPlus changes what that player may do. This is the
# path real permission checks take (Bukkit injection, Sponge's PermissionService), which
# RCON, running as the console, bypasses. Servers only: a proxy needs a backend behind it.
if [ "${PLAYER_TEST:-0}" = 1 ] && [ "$proxy" = false ] && [ "$legacy_commands" = false ]; then
    script_dir="$(cd "$(dirname "$0")" && pwd)"

    docker network create "$network" >/dev/null
    docker network connect --alias mc "$network" "$name"
    # cd inside the container rather than -w /tmp, which Git Bash on Windows rewrites.
    docker create --name "$bot" --network "$network" node:20-alpine \
        sh -c 'cd /tmp && npm install --silent --no-audit --no-fund mineflayer@4 >/dev/null 2>&1 && node bot.js mc' >/dev/null
    cp "$script_dir/player-bot/bot.js" "$work/bot.js"
    docker cp "$work/bot.js" "$bot:/tmp/bot.js"
    docker start "$bot" >/dev/null

    for _ in $(seq 1 150); do
        docker logs "$bot" 2>&1 | grep -E 'SPAWNED|KICKED|ERROR|ENDED' >/dev/null && break
        sleep 2
    done

    if ! docker logs "$bot" 2>&1 | grep SPAWNED >/dev/null; then
        docker logs "$bot" 2>&1 | tail -20
        echo "FAIL: player bot did not join"
        failures=$((failures + 1))
    else
        # Runs a command as the player and prints only the chat that followed it.
        player() {
            local before
            before=$(docker logs "$bot" 2>&1 | wc -l)
            docker exec "$bot" sh -c "printf '%s\n' '$1' >> /tmp/commands"
            sleep 4
            docker logs "$bot" 2>&1 | tail -n +"$((before + 1))" | grep '^CHAT ' | tr '\n' ' '
        }

        check_player() { # <description> <regex-expected> <regex-forbidden>
            local output
            output="$(player '/pex groups' || true)"
            if printf '%s' "$output" | grep -qE "$2" && ! printf '%s' "$output" | grep -qE "$3"; then
                echo "ok   player: $1"
            else
                echo "FAIL player: $1 -> got: $output"
                failures=$((failures + 1))
            fi
        }

        denied='permission|Unknown command'
        check_player "denied /pex groups without pex.groups.list" "$denied" 'Groups:'
        console "pex user PexBot permissions add pex.groups.list" >/dev/null
        check_player "allowed /pex groups after the grant" 'Groups:' "$denied"
        console "pex user PexBot permissions remove pex.groups.list" >/dev/null
        check_player "denied again after the revoke" "$denied" 'Groups:'

        # A plain chat message runs every plugin's chat listeners; the server must still
        # broadcast it. (Checked in the server log: chat plugins may reformat it in ways
        # the bot cannot parse.)
        player 'hello from PexBot' >/dev/null || true
        if log | grep 'hello from PexBot' >/dev/null; then
            echo "ok   player: chat message delivered"
        else
            echo "FAIL player: chat message was not broadcast"
            failures=$((failures + 1))
        fi
    fi
fi

# A plugin failing to link, enable, handle an event or run a command is a failure when
# PermissionsEx, PermissionsExPlus or an adapter is involved (in the message or the stack),
# which covers third-party plugins built against the original PermissionsEx API. Errors
# from plugins using Bukkit APIs the server no longer has are only reported.
# shellcheck disable=SC2016
plugin_errors="$(log | sed 's/\x1b\[[0-9;]*m//g' | awk '
    function flush() {
        if (block != "") {
            if (block ~ /ru[.\/]tehkode|dev[.\/]rono/) { ours = ours block "\n" } else { other = other first "\n" }
        }
        block = ""
    }
    /NoSuchMethodError|NoSuchFieldError|NoClassDefFoundError|AbstractMethodError|IncompatibleClassChangeError|Could not pass event|Error occurred while (enabling|disabling)|Unhandled exception executing|generated an exception/ {
        flush(); block = $0 "\n"; first = $0; lines = 12; next
    }
    lines > 0 { block = block $0 "\n"; lines--; next }
    END { flush(); printf "%s@@OTHER@@\n%s", ours, other }')"
ours_errors="${plugin_errors%%@@OTHER@@*}"
other_errors="${plugin_errors#*@@OTHER@@}"
if [ -n "${ours_errors//[[:space:]]/}" ]; then
    printf '%s\n' "$ours_errors" | head -60
    echo "FAIL: a plugin failed against PermissionsEx / PermissionsExPlus"
    failures=$((failures + 1))
fi
if [ -n "${other_errors//[[:space:]]/}" ]; then
    echo "note: plugin errors unrelated to PermissionsEx (usually Bukkit APIs this server lacks):"
    printf '%s\n' "$other_errors" | grep -v '^$' | sort -u | head -10 | sed 's/^/       /'
fi

# Any stack trace from PermissionsExPlus or the adapters is a failure.
if log | grep -E '^\s+at (dev\.rono|ru\.tehkode)\.' >/dev/null; then
    log | grep -B5 -A15 -E 'Exception|Error' | grep -vE 'Failed to read library list' | head -80
    echo "FAIL: stack trace from PermissionsExPlus in server log"
    failures=$((failures + 1))
fi

if [ "$failures" -gt 0 ]; then
    plugin_log
    echo "FAILED: $failures check(s) on $type $version"
    exit 1
fi

echo "PASSED: $type $version ($java)"
