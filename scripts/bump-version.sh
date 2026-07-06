#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"

if [ $# -ge 1 ]; then
  NEW_VERSION="$1"
else
  # Called from standard-version postbump without args; read from root package.json
  NEW_VERSION=$(node -p "require('$ROOT_DIR/package.json').version")
fi
VERSION_FILE="$ROOT_DIR/VERSION"

if [[ ! "$NEW_VERSION" =~ ^[0-9]+\.[0-9]+\.[0-9]+(-[a-zA-Z0-9.]+)?$ ]]; then
  echo "Error: version must follow SemVer format (e.g., 1.2.0, 1.2.0-rc.1)"
  exit 1
fi

echo "$NEW_VERSION" > "$VERSION_FILE"
echo "✓ Updated VERSION → $NEW_VERSION"

# Update pom.xml (artifactId + version block, not parent or dependencies)
POM="$ROOT_DIR/backend/pom.xml"
if [ -f "$POM" ]; then
  sed -i "/<artifactId>vaultflow-core<\/artifactId>/,/<version>/s|<version>[0-9]*\.[0-9]*\.[0-9]*[^<]*</version>|<version>$NEW_VERSION</version>|" "$POM"
  echo "✓ Updated backend/pom.xml → $NEW_VERSION"
fi

# Update package.json
PACKAGE_JSON="$ROOT_DIR/frontend/package.json"
if [ -f "$PACKAGE_JSON" ]; then
  sed -i "s|\"version\": \"[0-9]*\.[0-9]*\.[0-9]*[^\"]*\"|\"version\": \"$NEW_VERSION\"|" "$PACKAGE_JSON"
  echo "✓ Updated frontend/package.json → $NEW_VERSION"
fi

echo ""
echo "Version bumped to $NEW_VERSION"
echo "Run 'git add VERSION backend/pom.xml frontend/package.json' to stage changes."
