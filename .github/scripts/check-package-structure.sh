#!/usr/bin/env bash
set -euo pipefail

DOC_FILE="docs/package-structure/package_structure.md"

if [[ ! -f "$DOC_FILE" ]]; then
  echo "ERR :: Documentation file not found: $DOC_FILE"
  exit 1
fi

# Extract the Updated timestamp line
UPDATED_LINE=$(grep -E '^Updated:' "$DOC_FILE" || true)

if [[ -z "$UPDATED_LINE" ]]; then
  echo "ERR :: Missing 'Updated:' timestamp in $DOC_FILE"
  echo "Expected format: Updated: DD-MM-YYYY HH:MM:SS"
  exit 1
fi

# Extract timestamp value
UPDATED_VALUE=$(echo "$UPDATED_LINE" | sed 's/^Updated:[[:space:]]*//')

# Validate timestamp format (DD-MM-YYYY HH:MM:SS)
if ! date -d "$UPDATED_VALUE" "+%s" >/dev/null 2>&1; then
  echo "ERR :: Invalid timestamp format in $DOC_FILE"
  echo "Found: $UPDATED_VALUE"
  echo "Expected format: Updated: DD-MM-YYYY HH:MM:SS"
  exit 1
fi

UPDATED_EPOCH=$(date -d "$UPDATED_VALUE" "+%s")

# Get last commit timestamp that modified source code
LAST_CODE_CHANGE_EPOCH=$(git log -1 --format=%ct -- src || true)

if [[ -z "$LAST_CODE_CHANGE_EPOCH" ]]; then
  echo "INFO :: No source changes detected in src/. Skipping freshness check."
  exit 0
fi

if (( UPDATED_EPOCH < LAST_CODE_CHANGE_EPOCH )); then
  echo "ERR :: package_structure.md is outdated"
  echo "Last source change  : $(date -d @$LAST_CODE_CHANGE_EPOCH)"
  echo "Docs last updated   : $UPDATED_VALUE"
  echo "Please update docs/package_structure.md"
  exit 1
fi

echo "INFO :: package_structure.md is up to date"
exit 0
