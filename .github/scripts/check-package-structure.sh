#!/usr/bin/env bash
set -euo pipefail

DOC_FILE="docs/package-structure/package_structure.md"

if [[ ! -f "$DOC_FILE" ]]; then
  echo "ERR :: Documentation file not found: $DOC_FILE"
  exit 1
fi

# Ensure we have the base branch
git fetch origin "${GITHUB_BASE_REF}"

BASE_REF="origin/${GITHUB_BASE_REF}"

# Check if file changed in the PR
if git diff --name-only "$BASE_REF"...HEAD | grep -qx "$DOC_FILE"; then
  echo "INFO :: package_structure.md was updated in this PR"
  exit 0
else
  echo "ERR :: package_structure.md was NOT updated in this PR"
  echo "Please run scripts/run-folder-tree.sh before merging."
  exit 1
fi
