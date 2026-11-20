#!/usr/bin/env bash
# Builds the public API jar of an original PermissionsEx release from its git tag,
# as plugins of that era compiled against it. Used as a baseline by the API
# adapter's contract tests and to compile the legacy plugin probe.
#
# usage: scripts/pex-baseline/build-api.sh [tag] [output.jar]
#   tag      default STABLE-1.23.4
#   output   default PermissionsExApiAdapter/src/test/resources/baselines/PermissionsEx-<version>-api.jar
#
# PermissionsEx 1.23.4 depends on three artifacts that are no longer published
# (Mojang's AccountsClient, NetEvents and Gravity's Updater). It is compiled with
# the stand-ins in stubs/, which stay in the jar so reflection over private members
# can resolve them. The jar is only ever used on test classpaths.
set -euo pipefail

tag="${1:-STABLE-1.23.4}"
version="${tag#STABLE-}"
root="$(git rev-parse --show-toplevel)"
here="$root/scripts/pex-baseline"
output="${2:-$root/PermissionsExApiAdapter/src/test/resources/baselines/PermissionsEx-$version-api.jar}"

# Work under target/ (ignored), with relative paths, so it also works with Windows tools.
work="$root/target/pex-baseline-$version"
rm -rf "$work"
mkdir -p "$work/classes"
git -C "$root" archive --format=tar "$tag" src/main | tar -x -C "$work"
cp -r "$here/stubs" "$work/stubs"

mvn -q -B -f "$here/pom.xml" dependency:build-classpath -Dmdep.outputFile="$work/classpath.txt"

cd "$work"
find src/main/java stubs -name '*.java' > sources.txt
javac --release 8 -nowarn -encoding UTF-8 -cp "$(cat classpath.txt)" -d classes @sources.txt

sed "s/\${project.version}/$version/" src/main/resources/plugin.yml > classes/plugin.yml

mkdir -p "$(dirname "$output")"
rm -f "$output"
jar --create --file "$output" -C classes .
echo "Built $output ($(find classes -name '*.class' | wc -l) classes)"
