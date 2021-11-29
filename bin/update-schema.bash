#!/usr/bin/env bash
set -e
cd "$(dirname "${BASH_SOURCE[0]}")/.."
TARGET="$(pwd)/src/main/resources/change_sets"
CACHE="$(pwd)/.http-cache"
mkdir -p "${TARGET}"
mkdir -p "${CACHE}"
IMAGE="rubocop-schema-repo-builder"
export DOCKER_SCAN_SUGGEST=false # Stop Docker polluting its output with sales pitches
docker build . -f bin/update-schema.Dockerfile -t "${IMAGE}"
docker run \
  -v "${TARGET}:/repo" \
  -v "${CACHE}:/root/.rubocop-schema-cache" \
  --rm \
  "${IMAGE}"
