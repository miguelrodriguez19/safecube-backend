#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

RESOURCES_DIR="${SCRIPT_DIR}/resources"
BUILD_DIR="${SCRIPT_DIR}/.build"

JAVA_PACKAGE="com.safecube.tooling"
JAVA_CLASS="FolderTreeToFile"
JAVA_FILE="${RESOURCES_DIR}/com/safecube/tooling/${JAVA_CLASS}.java"

OUTPUT_TARGET="/docs/package-structure.txt"

command -v java >/dev/null 2>&1 || {
  echo "java not found (JDK 21+ required)" >&2
  exit 1
}

[[ -f "${JAVA_FILE}" ]] || {
  echo "Java file not found: ${JAVA_FILE}" >&2
  exit 1
}

# Build
rm -rf "${BUILD_DIR}"
mkdir -p "${BUILD_DIR}"

javac -d "${BUILD_DIR}" "${JAVA_FILE}"

# Run
java -cp "${BUILD_DIR}" "${JAVA_PACKAGE}.${JAVA_CLASS}" "${PROJECT_ROOT}" "${OUTPUT_TARGET}" "true" "false"

