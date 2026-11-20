#!/usr/bin/env bash
# Prepares real plugins written for PermissionsEx 1.23.4, to run on a test server
# against the API adapter:
#   - Vault 1.7.3, whose built-in PermissionsEx hooks were compiled against the
#     original PermissionsEx API and are what Vault hands to every consumer;
#   - LegacyPexProbe (probe/), compiled against the PermissionsEx 1.23.4 API and
#     Vault, which exercises both and reports each check over /legacyprobe.
#
# usage: scripts/legacy-plugins/build.sh [output-dir]   (default target/legacy-plugins)
# Prints the paths of the two jars, one per line.
set -euo pipefail

root="$(git rev-parse --show-toplevel)"
here="$root/scripts/legacy-plugins"
out="${1:-$root/target/legacy-plugins}"
api="$root/PermissionsExApiAdapter/src/test/resources/baselines/PermissionsEx-1.23.4-api.jar"

vault_url="https://github.com/MilkBowl/Vault/releases/download/1.7.3/Vault.jar"
vault_sha256="a6b5ed97f43a5cf5bbaf00a7c8cd23c5afc9bd003f849875af8b36e6cf77d01d"

mkdir -p "$out"
cd "$out"

if [ ! -f Vault-1.7.3.jar ] || ! sha256sum Vault-1.7.3.jar | grep -q "$vault_sha256"; then
    curl -sfL -o Vault-1.7.3.jar.tmp "$vault_url"
    if ! sha256sum Vault-1.7.3.jar.tmp | grep -q "$vault_sha256"; then
        echo "Vault 1.7.3 checksum mismatch" >&2
        exit 1
    fi
    mv Vault-1.7.3.jar.tmp Vault-1.7.3.jar
fi

[ -f "$api" ] || bash "$root/scripts/pex-baseline/build-api.sh" >&2

mvn -q -B -f "$root/scripts/pex-baseline/pom.xml" dependency:build-classpath -Dmdep.outputFile="$out/classpath.txt" >&2

rm -rf probe-classes probe-src
mkdir probe-classes
cp -r "$here/probe/src" probe-src
find probe-src -name '*.java' > probe-sources.txt
sep=":"
case "$(uname -s)" in MINGW* | MSYS* | CYGWIN*) sep=";" ;; esac
javac --release 8 -nowarn -encoding UTF-8 -cp "$(cat classpath.txt)${sep}$(cygpath -w "$api" 2>/dev/null || echo "$api")${sep}Vault-1.7.3.jar" \
    -d probe-classes @probe-sources.txt
rm -rf probe-src
cp "$here/probe/plugin.yml" probe-classes/
rm -f LegacyPexProbe.jar
jar --create --file LegacyPexProbe.jar -C probe-classes .

echo "$out/Vault-1.7.3.jar"
echo "$out/LegacyPexProbe.jar"
