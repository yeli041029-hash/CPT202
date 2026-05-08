#!/bin/sh

set -eu

SCRIPT_DIR=$(
  CDPATH= cd -- "$(dirname "$0")" && pwd
)
WRAPPER_PROPS="$SCRIPT_DIR/.mvn/wrapper/maven-wrapper.properties"

if [ ! -f "$WRAPPER_PROPS" ]; then
  echo "Missing wrapper properties: $WRAPPER_PROPS" >&2
  exit 1
fi

DISTRIBUTION_URL=$(
  sed -n 's/^[[:space:]]*distributionUrl[[:space:]]*=[[:space:]]*//p' "$WRAPPER_PROPS" | head -n 1
)

if [ -z "$DISTRIBUTION_URL" ]; then
  echo "distributionUrl is not configured in $WRAPPER_PROPS" >&2
  exit 1
fi

DIST_ZIP=$(basename "$DISTRIBUTION_URL")
DIST_NAME=${DIST_ZIP%.zip}
CACHE_DIR="${HOME}/.m2/wrapper/dists/${DIST_NAME}"
ZIP_FILE="${CACHE_DIR}/${DIST_ZIP}"

find_mvn() {
  if [ -d "$CACHE_DIR" ]; then
    find "$CACHE_DIR" -type f -path '*/bin/mvn' -print -quit 2>/dev/null || true
  fi
}

fix_mvn_permissions() {
  if [ -d "$CACHE_DIR" ]; then
    find "$CACHE_DIR" -type f -path '*/bin/*' -exec chmod +x {} + 2>/dev/null || true
  fi
}

extract_zip() {
  if command -v unzip >/dev/null 2>&1; then
    unzip -qo "$ZIP_FILE" -d "$CACHE_DIR"
    return 0
  fi

  if command -v jar >/dev/null 2>&1; then
    (
      cd "$CACHE_DIR"
      jar xf "$ZIP_FILE"
    )
    return 0
  fi

  if command -v python3 >/dev/null 2>&1; then
    python3 - "$ZIP_FILE" "$CACHE_DIR" <<'PY'
import sys
import zipfile

zip_path = sys.argv[1]
target_dir = sys.argv[2]

with zipfile.ZipFile(zip_path) as archive:
    archive.extractall(target_dir)
PY
    return 0
  fi

  echo "unzip, jar, or python3 is required to extract Maven." >&2
  return 1
}

MVN_BIN=$(find_mvn)

if [ -n "${MVN_BIN:-}" ]; then
  fix_mvn_permissions
  MVN_BIN=$(find_mvn)
fi

if [ -z "${MVN_BIN:-}" ]; then
  mkdir -p "$CACHE_DIR"
  echo "Downloading Maven from $DISTRIBUTION_URL" >&2

  if command -v curl >/dev/null 2>&1; then
    curl -fsSL "$DISTRIBUTION_URL" -o "$ZIP_FILE"
  elif command -v wget >/dev/null 2>&1; then
    wget -qO "$ZIP_FILE" "$DISTRIBUTION_URL"
  else
    echo "curl or wget is required to download Maven." >&2
    exit 1
  fi

  extract_zip
  fix_mvn_permissions
  MVN_BIN=$(find_mvn)
fi

if [ -z "${MVN_BIN:-}" ]; then
  echo "Unable to locate Maven after extraction." >&2
  exit 1
fi

exec "$MVN_BIN" "$@"
