#!/usr/bin/env bash
set -euo pipefail

OLD_VERSION=$(git show -q HEAD^:pom.xml | \
              xmllint --xpath "/*[local-name()='project']/*[local-name()='version']/text()" -)
NEW_VERSION=$(xmllint --xpath "/*[local-name()='project']/*[local-name()='version']/text()" pom.xml)

echo "Previous version: $OLD_VERSION"
echo "Current  version: $NEW_VERSION"

strip_snapshot() {
  echo "${1/-SNAPSHOT/}"
}

if [[ "$(strip_snapshot "$OLD_VERSION")" == "$(strip_snapshot "$NEW_VERSION")" ]]; then
  echo "Maven version has not been increased."
  exit 1
fi
echo "Version correctly bumped."
