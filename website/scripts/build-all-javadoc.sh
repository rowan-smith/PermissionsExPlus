#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
WEBSITE_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
REPO_ROOT="$(cd "$WEBSITE_ROOT/.." && pwd)"
APIDOCS_DIR="${WEBSITE_ROOT}/static/apidocs"

VERSION="$(node -p "JSON.parse(require('fs').readFileSync('${WEBSITE_ROOT}/site-vars.json','utf8')).version")"

echo "Building Javadoc artifacts under ${APIDOCS_DIR}"

mkdir -p "$APIDOCS_DIR"

declare -a CLASSIC_BUILDS=(
  "STABLE-1.22.1:1.22.1"
  "STABLE-1.23.1:1.23.1"
  "STABLE-1.23.2:1.23.2"
  "STABLE-1.23.3:1.23.3"
  "STABLE-1.23.4:1.23.4"
)

for entry in "${CLASSIC_BUILDS[@]}"; do
  tag="${entry%%:*}"
  out="${entry##*:}"
  "$SCRIPT_DIR/build-classic-javadoc.sh" "$tag" "$out"
done

CURRENT_OUT="${APIDOCS_DIR}/${VERSION}"
MODERN_DIR="$REPO_ROOT/api/target/reports/apidocs"
LEGACY_DIR="$REPO_ROOT/legacy-api/target/reports/apidocs"

mvn -f "$REPO_ROOT/pom.xml" \
  -pl api,legacy-api \
  -am compile javadoc:javadoc \
  -Ddoclint=none \
  -DskipTests \
  -q

rm -rf "$CURRENT_OUT"
mkdir -p "$CURRENT_OUT"

if [ -d "$MODERN_DIR" ]; then
  cp -a "$MODERN_DIR/." "$CURRENT_OUT/"
fi

if [ -d "$LEGACY_DIR" ]; then
  for item in "$LEGACY_DIR"/*; do
    base="$(basename "$item")"
    case "$base" in
      ru|dev|index.html|stylesheet.css|script.js|search.js|search-page.js|search.html|help-doc.html|overview-summary.html|element-list|member-search-index.js|package-search-index.js|type-search-index.js|module-search-index.js|tag-search-index.js|allclasses-index.html|allpackages-index.html|resource-files|script-dir|legal)
        cp -a "$item" "$CURRENT_OUT/"
        ;;
    esac
  done
fi

test -f "${CURRENT_OUT}/index.html"
echo "OK: ${VERSION} -> ${CURRENT_OUT}"
